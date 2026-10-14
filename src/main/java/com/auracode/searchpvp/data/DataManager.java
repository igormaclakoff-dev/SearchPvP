package com.auracode.searchpvp.data;

import com.auracode.searchpvp.SearchPvPPlugin;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Loads, caches and persists {@link PlayerData}. File I/O never runs on the main thread.
 */
public final class DataManager {

    private static final String ROOT = "players";

    private final SearchPvPPlugin plugin;
    private final File file;
    private final Map<UUID, PlayerData> cache = new ConcurrentHashMap<>();

    public DataManager(SearchPvPPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "playerdata.yml");
    }

    public void load() {
        cache.clear();
        if (!file.exists()) {
            return;
        }
        try {
            YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
            ConfigurationSection root = yaml.getConfigurationSection(ROOT);
            if (root == null) {
                return;
            }
            for (String key : root.getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(key);
                    PlayerData data = PlayerData.from(uuid, root.getConfigurationSection(key),
                            plugin.getPluginConfig().getStartingElo());
                    cache.put(uuid, data);
                } catch (IllegalArgumentException exception) {
                    plugin.getLogger().warning("Skipping malformed player entry: " + key);
                }
            }
            plugin.getLogger().info("Loaded " + cache.size() + " player profiles.");
        } catch (RuntimeException exception) {
            plugin.getLogger().severe("Failed to load player data: " + exception.getMessage());
        }
    }

    public PlayerData getOrCreate(UUID uuid, String name) {
        PlayerData data = cache.computeIfAbsent(uuid,
                id -> new PlayerData(id, name == null ? "unknown" : name, plugin.getPluginConfig().getStartingElo()));
        data.setName(name);
        return data;
    }

    public PlayerData get(UUID uuid) {
        return cache.get(uuid);
    }

    public PlayerData getByName(String name) {
        return cache.values().stream()
                .filter(data -> data.name().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    public Collection<PlayerData> all() {
        return cache.values();
    }

    public List<PlayerData> topByElo(int limit) {
        return cache.values().stream()
                .sorted(Comparator.comparingInt(PlayerData::elo).reversed()
                        .thenComparing(Comparator.comparingInt(PlayerData::wins).reversed()))
                .limit(limit)
                .toList();
    }

    public void save() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, this::saveSync);
    }

    public void saveSync() {
        YamlConfiguration yaml = new YamlConfiguration();
        for (PlayerData data : cache.values()) {
            data.write(yaml.createSection(ROOT + "." + data.uuid()));
        }
        try {
            if (!plugin.getDataFolder().exists() && !plugin.getDataFolder().mkdirs()) {
                plugin.getLogger().warning("Could not create the plugin data folder.");
            }
            yaml.save(file);
        } catch (IOException exception) {
            plugin.getLogger().severe("Failed to save player data: " + exception.getMessage());
        }
    }

    public void resetRatings(int startingElo) {
        cache.values().forEach(data -> data.resetForNewSeason(startingElo));
    }
}

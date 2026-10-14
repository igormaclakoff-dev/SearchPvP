package com.auracode.searchpvp.util;

import com.auracode.searchpvp.SearchPvPPlugin;
import com.auracode.searchpvp.model.ArmorTier;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Loads and dispatches MiniMessage messages from messages.yml.
 */
public final class Messages {

    private final SearchPvPPlugin plugin;
    private FileConfiguration config = new YamlConfiguration();

    public Messages(SearchPvPPlugin plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        File file = new File(plugin.getDataFolder(), "messages.yml");
        this.config = YamlConfiguration.loadConfiguration(file);
        Text.setPrefix(config.getString("prefix", ""));
    }

    public String raw(String key) {
        String value = config.getString(key);
        if (value == null) {
            plugin.getLogger().warning("Missing message key: " + key);
            return "<red>[missing: " + key + "]";
        }
        return value;
    }

    public Component component(String key) {
        return component(key, Map.of());
    }

    public Component component(String key, Map<String, String> placeholders) {
        return Text.parse(raw(key), placeholders);
    }

    public List<Component> componentList(String key, Map<String, String> placeholders) {
        List<Component> lines = new ArrayList<>();
        for (String line : config.getStringList(key)) {
            lines.add(Text.parseItem(line, placeholders));
        }
        return lines;
    }

    public String tierName(ArmorTier tier) {
        return raw("tiers." + tier.name().toLowerCase(Locale.ROOT));
    }

    /**
     * Formats a duration as plain text: full days while more than a day remains,
     * otherwise hours and minutes (the final-day countdown).
     */
    public String duration(long millis) {
        long totalMinutes = Math.max(0L, (millis + 59_999L) / 60_000L);
        long days = totalMinutes / (60L * 24L);
        if (days >= 1L) {
            return days + " " + config.getString("time.days", "дн.");
        }
        long hours = totalMinutes / 60L;
        long minutes = totalMinutes % 60L;
        String hoursUnit = config.getString("time.hours", "ч");
        String minutesUnit = config.getString("time.minutes", "м");
        if (hours > 0L) {
            return hours + hoursUnit + " " + minutes + minutesUnit;
        }
        return minutes + minutesUnit;
    }

    public void send(CommandSender target, String key) {
        send(target, key, Map.of());
    }

    public void send(CommandSender target, String key, Map<String, String> placeholders) {
        if (target == null) {
            return;
        }
        target.sendMessage(component(key, placeholders));
    }

    public void sendList(CommandSender target, String key, Map<String, String> placeholders) {
        if (target == null) {
            return;
        }
        for (Component line : componentList(key, placeholders)) {
            target.sendMessage(line);
        }
    }

    public void broadcast(String key, Map<String, String> placeholders) {
        Component message = component(key, placeholders);
        Bukkit.getConsoleSender().sendMessage(message);
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(message);
        }
    }

    public void title(Player player, String mainKey, String subKey, Map<String, String> placeholders, long stayMillis) {
        Component main = component(mainKey, placeholders);
        Component sub = component(subKey, placeholders);
        player.showTitle(Title.title(main, sub,
                Title.Times.times(Duration.ZERO, Duration.ofMillis(stayMillis), Duration.ofMillis(250))));
    }
}

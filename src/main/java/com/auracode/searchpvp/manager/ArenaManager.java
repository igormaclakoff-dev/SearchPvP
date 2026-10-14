package com.auracode.searchpvp.manager;

import com.auracode.searchpvp.SearchPvPPlugin;
import com.auracode.searchpvp.config.PluginConfig;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Picks safe, far-away arena spawn locations.
 */
public final class ArenaManager {

    private final SearchPvPPlugin plugin;

    public ArenaManager(SearchPvPPlugin plugin) {
        this.plugin = plugin;
    }

    public World arenaWorld() {
        World world = plugin.getServer().getWorld(plugin.getPluginConfig().getArenaWorld());
        return world != null ? world : plugin.getServer().getWorlds().get(0);
    }

    /**
     * @return two spawn locations for the fighters (adjacent when same-location is enabled).
     */
    public List<Location> spawnLocations() {
        PluginConfig config = plugin.getPluginConfig();
        Location first = randomSurfaceLocation();
        if (config.isSameLocation()) {
            Location second = first.clone().add(config.getSpawnOffset(), 0, 0);
            return List.of(first, second);
        }
        return List.of(first, randomSurfaceLocation());
    }

    private Location randomSurfaceLocation() {
        PluginConfig config = plugin.getPluginConfig();
        World world = arenaWorld();
        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int attempt = 0; attempt < config.getArenaMaxAttempts(); attempt++) {
            double angle = random.nextDouble(0.0, Math.PI * 2.0);
            double distance = random.nextDouble(config.getArenaMinDistance(), config.getArenaMaxDistance());
            int x = (int) Math.round(Math.cos(angle) * distance);
            int z = (int) Math.round(Math.sin(angle) * distance);

            world.getChunkAt(x >> 4, z >> 4);
            int y = world.getHighestBlockYAt(x, z);
            Block ground = world.getBlockAt(x, y, z);
            Material floor = ground.getType();
            Material feet = world.getBlockAt(x, y + 1, z).getType();
            if (floor.isSolid() && !ground.isLiquid() && feet.isAir()) {
                return new Location(world, x + 0.5, y + 1, z + 0.5);
            }
        }
        plugin.getLogger().warning("Could not find a safe arena spot, falling back to world spawn.");
        return world.getSpawnLocation();
    }
}

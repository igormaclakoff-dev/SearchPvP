package com.auracode.searchpvp.listener;

import com.auracode.searchpvp.SearchPvPPlugin;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

/**
 * Handles in-match death, movement freezing during the countdown and respawn placement.
 */
public final class CombatListener implements Listener {

    private final SearchPvPPlugin plugin;

    public CombatListener(SearchPvPPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        if (!plugin.getMatchManager().isInMatch(victim.getUniqueId())) {
            return;
        }
        if (plugin.getPluginConfig().isKeepInventory()) {
            event.setKeepInventory(true);
            event.setKeepLevel(true);
            event.getDrops().clear();
            event.setDroppedExp(0);
        }
        event.deathMessage(null);
        plugin.getMatchManager().handleDeath(victim);
    }

    @EventHandler(ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (!plugin.getMatchManager().isFrozen(player.getUniqueId())) {
            return;
        }
        Location from = event.getFrom();
        Location to = event.getTo();
        if (from.getX() == to.getX() && from.getY() == to.getY() && from.getZ() == to.getZ()) {
            return;
        }
        Location locked = to.clone();
        locked.setX(from.getX());
        locked.setY(from.getY());
        locked.setZ(from.getZ());
        event.setTo(locked);
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        Location location = plugin.getMatchManager().consumePendingRespawn(event.getPlayer().getUniqueId());
        if (location != null) {
            event.setRespawnLocation(location);
        }
    }
}

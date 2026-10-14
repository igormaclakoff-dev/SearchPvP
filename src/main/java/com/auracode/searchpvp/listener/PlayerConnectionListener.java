package com.auracode.searchpvp.listener;

import com.auracode.searchpvp.SearchPvPPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Keeps player profiles warm and cleans up queue/match state on disconnect.
 */
public final class PlayerConnectionListener implements Listener {

    private final SearchPvPPlugin plugin;

    public PlayerConnectionListener(SearchPvPPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        plugin.getDataManager().getOrCreate(player.getUniqueId(), player.getName());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (plugin.getMatchmakingManager().isSearching(player.getUniqueId())) {
            plugin.getMatchmakingManager().cancel(player, "search.cancelled-manual");
        }
        plugin.getMatchManager().handleQuit(player);
    }
}

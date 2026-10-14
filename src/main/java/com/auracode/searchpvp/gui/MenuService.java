package com.auracode.searchpvp.gui;

import com.auracode.searchpvp.SearchPvPPlugin;
import org.bukkit.entity.Player;

/**
 * Creates and opens the plugin's inventories.
 */
public final class MenuService {

    private final SearchPvPPlugin plugin;

    public MenuService(SearchPvPPlugin plugin) {
        this.plugin = plugin;
    }

    public void openMain(Player player) {
        player.openInventory(new PvPMenu(plugin, player).getInventory());
    }

    public void openTop(Player player, int page) {
        player.openInventory(new TopMenu(plugin, player, page).getInventory());
    }
}

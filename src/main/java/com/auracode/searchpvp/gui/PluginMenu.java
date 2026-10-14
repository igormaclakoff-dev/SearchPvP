package com.auracode.searchpvp.gui;

import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;

/**
 * Common contract for clickable inventories owned by the plugin.
 */
public interface PluginMenu extends InventoryHolder {

    void handleClick(Player who, int slot);
}

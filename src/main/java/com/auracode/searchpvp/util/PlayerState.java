package com.auracode.searchpvp.util;

import com.auracode.searchpvp.SearchPvPPlugin;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;

/**
 * Interoperable player state stored in the player's {@code PersistentDataContainer}.
 * Other plugins can read these flags to know if a player is busy.
 */
public final class PlayerState {

    private static NamespacedKey inMatchKey;
    private static NamespacedKey inQueueKey;

    private PlayerState() {
    }

    public static void init(SearchPvPPlugin plugin) {
        inMatchKey = new NamespacedKey(plugin, "in_match");
        inQueueKey = new NamespacedKey(plugin, "in_queue");
    }

    public static void setInMatch(Player player, boolean value) {
        player.getPersistentDataContainer().set(inMatchKey, PersistentDataType.BYTE, (byte) (value ? 1 : 0));
    }

    public static boolean isInMatch(Player player) {
        return player.getPersistentDataContainer().getOrDefault(inMatchKey, PersistentDataType.BYTE, (byte) 0) == 1;
    }

    public static void setInQueue(Player player, boolean value) {
        player.getPersistentDataContainer().set(inQueueKey, PersistentDataType.BYTE, (byte) (value ? 1 : 0));
    }

    public static boolean isInQueue(Player player) {
        return player.getPersistentDataContainer().getOrDefault(inQueueKey, PersistentDataType.BYTE, (byte) 0) == 1;
    }
}

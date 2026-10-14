package com.auracode.searchpvp.model;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Set;

/**
 * Supported matchmaking armor tiers. A player must wear a complete set of one tier.
 */
public enum ArmorTier {

    NETHERITE(Set.of(
            Material.NETHERITE_HELMET,
            Material.NETHERITE_CHESTPLATE,
            Material.NETHERITE_LEGGINGS,
            Material.NETHERITE_BOOTS)),
    DIAMOND(Set.of(
            Material.DIAMOND_HELMET,
            Material.DIAMOND_CHESTPLATE,
            Material.DIAMOND_LEGGINGS,
            Material.DIAMOND_BOOTS));

    private final Set<Material> pieces;

    ArmorTier(Set<Material> pieces) {
        this.pieces = pieces;
    }

    public Set<Material> pieces() {
        return pieces;
    }

    public static ArmorTier fromMaterial(Material material) {
        for (ArmorTier tier : values()) {
            if (tier.pieces.contains(material)) {
                return tier;
            }
        }
        return null;
    }

    /**
     * Immutable snapshot of the four armor slots, used to detect changes while queueing.
     */
    public record Snapshot(ItemStack helmet, ItemStack chestplate, ItemStack leggings, ItemStack boots) {

        public static Snapshot of(org.bukkit.entity.Player player) {
            var inventory = player.getInventory();
            return new Snapshot(
                    copy(inventory.getHelmet()),
                    copy(inventory.getChestplate()),
                    copy(inventory.getLeggings()),
                    copy(inventory.getBoots()));
        }

        /**
         * @return the detected tier if all four slots belong to the same tier, otherwise {@code null}.
         */
        public ArmorTier tier() {
            ArmorTier helmetTier = tierOf(helmet);
            if (helmetTier == null) {
                return null;
            }
            return helmetTier == tierOf(chestplate) && helmetTier == tierOf(leggings) && helmetTier == tierOf(boots)
                    ? helmetTier
                    : null;
        }

        public boolean matches(org.bukkit.entity.Player player) {
            var inventory = player.getInventory();
            return similar(helmet, inventory.getHelmet())
                    && similar(chestplate, inventory.getChestplate())
                    && similar(leggings, inventory.getLeggings())
                    && similar(boots, inventory.getBoots());
        }

        private static ArmorTier tierOf(ItemStack item) {
            if (isEmpty(item)) {
                return null;
            }
            return ArmorTier.fromMaterial(item.getType());
        }

        private static boolean similar(ItemStack expected, ItemStack current) {
            if (isEmpty(expected)) {
                return isEmpty(current);
            }
            return !isEmpty(current) && expected.isSimilar(current);
        }

        private static boolean isEmpty(ItemStack item) {
            return item == null || item.getType().isAir();
        }

        private static ItemStack copy(ItemStack item) {
            return isEmpty(item) ? null : item.clone();
        }
    }
}

package com.auracode.searchpvp.model;

import net.kyori.adventure.bossbar.BossBar;

import java.util.UUID;

/**
 * Mutable state of a single player waiting in the matchmaking queue.
 */
public final class QueueEntry {

    private final UUID uuid;
    private final String name;
    private final ArmorTier tier;
    private final ArmorTier.Snapshot armor;
    private final long startedAt;
    private final BossBar bossBar;

    private Long armorDeviatedAt;

    public QueueEntry(UUID uuid, String name, ArmorTier tier, ArmorTier.Snapshot armor, BossBar bossBar) {
        this.uuid = uuid;
        this.name = name;
        this.tier = tier;
        this.armor = armor;
        this.bossBar = bossBar;
        this.startedAt = System.currentTimeMillis();
    }

    public UUID uuid() {
        return uuid;
    }

    public String name() {
        return name;
    }

    public ArmorTier tier() {
        return tier;
    }

    public ArmorTier.Snapshot armor() {
        return armor;
    }

    public long startedAt() {
        return startedAt;
    }

    public BossBar bossBar() {
        return bossBar;
    }

    public Long armorDeviatedAt() {
        return armorDeviatedAt;
    }

    public void markArmorDeviation() {
        if (armorDeviatedAt == null) {
            armorDeviatedAt = System.currentTimeMillis();
        }
    }

    public void clearArmorDeviation() {
        armorDeviatedAt = null;
    }
}

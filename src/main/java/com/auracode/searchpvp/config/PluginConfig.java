package com.auracode.searchpvp.config;

import com.auracode.searchpvp.model.ArmorTier;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.EnumSet;
import java.util.List;
import java.util.Locale;

/**
 * Immutable, typed view over config.yml.
 */
public final class PluginConfig {

    private final String placementCommand;
    private final String arenaWorld;
    private final int arenaMinDistance;
    private final int arenaMaxDistance;
    private final boolean sameLocation;
    private final int spawnOffset;
    private final int arenaMaxAttempts;
    private final int matchmakingIntervalTicks;
    private final int armorChangeGraceSeconds;
    private final EnumSet<ArmorTier> allowedTiers;
    private final int startingElo;
    private final int eloKFactor;
    private final int seasonLengthDays;
    private final int countdownSeconds;
    private final int minRankSeconds;
    private final boolean returnToOrigin;
    private final boolean keepInventory;
    private final String winCommand;
    private final String loseCommand;
    private final String exitCommand;

    public PluginConfig(FileConfiguration config) {
        this.placementCommand = config.getString("settings.placement-command", "");
        this.arenaWorld = config.getString("arena.world", "world");
        this.arenaMinDistance = config.getInt("arena.min-distance", 2000);
        this.arenaMaxDistance = config.getInt("arena.max-distance", 5000);
        this.sameLocation = config.getBoolean("arena.same-location", true);
        this.spawnOffset = Math.max(1, config.getInt("arena.spawn-offset", 3));
        this.arenaMaxAttempts = Math.max(1, config.getInt("arena.max-attempts", 40));
        this.matchmakingIntervalTicks = Math.max(1, config.getInt("matchmaking.interval-ticks", 20));
        this.armorChangeGraceSeconds = Math.max(1, config.getInt("matchmaking.armor-change-grace-seconds", 10));
        this.allowedTiers = parseTiers(config.getStringList("matchmaking.allowed-tiers"));
        this.startingElo = Math.max(0, config.getInt("elo.starting-rating", 1000));
        this.eloKFactor = Math.max(1, config.getInt("elo.k-factor", 32));
        this.seasonLengthDays = Math.max(1, config.getInt("season.length-days", 30));
        this.countdownSeconds = Math.max(0, config.getInt("match.countdown-seconds", 3));
        this.minRankSeconds = Math.max(0, config.getInt("match.min-rank-seconds", 15));
        this.returnToOrigin = config.getBoolean("match.return-to-origin", true);
        this.keepInventory = config.getBoolean("match.keep-inventory", true);
        this.winCommand = config.getString("rewards.win-command", "");
        this.loseCommand = config.getString("rewards.lose-command", "");
        this.exitCommand = config.getString("rewards.exit-command", "");
    }

    private static EnumSet<ArmorTier> parseTiers(List<String> raw) {
        EnumSet<ArmorTier> tiers = EnumSet.noneOf(ArmorTier.class);
        for (String value : raw) {
            try {
                tiers.add(ArmorTier.valueOf(value.trim().toUpperCase(Locale.ROOT)));
            } catch (IllegalArgumentException exception) {
                Bukkit.getLogger().warning("[SearchPvP] Unknown armor tier in config: " + value);
            }
        }
        if (tiers.isEmpty()) {
            tiers = EnumSet.allOf(ArmorTier.class);
        }
        return tiers;
    }

    public String getPlacementCommand() {
        return placementCommand;
    }

    public String getArenaWorld() {
        return arenaWorld;
    }

    public int getArenaMinDistance() {
        return arenaMinDistance;
    }

    public int getArenaMaxDistance() {
        return arenaMaxDistance;
    }

    public boolean isSameLocation() {
        return sameLocation;
    }

    public int getSpawnOffset() {
        return spawnOffset;
    }

    public int getArenaMaxAttempts() {
        return arenaMaxAttempts;
    }

    public int getMatchmakingIntervalTicks() {
        return matchmakingIntervalTicks;
    }

    public int getArmorChangeGraceSeconds() {
        return armorChangeGraceSeconds;
    }

    public EnumSet<ArmorTier> getAllowedTiers() {
        return EnumSet.copyOf(allowedTiers);
    }

    public int getStartingElo() {
        return startingElo;
    }

    public int getEloKFactor() {
        return eloKFactor;
    }

    public int getSeasonLengthDays() {
        return seasonLengthDays;
    }

    public int getCountdownSeconds() {
        return countdownSeconds;
    }

    public int getMinRankSeconds() {
        return minRankSeconds;
    }

    public long getMinRankMillis() {
        return minRankSeconds * 1000L;
    }

    public boolean isReturnToOrigin() {
        return returnToOrigin;
    }

    public boolean isKeepInventory() {
        return keepInventory;
    }

    public String getWinCommand() {
        return winCommand;
    }

    public String getLoseCommand() {
        return loseCommand;
    }

    public String getExitCommand() {
        return exitCommand;
    }
}

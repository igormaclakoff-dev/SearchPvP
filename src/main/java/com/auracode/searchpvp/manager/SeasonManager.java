package com.auracode.searchpvp.manager;

import com.auracode.searchpvp.SearchPvPPlugin;
import com.auracode.searchpvp.data.PlayerData;
import com.auracode.searchpvp.util.Placeholders;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Tracks the current season, announces the final day countdown, and rewards the top-3 when it ends.
 */
public final class SeasonManager {

    private static final long FINAL_DAY_MILLIS = TimeUnit.DAYS.toMillis(1L);
    private static final long TICK_INTERVAL_TICKS = 20L;
    private static final long TICK_DELAY_TICKS = 100L;

    private final SearchPvPPlugin plugin;
    private final File file;

    private long seasonLengthMillis;
    private int season = 1;
    private long seasonStart = System.currentTimeMillis();
    private int lastRewardedSeason;
    private long lastAnnouncedBucket = Long.MIN_VALUE;
    private boolean ending;
    private BukkitTask task;

    public SeasonManager(SearchPvPPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "season.yml");
        this.seasonLengthMillis = plugin.getPluginConfig().getSeasonLengthDays() * FINAL_DAY_MILLIS;
        load();
    }

    public void start() {
        if (task != null) {
            task.cancel();
        }
        task = Bukkit.getScheduler().runTaskTimer(plugin, this::tick, TICK_DELAY_TICKS, TICK_INTERVAL_TICKS);
    }

    /**
     * Re-applies timing-related settings after a configuration reload.
     */
    public void reload() {
        this.seasonLengthMillis = plugin.getPluginConfig().getSeasonLengthDays() * FINAL_DAY_MILLIS;
        this.lastAnnouncedBucket = Long.MIN_VALUE;
    }

    public void shutdown() {
        if (task != null) {
            task.cancel();
        }
    }

    private void load() {
        if (!file.exists()) {
            this.seasonStart = System.currentTimeMillis();
            save();
            return;
        }
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        this.season = yaml.getInt("season", 1);
        this.seasonStart = yaml.getLong("season-start", System.currentTimeMillis());
        this.lastRewardedSeason = yaml.getInt("last-rewarded-season", this.season - 1);
    }

    private void tick() {
        long remaining = millisLeft();
        if (remaining <= 0L) {
            endSeason();
            return;
        }
        if (remaining > FINAL_DAY_MILLIS) {
            return;
        }
        long minutes = (remaining + 59_999L) / 60_000L;
        long bucket = minutes <= 60L ? minutes : minutes / 60L;
        if (bucket == lastAnnouncedBucket) {
            return;
        }
        lastAnnouncedBucket = bucket;
        plugin.getMessages().broadcast("season.countdown",
                Placeholders.of().set("time", timeLeft()).build());
    }

    /**
     * Ends the running season: announces the winners, grants their rewards and starts the next season.
     */
    public void endSeason() {
        if (ending) {
            return;
        }
        ending = true;
        int finished = season;
        List<PlayerData> top = plugin.getDataManager().topByElo(RewardManager.maxPlace());

        plugin.getMessages().broadcast("season.ended-header", Placeholders.of().set("season", finished).build());
        plugin.getMessages().broadcast("season.ended-title", Placeholders.of().set("season", finished).build());

        boolean rewardable = finished > lastRewardedSeason && hasPlayed(top);
        if (top.isEmpty() || !rewardable) {
            plugin.getMessages().broadcast("season.ended-empty", Placeholders.of().set("season", finished).build());
        } else {
            int place = 1;
            for (PlayerData data : top) {
                plugin.getMessages().broadcast("season.ended-winner", Placeholders.of()
                        .set("place", place)
                        .set("player", data.name())
                        .set("elo", data.elo())
                        .build());
                for (String line : plugin.getRewardManager().lines(place)) {
                    plugin.getMessages().broadcast("season.ended-reward", Placeholders.of().set("line", line).build());
                }
                plugin.getRewardManager().grant(place, data.name(), data.elo());
                place++;
            }
            lastRewardedSeason = finished;
            save();
        }

        startNewSeason();
        plugin.getMessages().broadcast("season.ended-footer",
                Placeholders.of().set("next", season).build());
        lastAnnouncedBucket = Long.MIN_VALUE;
        ending = false;
    }

    /**
     * Starts the next season: increments the number and resets all ratings.
     */
    public void startNewSeason() {
        season++;
        seasonStart = System.currentTimeMillis();
        lastAnnouncedBucket = Long.MIN_VALUE;
        plugin.getDataManager().resetRatings(plugin.getPluginConfig().getStartingElo());
        plugin.getDataManager().save();
        save();
        plugin.getLogger().info("Started season " + season + ".");
    }

    /**
     * Sets the season number and restarts its timer without touching ratings.
     */
    public void setSeason(int number) {
        this.season = Math.max(1, number);
        this.seasonStart = System.currentTimeMillis();
        this.lastAnnouncedBucket = Long.MIN_VALUE;
        save();
        plugin.getLogger().info("Season set to " + season + ".");
    }

    public int season() {
        return season;
    }

    public long millisLeft() {
        return Math.max(0L, seasonStart + seasonLengthMillis - System.currentTimeMillis());
    }

    public String timeLeft() {
        return plugin.getMessages().duration(millisLeft());
    }

    private boolean hasPlayed(List<PlayerData> top) {
        int starting = plugin.getPluginConfig().getStartingElo();
        for (PlayerData data : top) {
            if (data.matches() > 0 || data.elo() != starting) {
                return true;
            }
        }
        return false;
    }

    private void save() {
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("season", season);
        yaml.set("season-start", seasonStart);
        yaml.set("last-rewarded-season", lastRewardedSeason);
        try {
            yaml.save(file);
        } catch (IOException exception) {
            plugin.getLogger().severe("Failed to save season data: " + exception.getMessage());
        }
    }
}

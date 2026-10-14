package com.auracode.searchpvp.data;

import org.bukkit.configuration.ConfigurationSection;

import java.util.UUID;

/**
 * Persistent statistics of a single player.
 */
public final class PlayerData {

    private final UUID uuid;
    private String name;
    private int elo;
    private int wins;
    private int losses;
    private int kills;
    private int deaths;
    private int currentStreak;
    private int bestStreak;

    public PlayerData(UUID uuid, String name, int elo) {
        this.uuid = uuid;
        this.name = name;
        this.elo = elo;
    }

    public UUID uuid() {
        return uuid;
    }

    public String name() {
        return name;
    }

    public void setName(String name) {
        if (name != null && !name.isBlank()) {
            this.name = name;
        }
    }

    public int elo() {
        return elo;
    }

    public void setElo(int elo) {
        this.elo = elo;
    }

    public int wins() {
        return wins;
    }

    public int losses() {
        return losses;
    }

    public int kills() {
        return kills;
    }

    public int deaths() {
        return deaths;
    }

    public int currentStreak() {
        return currentStreak;
    }

    public int bestStreak() {
        return bestStreak;
    }

    public int matches() {
        return wins + losses;
    }

    public double winRate() {
        int played = matches();
        return played == 0 ? 0.0 : wins * 100.0 / played;
    }

    public double killDeathRatio() {
        return deaths == 0 ? kills : (double) kills / deaths;
    }

    public void addWin() {
        wins++;
        currentStreak++;
        bestStreak = Math.max(bestStreak, currentStreak);
    }

    public void addLoss() {
        losses++;
        currentStreak = 0;
    }

    public void addKill() {
        kills++;
    }

    public void addDeath() {
        deaths++;
    }

    public void resetForNewSeason(int startingElo) {
        this.elo = startingElo;
        this.wins = 0;
        this.losses = 0;
        this.kills = 0;
        this.deaths = 0;
        this.currentStreak = 0;
        this.bestStreak = 0;
    }

    public static PlayerData from(UUID uuid, ConfigurationSection section, int defaultElo) {
        PlayerData data = new PlayerData(
                uuid,
                section == null ? "unknown" : section.getString("name", "unknown"),
                section == null ? defaultElo : section.getInt("elo", defaultElo));
        if (section == null) {
            return data;
        }
        data.wins = section.getInt("wins");
        data.losses = section.getInt("losses");
        data.kills = section.getInt("kills");
        data.deaths = section.getInt("deaths");
        data.currentStreak = section.getInt("current-streak");
        data.bestStreak = section.getInt("best-streak");
        return data;
    }

    public void write(ConfigurationSection section) {
        section.set("name", name);
        section.set("elo", elo);
        section.set("wins", wins);
        section.set("losses", losses);
        section.set("kills", kills);
        section.set("deaths", deaths);
        section.set("current-streak", currentStreak);
        section.set("best-streak", bestStreak);
    }
}

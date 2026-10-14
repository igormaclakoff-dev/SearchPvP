package com.auracode.searchpvp.util;

/**
 * Maps an Elo rating to a human readable, coloured rank name.
 */
public enum Ranks {

    BRONZE(0, "Бронза"),
    SILVER(1000, "Серебро"),
    GOLD(1200, "Золото"),
    PLATINUM(1400, "Платина"),
    DIAMOND(1600, "Алмаз"),
    LEGEND(1800, "Легенда");

    private final int threshold;
    private final String display;

    Ranks(int threshold, String display) {
        this.threshold = threshold;
        this.display = display;
    }

    public String display() {
        return display;
    }

    public static Ranks of(int elo) {
        Ranks result = BRONZE;
        for (Ranks rank : values()) {
            if (elo >= rank.threshold) {
                result = rank;
            }
        }
        return result;
    }
}

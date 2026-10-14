package com.auracode.searchpvp.util;

/**
 * Standard Elo rating math for 1v1 duels.
 */
public final class Elo {

    private Elo() {
    }

    /**
     * Calculates both new ratings after a duel. Index {@code 0} is the winner,
     * index {@code 1} is the loser.
     *
     * @param winnerRating rating of the winner before the duel
     * @param loserRating  rating of the loser before the duel
     * @param kFactor      the K-factor of the calculation
     * @return the updated ratings as {@code [winner, loser]}
     */
    public static int[] duel(int winnerRating, int loserRating, int kFactor) {
        int newWinner = step(winnerRating, loserRating, 1.0, kFactor);
        int newLoser = step(loserRating, winnerRating, 0.0, kFactor);
        return new int[]{newWinner, newLoser};
    }

    private static int step(int rating, int opponentRating, double score, int kFactor) {
        double expected = 1.0 / (1.0 + Math.pow(10.0, (opponentRating - rating) / 400.0));
        return (int) Math.round(rating + kFactor * (score - expected));
    }
}

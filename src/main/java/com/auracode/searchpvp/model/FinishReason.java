package com.auracode.searchpvp.model;

/**
 * Describes why a match ended and how it should affect ratings.
 */
public enum FinishReason {

    /** A fighter died; counts as a ranked win/loss and a kill/death. */
    DEATH(true, true),
    /** A fighter disconnected; counts as a ranked win/loss but not as a kill/death. */
    QUIT(true, false),
    /** Forcefully stopped by an administrator; no rating changes. */
    ADMIN(false, false),
    /** Technical abort (e.g. a player went offline before the fight began); no rating changes. */
    ABORTED(false, false);

    private final boolean ranked;
    private final boolean countsAsKill;

    FinishReason(boolean ranked, boolean countsAsKill) {
        this.ranked = ranked;
        this.countsAsKill = countsAsKill;
    }

    public boolean isRanked() {
        return ranked;
    }

    public boolean countsAsKill() {
        return countsAsKill;
    }
}

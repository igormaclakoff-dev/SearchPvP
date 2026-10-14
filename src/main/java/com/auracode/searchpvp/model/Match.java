package com.auracode.searchpvp.model;

import org.bukkit.Location;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Represents an active 1v1 duel between two players.
 */
public final class Match {

    private final UUID firstId;
    private final String firstName;
    private final UUID secondId;
    private final String secondName;
    private final ArmorTier tier;
    private final long startedAt;

    private final Map<UUID, Location> returnLocations = new HashMap<>();

    private boolean active = true;
    private boolean countingDown = true;

    public Match(UUID firstId, String firstName, UUID secondId, String secondName, ArmorTier tier) {
        this.firstId = firstId;
        this.firstName = firstName;
        this.secondId = secondId;
        this.secondName = secondName;
        this.tier = tier;
        this.startedAt = System.currentTimeMillis();
    }

    public UUID firstId() {
        return firstId;
    }

    public UUID secondId() {
        return secondId;
    }

    public String nameOf(UUID id) {
        return id.equals(firstId) ? firstName : secondName;
    }

    public UUID opponentOf(UUID id) {
        return id.equals(firstId) ? secondId : firstId;
    }

    public boolean includes(UUID id) {
        return id.equals(firstId) || id.equals(secondId);
    }

    public ArmorTier tier() {
        return tier;
    }

    public long startedAt() {
        return startedAt;
    }

    public void setReturnLocation(UUID id, Location location) {
        returnLocations.put(id, location);
    }

    public Location returnLocation(UUID id) {
        return returnLocations.get(id);
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isCountingDown() {
        return countingDown;
    }

    public void setCountingDown(boolean countingDown) {
        this.countingDown = countingDown;
    }
}

package com.auracode.searchpvp.manager;

import com.auracode.searchpvp.SearchPvPPlugin;
import com.auracode.searchpvp.data.PlayerData;
import com.auracode.searchpvp.model.ArmorTier;
import com.auracode.searchpvp.model.FinishReason;
import com.auracode.searchpvp.model.Match;
import com.auracode.searchpvp.util.Elo;
import com.auracode.searchpvp.util.Placeholders;
import com.auracode.searchpvp.util.PlayerState;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandException;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Owns the lifecycle of active duels: teleport, countdown, PvP mode, rating and cleanup.
 */
public final class MatchManager {

    private static final long RESPAWN_RETURN_DELAY_TICKS = 40L;

    private final SearchPvPPlugin plugin;
    private final Map<UUID, Match> matches = new HashMap<>();
    private final Set<UUID> frozen = new HashSet<>();
    private final Map<UUID, Location> pendingRespawns = new HashMap<>();

    public MatchManager(SearchPvPPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean isInMatch(UUID uuid) {
        return matches.containsKey(uuid);
    }

    public Match matchOf(UUID uuid) {
        return matches.get(uuid);
    }

    public boolean isFrozen(UUID uuid) {
        return frozen.contains(uuid);
    }

    public Location consumePendingRespawn(UUID uuid) {
        return pendingRespawns.remove(uuid);
    }

    /**
     * Creates a new duel, moves both fighters to the arena and starts the countdown.
     */
    public void createMatch(Player first, Player second, ArmorTier tier) {
        Match match = new Match(first.getUniqueId(), first.getName(), second.getUniqueId(), second.getName(), tier);
        match.setReturnLocation(first.getUniqueId(), first.getLocation().clone());
        match.setReturnLocation(second.getUniqueId(), second.getLocation().clone());

        matches.put(first.getUniqueId(), match);
        matches.put(second.getUniqueId(), match);
        PlayerState.setInMatch(first, true);
        PlayerState.setInMatch(second, true);
        frozen.add(first.getUniqueId());
        frozen.add(second.getUniqueId());

        plugin.getMessages().send(first, "match.found",
                Placeholders.of().set("opponent", second.getName()).set("tier", plugin.getMessages().tierName(tier)).build());
        plugin.getMessages().send(second, "match.found",
                Placeholders.of().set("opponent", first.getName()).set("tier", plugin.getMessages().tierName(tier)).build());
        plugin.getMessages().broadcast("match.found-broadcast",
                Placeholders.of().set("first", first.getName()).set("second", second.getName()).build());

        List<Location> spawns = plugin.getArenaManager().spawnLocations();
        CompletableFuture<Boolean> firstFuture = first.teleportAsync(spawns.get(0));
        CompletableFuture<Boolean> secondFuture = second.teleportAsync(spawns.get(1));
        CompletableFuture.allOf(firstFuture, secondFuture)
                .thenRun(() -> Bukkit.getScheduler().runTask(plugin, () -> startCountdown(match)));
    }

    private void startCountdown(Match match) {
        if (!match.isActive()) {
            return;
        }
        int seconds = plugin.getPluginConfig().getCountdownSeconds();
        if (seconds <= 0) {
            beginFight(match);
            return;
        }
        runCountdown(match, seconds);
    }

    private void runCountdown(Match match, int remaining) {
        if (!match.isActive() || !match.isCountingDown()) {
            return;
        }
        Player first = Bukkit.getPlayer(match.firstId());
        Player second = Bukkit.getPlayer(match.secondId());
        if (first == null || second == null) {
            finish(match, null, null, FinishReason.ABORTED);
            return;
        }
        Map<String, String> placeholders = Placeholders.of().set("seconds", remaining).build();
        plugin.getMessages().title(first, "match.countdown", "match.countdown-sub", placeholders, 900L);
        plugin.getMessages().title(second, "match.countdown", "match.countdown-sub", placeholders, 900L);
        if (remaining <= 1) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> beginFight(match), 20L);
            return;
        }
        Bukkit.getScheduler().runTaskLater(plugin, () -> runCountdown(match, remaining - 1), 20L);
    }

    private void beginFight(Match match) {
        if (!match.isActive()) {
            return;
        }
        match.setCountingDown(false);
        frozen.remove(match.firstId());
        frozen.remove(match.secondId());

        Player first = Bukkit.getPlayer(match.firstId());
        Player second = Bukkit.getPlayer(match.secondId());
        if (first == null || second == null) {
            finish(match, null, null, FinishReason.ABORTED);
            return;
        }
        applyPvpMode(first);
        applyPvpMode(second);
        plugin.getMessages().title(first, "match.go", "match.go-sub", Map.of(), 900L);
        plugin.getMessages().title(second, "match.go", "match.go-sub", Map.of(), 900L);
    }

    /**
     * Ends the duel. Winner may be {@code null} for aborted/admin-stopped fights.
     */
    public void finish(Match match, UUID winnerId, UUID loserId, FinishReason reason) {
        if (match == null || !match.isActive()) {
            return;
        }
        match.setActive(false);
        matches.remove(match.firstId());
        matches.remove(match.secondId());
        frozen.remove(match.firstId());
        frozen.remove(match.secondId());

        Player first = Bukkit.getPlayer(match.firstId());
        Player second = Bukkit.getPlayer(match.secondId());
        if (first != null) {
            PlayerState.setInMatch(first, false);
        }
        if (second != null) {
            PlayerState.setInMatch(second, false);
        }

        RatingResult rating = null;
        if (reason.isRanked() && winnerId != null && loserId != null) {
            rating = applyRating(match, winnerId, loserId, reason);
        }

        cleanupTeleport(match, winnerId, loserId);

        if (reason == FinishReason.ADMIN || reason == FinishReason.ABORTED) {
            plugin.getMessages().send(first, "match.aborted");
            plugin.getMessages().send(second, "match.aborted");
            plugin.getDataManager().save();
            return;
        }

        announceResult(match, winnerId, loserId, rating);
        plugin.getDataManager().save();
    }

    private RatingResult applyRating(Match match, UUID winnerId, UUID loserId, FinishReason reason) {
        PlayerData winner = plugin.getDataManager().getOrCreate(winnerId, match.nameOf(winnerId));
        PlayerData loser = plugin.getDataManager().getOrCreate(loserId, match.nameOf(loserId));

        int[] updated = Elo.duel(winner.elo(), loser.elo(), plugin.getPluginConfig().getEloKFactor());
        int winnerDelta = updated[0] - winner.elo();
        int loserDelta = updated[1] - loser.elo();

        winner.setElo(updated[0]);
        loser.setElo(updated[1]);
        winner.addWin();
        loser.addLoss();
        if (reason.countsAsKill()) {
            winner.addKill();
            loser.addDeath();
        }
        return new RatingResult(updated[0], updated[1], winnerDelta, loserDelta);
    }

    private void cleanupTeleport(Match match, UUID winnerId, UUID loserId) {
        if (!plugin.getPluginConfig().isReturnToOrigin()) {
            return;
        }
        if (winnerId != null) {
            Player winner = Bukkit.getPlayer(winnerId);
            if (winner != null) {
                teleportBack(winner, match.returnLocation(winnerId));
            }
        }
        if (loserId != null) {
            Player loser = Bukkit.getPlayer(loserId);
            Location location = match.returnLocation(loserId);
            if (location != null) {
                if (loser != null) {
                    pendingRespawns.put(loserId, location);
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        if (loser.isOnline() && plugin.getMatchManager().consumePendingRespawn(loserId) != null) {
                            teleportBack(loser, location);
                        }
                    }, RESPAWN_RETURN_DELAY_TICKS);
                }
            }
        }
    }

    private void teleportBack(Player player, Location location) {
        if (location != null && player.isOnline()) {
            player.teleportAsync(location);
        }
    }

    private void announceResult(Match match, UUID winnerId, UUID loserId, RatingResult rating) {
        if (winnerId == null || loserId == null || rating == null) {
            return;
        }
        Player winner = Bukkit.getPlayer(winnerId);
        Player loser = Bukkit.getPlayer(loserId);
        plugin.getMessages().send(winner, "match.won",
                Placeholders.of().set("elo", rating.winnerElo()).set("change", signed(rating.winnerDelta())).build());
        plugin.getMessages().send(loser, "match.lost",
                Placeholders.of().set("elo", rating.loserElo()).set("change", signed(rating.loserDelta())).build());
        plugin.getMessages().broadcast("match.broadcast-result", Placeholders.of()
                .set("winner", match.nameOf(winnerId))
                .set("loser", match.nameOf(loserId))
                .set("winner_elo", rating.winnerElo())
                .set("loser_elo", rating.loserElo())
                .build());
        runReward(winner, plugin.getPluginConfig().getWinCommand(), rating.winnerElo());
        runReward(loser, plugin.getPluginConfig().getLoseCommand(), rating.loserElo());
    }

    private void runReward(Player player, String template, int elo) {
        if (player == null || template == null || template.isBlank()) {
            return;
        }
        String command = applyPlaceholders(template, player.getName(), String.valueOf(elo));
        dispatchConsole(command);
    }

    public void applyPvpMode(Player player) {
        String command = plugin.getPluginConfig().getPlacementCommand();
        if (command == null || command.isBlank()) {
            return;
        }
        dispatchConsole(applyPlaceholders(command, player.getName(), null));
    }

    /**
     * Replaces both {@code %name%}/{@code <name>} (and {@code %elo%}/{@code <elo>}) styles.
     * {@code elo} may be {@code null} when the value is not relevant.
     */
    private static String applyPlaceholders(String template, String name, String elo) {
        String result = template.replace("%name%", name).replace("<name>", name);
        if (elo != null) {
            result = result.replace("%elo%", elo).replace("<elo>", elo);
        }
        return result;
    }

    private void dispatchConsole(String command) {
        if (command == null || command.isBlank()) {
            return;
        }
        String cleaned = command.startsWith("/") ? command.substring(1) : command;
        Bukkit.getScheduler().runTask(plugin, () -> {
            try {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cleaned);
            } catch (CommandException exception) {
                plugin.getLogger().warning("Failed to run command '" + cleaned + "': " + exception.getMessage());
            }
        });
    }

    public void handleDeath(Player victim) {
        Match match = matches.get(victim.getUniqueId());
        if (match == null) {
            return;
        }
        UUID winnerId = match.opponentOf(victim.getUniqueId());
        Bukkit.getScheduler().runTask(plugin, () -> finish(match, winnerId, victim.getUniqueId(), FinishReason.DEATH));
    }

    public void handleQuit(Player player) {
        Match match = matches.get(player.getUniqueId());
        if (match == null) {
            return;
        }
        UUID winnerId = match.opponentOf(player.getUniqueId());
        finish(match, winnerId, player.getUniqueId(), FinishReason.QUIT);
    }

    public boolean forceStop(Player player) {
        if (plugin.getMatchmakingManager().cancel(player, "search.cancelled-manual")) {
            return true;
        }
        Match match = matches.get(player.getUniqueId());
        if (match == null) {
            return false;
        }
        finish(match, null, null, FinishReason.ADMIN);
        return true;
    }

    private static String signed(int value) {
        return (value >= 0 ? "+" : "") + value;
    }

    public void shutdown() {
        for (Match match : new HashSet<>(matches.values())) {
            match.setActive(false);
        }
        for (UUID id : frozen) {
            Player player = Bukkit.getPlayer(id);
            if (player != null) {
                PlayerState.setInMatch(player, false);
            }
        }
        frozen.clear();
        matches.clear();
        pendingRespawns.clear();
    }

    private record RatingResult(int winnerElo, int loserElo, int winnerDelta, int loserDelta) {
    }
}

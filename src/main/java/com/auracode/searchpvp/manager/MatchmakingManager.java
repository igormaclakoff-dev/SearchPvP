package com.auracode.searchpvp.manager;

import com.auracode.searchpvp.SearchPvPPlugin;
import com.auracode.searchpvp.model.ArmorTier;
import com.auracode.searchpvp.model.QueueEntry;
import com.auracode.searchpvp.util.Placeholders;
import com.auracode.searchpvp.util.PlayerState;
import net.kyori.adventure.bossbar.BossBar;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Matchmaking queue: validates armor, shows progress and pairs players of the same tier.
 */
public final class MatchmakingManager {

    private final SearchPvPPlugin plugin;
    private final Map<UUID, QueueEntry> queue = new LinkedHashMap<>();

    private BukkitTask task;
    private int animationTick;

    public MatchmakingManager(SearchPvPPlugin plugin) {
        this.plugin = plugin;
    }

    public void start() {
        if (task != null) {
            task.cancel();
        }
        long interval = plugin.getPluginConfig().getMatchmakingIntervalTicks();
        this.task = Bukkit.getScheduler().runTaskTimer(plugin, this::tick, interval, interval);
    }

    public void shutdown() {
        if (task != null) {
            task.cancel();
        }
        for (QueueEntry entry : queue.values()) {
            Player player = Bukkit.getPlayer(entry.uuid());
            if (player != null) {
                player.hideBossBar(entry.bossBar());
                PlayerState.setInQueue(player, false);
            }
        }
        queue.clear();
    }

    public boolean isSearching(UUID uuid) {
        return queue.containsKey(uuid);
    }

    /**
     * @return the armor tier the player is queued with, or {@code null} when not queued
     */
    public ArmorTier tierOf(UUID uuid) {
        QueueEntry entry = queue.get(uuid);
        return entry == null ? null : entry.tier();
    }

    /**
     * Adds a player to the queue after validating that a full allowed armor set is worn.
     */
    public void requestSearch(Player player) {
        UUID uuid = player.getUniqueId();
        if (plugin.getMatchManager().isInMatch(uuid)) {
            plugin.getMessages().send(player, "search.in-match");
            return;
        }
        if (queue.containsKey(uuid)) {
            plugin.getMessages().send(player, "search.already");
            return;
        }

        ArmorTier.Snapshot snapshot = ArmorTier.Snapshot.of(player);
        ArmorTier tier = snapshot.tier();
        if (tier == null) {
            plugin.getMessages().send(player, "search.need-armor");
            return;
        }
        if (!plugin.getPluginConfig().getAllowedTiers().contains(tier)) {
            plugin.getMessages().send(player, "search.tier-disabled",
                    Placeholders.of().set("tier", plugin.getMessages().tierName(tier)).build());
            return;
        }

        BossBar bossBar = BossBar.bossBar(
                plugin.getMessages().component("search.bossbar",
                        Placeholders.of().set("tier", plugin.getMessages().tierName(tier)).build()),
                1.0f, BossBar.Color.PURPLE, BossBar.Overlay.PROGRESS);

        queue.put(uuid, new QueueEntry(uuid, player.getName(), tier, snapshot, bossBar));
        PlayerState.setInQueue(player, true);
        player.showBossBar(bossBar);
        player.sendActionBar(plugin.getMessages().component("search.actionbar"));
        plugin.getMessages().send(player, "search.started",
                Placeholders.of().set("tier", plugin.getMessages().tierName(tier)).build());
        plugin.getMessages().broadcast("search.broadcast-started",
                Placeholders.of().set("player", player.getName())
                        .set("tier", plugin.getMessages().tierName(tier)).build());
        player.closeInventory();
    }

    /**
     * Removes a player from the queue.
     *
     * @return {@code true} if the player was actually queued
     */
    public boolean cancel(Player player, String reasonKey) {
        QueueEntry entry = queue.remove(player.getUniqueId());
        if (entry == null) {
            return false;
        }
        player.hideBossBar(entry.bossBar());
        PlayerState.setInQueue(player, false);
        plugin.getMessages().send(player, reasonKey);
        plugin.getMessages().broadcast("search.broadcast-cancelled",
                Placeholders.of().set("player", player.getName()).build());
        return true;
    }

    private void tick() {
        if (queue.isEmpty()) {
            return;
        }
        animationTick++;

        List<UUID> cancelled = new ArrayList<>();
        for (QueueEntry entry : new ArrayList<>(queue.values())) {
            Player player = Bukkit.getPlayer(entry.uuid());
            if (player == null || !player.isOnline()) {
                cancelled.add(entry.uuid());
                continue;
            }
            animate(entry.bossBar());
            if (!checkArmor(player, entry)) {
                cancelled.add(entry.uuid());
            }
        }
        for (UUID uuid : cancelled) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                cancel(player, "search.cancelled-armor");
            } else {
                removeSilently(uuid);
            }
        }
        for (ArmorTier tier : plugin.getPluginConfig().getAllowedTiers()) {
            tryMatch(tier);
        }
    }

    private void animate(BossBar bossBar) {
        float progress = (float) (0.5 + 0.5 * Math.sin(animationTick / 6.0));
        bossBar.progress(Math.max(0.05f, Math.min(1.0f, progress)));
    }

    /**
     * @return {@code false} when the armor was not restored in time and the search must be cancelled
     */
    private boolean checkArmor(Player player, QueueEntry entry) {
        if (entry.armor().matches(player)) {
            if (entry.armorDeviatedAt() != null) {
                entry.clearArmorDeviation();
                plugin.getMessages().send(player, "search.armour-restored");
            }
            return true;
        }

        entry.markArmorDeviation();
        long elapsed = System.currentTimeMillis() - entry.armorDeviatedAt();
        long graceMillis = plugin.getPluginConfig().getArmorChangeGraceSeconds() * 1000L;
        long remaining = graceMillis - elapsed;
        if (remaining <= 0) {
            return false;
        }
        long seconds = Math.max(1L, (remaining + 999L) / 1000L);
        plugin.getMessages().title(player, "search.armour-title", "search.armour-subtitle",
                Placeholders.of().set("seconds", seconds).build(), 1000L);
        return true;
    }

    private void tryMatch(ArmorTier tier) {
        List<QueueEntry> candidates = queue.values().stream()
                .filter(entry -> entry.tier() == tier)
                .sorted(Comparator.comparingLong(QueueEntry::startedAt))
                .toList();
        if (candidates.size() < 2) {
            return;
        }
        QueueEntry firstEntry = candidates.get(0);
        QueueEntry secondEntry = candidates.get(1);
        Player first = Bukkit.getPlayer(firstEntry.uuid());
        Player second = Bukkit.getPlayer(secondEntry.uuid());
        if (first == null || second == null) {
            return;
        }
        queue.remove(firstEntry.uuid());
        queue.remove(secondEntry.uuid());
        first.hideBossBar(firstEntry.bossBar());
        second.hideBossBar(secondEntry.bossBar());
        PlayerState.setInQueue(first, false);
        PlayerState.setInQueue(second, false);
        plugin.getMatchManager().createMatch(first, second, tier);
    }

    private void removeSilently(UUID uuid) {
        QueueEntry entry = queue.remove(uuid);
        if (entry != null) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                player.hideBossBar(entry.bossBar());
                PlayerState.setInQueue(player, false);
            }
        }
    }
}

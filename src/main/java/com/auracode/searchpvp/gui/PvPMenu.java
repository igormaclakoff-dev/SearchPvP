package com.auracode.searchpvp.gui;

import com.auracode.searchpvp.SearchPvPPlugin;
import com.auracode.searchpvp.data.PlayerData;
import com.auracode.searchpvp.manager.MatchmakingManager;
import com.auracode.searchpvp.model.ArmorTier;
import com.auracode.searchpvp.util.ItemBuilder;
import com.auracode.searchpvp.util.Placeholders;
import com.auracode.searchpvp.util.Ranks;
import com.auracode.searchpvp.util.Text;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Map;

/**
 * Main /pvp menu: personal statistics, search button, leaderboard and season info.
 */
public final class PvPMenu implements PluginMenu {

    private static final int SIZE = 45;
    private static final int SLOT_HEAD = 13;
    private static final int SLOT_SEARCH = 29;
    private static final int SLOT_TOP = 31;
    private static final int SLOT_SEASON = 33;
    private static final int SLOT_CLOSE = 40;

    private final SearchPvPPlugin plugin;
    private final Player viewer;
    private final Inventory inventory;

    public PvPMenu(SearchPvPPlugin plugin, Player viewer) {
        this.plugin = plugin;
        this.viewer = viewer;
        this.inventory = Bukkit.createInventory(this, SIZE, Text.parseItem(plugin.getMessages().raw("menu.title")));
        render();
    }

    private void render() {
        for (int slot = 0; slot < SIZE; slot++) {
            inventory.setItem(slot, border());
        }
        inventory.setItem(SLOT_HEAD, head());
        inventory.setItem(SLOT_SEARCH, search());
        inventory.setItem(SLOT_TOP, top());
        inventory.setItem(SLOT_SEASON, season());
        inventory.setItem(SLOT_CLOSE, close());
    }

    private ItemStack border() {
        return new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).name(Component.text(" ")).build();
    }

    private ItemStack head() {
        PlayerData data = plugin.getDataManager().getOrCreate(viewer.getUniqueId(), viewer.getName());
        Map<String, String> placeholders = statsPlaceholders(data);

        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        if (meta != null) {
            meta.setOwningPlayer(viewer);
            meta.displayName(Text.parseItem(plugin.getMessages().raw("menu.stats.name"), placeholders));
            meta.lore(plugin.getMessages().componentList("menu.stats.lore", placeholders));
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack search() {
        MatchmakingManager matchmaking = plugin.getMatchmakingManager();
        if (matchmaking.isSearching(viewer.getUniqueId())) {
            ArmorTier tier = matchmaking.tierOf(viewer.getUniqueId());
            Map<String, String> placeholders = Placeholders.of()
                    .set("tier", tier == null ? "" : plugin.getMessages().tierName(tier))
                    .build();
            return new ItemBuilder(Material.BARRIER)
                    .name(Text.parseItem(plugin.getMessages().raw("menu.cancel.name")))
                    .lore(plugin.getMessages().componentList("menu.cancel.lore", placeholders))
                    .build();
        }
        return new ItemBuilder(Material.DIAMOND_SWORD)
                .name(Text.parseItem(plugin.getMessages().raw("menu.search.name")))
                .lore(plugin.getMessages().componentList("menu.search.lore", Map.of()))
                .glow(true)
                .build();
    }

    private ItemStack top() {
        return new ItemBuilder(Material.NETHER_STAR)
                .name(Text.parseItem(plugin.getMessages().raw("menu.top.name")))
                .lore(plugin.getMessages().componentList("menu.top.lore",
                        Placeholders.of().set("season", plugin.getSeasonManager().season()).build()))
                .build();
    }

    private ItemStack season() {
        return new ItemBuilder(Material.CLOCK)
                .name(Text.parseItem(plugin.getMessages().raw("menu.season.name"),
                        Placeholders.of().set("season", plugin.getSeasonManager().season()).build()))
                .lore(plugin.getMessages().componentList("menu.season.lore",
                        Placeholders.of().set("time", plugin.getSeasonManager().timeLeft()).build()))
                .build();
    }

    private ItemStack close() {
        return new ItemBuilder(Material.BARRIER)
                .name(Text.parseItem(plugin.getMessages().raw("menu.close.name")))
                .lore(plugin.getMessages().componentList("menu.close.lore", Map.of()))
                .build();
    }

    private Map<String, String> statsPlaceholders(PlayerData data) {
        return Placeholders.of()
                .set("player", data.name())
                .set("elo", data.elo())
                .set("rank", Ranks.of(data.elo()).display())
                .set("season", plugin.getSeasonManager().season())
                .set("wins", data.wins())
                .set("losses", data.losses())
                .set("matches", data.matches())
                .set("winrate", String.format("%.0f", data.winRate()))
                .set("kills", data.kills())
                .set("deaths", data.deaths())
                .set("kd", String.format("%.2f", data.killDeathRatio()))
                .set("streak", data.currentStreak())
                .set("best_streak", data.bestStreak())
                .build();
    }

    @Override
    public void handleClick(Player who, int slot) {
        switch (slot) {
            case SLOT_SEARCH -> toggleSearch(who);
            case SLOT_TOP -> plugin.getMenuService().openTop(who, 0);
            case SLOT_CLOSE -> who.closeInventory();
            default -> {
                // decorative slot, ignore
            }
        }
    }

    private void toggleSearch(Player who) {
        MatchmakingManager matchmaking = plugin.getMatchmakingManager();
        if (matchmaking.isSearching(who.getUniqueId())) {
            matchmaking.cancel(who, "search.cancelled-manual");
            plugin.getMenuService().openMain(who);
        } else {
            matchmaking.requestSearch(who);
        }
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public Player viewer() {
        return viewer;
    }
}

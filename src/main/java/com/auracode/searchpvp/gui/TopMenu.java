package com.auracode.searchpvp.gui;

import com.auracode.searchpvp.SearchPvPPlugin;
import com.auracode.searchpvp.data.PlayerData;
import com.auracode.searchpvp.manager.RewardManager;
import com.auracode.searchpvp.util.ItemBuilder;
import com.auracode.searchpvp.util.Placeholders;
import com.auracode.searchpvp.util.Ranks;
import com.auracode.searchpvp.util.Text;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Paginated Elo leaderboard.
 */
public final class TopMenu implements PluginMenu {

    private static final int SIZE = 54;
    private static final int PAGE_SIZE = 28;
    private static final int SLOT_REWARDS = 4;
    private static final int SLOT_BACK = 45;
    private static final int SLOT_PREVIOUS = 48;
    private static final int SLOT_PAGE_INFO = 49;
    private static final int SLOT_NEXT = 50;

    private final SearchPvPPlugin plugin;
    private final Player viewer;
    private final int page;
    private final int totalPages;
    private final List<PlayerData> entries;
    private final Inventory inventory;

    public TopMenu(SearchPvPPlugin plugin, Player viewer, int requestedPage) {
        this.plugin = plugin;
        this.viewer = viewer;
        this.entries = plugin.getDataManager().topByElo(Integer.MAX_VALUE);
        this.totalPages = Math.max(1, (int) Math.ceil(entries.size() / (double) PAGE_SIZE));
        this.page = Math.max(0, Math.min(requestedPage, totalPages - 1));
        this.inventory = Bukkit.createInventory(this, SIZE, Text.parseItem(
                plugin.getMessages().raw("top.title"),
                Placeholders.of().set("page", page + 1).build()));
        render();
    }

    private void render() {
        for (int slot = 0; slot < SIZE; slot++) {
            inventory.setItem(slot, border());
        }

        if (plugin.getRewardManager().hasAnyRewards()) {
            inventory.setItem(SLOT_REWARDS, rewards());
        }

        if (entries.isEmpty()) {
            inventory.setItem(22, new ItemBuilder(Material.BARRIER)
                    .name(Text.parseItem(plugin.getMessages().raw("top.empty")))
                    .build());
        } else {
            int start = page * PAGE_SIZE;
            List<Integer> slots = contentSlots();
            for (int i = 0; i < PAGE_SIZE && start + i < entries.size(); i++) {
                inventory.setItem(slots.get(i), entry(entries.get(start + i), start + i + 1));
            }
        }

        inventory.setItem(SLOT_BACK, new ItemBuilder(Material.ARROW)
                .name(Text.parseItem(plugin.getMessages().raw("top.back")))
                .build());

        if (page > 0) {
            inventory.setItem(SLOT_PREVIOUS, new ItemBuilder(Material.LIME_DYE)
                    .name(Text.parseItem(plugin.getMessages().raw("top.previous")))
                    .build());
        }
        if (page < totalPages - 1) {
            inventory.setItem(SLOT_NEXT, new ItemBuilder(Material.LIME_DYE)
                    .name(Text.parseItem(plugin.getMessages().raw("top.next")))
                    .build());
        }
        inventory.setItem(SLOT_PAGE_INFO, new ItemBuilder(Material.PAPER)
                .name(Text.parseItem(plugin.getMessages().raw("top.page-info"),
                        Placeholders.of().set("page", page + 1).set("total", totalPages).build()))
                .build());
    }

    private ItemStack entry(PlayerData data, int position) {
        Map<String, String> placeholders = Placeholders.of()
                .set("position", position)
                .set("player", data.name())
                .set("elo", data.elo())
                .set("rank", Ranks.of(data.elo()).display())
                .set("wins", data.wins())
                .set("losses", data.losses())
                .build();

        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        if (meta != null) {
            OfflinePlayer owner = Bukkit.getOfflinePlayer(data.uuid());
            meta.setOwningPlayer(owner);
            meta.displayName(Text.parseItem(plugin.getMessages().raw("top.entry-name"), placeholders));
            meta.lore(plugin.getMessages().componentList("top.entry-lore", placeholders));
            item.setItemMeta(meta);
        }
        return item;
    }

    private static List<Integer> contentSlots() {
        List<Integer> slots = new ArrayList<>(PAGE_SIZE);
        for (int row = 1; row <= 4; row++) {
            for (int column = 1; column <= 7; column++) {
                slots.add(row * 9 + column);
            }
        }
        return slots;
    }

    private ItemStack border() {
        return new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).name(Component.text(" ")).build();
    }

    private ItemStack rewards() {
        List<Component> lore = new ArrayList<>();
        for (int place = 1; place <= RewardManager.maxPlace(); place++) {
            String holder = place <= entries.size() ? entries.get(place - 1).name() : "-";
            lore.add(Text.parseItem(plugin.getMessages().raw("top.reward-place"),
                    Placeholders.of().set("place", place).set("player", holder).build()));
            List<String> lines = plugin.getRewardManager().lines(place);
            if (lines.isEmpty()) {
                lore.add(Text.parseItem(plugin.getMessages().raw("top.reward-none")));
            } else {
                for (String line : lines) {
                    lore.add(Text.parseItem(plugin.getMessages().raw("top.reward-line"),
                            Placeholders.of().set("line", line).build()));
                }
            }
        }
        return new ItemBuilder(Material.GOLDEN_APPLE)
                .name(Text.parseItem(plugin.getMessages().raw("top.reward-name")))
                .lore(lore)
                .build();
    }

    @Override
    public void handleClick(Player who, int slot) {
        switch (slot) {
            case SLOT_BACK -> plugin.getMenuService().openMain(who);
            case SLOT_PREVIOUS -> plugin.getMenuService().openTop(who, page - 1);
            case SLOT_NEXT -> plugin.getMenuService().openTop(who, page + 1);
            default -> {
                // entry preview or decorative slot, ignore
            }
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

package com.auracode.searchpvp.command;

import com.auracode.searchpvp.SearchPvPPlugin;
import com.auracode.searchpvp.data.PlayerData;
import com.auracode.searchpvp.util.Placeholders;
import com.auracode.searchpvp.util.Ranks;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * {@code /top} - shows the leaderboard (GUI for players, chat list for console).
 */
public final class TopCommand implements TabExecutor {

    private static final int CONSOLE_LIMIT = 10;

    private final SearchPvPPlugin plugin;

    public TopCommand(SearchPvPPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (sender instanceof Player player && player.hasPermission("searchpvp.use")) {
            plugin.getMenuService().openTop(player, 0);
            return true;
        }
        sendConsoleTop(sender);
        return true;
    }

    private void sendConsoleTop(CommandSender sender) {
        List<PlayerData> top = plugin.getDataManager().topByElo(CONSOLE_LIMIT);
        if (top.isEmpty()) {
            plugin.getMessages().send(sender, "top.empty");
            return;
        }
        int position = 1;
        for (PlayerData data : top) {
            sender.sendMessage(plugin.getMessages().component("top.entry-name", Placeholders.of()
                    .set("position", position++)
                    .set("player", data.name())
                    .set("elo", data.elo())
                    .set("rank", Ranks.of(data.elo()).display())
                    .set("wins", data.wins())
                    .set("losses", data.losses())
                    .build()));
        }
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                      @NotNull String alias, @NotNull String[] args) {
        return List.of();
    }
}

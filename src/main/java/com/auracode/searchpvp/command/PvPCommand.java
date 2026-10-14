package com.auracode.searchpvp.command;

import com.auracode.searchpvp.SearchPvPPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Locale;

/**
 * {@code /pvp} - opens the main menu; {@code /pvp cancel} leaves the queue.
 */
public final class PvPCommand implements TabExecutor {

    private static final String SUBCOMMAND_CANCEL = "cancel";

    private final SearchPvPPlugin plugin;

    public PvPCommand(SearchPvPPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            plugin.getMessages().send(sender, "command.player-only");
            return true;
        }
        if (!player.hasPermission("searchpvp.use")) {
            plugin.getMessages().send(player, "command.no-permission");
            return true;
        }
        if (args.length > 0 && args[0].equalsIgnoreCase(SUBCOMMAND_CANCEL)) {
            cancelSearch(player);
            return true;
        }
        plugin.getMenuService().openMain(player);
        return true;
    }

    private void cancelSearch(Player player) {
        if (!plugin.getMatchmakingManager().cancel(player, "search.cancelled-manual")) {
            plugin.getMessages().send(player, "search.not-searching");
        }
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                      @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1 && SUBCOMMAND_CANCEL.startsWith(args[0].toLowerCase(Locale.ROOT))) {
            return List.of(SUBCOMMAND_CANCEL);
        }
        return List.of();
    }
}

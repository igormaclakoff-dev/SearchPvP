package com.auracode.searchpvp.command;

import com.auracode.searchpvp.SearchPvPPlugin;
import com.auracode.searchpvp.data.PlayerData;
import com.auracode.searchpvp.manager.RewardManager;
import com.auracode.searchpvp.util.Placeholders;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * {@code /pvpadmin} - administrative subcommands.
 */
public final class PvPAdminCommand implements TabExecutor {

    private static final List<String> SUBCOMMANDS = List.of(
            "help", "reload", "setpvpcommand", "season", "forcestop", "forcesearch", "setelo", "reward");

    private final SearchPvPPlugin plugin;

    public PvPAdminCommand(SearchPvPPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("searchpvp.admin")) {
            plugin.getMessages().send(sender, "command.no-permission");
            return true;
        }
        if (args.length == 0) {
            help(sender);
            return true;
        }

        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "help", "info" -> help(sender);
            case "reload" -> reload(sender);
            case "setpvpcommand" -> setPvpCommand(sender, args);
            case "season" -> season(sender, args);
            case "forcestop" -> forceStop(sender, args);
            case "forcesearch" -> forceSearch(sender, args);
            case "setelo" -> setElo(sender, args);
            case "reward" -> reward(sender, args);
            default -> {
                plugin.getMessages().send(sender, "command.unknown-subcommand",
                        Placeholders.of().set("arg", args[0]).build());
                plugin.getMessages().send(sender, "command.usage-admin");
            }
        }
        return true;
    }

    private void help(CommandSender sender) {
        plugin.getMessages().sendList(sender, "command.help",
                Placeholders.of().set("season", plugin.getSeasonManager().season()).build());
    }

    private void reload(CommandSender sender) {
        if (!sender.hasPermission("searchpvp.command.reload")) {
            plugin.getMessages().send(sender, "command.no-permission");
            return;
        }
        plugin.reload();
        plugin.getMessages().send(sender, "command.reloaded");
    }

    private void setPvpCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("searchpvp.command.setpvpcommand")) {
            plugin.getMessages().send(sender, "command.no-permission");
            return;
        }
        if (args.length < 2) {
            plugin.getMessages().send(sender, "command.usage-admin");
            return;
        }
        String value = String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length));
        plugin.setPlacementCommand(value);
        plugin.getMessages().send(sender, "command.pvpcommand-set", Placeholders.of().set("value", value).build());
    }

    private void season(CommandSender sender, String[] args) {
        if (!sender.hasPermission("searchpvp.command.season")) {
            plugin.getMessages().send(sender, "command.no-permission");
            return;
        }
        String action = args.length < 2 ? "info" : args[1].toLowerCase(Locale.ROOT);
        switch (action) {
            case "reset" -> {
                plugin.getSeasonManager().startNewSeason();
                plugin.getMessages().send(sender, "command.season-reset",
                        Placeholders.of().set("season", plugin.getSeasonManager().season()).build());
            }
            case "finish" -> {
                plugin.getSeasonManager().endSeason();
                plugin.getMessages().send(sender, "command.season-finished",
                        Placeholders.of().set("season", plugin.getSeasonManager().season()).build());
            }
            case "set" -> setSeason(sender, args);
            case "info" -> plugin.getMessages().send(sender, "command.season-info", Placeholders.of()
                    .set("season", plugin.getSeasonManager().season())
                    .set("time", plugin.getSeasonManager().timeLeft())
                    .build());
            default -> plugin.getMessages().send(sender, "command.unknown-subcommand",
                    Placeholders.of().set("arg", action).build());
        }
    }

    private void setSeason(CommandSender sender, String[] args) {
        if (args.length < 3) {
            plugin.getMessages().send(sender, "command.season-set-usage");
            return;
        }
        int number;
        try {
            number = Integer.parseInt(args[2]);
        } catch (NumberFormatException exception) {
            plugin.getMessages().send(sender, "command.season-set-usage");
            return;
        }
        plugin.getSeasonManager().setSeason(number);
        plugin.getMessages().send(sender, "command.season-set",
                Placeholders.of().set("season", plugin.getSeasonManager().season()).build());
    }

    private void forceStop(CommandSender sender, String[] args) {
        if (!sender.hasPermission("searchpvp.command.forcestop")) {
            plugin.getMessages().send(sender, "command.no-permission");
            return;
        }
        if (args.length < 2) {
            plugin.getMessages().send(sender, "command.usage-admin");
            return;
        }
        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            plugin.getMessages().send(sender, "command.setelo-unknown",
                    Placeholders.of().set("player", args[1]).build());
            return;
        }
        if (plugin.getMatchManager().forceStop(target)) {
            plugin.getMessages().send(sender, "command.forcestop-done",
                    Placeholders.of().set("player", target.getName()).build());
        } else {
            plugin.getMessages().send(sender, "command.forcestop-none",
                    Placeholders.of().set("player", target.getName()).build());
        }
    }

    private void forceSearch(CommandSender sender, String[] args) {
        if (!sender.hasPermission("searchpvp.command.forcesearch")) {
            plugin.getMessages().send(sender, "command.no-permission");
            return;
        }
        if (args.length < 2) {
            plugin.getMessages().send(sender, "command.usage-admin");
            return;
        }
        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            plugin.getMessages().send(sender, "command.setelo-unknown",
                    Placeholders.of().set("player", args[1]).build());
            return;
        }
        plugin.getMatchmakingManager().requestSearch(target);
        boolean queued = plugin.getMatchmakingManager().isSearching(target.getUniqueId());
        plugin.getMessages().send(sender, queued ? "command.forcesearch-done" : "command.forcesearch-fail",
                Placeholders.of().set("player", target.getName()).build());
    }

    private void reward(CommandSender sender, String[] args) {
        if (!sender.hasPermission("searchpvp.command.reward")) {
            plugin.getMessages().send(sender, "command.no-permission");
            return;
        }
        if (args.length < 3) {
            plugin.getMessages().send(sender, "command.reward-usage");
            return;
        }
        Integer place = parsePlace(args[1]);
        if (place == null) {
            plugin.getMessages().send(sender, "command.reward-bad-place");
            return;
        }
        String type = args[2].toLowerCase(Locale.ROOT);
        if (type.equals("info")) {
            rewardInfo(sender, place);
            return;
        }
        if (!type.equals("line") && !type.equals("command")) {
            plugin.getMessages().send(sender, "command.reward-usage");
            return;
        }
        if (args.length < 4) {
            plugin.getMessages().send(sender, "command.reward-usage");
            return;
        }
        boolean isLine = type.equals("line");
        switch (args[3].toLowerCase(Locale.ROOT)) {
            case "add" -> rewardAdd(sender, place, isLine, args);
            case "remove" -> rewardRemove(sender, place, isLine, args);
            case "clear" -> rewardClear(sender, place, isLine);
            default -> plugin.getMessages().send(sender, "command.reward-usage");
        }
    }

    private void rewardAdd(CommandSender sender, int place, boolean isLine, String[] args) {
        if (args.length < 5) {
            plugin.getMessages().send(sender, "command.reward-usage");
            return;
        }
        String value = String.join(" ", Arrays.copyOfRange(args, 4, args.length));
        if (isLine) {
            plugin.getRewardManager().addLine(place, value);
            plugin.getMessages().send(sender, "command.reward-line-added",
                    Placeholders.of().set("place", place).build());
        } else {
            plugin.getRewardManager().addCommand(place, value);
            plugin.getMessages().send(sender, "command.reward-command-added",
                    Placeholders.of().set("place", place).build());
        }
    }

    private void rewardRemove(CommandSender sender, int place, boolean isLine, String[] args) {
        if (args.length < 5) {
            plugin.getMessages().send(sender, "command.reward-usage");
            return;
        }
        Integer index = parseIndex(args[4]);
        if (index == null) {
            plugin.getMessages().send(sender, "command.reward-usage");
            return;
        }
        boolean removed = isLine
                ? plugin.getRewardManager().removeLine(place, index)
                : plugin.getRewardManager().removeCommand(place, index);
        if (!removed) {
            plugin.getMessages().send(sender, "command.reward-index-missing",
                    Placeholders.of().set("index", index + 1).build());
            return;
        }
        plugin.getMessages().send(sender,
                isLine ? "command.reward-line-removed" : "command.reward-command-removed",
                Placeholders.of().set("index", index + 1).set("place", place).build());
    }

    private void rewardClear(CommandSender sender, int place, boolean isLine) {
        if (isLine) {
            plugin.getRewardManager().clearLines(place);
        } else {
            plugin.getRewardManager().clearCommands(place);
        }
        plugin.getMessages().send(sender,
                isLine ? "command.reward-line-cleared" : "command.reward-command-cleared",
                Placeholders.of().set("place", place).build());
    }

    private void rewardInfo(CommandSender sender, int place) {
        plugin.getMessages().send(sender, "command.reward-info-header",
                Placeholders.of().set("place", place).build());
        List<String> lines = plugin.getRewardManager().lines(place);
        List<String> commands = plugin.getRewardManager().commands(place);
        if (lines.isEmpty() && commands.isEmpty()) {
            plugin.getMessages().send(sender, "command.reward-info-empty");
            return;
        }
        for (int i = 0; i < lines.size(); i++) {
            plugin.getMessages().send(sender, "command.reward-info-entry",
                    Placeholders.of().set("index", i + 1).set("value", lines.get(i)).build());
        }
        for (int i = 0; i < commands.size(); i++) {
            plugin.getMessages().send(sender, "command.reward-info-command",
                    Placeholders.of().set("index", i + 1).set("value", commands.get(i)).build());
        }
    }

    private static Integer parsePlace(String raw) {
        try {
            int value = Integer.parseInt(raw);
            return (value >= 1 && value <= RewardManager.maxPlace()) ? value : null;
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private static Integer parseIndex(String raw) {
        try {
            return Integer.parseInt(raw);
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private void setElo(CommandSender sender, String[] args) {
        if (!sender.hasPermission("searchpvp.command.setelo")) {
            plugin.getMessages().send(sender, "command.no-permission");
            return;
        }
        if (args.length < 3) {
            plugin.getMessages().send(sender, "command.usage-admin");
            return;
        }
        PlayerData data = resolveData(args[1]);
        if (data == null) {
            plugin.getMessages().send(sender, "command.setelo-unknown",
                    Placeholders.of().set("player", args[1]).build());
            return;
        }
        int value;
        try {
            value = Integer.parseInt(args[2]);
        } catch (NumberFormatException exception) {
            plugin.getMessages().send(sender, "command.usage-admin");
            return;
        }
        data.setElo(Math.max(0, value));
        plugin.getDataManager().save();
        plugin.getMessages().send(sender, "command.setelo-done", Placeholders.of()
                .set("player", data.name())
                .set("elo", data.elo())
                .build());
    }

    private PlayerData resolveData(String name) {
        Player online = Bukkit.getPlayerExact(name);
        if (online != null) {
            return plugin.getDataManager().getOrCreate(online.getUniqueId(), online.getName());
        }
        return plugin.getDataManager().getByName(name);
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                      @NotNull String alias, @NotNull String[] args) {
        if (!sender.hasPermission("searchpvp.admin")) {
            return List.of();
        }
        if (args.length == 1) {
            return filter(SUBCOMMANDS, args[0]);
        }
        String root = args[0].toLowerCase(Locale.ROOT);
        if (args.length == 2) {
            return switch (root) {
                case "forcestop", "setelo", "forcesearch" -> filter(onlineNames(), args[1]);
                case "season" -> filter(List.of("info", "reset", "set", "finish"), args[1]);
                case "reward" -> filter(List.of("1", "2", "3"), args[1]);
                default -> List.of();
            };
        }
        if (root.equals("reward")) {
            if (args.length == 3) {
                return filter(List.of("info", "line", "command"), args[2]);
            }
            if (args.length == 4) {
                return filter(List.of("add", "remove", "clear"), args[3]);
            }
        }
        return List.of();
    }

    private static List<String> onlineNames() {
        return Bukkit.getOnlinePlayers().stream().map(Player::getName).toList();
    }

    private static List<String> filter(List<String> options, String prefix) {
        String lower = prefix.toLowerCase(Locale.ROOT);
        List<String> result = new ArrayList<>();
        for (String option : options) {
            if (option.toLowerCase(Locale.ROOT).startsWith(lower)) {
                result.add(option);
            }
        }
        return result;
    }
}

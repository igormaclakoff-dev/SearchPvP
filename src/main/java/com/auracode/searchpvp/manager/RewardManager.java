package com.auracode.searchpvp.manager;

import com.auracode.searchpvp.SearchPvPPlugin;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandException;

import java.util.ArrayList;
import java.util.List;

/**
 * Stores and grants the season top-3 rewards (description lines + console commands).
 */
public final class RewardManager {

    private static final int MAX_PLACE = 3;

    private final SearchPvPPlugin plugin;

    public RewardManager(SearchPvPPlugin plugin) {
        this.plugin = plugin;
    }

    public static int maxPlace() {
        return MAX_PLACE;
    }

    public List<String> lines(int place) {
        return new ArrayList<>(plugin.getConfig().getStringList(path(place) + ".lines"));
    }

    public List<String> commands(int place) {
        return new ArrayList<>(plugin.getConfig().getStringList(path(place) + ".commands"));
    }

    public void addLine(int place, String text) {
        List<String> list = lines(place);
        list.add(text);
        set(place, "lines", list);
    }

    public boolean removeLine(int place, int index) {
        return remove(place, "lines", index);
    }

    public void clearLines(int place) {
        set(place, "lines", List.of());
    }

    public void addCommand(int place, String command) {
        List<String> list = commands(place);
        list.add(command);
        set(place, "commands", list);
    }

    public boolean removeCommand(int place, int index) {
        return remove(place, "commands", index);
    }

    public void clearCommands(int place) {
        set(place, "commands", List.of());
    }

    public boolean hasAnyRewards() {
        for (int place = 1; place <= MAX_PLACE; place++) {
            if (!lines(place).isEmpty() || !commands(place).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Runs every reward command configured for the given place.
     */
    public void grant(int place, String playerName, int elo) {
        for (String template : commands(place)) {
            dispatch(applyPlaceholders(template, playerName, place, elo));
        }
    }

    private boolean remove(int place, String key, int index) {
        List<String> list = new ArrayList<>(plugin.getConfig().getStringList(path(place) + "." + key));
        if (index < 0 || index >= list.size()) {
            return false;
        }
        list.remove(index);
        set(place, key, list);
        return true;
    }

    private void set(int place, String key, List<String> value) {
        plugin.getConfig().set(path(place) + "." + key, value);
        plugin.saveConfig();
    }

    private static String path(int place) {
        return "season-rewards." + place;
    }

    private static String applyPlaceholders(String template, String name, int place, int elo) {
        return template
                .replace("%name%", name).replace("<name>", name)
                .replace("%place%", String.valueOf(place)).replace("<place>", String.valueOf(place))
                .replace("%elo%", String.valueOf(elo)).replace("<elo>", String.valueOf(elo));
    }

    private void dispatch(String command) {
        if (command == null || command.isBlank()) {
            return;
        }
        String cleaned = command.startsWith("/") ? command.substring(1) : command;
        Bukkit.getScheduler().runTask(plugin, () -> {
            try {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cleaned);
            } catch (CommandException exception) {
                plugin.getLogger().warning("Failed to run reward command '" + cleaned + "': " + exception.getMessage());
            }
        });
    }
}

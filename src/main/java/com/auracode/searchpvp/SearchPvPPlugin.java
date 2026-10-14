package com.auracode.searchpvp;

import com.auracode.searchpvp.command.PvPAdminCommand;
import com.auracode.searchpvp.command.PvPCommand;
import com.auracode.searchpvp.command.TopCommand;
import com.auracode.searchpvp.config.PluginConfig;
import com.auracode.searchpvp.data.DataManager;
import com.auracode.searchpvp.gui.MenuService;
import com.auracode.searchpvp.listener.CombatListener;
import com.auracode.searchpvp.listener.MenuListener;
import com.auracode.searchpvp.listener.PlayerConnectionListener;
import com.auracode.searchpvp.manager.ArenaManager;
import com.auracode.searchpvp.manager.MatchManager;
import com.auracode.searchpvp.manager.MatchmakingManager;
import com.auracode.searchpvp.manager.RewardManager;
import com.auracode.searchpvp.manager.SeasonManager;
import com.auracode.searchpvp.util.Messages;
import com.auracode.searchpvp.util.PlayerState;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabExecutor;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

/**
 * Plugin entry point. Wires up all components and manages their lifecycle.
 */
public final class SearchPvPPlugin extends JavaPlugin {

    private PluginConfig pluginConfig;
    private Messages messages;
    private DataManager dataManager;
    private ArenaManager arenaManager;
    private MatchManager matchManager;
    private MatchmakingManager matchmakingManager;
    private SeasonManager seasonManager;
    private RewardManager rewardManager;
    private MenuService menuService;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        saveResourceIfMissing("messages.yml");
        PlayerState.init(this);

        this.pluginConfig = new PluginConfig(getConfig());
        this.messages = new Messages(this);
        this.dataManager = new DataManager(this);
        this.arenaManager = new ArenaManager(this);
        this.matchManager = new MatchManager(this);
        this.matchmakingManager = new MatchmakingManager(this);
        this.rewardManager = new RewardManager(this);
        this.seasonManager = new SeasonManager(this);
        this.menuService = new MenuService(this);

        this.dataManager.load();

        registerCommands();
        registerListeners();
        this.matchmakingManager.start();
        this.seasonManager.start();

        getLogger().info("SearchPvP enabled (season " + seasonManager.season() + ").");
    }

    @Override
    public void onDisable() {
        if (seasonManager != null) {
            seasonManager.shutdown();
        }
        if (matchmakingManager != null) {
            matchmakingManager.shutdown();
        }
        if (matchManager != null) {
            matchManager.shutdown();
        }
        if (dataManager != null) {
            dataManager.saveSync();
        }
    }

    private void registerCommands() {
        registerCommand("pvp", new PvPCommand(this));
        registerCommand("top", new TopCommand(this));
        registerCommand("pvpadmin", new PvPAdminCommand(this));
    }

    private void registerCommand(String name, TabExecutor executor) {
        PluginCommand command = getCommand(name);
        if (command == null) {
            getLogger().warning("Command '" + name + "' is missing from plugin.yml.");
            return;
        }
        command.setExecutor(executor);
        command.setTabCompleter(executor);
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new MenuListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerConnectionListener(this), this);
        getServer().getPluginManager().registerEvents(new CombatListener(this), this);
    }

    private void saveResourceIfMissing(String fileName) {
        if (!new File(getDataFolder(), fileName).exists()) {
            saveResource(fileName, false);
        }
    }

    public void reload() {
        reloadConfig();
        this.pluginConfig = new PluginConfig(getConfig());
        this.messages.reload();
    }

    public void setPlacementCommand(String value) {
        getConfig().set("settings.placement-command", value);
        saveConfig();
        this.pluginConfig = new PluginConfig(getConfig());
    }

    public PluginConfig getPluginConfig() {
        return pluginConfig;
    }

    public Messages getMessages() {
        return messages;
    }

    public DataManager getDataManager() {
        return dataManager;
    }

    public ArenaManager getArenaManager() {
        return arenaManager;
    }

    public MatchManager getMatchManager() {
        return matchManager;
    }

    public MatchmakingManager getMatchmakingManager() {
        return matchmakingManager;
    }

    public SeasonManager getSeasonManager() {
        return seasonManager;
    }

    public RewardManager getRewardManager() {
        return rewardManager;
    }

    public MenuService getMenuService() {
        return menuService;
    }
}

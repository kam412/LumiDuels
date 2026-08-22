package com.duelplugin;

import com.duelplugin.arena.ArenaManager;
import com.duelplugin.commands.DuelArenaCommand;
import com.duelplugin.commands.DuelCommand;
import com.duelplugin.commands.DuelKitCommand;
import com.duelplugin.duel.DuelManager;
import com.duelplugin.kit.KitManager;
import com.duelplugin.listeners.PlayerListener;
import com.duelplugin.stats.StatsManager;
import org.bukkit.plugin.java.JavaPlugin;

public class DuelPlugin extends JavaPlugin {

    private static DuelPlugin instance;

    private ArenaManager arenaManager;
    private KitManager kitManager;
    private StatsManager statsManager;
    private DuelManager duelManager;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();

        this.arenaManager = new ArenaManager(this);
        this.kitManager = new KitManager(this);
        this.statsManager = new StatsManager(this);
        this.duelManager = new DuelManager(this);

        arenaManager.load();
        kitManager.load();
        statsManager.load();

        getCommand("duel").setExecutor(new DuelCommand(this));
        getCommand("duelarena").setExecutor(new DuelArenaCommand(this));
        getCommand("duelkit").setExecutor(new DuelKitCommand(this));

        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);

        getLogger().info("DuelPlugin has been enabled.");
    }

    @Override
    public void onDisable() {
        if (duelManager != null) {
            duelManager.endAllDuels();
        }
        if (arenaManager != null) {
            arenaManager.save();
        }
        if (kitManager != null) {
            kitManager.save();
        }
        if (statsManager != null) {
            statsManager.save();
        }
        getLogger().info("DuelPlugin has been disabled.");
    }

    public static DuelPlugin getInstance() {
        return instance;
    }

    public ArenaManager getArenaManager() {
        return arenaManager;
    }

    public KitManager getKitManager() {
        return kitManager;
    }

    public StatsManager getStatsManager() {
        return statsManager;
    }

    public DuelManager getDuelManager() {
        return duelManager;
    }
}

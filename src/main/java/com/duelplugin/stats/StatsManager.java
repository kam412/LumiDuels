package com.duelplugin.stats;

import com.duelplugin.DuelPlugin;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class StatsManager {

    private final DuelPlugin plugin;
    private final File file;
    private YamlConfiguration config;

    public StatsManager(DuelPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "stats.yml");
    }

    public void load() {
        config = YamlConfiguration.loadConfiguration(file);
    }

    public void save() {
        try {
            if (!plugin.getDataFolder().exists()) {
                plugin.getDataFolder().mkdirs();
            }
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Could not save stats.yml: " + e.getMessage());
        }
    }

    public int getWins(UUID uuid) {
        return config.getInt("stats." + uuid + ".wins", 0);
    }

    public int getLosses(UUID uuid) {
        return config.getInt("stats." + uuid + ".losses", 0);
    }

    public void addWin(UUID uuid) {
        config.set("stats." + uuid + ".wins", getWins(uuid) + 1);
        save();
    }

    public void addLoss(UUID uuid) {
        config.set("stats." + uuid + ".losses", getLosses(uuid) + 1);
        save();
    }
}

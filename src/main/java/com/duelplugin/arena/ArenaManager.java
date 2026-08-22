package com.duelplugin.arena;

import com.duelplugin.DuelPlugin;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class ArenaManager {

    private final DuelPlugin plugin;
    private final File file;
    private final Map<String, Arena> arenas = new LinkedHashMap<>();

    public ArenaManager(DuelPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "arenas.yml");
    }

    public void load() {
        arenas.clear();
        if (!file.exists()) {
            return;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        if (config.getConfigurationSection("arenas") == null) {
            return;
        }

        for (String name : config.getConfigurationSection("arenas").getKeys(false)) {
            String path = "arenas." + name + ".";
            Arena arena = new Arena(name);
            arena.setPos1(loadLocation(config, path + "pos1"));
            arena.setPos2(loadLocation(config, path + "pos2"));
            arena.setSpawn1(loadLocation(config, path + "spawn1"));
            arena.setSpawn2(loadLocation(config, path + "spawn2"));
            arenas.put(name.toLowerCase(), arena);
        }
    }

    public void save() {
        YamlConfiguration config = new YamlConfiguration();

        for (Arena arena : arenas.values()) {
            String path = "arenas." + arena.getName() + ".";
            saveLocation(config, path + "pos1", arena.getPos1());
            saveLocation(config, path + "pos2", arena.getPos2());
            saveLocation(config, path + "spawn1", arena.getSpawn1());
            saveLocation(config, path + "spawn2", arena.getSpawn2());
        }

        try {
            if (!plugin.getDataFolder().exists()) {
                plugin.getDataFolder().mkdirs();
            }
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Could not save arenas.yml: " + e.getMessage());
        }
    }

    private Location loadLocation(YamlConfiguration config, String path) {
        if (!config.contains(path + ".world")) {
            return null;
        }
        World world = plugin.getServer().getWorld(config.getString(path + ".world"));
        if (world == null) {
            return null;
        }
        double x = config.getDouble(path + ".x");
        double y = config.getDouble(path + ".y");
        double z = config.getDouble(path + ".z");
        float yaw = (float) config.getDouble(path + ".yaw");
        float pitch = (float) config.getDouble(path + ".pitch");
        return new Location(world, x, y, z, yaw, pitch);
    }

    private void saveLocation(YamlConfiguration config, String path, Location loc) {
        if (loc == null) {
            return;
        }
        config.set(path + ".world", loc.getWorld().getName());
        config.set(path + ".x", loc.getX());
        config.set(path + ".y", loc.getY());
        config.set(path + ".z", loc.getZ());
        config.set(path + ".yaw", loc.getYaw());
        config.set(path + ".pitch", loc.getPitch());
    }

    public Arena createArena(String name) {
        Arena arena = new Arena(name);
        arenas.put(name.toLowerCase(), arena);
        return arena;
    }

    public boolean removeArena(String name) {
        return arenas.remove(name.toLowerCase()) != null;
    }

    public Optional<Arena> getArena(String name) {
        return Optional.ofNullable(arenas.get(name.toLowerCase()));
    }

    /** Returns the first fully-configured arena that isn't currently hosting a duel. */
    public Optional<Arena> getFreeArena() {
        return arenas.values().stream()
                .filter(Arena::isComplete)
                .filter(a -> !a.isInUse())
                .findFirst();
    }

    public Map<String, Arena> getArenas() {
        return arenas;
    }
}

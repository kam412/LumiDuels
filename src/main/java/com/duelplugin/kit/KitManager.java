package com.duelplugin.kit;

import com.duelplugin.DuelPlugin;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class KitManager {

    private final DuelPlugin plugin;
    private final File file;
    private final Map<String, Kit> kits = new LinkedHashMap<>();

    public KitManager(DuelPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "kits.yml");
    }

    public void load() {
        kits.clear();
        if (!file.exists()) {
            return;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        if (config.getConfigurationSection("kits") == null) {
            return;
        }

        for (String name : config.getConfigurationSection("kits").getKeys(false)) {
            Kit kit = new Kit(name);
            String base = "kits." + name;

            if (config.getConfigurationSection(base + ".contents") != null) {
                for (String slot : config.getConfigurationSection(base + ".contents").getKeys(false)) {
                    ItemStack item = config.getItemStack(base + ".contents." + slot);
                    int index = Integer.parseInt(slot);
                    if (index >= 0 && index < kit.getContents().length) {
                        kit.getContents()[index] = item;
                    }
                }
            }

            if (config.getConfigurationSection(base + ".armor") != null) {
                for (String slot : config.getConfigurationSection(base + ".armor").getKeys(false)) {
                    ItemStack item = config.getItemStack(base + ".armor." + slot);
                    int index = Integer.parseInt(slot);
                    if (index >= 0 && index < kit.getArmor().length) {
                        kit.getArmor()[index] = item;
                    }
                }
            }

            kits.put(name.toLowerCase(), kit);
        }
    }

    public void save() {
        YamlConfiguration config = new YamlConfiguration();

        for (Kit kit : kits.values()) {
            String base = "kits." + kit.getName();

            for (int i = 0; i < kit.getContents().length; i++) {
                ItemStack item = kit.getContents()[i];
                if (item != null) {
                    config.set(base + ".contents." + i, item);
                }
            }

            for (int i = 0; i < kit.getArmor().length; i++) {
                ItemStack item = kit.getArmor()[i];
                if (item != null) {
                    config.set(base + ".armor." + i, item);
                }
            }
        }

        try {
            if (!plugin.getDataFolder().exists()) {
                plugin.getDataFolder().mkdirs();
            }
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Could not save kits.yml: " + e.getMessage());
        }
    }

    /** Saves the player's current inventory and armor as a new kit. */
    public Kit createKitFromPlayer(String name, Player player) {
        Kit kit = new Kit(name);
        PlayerInventory inv = player.getInventory();
        kit.setContents(inv.getStorageContents().clone());
        kit.setArmor(inv.getArmorContents().clone());
        kits.put(name.toLowerCase(), kit);
        return kit;
    }

    public boolean deleteKit(String name) {
        return kits.remove(name.toLowerCase()) != null;
    }

    public Optional<Kit> getKit(String name) {
        return Optional.ofNullable(kits.get(name.toLowerCase()));
    }

    public Map<String, Kit> getKits() {
        return kits;
    }

    /** Wipes the player's inventory and armor, then gives them the kit's contents. */
    public void applyKit(Player player, Kit kit) {
        PlayerInventory inv = player.getInventory();
        inv.clear();
        inv.setArmorContents(new ItemStack[4]);
        inv.setStorageContents(cloneArray(kit.getContents(), 36));
        inv.setArmorContents(cloneArray(kit.getArmor(), 4));
        player.updateInventory();
    }

    private ItemStack[] cloneArray(ItemStack[] source, int size) {
        ItemStack[] result = new ItemStack[size];
        if (source != null) {
            for (int i = 0; i < Math.min(size, source.length); i++) {
                result[i] = source[i] == null ? null : source[i].clone();
            }
        }
        return result;
    }
}

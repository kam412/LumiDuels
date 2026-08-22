package com.duelplugin.kit;

import org.bukkit.inventory.ItemStack;

public class Kit {

    private final String name;
    private ItemStack[] contents;
    private ItemStack[] armor;

    public Kit(String name) {
        this.name = name;
        this.contents = new ItemStack[36];
        this.armor = new ItemStack[4];
    }

    public String getName() {
        return name;
    }

    public ItemStack[] getContents() {
        return contents;
    }

    public void setContents(ItemStack[] contents) {
        this.contents = contents;
    }

    public ItemStack[] getArmor() {
        return armor;
    }

    public void setArmor(ItemStack[] armor) {
        this.armor = armor;
    }
}

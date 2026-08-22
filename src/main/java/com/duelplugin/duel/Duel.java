package com.duelplugin.duel;

import com.duelplugin.arena.Arena;
import com.duelplugin.kit.Kit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.util.UUID;

public class Duel {

    private final UUID player1;
    private final UUID player2;
    private final Arena arena;
    private final Kit kit;

    private final PlayerSnapshot snapshot1;
    private final PlayerSnapshot snapshot2;

    public Duel(Player p1, Player p2, Arena arena, Kit kit) {
        this.player1 = p1.getUniqueId();
        this.player2 = p2.getUniqueId();
        this.arena = arena;
        this.kit = kit;
        this.snapshot1 = PlayerSnapshot.of(p1);
        this.snapshot2 = PlayerSnapshot.of(p2);
    }

    public UUID getPlayer1() {
        return player1;
    }

    public UUID getPlayer2() {
        return player2;
    }

    public Arena getArena() {
        return arena;
    }

    public Kit getKit() {
        return kit;
    }

    public UUID getOpponent(UUID uuid) {
        if (uuid.equals(player1)) {
            return player2;
        }
        if (uuid.equals(player2)) {
            return player1;
        }
        return null;
    }

    public boolean involves(UUID uuid) {
        return uuid.equals(player1) || uuid.equals(player2);
    }

    public PlayerSnapshot getSnapshot(UUID uuid) {
        if (uuid.equals(player1)) {
            return snapshot1;
        }
        if (uuid.equals(player2)) {
            return snapshot2;
        }
        return null;
    }

    /** Captures everything needed to put a player back exactly how they were before the duel. */
    public static class PlayerSnapshot {
        private final Location location;
        private final ItemStack[] contents;
        private final ItemStack[] armor;
        private final GameMode gameMode;
        private final double health;
        private final int foodLevel;
        private final PotionEffect[] potionEffects;

        private PlayerSnapshot(Location location, ItemStack[] contents, ItemStack[] armor,
                                GameMode gameMode, double health, int foodLevel,
                                PotionEffect[] potionEffects) {
            this.location = location;
            this.contents = contents;
            this.armor = armor;
            this.gameMode = gameMode;
            this.health = health;
            this.foodLevel = foodLevel;
            this.potionEffects = potionEffects;
        }

        public static PlayerSnapshot of(Player player) {
            return new PlayerSnapshot(
                    player.getLocation().clone(),
                    player.getInventory().getStorageContents().clone(),
                    player.getInventory().getArmorContents().clone(),
                    player.getGameMode(),
                    player.getHealth(),
                    player.getFoodLevel(),
                    player.getActivePotionEffects().toArray(new PotionEffect[0])
            );
        }

        public void restore(Player player) {
            player.getInventory().clear();
            player.getInventory().setStorageContents(contents);
            player.getInventory().setArmorContents(armor);
            player.setGameMode(gameMode);

            for (PotionEffect effect : player.getActivePotionEffects().toArray(new PotionEffect[0])) {
                player.removePotionEffect(effect.getType());
            }
            for (PotionEffect effect : potionEffects) {
                player.addPotionEffect(effect);
            }

            double maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
            player.setHealth(Math.min(health, maxHealth));
            player.setFoodLevel(foodLevel);
            player.teleport(location);
        }
    }
}

package com.duelplugin.listeners;

import com.duelplugin.DuelPlugin;
import com.duelplugin.duel.Duel;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {

    private final DuelPlugin plugin;

    public PlayerListener(DuelPlugin plugin) {
        this.plugin = plugin;
    }

    /** Instead of letting a duelist actually die, cancel the killing blow and declare a loser. */
    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getEntity();
        if (!plugin.getDuelManager().isInDuel(player.getUniqueId())) {
            return;
        }

        double resultingHealth = player.getHealth() - event.getFinalDamage();
        if (resultingHealth > 0) {
            return;
        }

        event.setCancelled(true);
        player.setHealth(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
        plugin.getDuelManager().endDuel(player.getUniqueId(), false);
    }

    /** Disconnecting mid-duel counts as a forfeit. */
    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (plugin.getDuelManager().isInDuel(player.getUniqueId())) {
            plugin.getDuelManager().endDuel(player.getUniqueId(), true);
        }
    }

    /** Restore a player's gear if they disconnected mid-duel and are now rejoining. */
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Duel.PlayerSnapshot snapshot = plugin.getDuelManager().consumePendingRestore(player.getUniqueId());
        if (snapshot != null) {
            snapshot.restore(player);
            player.sendMessage("\u00A7eYour inventory has been restored after your last duel disconnect.");
        }
    }

    /** Keep duelists inside their arena's bounding box. */
    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (!plugin.getDuelManager().isInDuel(player.getUniqueId())) {
            return;
        }

        Duel duel = plugin.getDuelManager().getDuel(player.getUniqueId());
        if (duel == null || event.getTo() == null) {
            return;
        }

        if (!duel.getArena().contains(event.getTo())) {
            event.setTo(event.getFrom());
        }
    }
}

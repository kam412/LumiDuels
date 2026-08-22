package com.duelplugin.duel;

import com.duelplugin.DuelPlugin;
import com.duelplugin.arena.Arena;
import com.duelplugin.kit.Kit;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class DuelManager {

    private final DuelPlugin plugin;
    private final long requestTimeoutMillis;

    private final Map<UUID, DuelRequest> pendingRequests = new HashMap<>();
    private final Map<UUID, Duel> activeDuels = new HashMap<>();
    private final Map<UUID, Duel.PlayerSnapshot> pendingRestores = new HashMap<>();

    public DuelManager(DuelPlugin plugin) {
        this.plugin = plugin;
        this.requestTimeoutMillis = plugin.getConfig().getLong("request-timeout-seconds", 30) * 1000L;
    }

    public boolean isInDuel(UUID uuid) {
        return activeDuels.containsKey(uuid);
    }

    public Duel getDuel(UUID uuid) {
        return activeDuels.get(uuid);
    }

    public boolean sendRequest(Player sender, Player target, String kitName) {
        if (isInDuel(sender.getUniqueId()) || isInDuel(target.getUniqueId())) {
            return false;
        }

        DuelRequest request = new DuelRequest(
                sender.getUniqueId(),
                target.getUniqueId(),
                kitName,
                System.currentTimeMillis() + requestTimeoutMillis
        );

        pendingRequests.put(target.getUniqueId(), request);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            DuelRequest current = pendingRequests.get(target.getUniqueId());
            if (current == request && current.isExpired()) {
                pendingRequests.remove(target.getUniqueId());
                Player s = Bukkit.getPlayer(sender.getUniqueId());
                Player t = Bukkit.getPlayer(target.getUniqueId());
                if (s != null) {
                    s.sendMessage("\u00A7cYour duel request to " + target.getName() + " expired.");
                }
                if (t != null) {
                    t.sendMessage("\u00A7cThe duel request from " + sender.getName() + " expired.");
                }
            }
        }, requestTimeoutMillis / 50L);

        return true;
    }

    public Optional<DuelRequest> getRequest(UUID target) {
        DuelRequest request = pendingRequests.get(target);
        if (request == null || request.isExpired()) {
            pendingRequests.remove(target);
            return Optional.empty();
        }
        return Optional.of(request);
    }

    public void removeRequest(UUID target) {
        pendingRequests.remove(target);
    }

    public boolean startDuel(Player p1, Player p2, String kitName) {
        Optional<Arena> arenaOpt = plugin.getArenaManager().getFreeArena();
        if (arenaOpt.isEmpty()) {
            p1.sendMessage("\u00A7cNo free arenas are available right now.");
            p2.sendMessage("\u00A7cNo free arenas are available right now.");
            return false;
        }

        Optional<Kit> kitOpt = plugin.getKitManager().getKit(kitName);
        if (kitOpt.isEmpty()) {
            p1.sendMessage("\u00A7cKit '" + kitName + "' does not exist.");
            return false;
        }

        Arena arena = arenaOpt.get();
        Kit kit = kitOpt.get();
        arena.setInUse(true);

        Duel duel = new Duel(p1, p2, arena, kit);
        activeDuels.put(p1.getUniqueId(), duel);
        activeDuels.put(p2.getUniqueId(), duel);

        plugin.getKitManager().applyKit(p1, kit);
        plugin.getKitManager().applyKit(p2, kit);

        p1.teleport(arena.getSpawn1());
        p2.teleport(arena.getSpawn2());

        p1.setHealth(p1.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
        p2.setHealth(p2.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
        p1.setFoodLevel(20);
        p2.setFoodLevel(20);

        p1.sendMessage("\u00A7aYour duel against " + p2.getName() + " has started!");
        p2.sendMessage("\u00A7aYour duel against " + p1.getName() + " has started!");

        return true;
    }

    /** Ends a duel with a result: the given player lost, their opponent wins. */
    public void endDuel(UUID loserUuid, boolean disconnected) {
        Duel duel = activeDuels.get(loserUuid);
        if (duel == null) {
            return;
        }

        UUID winnerUuid = duel.getOpponent(loserUuid);

        activeDuels.remove(duel.getPlayer1());
        activeDuels.remove(duel.getPlayer2());
        duel.getArena().setInUse(false);

        plugin.getStatsManager().addLoss(loserUuid);
        plugin.getStatsManager().addWin(winnerUuid);

        Player loser = Bukkit.getPlayer(loserUuid);
        Player winner = Bukkit.getPlayer(winnerUuid);

        Duel.PlayerSnapshot loserSnapshot = duel.getSnapshot(loserUuid);
        Duel.PlayerSnapshot winnerSnapshot = duel.getSnapshot(winnerUuid);

        if (disconnected) {
            // Player is offline right now - restore their inventory next time they join.
            pendingRestores.put(loserUuid, loserSnapshot);
        } else if (loser != null) {
            loserSnapshot.restore(loser);
            loser.sendMessage("\u00A7cYou lost the duel against "
                    + (winner != null ? winner.getName() : "your opponent") + ".");
        }

        if (winner != null) {
            winnerSnapshot.restore(winner);
            winner.sendMessage("\u00A7aYou won the duel against "
                    + (loser != null ? loser.getName() : "your opponent") + "!");
        }
    }

    /** Voluntarily cancels a duel in progress - both players are restored, no stats recorded. */
    public void cancelDuel(UUID uuid) {
        Duel duel = activeDuels.get(uuid);
        if (duel == null) {
            return;
        }

        activeDuels.remove(duel.getPlayer1());
        activeDuels.remove(duel.getPlayer2());
        duel.getArena().setInUse(false);

        Player p1 = Bukkit.getPlayer(duel.getPlayer1());
        Player p2 = Bukkit.getPlayer(duel.getPlayer2());

        if (p1 != null) {
            duel.getSnapshot(duel.getPlayer1()).restore(p1);
            p1.sendMessage("\u00A7eThe duel was cancelled.");
        } else {
            pendingRestores.put(duel.getPlayer1(), duel.getSnapshot(duel.getPlayer1()));
        }

        if (p2 != null) {
            duel.getSnapshot(duel.getPlayer2()).restore(p2);
            p2.sendMessage("\u00A7eThe duel was cancelled.");
        } else {
            pendingRestores.put(duel.getPlayer2(), duel.getSnapshot(duel.getPlayer2()));
        }
    }

    /** Called on plugin disable so nobody is stuck mid-duel across a restart. */
    public void endAllDuels() {
        for (Duel duel : new HashSet<>(activeDuels.values())) {
            Player p1 = Bukkit.getPlayer(duel.getPlayer1());
            Player p2 = Bukkit.getPlayer(duel.getPlayer2());
            if (p1 != null) {
                duel.getSnapshot(duel.getPlayer1()).restore(p1);
            }
            if (p2 != null) {
                duel.getSnapshot(duel.getPlayer2()).restore(p2);
            }
            duel.getArena().setInUse(false);
        }
        activeDuels.clear();
    }

    /** Returns and clears a queued restore for a player who disconnected mid-duel, if any. */
    public Duel.PlayerSnapshot consumePendingRestore(UUID uuid) {
        return pendingRestores.remove(uuid);
    }
}

package com.duelplugin.commands;

import com.duelplugin.DuelPlugin;
import com.duelplugin.duel.DuelRequest;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Optional;

public class DuelCommand implements CommandExecutor {

    private final DuelPlugin plugin;

    public DuelCommand(DuelPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            player.sendMessage(ChatColor.YELLOW
                    + "Usage: /duel <player> <kit> | /duel accept | /duel deny | /duel cancel | /duel stats [player]");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "accept":
                return handleAccept(player);
            case "deny":
                return handleDeny(player);
            case "cancel":
                return handleCancel(player);
            case "stats":
                return handleStats(player, args);
            default:
                return handleSend(player, args);
        }
    }

    private boolean handleSend(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.YELLOW + "Usage: /duel <player> <kit>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            player.sendMessage(ChatColor.RED + "That player is not online.");
            return true;
        }

        if (target.equals(player)) {
            player.sendMessage(ChatColor.RED + "You cannot duel yourself.");
            return true;
        }

        String kitName = args[1];
        if (plugin.getKitManager().getKit(kitName).isEmpty()) {
            player.sendMessage(ChatColor.RED + "Kit '" + kitName + "' does not exist. Use /duelkit list to see options.");
            return true;
        }

        if (plugin.getDuelManager().isInDuel(player.getUniqueId())
                || plugin.getDuelManager().isInDuel(target.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "One of you is already in a duel.");
            return true;
        }

        boolean sent = plugin.getDuelManager().sendRequest(player, target, kitName);
        if (!sent) {
            player.sendMessage(ChatColor.RED + "Could not send a duel request right now.");
            return true;
        }

        player.sendMessage(ChatColor.GREEN + "Duel request sent to " + target.getName() + ".");
        target.sendMessage(ChatColor.GREEN + player.getName() + " has challenged you to a duel ("
                + kitName + " kit). Type " + ChatColor.YELLOW + "/duel accept" + ChatColor.GREEN
                + " to accept, or " + ChatColor.YELLOW + "/duel deny" + ChatColor.GREEN + " to decline.");
        return true;
    }

    private boolean handleAccept(Player player) {
        Optional<DuelRequest> requestOpt = plugin.getDuelManager().getRequest(player.getUniqueId());
        if (requestOpt.isEmpty()) {
            player.sendMessage(ChatColor.RED + "You have no pending duel requests.");
            return true;
        }

        DuelRequest request = requestOpt.get();
        plugin.getDuelManager().removeRequest(player.getUniqueId());

        Player challenger = Bukkit.getPlayer(request.getSender());
        if (challenger == null) {
            player.sendMessage(ChatColor.RED + "That player is no longer online.");
            return true;
        }

        if (plugin.getDuelManager().isInDuel(player.getUniqueId())
                || plugin.getDuelManager().isInDuel(challenger.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "One of you is already in a duel.");
            return true;
        }

        plugin.getDuelManager().startDuel(challenger, player, request.getKitName());
        return true;
    }

    private boolean handleDeny(Player player) {
        Optional<DuelRequest> requestOpt = plugin.getDuelManager().getRequest(player.getUniqueId());
        if (requestOpt.isEmpty()) {
            player.sendMessage(ChatColor.RED + "You have no pending duel requests.");
            return true;
        }

        plugin.getDuelManager().removeRequest(player.getUniqueId());
        Player challenger = Bukkit.getPlayer(requestOpt.get().getSender());
        player.sendMessage(ChatColor.YELLOW + "Duel request denied.");
        if (challenger != null) {
            challenger.sendMessage(ChatColor.YELLOW + player.getName() + " denied your duel request.");
        }
        return true;
    }

    private boolean handleCancel(Player player) {
        if (!plugin.getDuelManager().isInDuel(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "You are not in a duel.");
            return true;
        }
        plugin.getDuelManager().cancelDuel(player.getUniqueId());
        return true;
    }

    private boolean handleStats(Player player, String[] args) {
        Player target = player;
        if (args.length >= 2) {
            Player specified = Bukkit.getPlayer(args[1]);
            if (specified != null) {
                target = specified;
            }
        }

        int wins = plugin.getStatsManager().getWins(target.getUniqueId());
        int losses = plugin.getStatsManager().getLosses(target.getUniqueId());
        player.sendMessage(ChatColor.GOLD + target.getName() + "'s record: "
                + ChatColor.GREEN + wins + " wins" + ChatColor.GRAY + " / "
                + ChatColor.RED + losses + " losses");
        return true;
    }
}

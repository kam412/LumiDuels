package com.duelplugin.commands;

import com.duelplugin.DuelPlugin;
import com.duelplugin.arena.Arena;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.function.Consumer;

public class DuelArenaCommand implements CommandExecutor {

    private final DuelPlugin plugin;

    public DuelArenaCommand(DuelPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length < 1) {
            player.sendMessage(ChatColor.YELLOW
                    + "Usage: /duelarena <create|pos1|pos2|spawn1|spawn2|remove|list> [name]");
            return true;
        }

        String sub = args[0].toLowerCase();

        if (sub.equals("list")) {
            if (plugin.getArenaManager().getArenas().isEmpty()) {
                player.sendMessage(ChatColor.YELLOW + "No arenas have been created yet.");
                return true;
            }
            player.sendMessage(ChatColor.GOLD + "Arenas:");
            for (Arena arena : plugin.getArenaManager().getArenas().values()) {
                player.sendMessage(ChatColor.GRAY + " - " + arena.getName()
                        + (arena.isComplete() ? ChatColor.GREEN + " (ready)" : ChatColor.RED + " (incomplete)"));
            }
            return true;
        }

        if (args.length < 2) {
            player.sendMessage(ChatColor.YELLOW + "Usage: /duelarena " + sub + " <name>");
            return true;
        }

        String name = args[1];

        switch (sub) {
            case "create":
                if (plugin.getArenaManager().getArena(name).isPresent()) {
                    player.sendMessage(ChatColor.RED + "An arena named '" + name + "' already exists.");
                    return true;
                }
                plugin.getArenaManager().createArena(name);
                player.sendMessage(ChatColor.GREEN + "Created arena '" + name
                        + "'. Set pos1, pos2, spawn1 and spawn2 before it can be used.");
                break;

            case "pos1":
                withArena(player, name, arena -> {
                    arena.setPos1(player.getLocation());
                    player.sendMessage(ChatColor.GREEN + "Set pos1 for arena '" + name + "' to your location.");
                });
                break;

            case "pos2":
                withArena(player, name, arena -> {
                    arena.setPos2(player.getLocation());
                    player.sendMessage(ChatColor.GREEN + "Set pos2 for arena '" + name + "' to your location.");
                });
                break;

            case "spawn1":
                withArena(player, name, arena -> {
                    arena.setSpawn1(player.getLocation());
                    player.sendMessage(ChatColor.GREEN + "Set spawn1 for arena '" + name + "' to your location.");
                });
                break;

            case "spawn2":
                withArena(player, name, arena -> {
                    arena.setSpawn2(player.getLocation());
                    player.sendMessage(ChatColor.GREEN + "Set spawn2 for arena '" + name + "' to your location.");
                });
                break;

            case "remove":
                if (plugin.getArenaManager().removeArena(name)) {
                    player.sendMessage(ChatColor.GREEN + "Removed arena '" + name + "'.");
                } else {
                    player.sendMessage(ChatColor.RED + "No arena named '" + name + "' exists.");
                }
                break;

            default:
                player.sendMessage(ChatColor.YELLOW
                        + "Usage: /duelarena <create|pos1|pos2|spawn1|spawn2|remove|list> [name]");
                return true;
        }

        plugin.getArenaManager().save();
        return true;
    }

    private void withArena(Player player, String name, Consumer<Arena> action) {
        plugin.getArenaManager().getArena(name).ifPresentOrElse(action,
                () -> player.sendMessage(ChatColor.RED + "No arena named '" + name
                        + "' exists. Create it first with /duelarena create " + name + "."));
    }
}

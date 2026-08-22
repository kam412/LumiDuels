package com.duelplugin.commands;

import com.duelplugin.DuelPlugin;
import com.duelplugin.kit.Kit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DuelKitCommand implements CommandExecutor {

    private final DuelPlugin plugin;

    public DuelKitCommand(DuelPlugin plugin) {
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
            player.sendMessage(ChatColor.YELLOW + "Usage: /duelkit <create|delete|list|give> [name]");
            return true;
        }

        String sub = args[0].toLowerCase();

        if (sub.equals("list")) {
            if (plugin.getKitManager().getKits().isEmpty()) {
                player.sendMessage(ChatColor.YELLOW + "No kits have been created yet.");
                return true;
            }
            StringBuilder builder = new StringBuilder();
            for (Kit kit : plugin.getKitManager().getKits().values()) {
                builder.append(kit.getName()).append(", ");
            }
            String list = builder.length() > 0 ? builder.substring(0, builder.length() - 2) : "";
            player.sendMessage(ChatColor.GOLD + "Kits: " + ChatColor.GRAY + list);
            return true;
        }

        if (args.length < 2) {
            player.sendMessage(ChatColor.YELLOW + "Usage: /duelkit " + sub + " <name>");
            return true;
        }

        String name = args[1];

        switch (sub) {
            case "create":
                plugin.getKitManager().createKitFromPlayer(name, player);
                plugin.getKitManager().save();
                player.sendMessage(ChatColor.GREEN + "Saved your current inventory and armor as kit '" + name + "'.");
                break;

            case "delete":
                if (plugin.getKitManager().deleteKit(name)) {
                    plugin.getKitManager().save();
                    player.sendMessage(ChatColor.GREEN + "Deleted kit '" + name + "'.");
                } else {
                    player.sendMessage(ChatColor.RED + "No kit named '" + name + "' exists.");
                }
                break;

            case "give":
                plugin.getKitManager().getKit(name).ifPresentOrElse(
                        kit -> {
                            plugin.getKitManager().applyKit(player, kit);
                            player.sendMessage(ChatColor.GREEN + "Gave you kit '" + name + "'.");
                        },
                        () -> player.sendMessage(ChatColor.RED + "No kit named '" + name + "' exists.")
                );
                break;

            default:
                player.sendMessage(ChatColor.YELLOW + "Usage: /duelkit <create|delete|list|give> [name]");
        }

        return true;
    }
}

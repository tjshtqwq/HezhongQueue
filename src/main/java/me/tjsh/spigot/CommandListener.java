package me.tjsh.spigot;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandListener implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender csr, Command command, String s, String[] args) {
        if (command.getName().equalsIgnoreCase("hqs")) {
            if (csr instanceof Player) {
                Player p = (Player) csr;
                if (args.length < 1) {
                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c/hqs move <服务器名>"));
                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c/hqs leave"));
                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c/hqs movegroup <服务器组>"));
                    return true;
                }
                if (args.length < 2 && !args[0].equalsIgnoreCase("leave")) {
                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c/hqs move <服务器名>"));
                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c/hqs leave"));
                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c/hqs movegroup <服务器组>"));
                    return true;
                }
                String mode = args[0];
                String server;
                if (mode.equalsIgnoreCase("leave")) {
                    server = "";
                } else {
                    server = args[1];
                }
                ByteArrayDataOutput out = ByteStreams.newDataOutput();
                out.writeUTF(mode);
                out.writeUTF(p.getName());
                out.writeUTF(server);
                p.sendPluginMessage(HezhongQueue.instance, "hezhong:queuecommand", out.toByteArray());
                p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&b已发送指令。"));

            } else {
                if (args.length < 2) {
                    csr.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c/hqs move <玩家名> <服务器名>"));
                    csr.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c/hqs leave <玩家名>"));
                    csr.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c/hqs movegroup <玩家名> <服务器组>"));
                    return true;
                }
                if (args.length < 3 && !args[0].equalsIgnoreCase("leave")) {
                    csr.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c/hqs move <玩家名> <服务器名>"));
                    csr.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c/hqs leave <玩家名>"));
                    csr.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c/hqs movegroup <玩家名> <服务器组>"));
                    return true;
                }
                String mode = args[0];
                String player = args[1];
                String server;
                if (mode.equalsIgnoreCase("leave")) {
                    server = "";
                } else {
                    server = args[2];
                }
                ByteArrayDataOutput out = ByteStreams.newDataOutput();
                out.writeUTF(mode);
                out.writeUTF(player);
                out.writeUTF(server);
                HezhongQueue.instance.getServer().sendPluginMessage(HezhongQueue.instance, "hezhong:queuecommand", out.toByteArray());
            }
        }
        return true;
    }
}

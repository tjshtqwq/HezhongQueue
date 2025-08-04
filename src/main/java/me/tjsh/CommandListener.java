package me.tjsh;

import me.tjsh.utils.QueueUtil;
import net.md_5.bungee.api.Callback;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.util.ArrayList;
import java.util.UUID;

public class CommandListener extends Command {
    public CommandListener() {
        super("hq");
    }

    @Override
    public void execute(CommandSender commandSender, String[] args) {
        if (args.length < 1) {
            commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c/hq move <服务器名>"));
            commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c/hq movegroup <服务器组名>"));
            commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c/hq leave"));
            return;
        }
        if (args[0].equalsIgnoreCase("move")) {
            if (commandSender instanceof ProxiedPlayer) {
                ProxiedPlayer p = (ProxiedPlayer) commandSender;
                if (args.length < 2) {
                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c/hq move <服务器名>"));
                    return;
                }
                String targetServer = args[1];
                QueueUtil.moveToServer(p, targetServer);
            }
        }
        else if (args[0].equalsIgnoreCase("leave")) {
            if (commandSender instanceof ProxiedPlayer) {
                ProxiedPlayer p = (ProxiedPlayer) commandSender;
                QueueUtil.leaveQueue(p);
            }
        }
        else if (args[0].equalsIgnoreCase("movegroup")) {
            if (commandSender instanceof ProxiedPlayer) {
                ProxiedPlayer p = (ProxiedPlayer) commandSender;
                if (args.length < 2) {
                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c/hq movegroup <服务器组名>"));
                    return;
                }
                String targetServerGroup = args[1];
                QueueUtil.moveGroup(p, targetServerGroup);
            }
        }
        else if (args[0].equalsIgnoreCase("reload")) {
            if (commandSender.hasPermission("hq.reload")) {
                try {
                    ConfigManager.reloadConfig();
                    commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a重载成功"));
                } catch (Exception e) {
                    commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c重载失败"));
                    e.printStackTrace();
                }
            }
        }
        else {
            commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c/hq move <服务器名>"));
            commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c/hq movegroup <服务器组名>"));
            commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c/hq leave"));
        }
    }
}

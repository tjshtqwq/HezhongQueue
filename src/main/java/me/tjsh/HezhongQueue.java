package me.tjsh;

import net.md_5.bungee.api.*;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import sun.security.krb5.Config;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public final class HezhongQueue extends Plugin {

    public static HezhongQueue instance;
    public static Map<String, ArrayList<UUID>> allServerMap = new ConcurrentHashMap<>();
    public static Map<String, ArrayList<UUID>> allServerMapVIP = new ConcurrentHashMap<>(); // hq.priority
    public static Map<String, String> serverGroups = new HashMap<>();
    public static Map<String, Boolean> serverGroupsCheckingMotd = new HashMap<>();

    @Override
    public void onEnable() {
        instance = this;
        processConfig();
        try {
            ConfigManager.reloadConfig();
        } catch (IOException e) {
            e.printStackTrace();
        }
        getProxy().getPluginManager().registerCommand(this, new CommandListener());
        getProxy().getPluginManager().registerCommand(this, new HubCommandListener());
        getProxy().getPluginManager().registerCommand(this, new LobbyCommandListener());
        getProxy().getPluginManager().registerListener(this, new EventListener());
        getProxy().registerChannel("hezhong:queuecommand");
        for (String server : getProxy().getServers().keySet()) {
            allServerMap.put(server, new ArrayList<>());
        }
        for (String server : getProxy().getServers().keySet()) {
            allServerMapVIP.put(server, new ArrayList<>());
        }
        getProxy().getScheduler().schedule(this, () -> {
            for (String server : allServerMap.keySet()) {
                ArrayList<UUID> uuids = allServerMap.get(server);
                ArrayList<UUID> uuidsVIP = allServerMapVIP.get(server);
                ServerInfo serverInfo = getProxy().getServerInfo(server);
                if (serverInfo == null) continue;
                serverInfo.ping(new Callback<ServerPing>() {
                    @Override
                    public void done(ServerPing result, Throwable error) {
                        if (error == null && result != null) {
                            int max = result.getPlayers().getMax();
                            int online = result.getPlayers().getOnline();
                            int canJoin = max - online;
                            int canJoinVIP = max - online;
                            if (canJoinVIP > 0) {
                                for (int i = 1; i <= canJoinVIP; i++) {
                                    UUID uuid = uuidsVIP.get(0);
                                    ProxiedPlayer p = getProxy().getPlayer(uuid);
                                    if (p != null) {
                                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a正在尝试让你加入 " + server + "，请稍等..."));
                                        Title empty = getProxy().createTitle().clear();
                                        p.sendTitle(empty);
                                        p.connect(serverInfo);
                                        uuidsVIP.remove(uuid);
                                        canJoin--;
                                    } else {
                                        uuidsVIP.remove(uuid);
                                        i--;
                                    }
                                }
                            }
                            for (UUID uuid : uuidsVIP) {
                                ProxiedPlayer p = getProxy().getPlayer(uuid);
                                if (p != null) {
                                    int nowPos = uuidsVIP.indexOf(uuid) + 1;
                                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6你当前在第" + nowPos + "位，共" + uuids.size() + "位 &a优先队列"));
                                    sendQueuePositionTitle(p, nowPos, uuids.size(), true);
                                } else {
                                    uuidsVIP.remove(uuid);
                                }
                            }
                            if (canJoin > 0) {
                                for (int i = 1; i <= canJoin; i++) {
                                    UUID uuid = uuids.get(0);
                                    ProxiedPlayer p = getProxy().getPlayer(uuid);
                                    if (p != null) {
                                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a正在尝试让你加入 " + server + "，请稍等..."));
                                        Title empty = getProxy().createTitle().clear();
                                        p.sendTitle(empty);
                                        p.connect(serverInfo);
                                        uuids.remove(uuid);
                                    } else {
                                        uuids.remove(uuid);
                                        i--;
                                    }
                                }
                            }
                            for (UUID uuid : uuids) {
                                ProxiedPlayer p = getProxy().getPlayer(uuid);
                                if (p != null) {
                                    int nowPos = uuids.indexOf(uuid) + 1;
                                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6你当前在第" + nowPos + "位，共" + uuids.size() + "位 &c普通队列"));
                                    sendQueuePositionTitle(p, nowPos, uuids.size(), false);
                                } else {
                                    uuids.remove(uuid);
                                }
                            }
                        } else {
                            for (UUID uuid : uuids) {
                                ProxiedPlayer p = getProxy().getPlayer(uuid);
                                if (p != null) {
                                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c服务器异常 E=" + error.getMessage()));
                                }
                                uuids.remove(uuid);
                            }
                            for (UUID uuid : uuidsVIP) {
                                ProxiedPlayer p = getProxy().getPlayer(uuid);
                                if (p != null) {
                                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c服务器异常 E=" + error.getMessage()));
                                }
                                uuidsVIP.remove(uuid);
                            }
                        }
                    }
                });
                allServerMap.put(server, uuids);
                allServerMapVIP.put(server, uuidsVIP);
            }
        }, 0, 5000, TimeUnit.MILLISECONDS);
    }

    @Override
    public void onDisable() {

    }

    void processConfig() {
        try {
            ConfigManager.loadConfig();
        } catch (IOException e) {
            if (!getDataFolder().exists()) {
                getDataFolder().mkdir();
            }

            File file = new File(getDataFolder(), "config.yml");
            if (!file.exists()) {
                try {
                    file.createNewFile();
                    ConfigManager.loadConfig();
                } catch (IOException ie) {
                    ie.printStackTrace();
                }
            }
        }
    }

    public static void sendQueuePositionTitle(ProxiedPlayer player, int nowPos, int total, boolean p) {
        Title title = ProxyServer.getInstance().createTitle();
        String subtitleText = ChatColor.translateAlternateColorCodes('&',
                "&6你当前在第" + nowPos + "位，共" + total + "位" + (p ? " &a优先队列" : " &c普通队列"));

        title.title(new TextComponent(""));
        title.subTitle(new TextComponent(subtitleText));
        title.fadeIn(0);
        title.stay(400);
        title.fadeOut(0);

        player.sendTitle(title);
    }
}

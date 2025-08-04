package me.tjsh.utils;

import me.tjsh.ConfigManager;
import me.tjsh.HezhongQueue;
import net.md_5.bungee.api.Callback;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.*;

public class QueueUtil {
    public static List<UUID> findingGroupServerList = new ArrayList<>();

    public static void moveToServer(ProxiedPlayer p, String targetServer) {
        String[] black = ConfigManager.config.getString("blacklist-servers").split(",");
        for (String s : black) {
            if (s.equalsIgnoreCase(p.getServer().getInfo().getName())) {
                p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c&l你所在的服务器不允许使用！"));
                return;
            }
        }

        ServerInfo serverInfo = HezhongQueue.instance.getProxy().getServerInfo(targetServer);
        if (serverInfo == null) {
            p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c服务器不存在"));
            return;
        }
        if (p.getServer().getInfo().getName().equalsIgnoreCase(targetServer)) {
            p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c你已经在目标服务器中"));
            return;
        }
        // p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a正在尝试连接服务器......"));
        serverInfo.ping(new Callback<ServerPing>() {
            @Override
            public void done(ServerPing result, Throwable error) {
                if (error != null) {
                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c服务器异常 E=" + error.getMessage()));
                    return;
                }
                if (result != null) {
                    int online = result.getPlayers().getOnline();
                    int max = result.getPlayers().getMax();
                    boolean priority = false;
                    if (p.hasPermission("hq.priority")) priority = true;
                    p.sendMessage("&a服务器在线 尝试连接......");
                    if (online >= max) {
                        ArrayList<UUID> list = new ArrayList<>();
                        ArrayList<UUID> listVIP = new ArrayList<>();
                        if (!priority) {
                            for (ArrayList<UUID> uuids : HezhongQueue.instance.allServerMap.values()) {
                                if (uuids.contains(p.getUniqueId())) {
                                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c你在其他队列中！"));
                                    return;
                                }
                            }
                        } else {
                            listVIP = HezhongQueue.instance.allServerMapVIP.get(targetServer);
                            for (ArrayList<UUID> uuids : HezhongQueue.instance.allServerMapVIP.values()) {
                                if (uuids.contains(p.getUniqueId())) {
                                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c你在其他队列中！"));
                                    return;
                                }
                            }
                        }
                        if (!priority) {
                            list.add(p.getUniqueId());
                            p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c&l服务器已满。"));
                            p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a你已加入队列，共" + list.size() + "人。&c普通队列"));
                            p.sendMessage(ChatColor.translateAlternateColorCodes('&', ConfigManager.config.getString("buy-priority-queue-message")));
                            HezhongQueue.instance.allServerMap.put(targetServer, list);
                        } else {
                            listVIP.add(p.getUniqueId());
                            p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c&l服务器已满。"));
                            p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a你已加入队列，共" + listVIP.size() + "人。&a优先队列"));
                            HezhongQueue.instance.allServerMapVIP.put(targetServer, listVIP);
                        }
                    } else {
                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a服务器未满，加入服务器中...... 当前" + online + "/" + max + "人"));
                        p.connect(serverInfo);
                    }
                } else {
                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c服务器异常"));
                    return;
                }
            }
        });
    }

    public static void leaveQueue(ProxiedPlayer p) {
        for (String server : HezhongQueue.instance.allServerMap.keySet()) {
            ArrayList<UUID> list = HezhongQueue.instance.allServerMap.get(server);
            if (list.contains(p.getUniqueId())) {
                list.remove(p.getUniqueId());
                p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a你已离开队列。"));
                ProxyServer.getInstance().createTitle().clear().send(p);
                return;
            }
            HezhongQueue.instance.allServerMap.put(server, list);
        }
        if (findingGroupServerList.contains(p.getUniqueId())) {
            p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a你已离开匹配。"));
            findingGroupServerList.remove(p.getUniqueId());
            return;
        }
        p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c你没有在队列中。"));
    }

    public static void moveGroup(ProxiedPlayer p, String targetServerGroup) {
        String[] black = ConfigManager.config.getString("blacklist-servers").split(",");
        for (String s : black) {
            if (s.equalsIgnoreCase(p.getServer().getInfo().getName())) {
                p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c&l你所在的服务器不允许使用！"));
                return;
            }
        }
        String servers = HezhongQueue.serverGroups.get(targetServerGroup);
        if (p.hasPermission("hq.debug")) p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a服务器组：" + targetServerGroup + "，服务器：" + servers.toString() + "，当前：" + p.getServer().getInfo().getName()));
        if (servers == null) {
            p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c服务器组不存在"));
            return;
        }
        if (targetServerGroup.contains(p.getServer().getInfo().getName())) {
            p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c你已经在目标服务器组中的任一服务器了！"));
            return;
        }
        if (findingGroupServerList.contains(p.getUniqueId())) {
            p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c你已经在匹配中，请通过/hq leave离开。"));
            return;
        } else {
            findingGroupServerList.add(p.getUniqueId());
        }
        String[] serverList = servers.split(",");
        List<GroupServer> allCanJoinServerList = Collections.synchronizedList(new ArrayList<>());
        AtomicInteger remaining = new AtomicInteger(serverList.length);

        p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a正在为您寻找可用服务器，请稍等......"));

        for (String server : serverList) {
            ServerInfo serverInfo = HezhongQueue.instance.getProxy().getServerInfo(server);
            if (serverInfo == null) {
                p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c服务器 " + server + " 不存在"));
                if (remaining.decrementAndGet() == 0) {
                    // checkAndConnect(p, allCanJoinServerList, targetServerGroup);
                }
                continue;
            }

            serverInfo.ping(new Callback<ServerPing>() {
                @Override
                public void done(ServerPing serverPing, Throwable throwable) {
                    synchronized (remaining) {
                        if (throwable != null) {
                            remaining.decrementAndGet();
                            p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c服务器 " + server + " 异常: " + throwable.getMessage()));
                        } else if (serverPing != null) {
                            int online = serverPing.getPlayers().getOnline();
                            int max = serverPing.getPlayers().getMax();
                            String motd = serverPing.getDescriptionComponent().toString();
                            boolean checkMotd = ConfigManager.config.getBoolean("groups." + targetServerGroup + "-motd-check");
                            boolean useRegex = ConfigManager.config.getBoolean("groups." + targetServerGroup + "-motd-check-use-regex");
                            String matchStr = ConfigManager.config.getString("groups." + targetServerGroup + "-motd-check-match-string");
                            boolean matched = useRegex ? Pattern.matches(matchStr, motd) : motd.toLowerCase().contains(matchStr);
                            if (!checkMotd) matched = true;
                            if (online < max && matched) {
                                allCanJoinServerList.add(new GroupServer(server, online, true));
                            } else {
                                allCanJoinServerList.add(new GroupServer(server, online, false));
                            }

                        }
                        if (remaining.decrementAndGet() == 0) {
                            checkAndConnect(p, allCanJoinServerList, targetServerGroup);
                        }
                    }
                }
            });
        }
    }

    private static void checkAndConnect(ProxiedPlayer p, List<GroupServer> allCanJoinServerList, String targetServerGroup) {
        String[] black = ConfigManager.config.getString("blacklist-servers").split(",");
        for (String s : black) {
            if (s.equalsIgnoreCase(p.getServer().getInfo().getName())) {
                p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c&l你所在的服务器不允许使用！"));
                return;
            }
        }
        if (!findingGroupServerList.contains(p.getUniqueId())) {
            return;
        }
        GroupServer bestOnlineServer = null;
        String matchStrategy = ConfigManager.config.getString("groups." + targetServerGroup + "-matching-strategy");
        if (matchStrategy.equalsIgnoreCase("max")) {
            bestOnlineServer = allCanJoinServerList.stream().filter(gs -> gs.canJoin).max(Comparator.comparingInt(s -> s.online)).orElse(null);
        } else if (matchStrategy.equalsIgnoreCase("min")) {
            bestOnlineServer = allCanJoinServerList.stream().filter(gs -> gs.canJoin).min(Comparator.comparingInt(s -> s.online)).orElse(null);
        } else {
            bestOnlineServer = allCanJoinServerList.stream().filter(gs -> gs.canJoin).findFirst().orElse(null);
        }

        if (bestOnlineServer == null) {
            p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c服务器组内没有可用服务器"));
            findingGroupServerList.remove(p.getUniqueId());
        } else {
            p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a找到可用服务器，正在前往...... " + bestOnlineServer.server));
            findingGroupServerList.remove(p.getUniqueId());
            p.connect(HezhongQueue.instance.getProxy().getServerInfo(bestOnlineServer.server));
        }
    }
}
package me.tjsh;

import me.tjsh.ConfigManager;
import me.tjsh.HezhongQueue;
import me.tjsh.utils.QueueUtil;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class LobbyCommandListener extends Command {

    public LobbyCommandListener() {
        super("lobby");
    }

    @Override
    public void execute(CommandSender commandSender, String[] strings) {
        if (commandSender instanceof ProxiedPlayer) {
            ProxiedPlayer p = (ProxiedPlayer) commandSender;
            for (String serverGroupName : HezhongQueue.serverGroups.keySet()) {
                if (HezhongQueue.serverGroups.get(serverGroupName).contains(p.getServer().getInfo().getName())) {
                    String lobbyServerGroup = ConfigManager.config.getString("groups." + serverGroupName + "-lobby-server-group");
                    if (lobbyServerGroup == null || lobbyServerGroup.equalsIgnoreCase("")) {
                        QueueUtil.moveGroup(p, ConfigManager.config.getString("default-lobby-server-group"));
                        return;
                    } else {
                        QueueUtil.moveGroup(p, lobbyServerGroup);
                        return;
                    }
                }
            }
            QueueUtil.moveGroup(p, ConfigManager.config.getString("default-lobby-server-group"));
            return;
        }
    }
}

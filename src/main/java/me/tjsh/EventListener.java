package me.tjsh;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import me.tjsh.utils.QueueUtil;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class EventListener implements Listener {
    @EventHandler
    public void onPluginMessage(PluginMessageEvent e) {
        if (e.getTag().equalsIgnoreCase("hezhong:queuecommand")) {
            ByteArrayDataInput in = ByteStreams.newDataInput(e.getData());
            String command = in.readUTF();
            String username = in.readUTF();
            String server = in.readUTF();
            ProxiedPlayer p = HezhongQueue.instance.getProxy().getPlayer(username);
            if (p != null) {
                if (command.equalsIgnoreCase("move")) {
                    QueueUtil.moveToServer(p, server);
                } else if (command.equalsIgnoreCase("leave")) {
                    QueueUtil.leaveQueue(p);
                } else if (command.equalsIgnoreCase("movegroup")) {
                    QueueUtil.moveGroup(p, server);
                }
            }
        }
    }
}

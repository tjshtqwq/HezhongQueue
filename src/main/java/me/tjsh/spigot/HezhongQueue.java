package me.tjsh.spigot;

import org.bukkit.plugin.java.JavaPlugin;

public class HezhongQueue extends JavaPlugin {
    public static HezhongQueue instance;
    public void onLoad() {
        instance = this;
    }
    public void onEnable() {
        getCommand("hqs").setExecutor(new CommandListener());
        getServer().getMessenger().registerOutgoingPluginChannel(this, "hezhong:queuecommand");
    }
}

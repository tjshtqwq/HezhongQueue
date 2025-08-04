package me.tjsh;

import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class ConfigManager {
    public static Configuration config;
    public static void loadConfig() throws IOException {
        config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(HezhongQueue.instance.getDataFolder(), "config.yml"));
    }
    public static void saveConfig() throws IOException {
        ConfigurationProvider.getProvider(YamlConfiguration.class).save(config, new File(HezhongQueue.instance.getDataFolder(), "config.yml"));
    }
    public static void reloadConfig() throws IOException {
        loadConfig();
        loadGroups();
    }
    public static void loadGroups () {
        HezhongQueue.instance.serverGroups.clear();
        Configuration groupsSection = config.getSection("groups");
        if (groupsSection != null) {
            for (String groupName : groupsSection.getKeys()) {
                HezhongQueue.instance.serverGroups.put(
                        groupName,
                        groupsSection.getString(groupName, "").replace(" ", "")
                );
            }
        }
    }
}

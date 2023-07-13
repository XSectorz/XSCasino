package net.xsapi.panat.xscasino.configuration;

import net.xsapi.panat.xscasino.core.XSCasino;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class messages {
    public static File customConfigFile;

    public static FileConfiguration customConfig;

    public void loadConfigu() {
        customConfigFile = new File(XSCasino.getPlugin().getDataFolder(), "messages.yml");
        if (!customConfigFile.exists()) {
            customConfigFile.getParentFile().mkdirs();
            XSCasino.getPlugin().saveResource("messages.yml", false);
        }
        customConfig = (FileConfiguration) new YamlConfiguration();
        try {
            customConfig.load(customConfigFile);
        } catch (IOException | org.bukkit.configuration.InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    public static void save() {
        customConfigFile = new File(XSCasino.getPlugin().getDataFolder(), "messages.yml");
        try {
            customConfig.options().copyDefaults(true);
            customConfig.save(customConfigFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void reload() {
        config.customConfigFile = new File(XSCasino.getPlugin().getDataFolder(), "messages.yml");
        if (!config.customConfigFile.exists()) {
            config.customConfigFile.getParentFile().mkdirs();
            XSCasino.getPlugin().saveResource("messages.yml", false);
        } else {
            config.customConfig = (FileConfiguration) YamlConfiguration.loadConfiguration(config.customConfigFile);
            try {
                config.customConfig.save(config.customConfigFile);
                XSCasino.getPlugin().reloadConfig();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

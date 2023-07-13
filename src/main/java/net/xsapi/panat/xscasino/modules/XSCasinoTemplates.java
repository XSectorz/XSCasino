package net.xsapi.panat.xscasino.modules;

import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;

public class XSCasinoTemplates {

    private String name;
    private File customConfigFile;
    private FileConfiguration customConfig;

    public void setCustomConfig(FileConfiguration customConfig) {
        this.customConfig = customConfig;
    }

    public void setCustomConfigFile(File customConfigFile) {
        this.customConfigFile = customConfigFile;
    }

    public void setName(String name) {
        this.name = name;
    }

    public File getCustomConfigFile() {
        return customConfigFile;
    }

    public FileConfiguration getCustomConfig() {
        return customConfig;
    }

    public String getName() {
        return name;
    }
}

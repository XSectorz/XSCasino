package net.xsapi.panat.xscasino.modules;

import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;

public class XSCasinoTemplates {

    public int invSize;
    public String title;
    private File customConfigFile;
    private FileConfiguration customConfig;

    public void setCustomConfig(FileConfiguration customConfig) {
        this.customConfig = customConfig;
    }

    public void setCustomConfigFile(File customConfigFile) {
        this.customConfigFile = customConfigFile;
    }

    public File getCustomConfigFile() {
        return customConfigFile;
    }

    public FileConfiguration getCustomConfig() {
        return customConfig;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setInvSize(int invSize) {
        this.invSize = invSize;
    }

    public int getInvSize() {
        return invSize;
    }

    public String getTitle() {
        return title;
    }


}

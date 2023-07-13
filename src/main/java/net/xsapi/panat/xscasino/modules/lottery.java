package net.xsapi.panat.xscasino.modules;

import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;

public class lottery extends XSCasinoTemplates {

    public int invSize;
    public String title;

    public lottery(File customConfigFile, FileConfiguration customConfig) {
        setCustomConfigFile(customConfigFile);
        setCustomConfig(customConfig);

        setTitle(getCustomConfig().getString("configuration.title").replace("&","§"));
        setInvSize(getCustomConfig().getInt("configuration.invnetorySize"));
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

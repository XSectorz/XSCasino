package net.xsapi.panat.xscasino.user;

import net.xsapi.panat.xscasino.core.XSCasino;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class XSUser {

    private Player p;
    private HashMap<Integer,Integer> lottery = new HashMap<>();
    private File userFile;
    private FileConfiguration userConfig;

    public XSUser(Player p) {
        this.p = p;
        this.userFile = new File(XSCasino.getPlugin().getDataFolder() + "/data", p.getUniqueId() + ".yml");
        this.userConfig = (FileConfiguration) YamlConfiguration.loadConfiguration(this.userFile);
    }

    public void createUser() {
        this.userConfig = YamlConfiguration.loadConfiguration(this.userFile);
        if (!this.userFile.exists()) {
            this.userConfig.set("AccoutName", this.p.getName());
            this.userConfig.set("modules.lottery.data", new ArrayList<String>());
        }
        saveData();
    }

    public void saveData() {
        try {
            getUserConfig().save(this.userFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public File getUserFile() {
        return userFile;
    }

    public FileConfiguration getUserConfig() {
        return userConfig;
    }

    public HashMap<Integer, Integer> getLottery() {
        return lottery;
    }
}

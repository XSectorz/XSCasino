package net.xsapi.panat.xscasino.user;

import net.xsapi.panat.xscasino.core.XSCasino;
import net.xsapi.panat.xscasino.handlers.XSHandlers;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class XSUser {

    private Player p;
    private HashMap<Integer,Integer> lottery = new HashMap<>();
    private File userFile;
    private FileConfiguration userConfig;

    public XSUser(Player p) {
        this.p = p;
        this.userFile = new File(XSCasino.getPlugin().getDataFolder() + "/data", p.getUniqueId() + ".yml");
        this.userConfig = YamlConfiguration.loadConfiguration(this.userFile);
    }

    public void createUser() {
        this.userConfig = YamlConfiguration.loadConfiguration(this.userFile);
        if (!this.userFile.exists()) {
            this.userConfig.set("AccountName", this.p.getName());
            this.userConfig.set("modules.lottery.data", new ArrayList<String>());
        }
        saveData();
    }

    public void loadUserData() {
        for(String lottery : this.userConfig.getStringList("modules.lottery.data")) {
            int ticket = Integer.parseInt(lottery.split(":")[0]);
            int amount = Integer.parseInt(lottery.split(":")[1]);

            getLottery().put(ticket,amount);
        }
    }

    public void saveModulesData() {

        ArrayList<String> lotteryList = new ArrayList<>();
        for(Map.Entry<Integer,Integer> lottery : this.getLottery().entrySet()) {
            lotteryList.add(lottery.getKey()+":"+lottery.getValue());
        }
        this.getUserConfig().set("modules.lottery.data",lotteryList);
        this.saveData();
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

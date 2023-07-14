package net.xsapi.panat.xscasino.modules;

import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class lottery extends XSCasinoTemplates {

    public int invSize;
    public String title;
    public HashMap<Integer,Integer> lotteryList = new HashMap<>();

    public lottery(File customConfigFile, FileConfiguration customConfig) {
        setCustomConfigFile(customConfigFile);
        setCustomConfig(customConfig);

        setTitle(getCustomConfig().getString("configuration.title").replace("&","§"));
        setInvSize(getCustomConfig().getInt("configuration.invnetorySize"));

        for (String lottery : getCustomConfig().getStringList("data.lottery_list")) {
            int key = Integer.parseInt(lottery.split(":")[0]);
            int amount = Integer.parseInt(lottery.split(":")[1]);
            lotteryList.put(key,amount);
        }

    }

    public HashMap<Integer, Integer> getLotteryList() {
        return lotteryList;
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

    public void saveData() {
        ArrayList<String> lotteryList = new ArrayList<>();
        for(Map.Entry<Integer,Integer> lottery : this.getLotteryList().entrySet()) {
            lotteryList.add(lottery.getKey()+":"+lottery.getValue());
        }
        this.getCustomConfig().set("data.lottery_list",lotteryList);

        try {
            getCustomConfig().save(getCustomConfigFile());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

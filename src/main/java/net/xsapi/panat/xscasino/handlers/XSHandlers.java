package net.xsapi.panat.xscasino.handlers;

import net.milkbowl.vault.economy.Economy;
import net.xsapi.panat.xscasino.configuration.lotteryConfig;
import net.xsapi.panat.xscasino.core.XSCasino;
import net.xsapi.panat.xscasino.events.inventoryEvent;
import net.xsapi.panat.xscasino.events.joinEvent;
import net.xsapi.panat.xscasino.events.leaveEvent;
import net.xsapi.panat.xscasino.modules.lottery;
import net.xsapi.panat.xscasino.user.XSUser;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class XSHandlers {

    public static lottery XSLottery;
    public static HashMap<UUID, XSUser> xsCasinoUser = new HashMap<>();

    private static Economy econ = null;

    public static void loadXSCasinoModules() {

        Bukkit.getConsoleSender().sendMessage("§x§f§f§a§c§2§f[XSCasino] trying to load data...");
        XSLottery = new lottery(lotteryConfig.customConfigFile,lotteryConfig.customConfig);
        Bukkit.getConsoleSender().sendMessage("§x§f§f§a§c§2§f[XSCasino] loaded §x§6§0§F§F§0§0100% §x§f§f§a§c§2§fcomplete!");

    }

    public static void saveXSCasinoModules() {
        XSLottery.saveData();
    }

    public static void saveUserData() {
        for(Player p : Bukkit.getOnlinePlayers()) {
            if(XSHandlers.xsCasinoUser.containsKey(p.getUniqueId())) {

                XSUser xsUser = XSHandlers.xsCasinoUser.get(p.getUniqueId());

                ArrayList<String> lotteryList = new ArrayList<>();
                for(Map.Entry<Integer,Integer> lottery : xsUser.getLottery().entrySet()) {
                    lotteryList.add(lottery.getKey()+":"+lottery.getValue());
                }
                xsUser.getUserConfig().set("modules.lottery.data",lotteryList);

                xsUser.saveData();

                XSHandlers.xsCasinoUser.remove(p.getUniqueId());
            }
        }
    }

    public static void setupAPI() {
        if (XSCasino.getPlugin().getServer().getPluginManager().getPlugin("Vault") == null) {
            Bukkit.getConsoleSender().sendMessage("§x§f§f§a§c§2§f[XSCasino] Vault : §x§D§F§1§C§6§3Not Found!");
            XSCasino.getPlugin().getServer().getPluginManager().disablePlugin(XSCasino.getPlugin());
            Bukkit.getConsoleSender().sendMessage("§x§f§f§a§c§2§f[XSCasino] §x§D§F§1§C§6§3Plugin Disabled due not found vault!");
        } else {
            RegisteredServiceProvider<Economy> rsp = XSCasino.getPlugin().getServer().getServicesManager().getRegistration(Economy.class);
            econ = rsp.getProvider();
            Bukkit.getConsoleSender().sendMessage("§x§f§f§a§c§2§f[XSCasino] Vault : §x§2§F§C§0§2§0Found!");
        }
    }

    public static Economy getEconomy() {
        return econ;
    }

    public static void registerEvents() {
        Bukkit.getPluginManager().registerEvents(new inventoryEvent(), XSCasino.getPlugin());
        Bukkit.getPluginManager().registerEvents(new joinEvent(), XSCasino.getPlugin());
        Bukkit.getPluginManager().registerEvents(new leaveEvent(), XSCasino.getPlugin());
    }

}

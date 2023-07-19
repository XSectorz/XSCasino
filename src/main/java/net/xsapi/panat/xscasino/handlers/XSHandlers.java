package net.xsapi.panat.xscasino.handlers;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import net.xsapi.panat.xscasino.configuration.lotteryConfig;
import net.xsapi.panat.xscasino.core.XSCasino;
import net.xsapi.panat.xscasino.events.joinEvent;
import net.xsapi.panat.xscasino.events.leaveEvent;
import net.xsapi.panat.xscasino.gui.ui_main_lottery;
import net.xsapi.panat.xscasino.gui.ui_myticket_lottery;
import net.xsapi.panat.xscasino.gui.ui_topticket_lottery;
import net.xsapi.panat.xscasino.modules.lottery;
import net.xsapi.panat.xscasino.user.UserData;
import net.xsapi.panat.xscasino.user.XSUser;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class XSHandlers {

    public static lottery XSLottery;
    public static HashMap<UUID, XSUser> xsCasinoUser = new HashMap<>();
    private static HashMap<UUID, UserData> userData = new HashMap<>();

    private static Economy econ = null;
    private static Permission perms = null;

    public static void loadXSCasinoModules() {

        Bukkit.getConsoleSender().sendMessage("§x§f§f§a§c§2§f[XSCasino] trying to load data...");
        XSLottery = new lottery(lotteryConfig.customConfigFile,lotteryConfig.customConfig);
        Bukkit.getConsoleSender().sendMessage("§x§f§f§a§c§2§f[XSCasino] loaded §x§6§0§F§F§0§0100% §x§f§f§a§c§2§fcomplete!");

    }

    public static HashMap<UUID, UserData> getUserData() {
        return userData;
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

    public static void loadUserData() {
        for(Player p : Bukkit.getOnlinePlayers()) {
            File pFile = new File(XSCasino.getPlugin().getDataFolder() + "/data", p.getUniqueId() + ".yml");

            if(pFile.exists()) {
                XSUser xsUser = new XSUser(p);

                if(xsUser.getUserConfig().get("modules.lottery.data") != null) {
                    xsUser.loadUserData();
                }

                XSHandlers.xsCasinoUser.put(p.getUniqueId(),xsUser);
            }

            XSHandlers.getUserData().put(p.getUniqueId(),new UserData(p));
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
            RegisteredServiceProvider<Permission> rspPermission = XSCasino.getPlugin().getServer().getServicesManager().getRegistration(Permission.class);
            perms = rspPermission.getProvider();
            Bukkit.getConsoleSender().sendMessage("§x§f§f§a§c§2§f[XSCasino] Vault : §x§2§F§C§0§2§0Found!");
        }
    }

    public static Economy getEconomy() {
        return econ;
    }

    public static Permission getPerms() {
        return perms;
    }

    public static void registerEvents() {
        Bukkit.getPluginManager().registerEvents(new joinEvent(), XSCasino.getPlugin());
        Bukkit.getPluginManager().registerEvents(new leaveEvent(), XSCasino.getPlugin());
        Bukkit.getPluginManager().registerEvents(new ui_main_lottery(), XSCasino.getPlugin());
        Bukkit.getPluginManager().registerEvents(new ui_topticket_lottery(), XSCasino.getPlugin());
        Bukkit.getPluginManager().registerEvents(new ui_myticket_lottery(), XSCasino.getPlugin());
    }

    public static String convertTime(long millis) {
        int seconds = (int) (millis / 1000) % 60;
        int minutes = (int) ((millis / (1000 * 60)) % 60);
        int hours = (int) ((millis / (1000 * 60 * 60)) % 24);
        long days = millis / (1000 * 60 * 60 * 24);

        String timer = "";

        if(days >= 1) {
            timer += days;
            if(days == 1) {
                timer += " " + XSUtils.getMessagesConfig("time.day") + " ";
            } else {
                timer += " " + XSUtils.getMessagesConfig("time.days") + " ";
            }
        }
        if(hours >= 1) {
            timer += hours;
            if(hours == 1) {
                timer += " " + XSUtils.getMessagesConfig("time.hour") + " ";
            } else {
                timer += " " + XSUtils.getMessagesConfig("time.hours") + " ";
            }
        }
        if(minutes >= 1) {
            timer += minutes;
            if(minutes == 1) {
                timer += " " + XSUtils.getMessagesConfig("time.minute") + " ";
            } else {
                timer += " " + XSUtils.getMessagesConfig("time.minutes") + " ";
            }
        }

        if(seconds >= 1) {
            timer += seconds;
            if(minutes == 1) {
                timer += " " + XSUtils.getMessagesConfig("time.second") + " ";
            } else {
                timer += " " + XSUtils.getMessagesConfig("time.seconds") + " ";
            }
        }

        if(timer.length() == 0) {
            timer += XSUtils.getMessagesConfig("time.soon");
        }

        return timer;
    }


}

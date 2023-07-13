package net.xsapi.panat.xscasino.handlers;

import net.xsapi.panat.xscasino.configuration.lotteryConfig;
import net.xsapi.panat.xscasino.core.XSCasino;
import net.xsapi.panat.xscasino.events.inventoryEvent;
import net.xsapi.panat.xscasino.modules.lottery;
import org.bukkit.Bukkit;

public class XSHandlers {

    public static lottery XSLottery;

    public static void loadXSCasinoModules() {

        Bukkit.getConsoleSender().sendMessage("§x§f§f§a§c§2§f[XSCasino] trying to load data...");
        XSLottery = new lottery(lotteryConfig.customConfigFile,lotteryConfig.customConfig);
        Bukkit.getConsoleSender().sendMessage("§x§f§f§a§c§2§f[XSCasino] loaded §x§6§0§F§F§0§0100% §x§f§f§a§c§2§fcomplete!");

    }

    public static void registerEvents() {
        Bukkit.getPluginManager().registerEvents(new inventoryEvent(), XSCasino.getPlugin());
    }

}

package net.xsapi.panat.xscasino.core;

import net.xsapi.panat.xscasino.commands.XSCommand;
import net.xsapi.panat.xscasino.configuration.config;
import net.xsapi.panat.xscasino.configuration.configLoader;
import net.xsapi.panat.xscasino.handlers.XSHandlers;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class XSCasino extends JavaPlugin {

    private static XSCasino plugin;

    public static XSCasino getPlugin() {
        return plugin;
    }


    @Override
    public void onEnable() {
        Bukkit.getConsoleSender().sendMessage("§x§f§f§a§c§2§f******************************");
        Bukkit.getConsoleSender().sendMessage("§x§f§f§a§c§2§f   XSAPI Casino v1.0     ");
        Bukkit.getConsoleSender().sendMessage("§r");
        Bukkit.getConsoleSender().sendMessage("§x§f§f§a§c§2§f  Did you have a goodluck?");
        Bukkit.getConsoleSender().sendMessage("§r");
        Bukkit.getConsoleSender().sendMessage("§x§f§f§a§c§2§f******************************");

        plugin = this;


        new configLoader();

        XSHandlers.setupAPI();
        XSHandlers.setupDefault();

        XSHandlers.loadXSCasinoModules();
        XSHandlers.registerEvents();
        XSHandlers.loadUserData();

        getCommand("xscasino").setExecutor(new XSCommand());
    }

    @Override
    public void onDisable() {
        Bukkit.getConsoleSender().sendMessage("§cPlugin Disabled 1.20.1!");
        XSHandlers.saveXSCasinoModules();
        XSHandlers.saveUserData();
        XSHandlers.destroyAllThread();
    }
}

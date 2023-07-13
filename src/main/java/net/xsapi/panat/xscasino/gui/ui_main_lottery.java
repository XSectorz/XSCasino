package net.xsapi.panat.xscasino.gui;

import net.xsapi.panat.xscasino.handlers.XSHandlers;
import net.xsapi.panat.xscasino.handlers.XSUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;

public class ui_main_lottery {

    public static void openLotteryGUI(Player p) {

        Inventory inv = Bukkit.createInventory(null, XSHandlers.XSLottery.getInvSize(),XSHandlers.XSLottery.getTitle());

        for(String contents : XSHandlers.XSLottery.getCustomConfig().getConfigurationSection("contents").getKeys(false)) {
            Material mat = Material.getMaterial(XSHandlers.XSLottery.getCustomConfig().getString("contents."
            + contents + ".Material"));
            int amount = XSHandlers.XSLottery.getCustomConfig().getInt("contents."  + contents + ".amount");
            int modelData = XSHandlers.XSLottery.getCustomConfig().getInt("contents."  + contents + ".customModelData");
            String display = XSHandlers.XSLottery.getCustomConfig().getString("contents." + contents + ".display");
            ArrayList<String> lores = (ArrayList<String>) XSHandlers.XSLottery.getCustomConfig().getStringList("contents." + contents + ".lore");
            ArrayList<String> slots = (ArrayList<String>) XSHandlers.XSLottery.getCustomConfig().getStringList("contents." + contents + ".slots");

            ItemStack it = XSUtils.createItemStack(mat,amount,modelData,display,lores);
            for(int i = 0 ; i < slots.size() ; i++) {
                inv.setItem(Integer.parseInt(slots.get(i)),it);
            }

        }

        p.openInventory(inv);
    }

}

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

        Material mat = Material.getMaterial(XSHandlers.XSLottery.getCustomConfig().getString("background_contents.Material"));
        int amount = XSHandlers.XSLottery.getCustomConfig().getInt("background_contents.amount");
        int modelData = XSHandlers.XSLottery.getCustomConfig().getInt("background_contents.customModelData");
        String display = XSHandlers.XSLottery.getCustomConfig().getString("background_contents.display");
        ArrayList<String> lores = (ArrayList<String>) XSHandlers.XSLottery.getCustomConfig().getStringList("background_contents.lore");

        ItemStack it = XSUtils.createItemStack(mat,amount,modelData,display,lores);
        for(String i : XSHandlers.XSLottery.getCustomConfig().getStringList("configuration.backgroundSlot")) {
            inv.setItem(Integer.parseInt(i),it);
        }

        if(!XSHandlers.XSLottery.getXsLotteryUserOpenUI().containsKey(p.getUniqueId())) {
            XSHandlers.XSLottery.getXsLotteryUserOpenUI().put(p.getUniqueId(),inv);
        }

        updateInventory(p);
        p.openInventory(inv);
    }

    public static void updateInventory(Player p) {

        Inventory inv = XSHandlers.XSLottery.getXsLotteryUserOpenUI().get(p.getUniqueId());
        updateInventoryContents(inv,p);
        p.updateInventory();
    }

    public static void updateInventoryContents(Inventory inv,Player p) {

        String currTime = XSHandlers.convertTime(Math.abs(System.currentTimeMillis()-XSHandlers.XSLottery.getNextPrizeTime()));

        String winner = "";
        String number = "";
        String numberTicket = "";
        String totalPrize = "";

        if(!XSHandlers.XSLottery.getWinner().isEmpty()) {
            winner = XSHandlers.XSLottery.getWinner();
            number = String.valueOf(XSHandlers.XSLottery.getTicketWinNum());
            numberTicket = String.valueOf(XSHandlers.XSLottery.getNumberTicketWin());
            totalPrize = String.valueOf(XSHandlers.XSLottery.getTotalWinPrize());
        } else {
            winner = XSUtils.getMessagesConfig("gui_message.no_winner");
            number = XSUtils.getMessagesConfig("gui_message.no_data");
            numberTicket = XSUtils.getMessagesConfig("gui_message.no_data");
            totalPrize = XSUtils.getMessagesConfig("gui_message.no_data");
        }

        for(String contents : XSHandlers.XSLottery.getCustomConfig().getConfigurationSection("contents").getKeys(false)) {
            Material mat = Material.getMaterial(XSHandlers.XSLottery.getCustomConfig().getString("contents."
                    + contents + ".Material"));
            int amount = XSHandlers.XSLottery.getCustomConfig().getInt("contents."  + contents + ".amount");
            int modelData = XSHandlers.XSLottery.getCustomConfig().getInt("contents."  + contents + ".customModelData");
            String display = XSHandlers.XSLottery.getCustomConfig().getString("contents." + contents + ".display");
            ArrayList<String> lores = (ArrayList<String>) XSHandlers.XSLottery.getCustomConfig().getStringList("contents." + contents + ".lore");
            ArrayList<String> slots = (ArrayList<String>) XSHandlers.XSLottery.getCustomConfig().getStringList("contents." + contents + ".slots");

            String finalWinner = winner;
            String finalNumberTicket = numberTicket;
            String finalNumber = number;
            String finalTotalPrize = totalPrize;

            lores.replaceAll(e -> e.replace("%current_pot%",
                            String.valueOf(XSHandlers.XSLottery.getPotPrize()))
                    .replace("%current_lottery%",
                            String.valueOf(XSHandlers.XSLottery.getAmountTicket()))
                    .replace("%lottery_timer%",currTime)
                    .replace("%winner_name%", finalWinner)
                    .replace("%ticket_number%", finalNumber)
                    .replace("%ticket_amount%", finalNumberTicket)
                    .replace("%totalPot%", finalTotalPrize));

            ItemStack it = XSUtils.createItemStack(mat,amount,modelData,display,lores);
            for(int i = 0 ; i < slots.size() ; i++) {
                inv.setItem(Integer.parseInt(slots.get(i)),it);
            }

        }
    }

}

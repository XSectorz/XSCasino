package net.xsapi.panat.xscasino.gui;

import net.xsapi.panat.xscasino.handlers.XSHandlers;
import net.xsapi.panat.xscasino.handlers.XSUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class ui_topticket_lottery implements Listener {

    public static void openTopTicket(Player p) {

        Inventory inv = Bukkit.createInventory(null, XSHandlers.XSLottery.getTopTicketSize(),XSHandlers.XSLottery.getTopTicketTitle());

        if(XSHandlers.XSLottery.getXsLotteryUserOpenUI().containsKey(p.getUniqueId())) {
            XSHandlers.XSLottery.getXsLotteryUserOpenUI().remove(p.getUniqueId());
        }

        inv.setMaxStackSize(100);

        Material mat = Material.getMaterial(XSHandlers.XSLottery.getCustomConfig().getString("background_contents.Material"));
        int amount = XSHandlers.XSLottery.getCustomConfig().getInt("background_contents.amount");
        int modelData = XSHandlers.XSLottery.getCustomConfig().getInt("background_contents.customModelData");
        String display = XSHandlers.XSLottery.getCustomConfig().getString("background_contents.display");
        ArrayList<String> lores = (ArrayList<String>) XSHandlers.XSLottery.getCustomConfig().getStringList("background_contents.lore");

        ItemStack it = XSUtils.createItemStack(mat,amount,modelData,display,lores);

        for(String i : XSHandlers.XSLottery.getCustomConfig().getStringList("topTicket_configuration.backgroundSlot")) {
            inv.setItem(Integer.parseInt(i),it);
        }

        setUpTicket(inv);
        setUpContents(inv);
        p.openInventory(inv);
    }

    public static void setUpTicket(Inventory inv) {

        HashMap<Integer,Integer> lottery = XSHandlers.XSLottery.getLotteryList();
        if(lottery.size() == 0) {
            Material mat = Material.getMaterial(XSHandlers.XSLottery.getCustomConfig().getString("empty_contents.Material"));
            int amount = XSHandlers.XSLottery.getCustomConfig().getInt("empty_contents.amount");
            int modelData = XSHandlers.XSLottery.getCustomConfig().getInt("empty_contents.customModelData");
            String display = XSHandlers.XSLottery.getCustomConfig().getString("empty_contents.display");
            ArrayList<String> lores = (ArrayList<String>) XSHandlers.XSLottery.getCustomConfig().getStringList("empty_contents.lore");

            ItemStack it = XSUtils.createItemStack(mat,amount,modelData,display,lores);

            for(String i : XSHandlers.XSLottery.getCustomConfig().getStringList("topTicket_configuration.emptySlot")) {
                inv.setItem(Integer.parseInt(i),it);
            }
        } else {
            List<Map.Entry<Integer, Integer>> list = new ArrayList<>(lottery.entrySet());
            Collections.sort(list, new Comparator<Map.Entry<Integer, Integer>>() {
                @Override
                public int compare(Map.Entry<Integer, Integer> o1, Map.Entry<Integer, Integer> o2) {
                    return o2.getValue().compareTo(o1.getValue());
                }
            });

            Material mat = Material.getMaterial(XSHandlers.XSLottery.getCustomConfig().getString("topTicket_contents.Material"));
            int modelData = XSHandlers.XSLottery.getCustomConfig().getInt("topTicket_contents.customModelData");
            int index = 0;
            for (Map.Entry<Integer, Integer> entry : list) {
                //Bukkit.broadcastMessage("KEY : " + entry.getKey() + " | " + entry.getValue());
                if(index >= XSHandlers.XSLottery.getCustomConfig().getStringList("topTicket_configuration.contentSlot").size()) {
                    break;
                }
                String display = XSHandlers.XSLottery.getCustomConfig().getString("topTicket_contents.display");
                ArrayList<String> lores = new ArrayList<>(XSHandlers.XSLottery.getCustomConfig().getStringList("topTicket_contents.lore"));

                display = display.replace("%ticketNum%", String.valueOf(entry.getKey()));
                display = display.replace("%ticketAmount%", String.valueOf(entry.getValue()));

                lores.replaceAll(e -> e.replace("%ticketNum%", String.valueOf(entry.getKey())));
                lores.replaceAll(e -> e.replace("%ticketAmount%", String.valueOf(entry.getValue())));

                ItemStack it = XSUtils.createItemStack(mat,index+1,modelData,display,lores);

                inv.setItem(Integer.parseInt(XSHandlers.XSLottery.getCustomConfig().getStringList("topTicket_configuration.contentSlot").get(index)),
                        it
                        );
                index += 1;
            }
        }
    }

    public static void setUpContents(Inventory inv) {
        for(String contents : XSHandlers.XSLottery.getCustomConfig().getConfigurationSection("top_ticket_contents").getKeys(false)) {
            Material mat = Material.getMaterial(XSHandlers.XSLottery.getCustomConfig().getString("top_ticket_contents."
                    + contents + ".Material"));
            int amount = XSHandlers.XSLottery.getCustomConfig().getInt("top_ticket_contents."  + contents + ".amount");
            int modelData = XSHandlers.XSLottery.getCustomConfig().getInt("top_ticket_contents."  + contents + ".customModelData");
            String display = XSHandlers.XSLottery.getCustomConfig().getString("top_ticket_contents." + contents + ".display");
            ArrayList<String> lores = (ArrayList<String>) XSHandlers.XSLottery.getCustomConfig().getStringList("top_ticket_contents." + contents + ".lore");
            ArrayList<String> slots = (ArrayList<String>) XSHandlers.XSLottery.getCustomConfig().getStringList("top_ticket_contents." + contents + ".slots");

            ItemStack it = XSUtils.createItemStack(mat,amount,modelData,display,lores);
            for(int i = 0 ; i < slots.size() ; i++) {
                inv.setItem(Integer.parseInt(slots.get(i)),it);
            }

        }
    }

    @EventHandler
    public void onClickInventory(InventoryClickEvent e) {
        Player p = (Player) e.getWhoClicked();

        if(e.getView().getTitle().equalsIgnoreCase(XSHandlers.XSLottery.getTopTicketTitle())) {

            if (e.getSlot() < 0) {
                return;
            }
            e.setCancelled(true);

            if (XSHandlers.XSLottery.getCustomConfig().getStringList("top_ticket_contents.close.slots").contains(String.valueOf(e.getSlot()))) {
                ui_main_lottery.openLotteryGUI(p);
            }
        }
    }

}

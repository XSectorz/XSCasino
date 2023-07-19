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

public class ui_myticket_lottery implements Listener {

    public static void openMyTicket(Player p) {

        Inventory inv = Bukkit.createInventory(null, XSHandlers.XSLottery.getMyTicketSize(),XSHandlers.XSLottery.getMyTicketTitle());

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

        for(String i : XSHandlers.XSLottery.getCustomConfig().getStringList("myTicket_configuration.backgroundSlot")) {
            inv.setItem(Integer.parseInt(i),it);
        }

        setUpTicket(p,inv);
        setUpContents(inv);
        p.openInventory(inv);
    }

    public static void setUpContents(Inventory inv) {
        for(String contents : XSHandlers.XSLottery.getCustomConfig().getConfigurationSection("my_ticket_contents").getKeys(false)) {
            Material mat = Material.getMaterial(XSHandlers.XSLottery.getCustomConfig().getString("top_ticket_contents."
                    + contents + ".Material"));
            int amount = XSHandlers.XSLottery.getCustomConfig().getInt("my_ticket_contents."  + contents + ".amount");
            int modelData = XSHandlers.XSLottery.getCustomConfig().getInt("my_ticket_contents."  + contents + ".customModelData");
            String display = XSHandlers.XSLottery.getCustomConfig().getString("my_ticket_contents." + contents + ".display");
            ArrayList<String> lores = (ArrayList<String>) XSHandlers.XSLottery.getCustomConfig().getStringList("my_ticket_contents." + contents + ".lore");
            ArrayList<String> slots = (ArrayList<String>) XSHandlers.XSLottery.getCustomConfig().getStringList("my_ticket_contents." + contents + ".slots");

            ItemStack it = XSUtils.createItemStack(mat,amount,modelData,display,lores);
            for(int i = 0 ; i < slots.size() ; i++) {
                inv.setItem(Integer.parseInt(slots.get(i)),it);
            }

        }
    }

    public static void setUpTicket(Player p,Inventory inv) {

        HashMap<Integer,Integer> lottery = new HashMap<>();

        if(XSHandlers.xsCasinoUser.containsKey(p.getUniqueId())) {
            lottery = XSHandlers.xsCasinoUser.get(p.getUniqueId()).getLottery();
        }

        int startIndex = 0;
        if(lottery.size() == 0) {
            Material mat = Material.getMaterial(XSHandlers.XSLottery.getCustomConfig().getString("empty_contents.Material"));
            int amount = XSHandlers.XSLottery.getCustomConfig().getInt("empty_contents.amount");
            int modelData = XSHandlers.XSLottery.getCustomConfig().getInt("empty_contents.customModelData");
            String display = XSHandlers.XSLottery.getCustomConfig().getString("empty_contents.display");
            ArrayList<String> lores = (ArrayList<String>) XSHandlers.XSLottery.getCustomConfig().getStringList("empty_contents.lore");

            ItemStack it = XSUtils.createItemStack(mat,amount,modelData,display,lores);

            for(String i : XSHandlers.XSLottery.getCustomConfig().getStringList("myTicket_configuration.emptySlot")) {
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

            Material mat = Material.getMaterial(XSHandlers.XSLottery.getCustomConfig().getString("myTicket_contents.Material"));
            int modelData = XSHandlers.XSLottery.getCustomConfig().getInt("myTicket_contents.customModelData");
            startIndex = (XSHandlers.getUserData().get(p.getUniqueId()).getMyLotteryPage()-1)*XSHandlers.XSLottery.getCustomConfig().getStringList("myTicket_configuration.contentSlot").size();
            for (int i = 0 ; i < XSHandlers.XSLottery.getCustomConfig().getStringList("myTicket_configuration.contentSlot").size() ; i++) {

                //Bukkit.broadcastMessage("KEY : " + entry.getKey() + " | " + entry.getValue());
                if(startIndex+i >= lottery.size()) {
                    break;
                }
                Map.Entry<Integer, Integer> ticket = list.get(i+startIndex);

                String display = XSHandlers.XSLottery.getCustomConfig().getString("myTicket_contents.display");
                ArrayList<String> lores = new ArrayList<>(XSHandlers.XSLottery.getCustomConfig().getStringList("myTicket_contents.lore"));

                display = display.replace("%ticketNum%", String.valueOf(ticket.getKey()));
                display = display.replace("%ticketAmount%", String.valueOf(ticket.getValue()));

                lores.replaceAll(e -> e.replace("%ticketNum%", String.valueOf(ticket.getKey())));
                lores.replaceAll(e -> e.replace("%ticketAmount%", String.valueOf(ticket.getValue())));

                ItemStack it = XSUtils.createItemStack(mat,i+1+startIndex,modelData,display,lores);

                inv.setItem(Integer.parseInt(XSHandlers.XSLottery.getCustomConfig().getStringList("myTicket_configuration.contentSlot").get(i)),
                        it
                );
            }
        }
        int tempMaxPage = (XSHandlers.getUserData().get(p.getUniqueId()).getMyLotteryPage())*XSHandlers.XSLottery.getCustomConfig().getStringList("myTicket_configuration.contentSlot").size();
        if(XSHandlers.getUserData().get(p.getUniqueId()).getMyLotteryPage() > 1) {
            for (String i : XSHandlers.XSLottery.getCustomConfig().getStringList("myTicket_configuration.prevSlot")) {
                inv.setItem(Integer.parseInt(i),createItemStack("prevSlotAvaible_contents"));
            }
        } else {
            for (String i : XSHandlers.XSLottery.getCustomConfig().getStringList("myTicket_configuration.prevSlot")) {
                inv.setItem(Integer.parseInt(i),createItemStack("prevSlotUnAvaible_contents"));
            }
        }

        if(lottery.size() > tempMaxPage) {
            for (String i : XSHandlers.XSLottery.getCustomConfig().getStringList("myTicket_configuration.nextSlot")) {
                inv.setItem(Integer.parseInt(i),createItemStack("nextSlotAvaible_contents"));
            }
        } else {
            for (String i : XSHandlers.XSLottery.getCustomConfig().getStringList("myTicket_configuration.nextSlot")) {
                inv.setItem(Integer.parseInt(i),createItemStack("nextSlotUnAvaible_contents"));
            }
        }
    }

    public static ItemStack createItemStack(String key) {
        Material mat = Material.getMaterial(XSHandlers.XSLottery.getCustomConfig().getString(key + ".Material"));
        int modelData = XSHandlers.XSLottery.getCustomConfig().getInt(key + ".customModelData");
        String display = XSHandlers.XSLottery.getCustomConfig().getString(key + ".display");
        ArrayList<String> lores = (ArrayList<String>) XSHandlers.XSLottery.getCustomConfig().getStringList(key+".lore");

        ItemStack it = XSUtils.createItemStack(mat,1,modelData,display,lores);
        return it;
    }
    @EventHandler
    public void onClickInventory(InventoryClickEvent e) {
        Player p = (Player) e.getWhoClicked();
        if(e.getView().getTitle().equalsIgnoreCase(XSHandlers.XSLottery.getMyTicketTitle())) {
            if (e.getSlot() < 0) {
                return;
            }
            e.setCancelled(true);

            if (XSHandlers.XSLottery.getCustomConfig().getStringList("my_ticket_contents.close.slots").contains(String.valueOf(e.getSlot()))) {
                ui_main_lottery.openLotteryGUI(p);
            }
            else if (XSHandlers.XSLottery.getCustomConfig().getStringList("myTicket_configuration.prevSlot").contains(String.valueOf(e.getSlot()))) {
                if(XSHandlers.getUserData().get(p.getUniqueId()).getMyLotteryPage() > 1) {
                    XSHandlers.getUserData().get(p.getUniqueId()).setMyLotteryPage(XSHandlers.getUserData().get(p.getUniqueId()).getMyLotteryPage()-1);
                    ui_myticket_lottery.openMyTicket(p);
                }
            }
            else if (XSHandlers.XSLottery.getCustomConfig().getStringList("myTicket_configuration.nextSlot").contains(String.valueOf(e.getSlot()))) {
                int startIndex = (XSHandlers.getUserData().get(p.getUniqueId()).getMyLotteryPage())*XSHandlers.XSLottery.getCustomConfig().getStringList("myTicket_configuration.contentSlot").size();

                if(XSHandlers.xsCasinoUser.containsKey(p.getUniqueId())) {
                    if(XSHandlers.xsCasinoUser.get(p.getUniqueId()).getLottery().size() > startIndex) {
                        XSHandlers.getUserData().get(p.getUniqueId()).setMyLotteryPage(XSHandlers.getUserData().get(p.getUniqueId()).getMyLotteryPage()+1);
                        ui_myticket_lottery.openMyTicket(p);
                    }
                }
            }
        }
    }
}

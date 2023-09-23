package net.xsapi.panat.xscasino.gui;

import de.rapha149.signgui.SignGUI;
import net.xsapi.panat.xscasino.configuration.messages;
import net.xsapi.panat.xscasino.core.XSCasino;
import net.xsapi.panat.xscasino.handlers.XSHandlers;
import net.xsapi.panat.xscasino.handlers.XSUtils;
import net.xsapi.panat.xscasino.user.XSUser;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.time.LocalTime;
import java.util.ArrayList;

public class ui_main_lottery implements Listener {

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
        updateInventoryContents(inv);
        p.updateInventory();
    }

    public static void updateInventoryContents(Inventory inv) {

        String currTime;

        currTime = XSUtils.convertTime(Math.abs(System.currentTimeMillis()-XSHandlers.XSLottery.getNextPrizeTime()));

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

    @EventHandler
    public void onClickUI(InventoryClickEvent e) {

        Player p = (Player) e.getWhoClicked();

        if(e.getView().getTitle().equalsIgnoreCase(XSHandlers.XSLottery.getTitle())) {

            if(e.getSlot() < 0) {
                return;
            }

            if(XSHandlers.XSLottery.getCustomConfig().getStringList("contents.close.slots").contains(String.valueOf(e.getSlot()))) {
                p.closeInventory();
            }
            else if(XSHandlers.XSLottery.getCustomConfig().getStringList("contents.top_ticket.slots").contains(String.valueOf(e.getSlot()))) {
                ui_topticket_lottery.openTopTicket(p);
            } else if(XSHandlers.XSLottery.getCustomConfig().getStringList("contents.my_lottery.slots").contains(String.valueOf(e.getSlot()))) {
                XSHandlers.getUserData().get(p.getUniqueId()).setMyLotteryPage(1);
                ui_myticket_lottery.openMyTicket(p);
            }
            else if(XSHandlers.XSLottery.getCustomConfig().getStringList("contents.buy_ticket.slots").contains(String.valueOf(e.getSlot()))) {
                p.closeInventory();
                if(XSHandlers.getUsingRedis()) {
                    Bukkit.broadcastMessage("Can buy: " + XSHandlers.XSLottery.isBuyAble());
                    if(!XSHandlers.XSLottery.isBuyAble()) {
                        XSUtils.sendMessages(p,"redis_not_connect");
                        return;
                    }
                }
                new SignGUI()
                        .lines("§6พิมพ์เลข/จำนวน", "69", "-1", "§6^^^^^^^^^^^")
                        .type(Material.DARK_OAK_SIGN)
                        .color(DyeColor.YELLOW)
                        .stripColor()
                        .onFinish((lines) -> {
                            if (!lines[1].isEmpty() && !lines[2].isEmpty() && !lines[2].equalsIgnoreCase("-1")) {
                                int ticket = Integer.parseInt(lines[1]);
                                int amount = 0;

                                try {
                                    amount = Integer.parseInt(lines[2]);
                                } catch (NumberFormatException nf) {
                                    XSUtils.sendMessages(p,"inputNAN");
                                    return null;
                                }

                                if(amount <= 0) {
                                    XSUtils.sendMessages(p,"only_positive");
                                    return null;
                                }

                                long price = (long) (amount*XSHandlers.XSLottery.getPriceTicket());

                                if(XSHandlers.getEconomy().getBalance(p) < price) {
                                    XSUtils.sendMessages(p,"cant_afford");
                                    return null;
                                }

                                if(!(ticket >= 0 && ticket <= 99)) {
                                    XSUtils.sendMessages(p,"not_in_range");
                                    return null;
                                }

                                XSHandlers.getEconomy().withdrawPlayer(p,(double) price);

                                String message = messages.customConfig.getString("bought_success").replace("%amount%",String.valueOf(amount))
                                        .replace("%price%",String.valueOf(price));
                                XSUtils.sendReplaceComponents(p,message);

                                if(XSHandlers.xsCasinoUser.containsKey(p.getUniqueId())) {
                                    XSUser xsUser = XSHandlers.xsCasinoUser.get(p.getUniqueId());
                                    if(xsUser.getLottery().containsKey(ticket)) {
                                        xsUser.getLottery().replace(ticket,
                                                xsUser.getLottery().get(ticket)+amount);
                                        //p.sendMessage("Buy Add: " + lines[1] + "  " + xsUser.getLottery().get(Integer.parseInt(lines[1])));
                                    } else {
                                        xsUser.getLottery().put(ticket,amount);
                                        //p.sendMessage("Buy: " + lines[1] + "  " + lines[2]);
                                    }
                                } else {
                                    XSUser xsUser = new XSUser(p);
                                    xsUser.createUser();
                                    xsUser.getLottery().put(ticket,amount);
                                    XSHandlers.xsCasinoUser.put(p.getUniqueId(),xsUser);
                                    // p.sendMessage("Buy New: " + lines[1] + "  " + lines[2]);
                                }

                                if(XSHandlers.getUsingRedis()) {
                                    XSHandlers.sendDataObjectRedis("XSCasinoRedisData/XSLottery/"+ XSHandlers.getHostCrossServer() + "/" + XSHandlers.getLocalRedis(),ticket + ":" + amount);

                                } else {
                                    XSHandlers.XSLottery.addPotPrize(amount);
                                    XSHandlers.XSLottery.setAmountTicket(XSHandlers.XSLottery.getAmountTicket()+amount);
                                }
                                if(XSHandlers.XSLottery.getLotteryList().containsKey(ticket)) {
                                    XSHandlers.XSLottery.getLotteryList().replace(ticket,
                                            XSHandlers.XSLottery.getLotteryList().get(ticket)+amount);
                                } else {
                                    XSHandlers.XSLottery.getLotteryList().put(ticket,amount);
                                }
                                if(XSHandlers.XSLottery.getXsLotteryUserOpenUI().containsKey(p.getUniqueId())) {
                                    XSHandlers.XSLottery.getXsLotteryUserOpenUI().remove(p.getUniqueId());
                                }

                            }
                            return null;
                        }).open(p);
            }

            e.setCancelled(true);
        }

    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        Player p = (Player) e.getPlayer();

        if(e.getView().getTitle().equalsIgnoreCase(XSHandlers.XSLottery.getTitle())) {
            if (XSHandlers.XSLottery.getXsLotteryUserOpenUI().containsKey(p.getUniqueId())) {
                XSHandlers.XSLottery.getXsLotteryUserOpenUI().remove(p.getUniqueId());
            }
        }
    }

}

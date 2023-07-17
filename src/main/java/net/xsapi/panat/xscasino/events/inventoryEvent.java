package net.xsapi.panat.xscasino.events;

import de.rapha149.signgui.SignGUI;
import net.xsapi.panat.xscasino.configuration.messages;
import net.xsapi.panat.xscasino.gui.ui_topticket_lottery;
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


public class inventoryEvent implements Listener {

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
            }
            else if(XSHandlers.XSLottery.getCustomConfig().getStringList("contents.buy_ticket.slots").contains(String.valueOf(e.getSlot()))) {
                p.closeInventory();
                new SignGUI()
                        .lines("§6พิมพ์เลข/จำนวน", "69", "1", "§6^^^^^^^^^^^")
                        .type(Material.DARK_OAK_SIGN)
                        .color(DyeColor.YELLOW)
                        .stripColor()
                        .onFinish((lines) -> {
                            if (!lines[1].isEmpty() && !lines[2].isEmpty()) {
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

                                XSHandlers.XSLottery.addPotPrize(amount);
                                XSHandlers.XSLottery.setAmountTicket(XSHandlers.XSLottery.getAmountTicket()+amount);

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

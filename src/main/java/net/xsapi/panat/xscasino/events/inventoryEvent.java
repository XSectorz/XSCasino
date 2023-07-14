package net.xsapi.panat.xscasino.events;

import de.rapha149.signgui.SignGUI;
import net.xsapi.panat.xscasino.handlers.XSHandlers;
import net.xsapi.panat.xscasino.user.XSUser;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;


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
            if(XSHandlers.XSLottery.getCustomConfig().getStringList("contents.buy_ticket.slots").contains(String.valueOf(e.getSlot()))) {

                new SignGUI()
                        .lines("§6พิมพ์เลข/จำนวน", "69", "1", "§6^^^^^^^^^^^")
                        .type(Material.DARK_OAK_SIGN)
                        .color(DyeColor.YELLOW)
                        .stripColor()
                        .onFinish((lines) -> {
                            if (!lines[1].isEmpty() && !lines[2].isEmpty()) {
                                int ticket = Integer.parseInt(lines[1]);
                                int amount = Integer.parseInt(lines[2]);
                                if(XSHandlers.xsCasinoUser.containsKey(p.getUniqueId())) {
                                    XSUser xsUser = XSHandlers.xsCasinoUser.get(p.getUniqueId());
                                    if(xsUser.getLottery().containsKey(ticket)) {
                                        xsUser.getLottery().replace(ticket,
                                                xsUser.getLottery().get(ticket)+amount);
                                        p.sendMessage("Buy Add: " + lines[1] + "  " + xsUser.getLottery().get(Integer.parseInt(lines[1])));
                                    } else {
                                        xsUser.getLottery().put(ticket,amount);
                                        p.sendMessage("Buy: " + lines[1] + "  " + lines[2]);
                                    }
                                } else {
                                    XSUser xsUser = new XSUser(p);
                                    xsUser.createUser();
                                    xsUser.getLottery().put(ticket,amount);
                                    XSHandlers.xsCasinoUser.put(p.getUniqueId(),xsUser);
                                    p.sendMessage("Buy New: " + lines[1] + "  " + lines[2]);
                                }

                                if(XSHandlers.XSLottery.getLotteryList().containsKey(ticket)) {
                                    XSHandlers.XSLottery.getLotteryList().replace(ticket,
                                            XSHandlers.XSLottery.getLotteryList().get(ticket)+amount);
                                } else {
                                    XSHandlers.XSLottery.getLotteryList().put(ticket,amount);
                                }

                            }
                            return null;
                        }).open(p);
            }

            e.setCancelled(true);
        }

    }

}

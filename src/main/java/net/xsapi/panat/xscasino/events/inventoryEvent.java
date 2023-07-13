package net.xsapi.panat.xscasino.events;

import de.rapha149.signgui.SignGUI;
import net.xsapi.panat.xscasino.handlers.XSHandlers;
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
                                p.sendMessage("Line: " + lines[1] + "  " + lines[2]);
                            }
                            return null;
                        }).open(p);
            }

            e.setCancelled(true);
        }

    }

}

package net.xsapi.panat.xscasino.gui;

import de.rapha149.signgui.SignGUI;
import net.xsapi.panat.xscasino.configuration.tokenConfig;
import net.xsapi.panat.xscasino.handlers.XSHandlers;
import net.xsapi.panat.xscasino.handlers.XSUtils;
import net.xsapi.panat.xscasino.modules.token;
import net.xsapi.panat.xscasino.types.TokenType;
import net.xsapi.panat.xscasino.user.XSUser;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Objects;

public class ui_main_token implements Listener {

    public static void openTokenMenu(Player p) {
        Inventory inv = Bukkit.createInventory(null, tokenConfig.customConfig.getInt("configuration.inventorySize"),
                tokenConfig.customConfig.getString("configuration.title").replace("&","§"));

        Material mat = Material.getMaterial(Objects.requireNonNull(tokenConfig.customConfig.getString("background_contents.Material")));
        int amount = tokenConfig.customConfig.getInt("background_contents.amount");
        int modelData = tokenConfig.customConfig.getInt("background_contents.customModelData");
        String display = tokenConfig.customConfig.getString("background_contents.display");
        ArrayList<String> lores = (ArrayList<String>) tokenConfig.customConfig.getStringList("background_contents.lore");

        ItemStack it = XSUtils.createItemStack(mat,amount,modelData,display,lores);
        for(String i : tokenConfig.customConfig.getStringList("configuration.backgroundSlot")) {
            inv.setItem(Integer.parseInt(i),it);
        }
        setupContents(inv);
        p.openInventory(inv);

    }

    public static void setupContents(Inventory inv) {
        for(String contents : tokenConfig.customConfig.getConfigurationSection("contents").getKeys(false)) {
            Material mat = Material.getMaterial(tokenConfig.customConfig.getString("contents."
                    + contents + ".Material"));
            int amount = tokenConfig.customConfig.getInt("contents."  + contents + ".amount");
            int modelData = tokenConfig.customConfig.getInt("contents."  + contents + ".customModelData");
            String display = tokenConfig.customConfig.getString("contents." + contents + ".display");
            ArrayList<String> lores = (ArrayList<String>) tokenConfig.customConfig.getStringList("contents." + contents + ".lore");
            ArrayList<String> slots = (ArrayList<String>) tokenConfig.customConfig.getStringList("contents." + contents + ".slots");

            ItemStack it = XSUtils.createItemStack(mat,amount,modelData,display,lores);
            for(int i = 0 ; i < slots.size() ; i++) {
                inv.setItem(Integer.parseInt(slots.get(i)),it);
            }

        }
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        Player p = (Player) e.getWhoClicked();

        if(e.getView().getTitle().equalsIgnoreCase(tokenConfig.customConfig.getString("configuration.title").replace("&","§"))) {

            XSUser xsUser = XSHandlers.xsCasinoUser.get(p.getUniqueId());

            e.setCancelled(true);

            boolean isClickToken = false;
            ClickType clickType = e.getClick();
            String showType = "";
            long basePrice;

            if(tokenConfig.customConfig.getStringList("contents.close.slots").contains(String.valueOf(e.getSlot()))) {
                basePrice = 0;
                p.closeInventory();
            } else if(tokenConfig.customConfig.getStringList("contents.buy_token_100.slots").contains(String.valueOf(e.getSlot()))) {
                xsUser.setTokenType(TokenType.TOKEN_100);
                basePrice = 100;
                isClickToken = true;
            } else if(tokenConfig.customConfig.getStringList("contents.buy_token_1000.slots").contains(String.valueOf(e.getSlot()))) {
                xsUser.setTokenType(TokenType.TOKEN_1000);
                basePrice = 1000;
                isClickToken = true;
            } else if(tokenConfig.customConfig.getStringList("contents.buy_token_10000.slots").contains(String.valueOf(e.getSlot()))) {
                xsUser.setTokenType(TokenType.TOKEN_10000);
                basePrice = 10000;
                isClickToken = true;
            } else {
                basePrice = 0;
            }

            if(isClickToken) {
                p.closeInventory();
                if(clickType.equals(ClickType.LEFT)) {
                    showType = "§6พิมพ์จำนวนที่ต้องการซื้อ";
                } else  {
                    showType = "§6พิมพ์จำนวนที่ต้องการขาย";
                }
                new SignGUI()
                        .lines(showType, "§6---------------", "1", "§6---------------")
                        .type(Material.DARK_OAK_SIGN)
                        .color(DyeColor.YELLOW)
                        .stripColor()
                        .onFinish((lines) -> {
                            if (!lines[2].isEmpty()) {
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
                                ItemStack it = null;
                                switch (xsUser.getTokenType()) {
                                    case TOKEN_100:
                                        it = token.getTokenList().get("token_100");
                                        break;
                                    case TOKEN_1000:
                                        it = token.getTokenList().get("token_1000");
                                        break;
                                    case TOKEN_10000:
                                        it = token.getTokenList().get("token_10000");
                                        break;
                                    default:
                                        break;
                                }

                                if(clickType.equals(ClickType.LEFT)) {
                                    long price = (long) (amount*basePrice);
                                    if(XSHandlers.getEconomy().getBalance(p) < price) {
                                        XSUtils.sendMessages(p,"cant_afford");
                                        return null;
                                    }
                                    XSHandlers.getEconomy().withdrawPlayer(p,(double) price);

                                  //  Bukkit.broadcastMessage("Token" + it.getItemMeta().getDisplayName());

                                    it.setAmount(amount);
                                    p.getInventory().addItem(it);
                                    XSUtils.sendMessages(p,"token_buy_success");
                                } else if(clickType.equals(ClickType.RIGHT)) {

                                    int have = 0;
                                    int needTosell = amount;
                                    for(ItemStack itemCheck : p.getInventory().getContents()) {
                                        if(itemCheck == null || itemCheck.getType().equals(Material.AIR)) {
                                            continue;
                                        }
                                        if(it.hasItemMeta() && itemCheck.hasItemMeta()) {
                                            if(it.getItemMeta().hasDisplayName() && itemCheck.getItemMeta().hasDisplayName()) {
                                                if(it.getItemMeta().getDisplayName().equalsIgnoreCase(itemCheck.getItemMeta().getDisplayName())) {
                                                    have += itemCheck.getAmount();
                                                }
                                            }
                                        }
                                    }

                                    if(have < amount) {
                                        XSUtils.sendMessages(p,"token_sell_not_enough");
                                        return null;
                                    }

                                    for(ItemStack itemCheck : p.getInventory().getContents()) {
                                        if(itemCheck == null || itemCheck.getType().equals(Material.AIR)) {
                                            continue;
                                        }
                                        if(it.hasItemMeta() && itemCheck.hasItemMeta()) {
                                            if(it.getItemMeta().hasDisplayName() && itemCheck.getItemMeta().hasDisplayName()) {
                                                if(it.getItemMeta().getDisplayName().equalsIgnoreCase(itemCheck.getItemMeta().getDisplayName())) {

                                                    int preAmount = itemCheck.getAmount();
                                                    int newAmount = Math.max(0, preAmount - amount);
                                                    amount = Math.max(0, amount - preAmount);
                                                    itemCheck.setAmount(newAmount);
                                                    if(amount == 0) {
                                                        break;
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    long price = (long) (needTosell*(basePrice*0.95));
                                    XSHandlers.getEconomy().depositPlayer(p,(double) price);

                                    XSUtils.sendMessages(p,"token_sell_success");

                                }

                            }
                            return null;
                        }).open(p);
            }
        }
    }
}

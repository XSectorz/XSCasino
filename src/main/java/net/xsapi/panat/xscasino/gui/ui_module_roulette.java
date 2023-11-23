package net.xsapi.panat.xscasino.gui;

import net.xsapi.panat.xscasino.configuration.rouletteConfig;
import net.xsapi.panat.xscasino.core.XSCasino;
import net.xsapi.panat.xscasino.handlers.XSHandlers;
import net.xsapi.panat.xscasino.handlers.XSUtils;
import net.xsapi.panat.xscasino.modules.token;
import net.xsapi.panat.xscasino.types.RouletteType;
import net.xsapi.panat.xscasino.user.XSUser;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

public class ui_module_roulette implements Listener {

    public static void openRoulette(Player p) {
        Inventory inv = Bukkit.createInventory(null, XSHandlers.XSRoullete.getCustomConfig().getInt("configuration.inventorySize"),
                XSHandlers.XSRoullete.getCustomConfig().getString("configuration.title").replace("&", "§"));

        Material mat = Material.getMaterial(Objects.requireNonNull(XSHandlers.XSRoullete.getCustomConfig().getString("background_contents.Material")));
        int amount = XSHandlers.XSRoullete.getCustomConfig().getInt("background_contents.amount");
        int modelData = XSHandlers.XSRoullete.getCustomConfig().getInt("background_contents.customModelData");
        String display = XSHandlers.XSRoullete.getCustomConfig().getString("background_contents.display");
        ArrayList<String> lores = (ArrayList<String>) XSHandlers.XSRoullete.getCustomConfig().getStringList("background_contents.lore");

        ItemStack it = XSUtils.createItemStack(mat, amount, modelData, display, lores);
        for (String i : XSHandlers.XSRoullete.getCustomConfig().getStringList("configuration.backgroundSlot")) {
            inv.setItem(Integer.parseInt(i), it);
        }

        XSUser xsUser = XSHandlers.xsCasinoUser.get(p.getUniqueId());
        xsUser.setRouletteType(RouletteType.NONE);

        if(!XSHandlers.XSRoullete.getXsRouletteOpenUI().containsKey(p.getUniqueId())) {
            XSHandlers.XSRoullete.getXsRouletteOpenUI().put(p.getUniqueId(),inv);
        }

        setupContents(inv);
        updateInventory(p);
        p.openInventory(inv);
    }

    public static void setupContents(Inventory inv) {
        for (String contents : XSHandlers.XSRoullete.getCustomConfig().getConfigurationSection("contents").getKeys(false)) {
            Material mat = Material.getMaterial(XSHandlers.XSRoullete.getCustomConfig().getString("contents."
                    + contents + ".Material"));
            int amount = XSHandlers.XSRoullete.getCustomConfig().getInt("contents." + contents + ".amount");
            int modelData = XSHandlers.XSRoullete.getCustomConfig().getInt("contents." + contents + ".customModelData");
            String display = XSHandlers.XSRoullete.getCustomConfig().getString("contents." + contents + ".display");
            ArrayList<String> lores = (ArrayList<String>) XSHandlers.XSRoullete.getCustomConfig().getStringList("contents." + contents + ".lore");
            ArrayList<String> slots = (ArrayList<String>) XSHandlers.XSRoullete.getCustomConfig().getStringList("contents." + contents + ".slots");

            ItemStack it = XSUtils.createItemStack(mat, amount, modelData, display, lores);
            for (int i = 0; i < slots.size(); i++) {
                inv.setItem(Integer.parseInt(slots.get(i)), it);
            }

        }

        for (String item : XSHandlers.XSRoullete.getCustomConfig().getConfigurationSection("module_configuration.randSlot").getKeys(false)) {
            Material mat =  Material.getMaterial(XSHandlers.XSRoullete.getCustomConfig().getString("module_configuration.randSlot." + item + ".material"));
            String display = XSHandlers.XSRoullete.getCustomConfig().getString("module_configuration.randSlot." + item + ".color") + "x" + XSHandlers.XSRoullete.getCustomConfig().getInt("module_configuration.randSlot." + item + ".multiple");

            ItemStack it = XSUtils.createItemStack(mat,1,0,display,new ArrayList<>());

            for(String slot : XSHandlers.XSRoullete.getCustomConfig().getStringList("module_configuration.randSlot." + item + ".slots")) {
                inv.setItem(Integer.parseInt(slot),it);
            }
        }

    }

    public static void updateInventory(Player p) {
        Inventory inv = XSHandlers.XSRoullete.getXsRouletteOpenUI().get(p.getUniqueId());
        updateInventoryContents(inv,p);
        p.updateInventory();
    }

    public static void updateInventoryContents(Inventory inv,Player p) {

        XSUser xsUser = XSHandlers.xsCasinoUser.get(p.getUniqueId());
        RouletteType rouletteType = xsUser.getRouletteType();
        String displaySelect = XSHandlers.XSRoullete.getCustomConfig().getString("module_configuration.selected.name_selected");
        Material displayMat = Material.getMaterial(XSHandlers.XSRoullete.getCustomConfig().getString("module_configuration.selected.material_not_select"));
        int displayModel = XSHandlers.XSRoullete.getCustomConfig().getInt("module_configuration.selected.model_not_select");

        if(rouletteType.equals(RouletteType.RED)) {
            displaySelect = displaySelect.replace("{color}",XSHandlers.XSRoullete.getCustomConfig().getString("contents.red_item.display").replace("&","§"));
            displayMat = Material.RED_WOOL;
        } else if(rouletteType.equals(RouletteType.BLACK)) {
            displaySelect = displaySelect.replace("{color}",XSHandlers.XSRoullete.getCustomConfig().getString("contents.black_item.display").replace("&","§"));
            displayMat = Material.BLACK_WOOL;
        } else if(rouletteType.equals(RouletteType.GREEN)) {
            displaySelect = displaySelect.replace("{color}",XSHandlers.XSRoullete.getCustomConfig().getString("contents.green_item.display").replace("&","§"));
            displayMat = Material.GREEN_WOOL;
        } else {
            displaySelect = XSHandlers.XSRoullete.getCustomConfig().getString("module_configuration.selected.name_not_select");
        }

        for(String slot : XSHandlers.XSRoullete.getCustomConfig().getStringList("module_configuration.selected.slots")) {
            inv.setItem(Integer.parseInt(slot),XSUtils.createItemStack(displayMat,1,displayModel,displaySelect,new ArrayList<>()));
        }

    }

    @EventHandler
    public void onClickUI(InventoryClickEvent e) {

        Player p = (Player) e.getWhoClicked();

        if (e.getView().getTitle().equalsIgnoreCase(XSHandlers.XSRoullete.getTitle())) {

            if(e.getClickedInventory() == null) {
                return;
            }

            if (e.getRawSlot() < e.getView().getTopInventory().getSize()) {
                XSUser xsUser = XSHandlers.xsCasinoUser.get(p.getUniqueId());

                if(XSHandlers.XSRoullete.getCustomConfig().getStringList("contents.close.slots").contains(String.valueOf(e.getSlot()))) {
                    p.closeInventory();
                    return;
                } else if(XSHandlers.XSRoullete.getCustomConfig().getStringList("contents.red_item.slots").contains(String.valueOf(e.getSlot()))) {
                    xsUser.setRouletteType(RouletteType.RED);
                } else if(XSHandlers.XSRoullete.getCustomConfig().getStringList("contents.black_item.slots").contains(String.valueOf(e.getSlot()))) {
                    xsUser.setRouletteType(RouletteType.BLACK);
                } else if(XSHandlers.XSRoullete.getCustomConfig().getStringList("contents.green_item.slots").contains(String.valueOf(e.getSlot()))) {
                    xsUser.setRouletteType(RouletteType.GREEN);
                } else if(XSHandlers.XSRoullete.getCustomConfig().getStringList("contents.play.slots").contains(String.valueOf(e.getSlot()))) {

                    if(!xsUser.getRouletteType().equals(RouletteType.NONE)) {

                        boolean isTokenEmpty = true;

                        for(String slot : XSHandlers.XSRoullete.getCustomConfig().getStringList("module_configuration.tokenSlot.slots")) {
                            if(e.getView().getItem(Integer.parseInt(slot)) != null) {
                                isTokenEmpty = false;
                                XSHandlers.XSRoullete.getXsRouletteOpenUI().get(p.getUniqueId()).setItem(Integer.parseInt(slot),new ItemStack(Material.AIR));
                            }
                        }

                        if(!isTokenEmpty) {

                            if(!XSHandlers.XSRoullete.getPlayerStartRoulette().contains(p)) {
                                int randNum = (int) ((Math.random() * (40)) + 0);
                                xsUser.setCurrentRouletteCount(0);
                                xsUser.setMaxRouletteCount(1);
                                xsUser.setCurrentRouletteCheck(0);
                                xsUser.setMaxRouletteCheck(50+randNum);
                                xsUser.setRouletteUpdateCount(0);
                                xsUser.setRouletteMaxUpdateCount((int) (xsUser.getMaxRouletteCheck()/2.5));

                                XSHandlers.XSRoullete.getPlayerStartRoulette().add(p);
                                p.sendMessage("Play...");
                            } else {
                                p.sendMessage("You currently play!");
                            }
                        } else {
                            p.sendMessage("Token Must not empty!");
                        }

                    } else {
                        p.sendMessage("Please Select Color");
                    }


                } else if(XSHandlers.XSRoullete.getCustomConfig().getStringList("module_configuration.tokenSlot.slots").contains(String.valueOf(e.getSlot()))) {
                    return;
                }
                updateInventory(p);
                e.setCancelled(true);
            } else {

                ItemStack it = e.getCurrentItem();

                boolean isContain = false;

                for(Map.Entry<String,ItemStack> tokens : token.getTokenList().entrySet()) {
                    ItemStack token = tokens.getValue();
                    if(it.getType().equals(token.getType())) {
                        if((token.hasItemMeta() && it.hasItemMeta()) || (!token.hasItemMeta() && !it.hasItemMeta())) {

                            if(token.hasItemMeta()) {
                                if(token.getItemMeta().hasDisplayName() && it.getItemMeta().hasDisplayName()) {
                                    if(token.getItemMeta().getDisplayName().equalsIgnoreCase(it.getItemMeta().getDisplayName())) { //Contain
                                        isContain = true;
                                        break;
                                    }
                                }
                            } else {
                                isContain = true;
                                break;
                            }
                        }
                    }
                }

                if(!isContain) {
                    e.setCancelled(true);
                }

            }


        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        Player p = (Player) e.getPlayer();

        if(e.getView().getTitle().equalsIgnoreCase(XSHandlers.XSRoullete.getTitle())) {

            if(XSHandlers.XSRoullete.getPlayerStartRoulette().contains(p)) {
                Bukkit.getScheduler().scheduleSyncDelayedTask(XSCasino.getPlugin(), new Runnable() {
                    @Override
                    public void run() {
                        p.sendMessage("Reopen...");
                        p.openInventory(XSHandlers.XSRoullete.getXsRouletteOpenUI().get(p.getUniqueId()));
                    }
                }, 2L);
            } else {
                p.sendMessage("Close...");
                if (XSHandlers.XSRoullete.getXsRouletteOpenUI().containsKey(p.getUniqueId())) {

                    for(String slot : XSHandlers.XSRoullete.getCustomConfig().getStringList("module_configuration.tokenSlot.slots")) {
                        if(e.getInventory().getItem(Integer.parseInt(slot)) != null) {
                            p.getInventory().addItem(e.getInventory().getItem(Integer.parseInt(slot)));
                        }
                    }

                    XSHandlers.XSRoullete.getXsRouletteOpenUI().remove(p.getUniqueId());
                }
            }

        }
    }
}
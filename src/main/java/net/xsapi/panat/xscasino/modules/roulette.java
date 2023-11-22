package net.xsapi.panat.xscasino.modules;

import net.xsapi.panat.xscasino.configuration.messages;
import net.xsapi.panat.xscasino.core.XSCasino;
import net.xsapi.panat.xscasino.handlers.XSHandlers;
import net.xsapi.panat.xscasino.handlers.XSUtils;
import net.xsapi.panat.xscasino.user.XSUser;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.*;

import static net.xsapi.panat.xscasino.gui.ui_main_lottery.updateInventory;

public class roulette extends XSCasinoTemplates{

    public HashMap<UUID, Inventory> xsRouletteOpenUI = new HashMap<>();

    public HashMap<UUID, Inventory> getXsRouletteOpenUI() {
        return xsRouletteOpenUI;
    }
    public ArrayList<Player> playerStartRoulette = new ArrayList<>();

    public ArrayList<Integer> listRandIndex = new ArrayList<>();
    public int winIndex = 0;

    public roulette(File customConfigFile, FileConfiguration customConfig) {
        setCustomConfigFile(customConfigFile);
        setCustomConfig(customConfig);

        setTitle(getCustomConfig().getString("configuration.title").replace("&","§"));
        setInvSize(getCustomConfig().getInt("configuration.inventorySize"));

        for(String index : getCustomConfig().getString("module_configuration.randOption.randIndex").split(",")) {
            listRandIndex.add(Integer.parseInt(index));
        }

        winIndex = getCustomConfig().getInt("module_configuration.randOption.winIndex");

        createTask();
    }

    public ArrayList<Player> getPlayerStartRoulette() {
        return playerStartRoulette;
    }

    public static void updateInventoryRoulette(Player p, HashMap<Integer,ItemStack> newItemInventory) {
        Inventory inv = XSHandlers.XSRoullete.getXsRouletteOpenUI().get(p.getUniqueId());

        for(Map.Entry<Integer,ItemStack> listItem : newItemInventory.entrySet()) {
            inv.setItem(listItem.getKey(),listItem.getValue());
        }

        p.updateInventory();
    }

    public void createTask() {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(XSCasino.getPlugin(), new Runnable() {
            @Override
            public void run() {
                Iterator<Player> iterator = getPlayerStartRoulette().iterator();
                while (iterator.hasNext()) {
                    Player p = iterator.next();

                    ArrayList<ItemStack> currentItemInventory = new ArrayList<>();
                    HashMap<Integer,ItemStack> newItemInventory = new HashMap<>();

                    XSUser xsUser = XSHandlers.xsCasinoUser.get(p.getUniqueId());

                    for(int slot : listRandIndex) {
                        currentItemInventory.add(XSHandlers.XSRoullete.getXsRouletteOpenUI().get(p.getUniqueId()).getItem(slot));
                    }

                    for(int i = 1 ; i < listRandIndex.size()+1 ; i++) {
                        if(i == listRandIndex.size()) {
                            newItemInventory.put(listRandIndex.get(0),currentItemInventory.get(i-1));
                        } else {
                            newItemInventory.put(listRandIndex.get(i),currentItemInventory.get(i-1));
                        }
                    }

                    xsUser.setCurrentRouletteCount(xsUser.getCurrentRouletteCount()+1);

                    if(xsUser.getCurrentRouletteCount() >= xsUser.getMaxRouletteCount()) {
                        p.sendMessage("Update...");
                        xsUser.setCurrentRouletteCount(0);
                        xsUser.setCurrentRouletteCheck(xsUser.getCurrentRouletteCheck()+1);
                        xsUser.setRouletteUpdateCount(xsUser.getRouletteUpdateCount()+1);
                        updateInventoryRoulette(p,newItemInventory);
                    }

                    if(xsUser.getRouletteUpdateCount() >= xsUser.getRouletteMaxUpdateCount()) { //Increase delay
                        xsUser.setMaxRouletteCount(xsUser.getMaxRouletteCount()+2);
                        //xsUser.setCurrentRouletteCheck(xsUser.getCurrentRouletteCheck()+1);
                        xsUser.setRouletteUpdateCount(0);
                        if(xsUser.getRouletteMaxUpdateCount() > 5) {
                            xsUser.setRouletteMaxUpdateCount(xsUser.getRouletteMaxUpdateCount()-5);
                            p.sendMessage("Increase delayed " + xsUser.getMaxRouletteCount());
                        }
                    }

                    if(xsUser.getCurrentRouletteCheck() >= xsUser.getMaxRouletteCheck()) { //End
                        p.sendMessage("Rand End");
                        iterator.remove();
                    }

                    //iterator.remove();

                }

            }
        }, 0L, 2L);
    }
}

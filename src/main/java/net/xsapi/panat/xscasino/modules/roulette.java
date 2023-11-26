package net.xsapi.panat.xscasino.modules;

import net.xsapi.panat.xscasino.configuration.messages;
import net.xsapi.panat.xscasino.core.XSCasino;
import net.xsapi.panat.xscasino.handlers.XSHandlers;
import net.xsapi.panat.xscasino.handlers.XSUtils;
import net.xsapi.panat.xscasino.types.RouletteType;
import net.xsapi.panat.xscasino.user.XSUser;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
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
              /*  Bukkit.broadcastMessage("Task New Data");
                for(Map.Entry<String,ItemStack> tokensL : token.getTokenList().entrySet()) {
                    Bukkit.broadcastMessage("Key: " + tokensL.getKey() + " val: " + tokensL.getValue().getType());
                }*/
                Iterator<Player> iterator = getPlayerStartRoulette().iterator();
                while (iterator.hasNext()) {
                    Player p = iterator.next();

                    if(!p.isOnline()) {
                        iterator.remove();
                        continue;
                    }

                    ArrayList<ItemStack> currentItemInventory = new ArrayList<>();

                    XSUser xsUser = XSHandlers.xsCasinoUser.get(p.getUniqueId());

                    for(int slot : listRandIndex) {
                        currentItemInventory.add(XSHandlers.XSRoullete.getXsRouletteOpenUI().get(p.getUniqueId()).getItem(slot));
                    }

                    xsUser.setCurrentRouletteCount(xsUser.getCurrentRouletteCount()+1);

                    if(xsUser.getCurrentRouletteCount() >= xsUser.getMaxRouletteCount()) {
                       // p.sendMessage("Update... " + xsUser.getCurrentRouletteCount());

                        for(int i = 1 ; i < listRandIndex.size()+1 ; i++) {
                            if(i == listRandIndex.size()) {
                                xsUser.getNewItemInventory().put(listRandIndex.get(0),currentItemInventory.get(i-1));
                            } else {
                                xsUser.getNewItemInventory().put(listRandIndex.get(i),currentItemInventory.get(i-1));
                            }
                        }

                        if(!xsUser.isUpdateRouletteUI()) {
                            xsUser.setCurrentRouletteCheck(xsUser.getCurrentRouletteCheck()+1);
                            xsUser.setRouletteUpdateCount(xsUser.getRouletteUpdateCount()+1);
                        }
                        xsUser.setCurrentRouletteCount(0);

                        updateInventoryRoulette(p,xsUser.getNewItemInventory());
                        p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK,5f,5f);
                    }

                    if(xsUser.getRouletteUpdateCount() >= xsUser.getRouletteMaxUpdateCount()) { //Increase delay
                        xsUser.setMaxRouletteCount(xsUser.getMaxRouletteCount()+2);
                        //xsUser.setCurrentRouletteCheck(xsUser.getCurrentRouletteCheck()+1);
                        xsUser.setRouletteUpdateCount(0);
                        if(xsUser.getRouletteMaxUpdateCount() > 5) {
                            xsUser.setRouletteMaxUpdateCount(xsUser.getRouletteMaxUpdateCount()-5);
                            //p.sendMessage("Increase delayed " + xsUser.getMaxRouletteCount());
                        }
                    }

                    if(xsUser.getCurrentRouletteCheck() >= xsUser.getMaxRouletteCheck() || xsUser.isUpdateRouletteUI()) { //End
                        //p.sendMessage("Rand End" + newItemInventory.get(winIndex).getType());

                        Material mat = Material.ACACIA_BOAT;
                        Material predictWinMat = Material.ACACIA_BOAT;

                        int multiple = 0;

                        if(xsUser.getRouletteType().equals(RouletteType.RED)) {
                            mat = Material.RED_WOOL;
                            multiple = 2;
                        } else if(xsUser.getRouletteType().equals(RouletteType.GREEN)) {
                            mat = Material.GREEN_WOOL;
                            multiple = 20;
                        } else if(xsUser.getRouletteType().equals(RouletteType.BLACK)) {
                            mat = Material.BLACK_WOOL;
                            multiple = 2;
                        }

                        if(xsUser.getPredictWinType().equals(RouletteType.RED)) {
                            predictWinMat = Material.RED_WOOL;
                        } else if(xsUser.getPredictWinType().equals(RouletteType.GREEN)) {
                            predictWinMat = Material.GREEN_WOOL;
                        } else if(xsUser.getPredictWinType().equals(RouletteType.BLACK)) {
                            predictWinMat = Material.BLACK_WOOL;
                        }

                        if(!xsUser.getNewItemInventory().get(winIndex).getType().equals(predictWinMat)) {
                            xsUser.setUpdateRouletteUI(true);
                        //    Bukkit.broadcastMessage("Not Match Continue.. " + xsUser.getNewItemInventory().get(winIndex).getType() + " | " + predictWinMat);
                            continue;
                        }

                   //     Bukkit.broadcastMessage("Match! Predict " + xsUser.getNewItemInventory().get(winIndex).getType() + " | " + predictWinMat);

                        if(xsUser.getNewItemInventory().get(winIndex).getType().equals(mat)) {
                          /*  Bukkit.broadcastMessage("-----------------");
                            for(Map.Entry<String,Integer> listData : xsUser.getUseToken().entrySet()) {
                                Bukkit.broadcastMessage("Token: " + listData.getKey() + " use : " + listData.getValue());
                            }
                            Bukkit.broadcastMessage("-----------------");*/

                            for(Map.Entry<String,Integer> tokens : xsUser.getUseToken().entrySet()) {
                                ItemStack tokenUse = XSUtils.itemStackFromBase64(token.getTokenList().get(tokens.getKey()));

                                tokenUse.setAmount(xsUser.getUseToken().get(tokens.getKey())*multiple);
                               // p.sendMessage("Send : " + tokens.getKey() + " amt: " + tokenUse.getAmount());
                                p.getInventory().addItem(tokenUse);

                            }

                            p.playSound(p.getLocation(),Sound.ENTITY_PLAYER_LEVELUP,5f,2f);
                            XSUtils.sendMessages(p,"roulette_win");

                           /* for(Map.Entry<String,ItemStack> tokensL : token.getTokenList().entrySet()) {
                                Bukkit.broadcastMessage("Key: " + tokensL.getKey() + " val: " + tokensL.getValue().getType());
                            }*/
                        } else {
                            p.playSound(p.getLocation(),Sound.ENTITY_WITHER_DEATH,5f,2f);
                            XSUtils.sendMessages(p,"roulette_lose");

                           /* for(Map.Entry<String,ItemStack> tokensL : token.getTokenList().entrySet()) {
                                Bukkit.broadcastMessage("Key: " + tokensL.getKey() + " val: " + tokensL.getValue().getType());
                            }*/
                        }
                        xsUser.getUseToken().clear();
                        iterator.remove();
                    }

                    //iterator.remove();

                }

            }
        }, 0L, 2L);
    }
}

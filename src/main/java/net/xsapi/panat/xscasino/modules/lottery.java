package net.xsapi.panat.xscasino.modules;

import net.xsapi.panat.xscasino.configuration.messages;
import net.xsapi.panat.xscasino.core.XSCasino;
import net.xsapi.panat.xscasino.handlers.XSHandlers;
import net.xsapi.panat.xscasino.handlers.XSUtils;
import net.xsapi.panat.xscasino.user.XSUser;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.io.File;
import java.util.*;

import static net.xsapi.panat.xscasino.gui.ui_main_lottery.updateInventory;

public class lottery extends XSCasinoTemplates {

    public int invSize;
    public String title;
    public HashMap<Integer,Integer> lotteryList = new HashMap<>();
    public HashMap<UUID, Inventory> xsLotteryUserOpenUI = new HashMap<>();
    public double priceTicket;
    public long potPrize;
    public long potExtra;
    public int amountTicket;
    public int prizeTime;
    public long nextPrizeTime;

    /* Inventory */
    public String topTicketTitle;
    public int topTicketSize;
    public String myTicketTitle;
    public int myTicketSize;
    public int myTicketNextSlot;
    public int myTicketPrevSlot;

    public String winner;
    public int ticketWinNum;
    public int numberTicketWin;
    public int totalWinPrize;

    public lottery(File customConfigFile, FileConfiguration customConfig) {
        setCustomConfigFile(customConfigFile);
        setCustomConfig(customConfig);

        setTitle(getCustomConfig().getString("configuration.title").replace("&","§"));
        setTopTicketTitle(getCustomConfig().getString("topTicket_configuration.title").replace("&","§"));
        setMyTicketTitle(getCustomConfig().getString("myTicket_configuration.title").replace("&","§"));
        setInvSize(getCustomConfig().getInt("configuration.inventorySize"));
        setTopTicketSize(getCustomConfig().getInt("topTicket_configuration.inventorySize"));
        setMyTicketSize(getCustomConfig().getInt("myTicket_configuration.inventorySize"));
        setPriceTicket(getCustomConfig().getDouble("configuration.price_per_ticket"));
        setPotExtra(getCustomConfig().getLong("configuration.pot_extra"));
        setPotPrize(getCustomConfig().getLong("configuration.start_pot"));
        setPrizeTime(getCustomConfig().getInt("configuration.prize_time"));

        setMyTicketPrevSlot(getCustomConfig().getInt("myTicket_configuration.prevSlot"));
        setMyTicketNextSlot(getCustomConfig().getInt("myTicket_configuration.nextSlot"));

        if(getCustomConfig().get("data.next_prize_time") == null) {
            setNextPrizeTime(System.currentTimeMillis() + (getPrizeTime()*1000L));
        } else {
            setNextPrizeTime(getCustomConfig().getLong("data.next_prize_time"));
        }

        if(getCustomConfig().get("data.winner") == null) {
            setWinner("");
            setTicketWinNum(-1);
            setNumberTicketWin(-1);
            setTotalWinPrize(-1);
        } else {
            setWinner(getCustomConfig().getString("data.winner.name"));
            setTicketWinNum(getCustomConfig().getInt("data.winner.numberTicket"));
            setNumberTicketWin(getCustomConfig().getInt("data.winner.number"));
            setTotalWinPrize(getCustomConfig().getInt("data.winner.totalPrize"));
        }

        int currentAmt = 0;
        for (String lottery : getCustomConfig().getStringList("data.lottery_list")) {
            int key = Integer.parseInt(lottery.split(":")[0]);
            int amount = Integer.parseInt(lottery.split(":")[1]);
            lotteryList.put(key,amount);
            currentAmt += amount;
        }

        setAmountTicket(currentAmt);

        setPotPrize((long) (getPotPrize() + currentAmt*getPotExtra()));
        createTask();
        loadUser();
    }

    public int getMyTicketNextSlot() {
        return myTicketNextSlot;
    }

    public int getMyTicketPrevSlot() {
        return myTicketPrevSlot;
    }

    public void setMyTicketNextSlot(int myTicketNextSlot) {
        this.myTicketNextSlot = myTicketNextSlot;
    }

    public void setMyTicketPrevSlot(int myTicketPrevSlot) {
        this.myTicketPrevSlot = myTicketPrevSlot;
    }

    public void setMyTicketSize(int myTicketSize) {
        this.myTicketSize = myTicketSize;
    }

    public void setMyTicketTitle(String myTicketTitle) {
        this.myTicketTitle = myTicketTitle;
    }

    public int getMyTicketSize() {
        return myTicketSize;
    }

    public String getMyTicketTitle() {
        return myTicketTitle;
    }

    public int getNumberTicketWin() {
        return numberTicketWin;
    }

    public void setNumberTicketWin(int numberTicketWin) {
        this.numberTicketWin = numberTicketWin;
    }

    public void setTotalWinPrize(int totalWinPrize) {
        this.totalWinPrize = totalWinPrize;
    }

    public int getTotalWinPrize() {
        return totalWinPrize;
    }

    public void setTicketWinNum(int ticketWinNum) {
        this.ticketWinNum = ticketWinNum;
    }

    public int getTicketWinNum() {
        return ticketWinNum;
    }

    public String getWinner() {
        return winner;
    }

    public void setWinner(String winner) {
        this.winner = winner;
    }

    public void setTopTicketSize(int topTicketSize) {
        this.topTicketSize = topTicketSize;
    }

    public void setTopTicketTitle(String topTicketTitle) {
        this.topTicketTitle = topTicketTitle;
    }

    public int getTopTicketSize() {
        return topTicketSize;
    }

    public String getTopTicketTitle() {
        return topTicketTitle;
    }

    public void createTask() {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(XSCasino.getPlugin(), new Runnable() {
            @Override
            public void run() {
                //Bukkit.broadcastMessage("CURRENT: " + System.currentTimeMillis() + " NEXT: " + getNextPrizeTime() + " = " + (System.currentTimeMillis() - getNextPrizeTime()));
                if(System.currentTimeMillis() - getNextPrizeTime() >= 0L) {
                    setNextPrizeTime(System.currentTimeMillis() + (getPrizeTime()*1000L));

                    int prizeNum = generatePrizeNumber();
                    String str = String.valueOf(prizeNum);

                    if(str.length() == 1) {
                        str = ("0" + str);
                    }

                   // Bukkit.broadcastMessage("Lottery Prize Out! " + str);
                    for(Player p : Bukkit.getOnlinePlayers()) {
                        String winMsg = messages.customConfig.getString("lottery_prize_annoucement")
                                .replace("%number%",str).replace("%prize%",String.valueOf(getPotPrize()));

                        XSUtils.sendReplaceComponents(p,winMsg);
                    }
                    sendReward(prizeNum);
                }
                for(Map.Entry<UUID,Inventory> playerOpen : xsLotteryUserOpenUI.entrySet()) {
                    updateInventory(Bukkit.getPlayer(playerOpen.getKey()));
                }
            }
        }, 0L, 20L);

    }

    public void loadUser() {
        for(Player p : Bukkit.getOnlinePlayers()) {
            File pFile = new File(XSCasino.getPlugin().getDataFolder() + "/data", p.getUniqueId() + ".yml");

            if(pFile.exists()) {
                XSUser xsUser = new XSUser(p);

                if(xsUser.getUserConfig().get("modules.lottery.data") != null) {
                    xsUser.loadUserData();
                }
                XSHandlers.xsCasinoUser.put(p.getUniqueId(),xsUser);
            }
        }
        //Bukkit.broadcastMessage("-------------------");
        //for(Map.Entry<UUID,XSUser> xsPlayer : XSHandlers.xsCasinoUser.entrySet()) {
        //    Bukkit.broadcastMessage("UUID: " + xsPlayer.getKey());
        //}
        //Bukkit.broadcastMessage("-------------------");
    }

    public void sendReward(int prizeNum) {
        prizeNum = 70;
        //Bukkit.broadcastMessage("STARTING CHECK");
        HashMap<UUID,Integer> lotteryWinner = new HashMap<>();
        File[] allData = new File(XSCasino.getPlugin().getDataFolder() + "/data").listFiles();

        int amountWinticket = 0;

        String winnerName = "";
        int MaxAmountOfTicket = 0;
      //  Bukkit.broadcastMessage("-------------------");
      //  for(Map.Entry<UUID,XSUser> xsPlayer : XSHandlers.xsCasinoUser.entrySet()) {
      //     Bukkit.broadcastMessage("UUID: " + xsPlayer.getKey());
      //      for(Map.Entry<Integer,Integer> ticket : xsPlayer.getValue().getLottery().entrySet()) {
      //          Bukkit.broadcastMessage("TICKET : " + ticket.getKey() + " x" + ticket.getValue());
      //      }
      //  }
      //  Bukkit.broadcastMessage("-------------------");
        if(allData != null) {
            for (File file : allData) {
                if(file.getName().endsWith(".yml")) {
                    FileConfiguration fileConfig = YamlConfiguration.loadConfiguration(file);
                    UUID uuid = UUID.fromString(file.getName().replace(".yml",""));
                  //  Bukkit.broadcastMessage("FILE_NAME: " + uuid.toString());
                    XSUser xsUser = null;
                    if(XSHandlers.xsCasinoUser.containsKey(uuid)) {

                        xsUser = XSHandlers.xsCasinoUser.get(uuid);
                        ArrayList<String> lotteryList = new ArrayList<>();
                        if(xsUser.getLottery().size() != 0) {
                           // Bukkit.broadcastMessage("LOOP: " + xsUser.getLottery().size());
                            for(Map.Entry<Integer,Integer> lottery : xsUser.getLottery().entrySet()) {
                                lotteryList.add(lottery.getKey()+":"+lottery.getValue());
                            }
                            xsUser.getUserConfig().set("modules.lottery.data",lotteryList);
                            xsUser.saveData();
                        } else {
                            //Bukkit.broadcastMessage("ZERO TICKET");
                        }

                        file = xsUser.getUserFile();
                        fileConfig = xsUser.getUserConfig();
                    } else {
                        //Bukkit.broadcastMessage("NULL");
                    }

                    if(fileConfig.get("modules.lottery.data") != null) {
                        //Bukkit.broadcastMessage("NOT NULL -> " + fileConfig.getStringList("modules.lottery.data").size());
                        for(String lottery : fileConfig.getStringList("modules.lottery.data")) {
                            int ticket = Integer.parseInt(lottery.split(":")[0]);
                            int amount = Integer.parseInt(lottery.split(":")[1]);
                            //Bukkit.broadcastMessage("TICKET: " + ticket + " | " + prizeNum);
                            if(ticket == prizeNum) {
                               // Bukkit.broadcastMessage("ADD!");
                                lotteryWinner.put(uuid,amount);
                                amountWinticket += amount;

                                if(amount > MaxAmountOfTicket) {
                                    MaxAmountOfTicket = amount;
                                    winnerName = fileConfig.getString("AccountName");
                                }
                            }

                            if(XSHandlers.xsCasinoUser.containsKey(uuid)) {
                                xsUser.getUserConfig().set("modules.lottery.data",new ArrayList<>());
                                xsUser.saveData();
                                xsUser.getLottery().clear();
                            } else {
                                fileConfig.set("modules.lottery.data",new ArrayList<>());
                                try {
                                    fileConfig.save(file);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                    //Bukkit.broadcastMessage("------------");
                }
            }
        }

        setWinner(winnerName);
        setNumberTicketWin(amountWinticket);
        setTicketWinNum(prizeNum);
        setTotalWinPrize((int) getPotPrize());
        String winMsg = "";
        if(winnerName.isEmpty()) {
            winMsg = messages.customConfig.getString("lottery_prize_win")
                    .replace("%player_winner%",messages.customConfig.getString("win_condition.no_data"));
        } else {
            winMsg = messages.customConfig.getString("lottery_prize_win")
                    .replace("%player_winner%",winnerName);
        }

        for(Player p : Bukkit.getOnlinePlayers()) {
            XSUtils.sendReplaceComponents(p,winMsg);
        }

        for (Map.Entry<UUID,Integer> winner : lotteryWinner.entrySet()) {
            double prizePool = (double) winner.getValue()/amountWinticket;
            double reward =  (getPotPrize()*prizePool);
          //  Bukkit.broadcastMessage("WINNER: " + winner.getKey() + " POOL " + prizePool + " | " + reward);
          //  Bukkit.broadcastMessage("TICKET ALL : " + amountWinticket + " / "  + winner.getValue());

            if(Bukkit.getPlayer(winner.getKey()) == null) { //offline
                OfflinePlayer offPlayer = Bukkit.getOfflinePlayer(winner.getKey());

                File file = new File(XSCasino.getPlugin().getDataFolder() + "/data", winner.getKey() + ".yml");
                FileConfiguration fileConfig = YamlConfiguration.loadConfiguration(file);

                if(XSHandlers.getPerms().playerHas(Bukkit.getWorlds().get(0).getName(),offPlayer,"xsapi.casino.offlineReward")) {
               //     Bukkit.broadcastMessage("ADD REWARD SUCCESS " + fileConfig.getString("AccountName"));
                    XSHandlers.getEconomy().depositPlayer(Bukkit.getOfflinePlayer(winner.getKey()),reward);
                }
            } else {
               // Bukkit.broadcastMessage("ADD REWARD NORMAL SUCCESS " + Bukkit.getPlayer(winner.getKey()).getName());
                XSHandlers.getEconomy().depositPlayer(Bukkit.getPlayer(winner.getKey()),reward);
            }

        }
      //  Bukkit.broadcastMessage("-----------------------------");

        clearLotteryData();
    }

    public HashMap<UUID, Inventory> getXsLotteryUserOpenUI() {
        return xsLotteryUserOpenUI;
    }


    public void clearLotteryData() {
        this.setAmountTicket(0);
        this.setPotPrize(getCustomConfig().getLong("configuration.start_pot"));
        this.getLotteryList().clear();
    }

    public int generatePrizeNumber() {
        Random r = new Random();
        int rand = r.nextInt(99);
        return rand;
    }

    public void setNextPrizeTime(long nextPrizeTime) {
        this.nextPrizeTime = nextPrizeTime;
    }

    public long getNextPrizeTime() {
        return nextPrizeTime;
    }

    public void setPrizeTime(int prizeTime) {
        this.prizeTime = prizeTime;
    }

    public int getPrizeTime() {
        return prizeTime;
    }

    public int getAmountTicket() {
        return amountTicket;
    }

    public void setAmountTicket(int amountTicket) {
        this.amountTicket = amountTicket;
    }

    public void addPotPrize(long amount) {
        this.potPrize = (long) (this.potPrize+(amount*this.potExtra));
    }

    public double getPotExtra() {
        return potExtra;
    }

    public long getPotPrize() {
        return potPrize;
    }

    public void setPotExtra(long potExtra) {
        this.potExtra = potExtra;
    }

    public void setPotPrize(long potPrize) {
        this.potPrize = potPrize;
    }

    public void setPriceTicket(double priceTicket) {
        this.priceTicket = priceTicket;
    }

    public double getPriceTicket() {
        return priceTicket;
    }

    public HashMap<Integer, Integer> getLotteryList() {
        return lotteryList;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setInvSize(int invSize) {
        this.invSize = invSize;
    }

    public int getInvSize() {
        return invSize;
    }

    public String getTitle() {
        return title;
    }

    public void saveData() {
        ArrayList<String> lotteryList = new ArrayList<>();
        for(Map.Entry<Integer,Integer> lottery : this.getLotteryList().entrySet()) {
            lotteryList.add(lottery.getKey()+":"+lottery.getValue());
        }
        this.getCustomConfig().set("data.lottery_list",lotteryList);
        this.getCustomConfig().set("data.next_prize_time",getNextPrizeTime());

        if(!getWinner().isEmpty()) {
            this.getCustomConfig().set("data.winner.name",getWinner());
            this.getCustomConfig().set("data.winner.number",getTicketWinNum());
            this.getCustomConfig().set("data.winner.numberTicket",getNumberTicketWin());
            this.getCustomConfig().set("data.winner.totalPrize",getTotalWinPrize());
        }


        try {
            getCustomConfig().save(getCustomConfigFile());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

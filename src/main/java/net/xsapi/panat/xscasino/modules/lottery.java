package net.xsapi.panat.xscasino.modules;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.xsapi.panat.xscasino.configuration.config;
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
import java.sql.*;
import java.time.Duration;
import java.time.LocalTime;
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
    public String prizeString;

    /* Inventory */
    public String topTicketTitle;
    public int topTicketSize;
    public String myTicketTitle;
    public int myTicketSize;

    /* lockPrizeNumber */
    public int LockPrize;
    public String setterName;

    public String winner;
    public int ticketWinNum;
    public int numberTicketWin;
    public int totalWinPrize;

    /* Redis Options */
    public boolean buyAble = false;

    public boolean isBuyAble() {
        return buyAble;
    }

    public void setBuyAble(boolean buyAble) {
        this.buyAble = buyAble;
    }

    public String getPrizeString() {
        return prizeString;
    }

    public void setPrizeString(String prizeString) {
        this.prizeString = prizeString;
    }

    public int getLockPrize() {
        return LockPrize;
    }

    public void setLockPrize(int lockPrize) {
        LockPrize = lockPrize;
    }

    public String getSetterName() {
        return setterName;
    }

    public void setSetterName(String setterName) {
        this.setterName = setterName;
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
        setPrizeString(getCustomConfig().getString("configuration.redis_timer"));

        if(XSHandlers.getUsingSQL()) {
            loadDataSQL(XSHandlers.getJDBC_URL(),XSHandlers.getUSER(),XSHandlers.getPASS());
        } else {
            int currentAmt = 0;
            if(getCustomConfig().get("data.next_prize_time") == null) {
                setNextPrizeTime(System.currentTimeMillis() + (getPrizeTime()*1000L));
            } else {
                setNextPrizeTime(getCustomConfig().getLong("data.next_prize_time"));
            }

            if(getCustomConfig().getInt("data.lockPrize.number") != -1) {
                setLockPrize(getCustomConfig().getInt("data.lockPrize.number"));
                setSetterName(getCustomConfig().getString("data.lockPrize.setter"));
            } else {
                setLockPrize(-1);
                setSetterName("");
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

            for (String lottery : getCustomConfig().getStringList("data.lottery_list")) {
                int key = Integer.parseInt(lottery.split(":")[0]);
                int amount = Integer.parseInt(lottery.split(":")[1]);
                lotteryList.put(key,amount);
                currentAmt += amount;
            }
            setAmountTicket(currentAmt);
        }

        if(!XSHandlers.getUsingRedis()) {
            setBuyAble(true);
        }


        setPotPrize((long) (getPotPrize() + getAmountTicket()*getPotExtra()));
        createTask();
    }

    public void redisConvertObject(String data) {
        if (data.isEmpty()) {
            return;
        }
        int ticketNumber = Integer.parseInt(data.split(":")[0]);
        int ticketAmount = Integer.parseInt(data.split(":")[1]);

        addPotPrize(ticketAmount);
        setAmountTicket(XSHandlers.XSLottery.getAmountTicket()+ticketAmount);

        if(lotteryList.containsKey(ticketNumber)) {
            lotteryList.replace(ticketNumber, lotteryList.get(ticketNumber)+ticketAmount);
        } else {
            lotteryList.put(ticketNumber,ticketAmount);
        }

    }

    public void loadDataSQL(String JDBC_URL,String USER,String PASS) {
        try {
            Connection connection = DriverManager.getConnection(JDBC_URL, USER, PASS) ;

            Statement statement = connection.createStatement();

            String selectQuery = "SELECT * FROM " + XSHandlers.getTableLottery();

            ResultSet resultSet = statement.executeQuery(selectQuery);

            if (resultSet.next()) {
                String lotteryListData = resultSet.getString("lotteryList");
                long nextPrizeTime = resultSet.getLong("NextPrizeTime");
                String winnerName = resultSet.getString("winnerName");
                String winnerNumber = resultSet.getString("winnerNumber");
                String winnerAmountTicket = resultSet.getString("winnerNumberTicket");
                String winnerTotalPrize = resultSet.getString("winnerPrize");
                String lockPrize = resultSet.getString("lockPrize");
                String lockSetter = resultSet.getString("lockSetter");

                //Bukkit.broadcastMessage(lotteryList);

                int currentAmt = 0;

                if(!lotteryListData.equalsIgnoreCase("[]")) {
                    lotteryListData = lotteryListData.replaceAll("\\[|\\]", "");
                    String[] dataArray = lotteryListData.split(",");

                    ArrayList<String> arrayList = new ArrayList<>(Arrays.asList(dataArray));
                    for (String lottery : arrayList) {
                        int key = Integer.parseInt(lottery.trim().split(":")[0]);
                        int amount = Integer.parseInt(lottery.trim().split(":")[1]);
                        lotteryList.put(key,amount);
                        currentAmt += amount;
                    }
                }

                if(XSHandlers.getUsingRedis()) {
                    //Bukkit.broadcastMessage("Start up Requesting data from redis...");
                    Bukkit.getScheduler().scheduleSyncDelayedTask(XSCasino.getPlugin(), new Runnable() {
                        @Override
                        public void run() {
                            XSHandlers.sendMessageToRedisAsync("XSCasinoRedisData/XSLottery/Requests/"+ XSHandlers.getHostCrossServer() + "/" + XSHandlers.getLocalRedis(),XSHandlers.getLocalRedis());
                        }
                    }, 200L);
                }

                setAmountTicket(currentAmt);

                if(XSHandlers.getUsingRedis()) {
                    setNextPrizeTime(System.currentTimeMillis()+XSUtils.calculateTimeRedis(getPrizeString()));
                } else {
                    setNextPrizeTime(nextPrizeTime);
                    if(nextPrizeTime == 0) {
                        setNextPrizeTime(System.currentTimeMillis() + (getPrizeTime()*1000L));
                    } else {
                        setNextPrizeTime(nextPrizeTime);
                    }
                }

                setLockPrize(-1);
                setSetterName("");

                if(!lockPrize.isEmpty()) {
                    try {
                        if (Integer.parseInt(lockPrize) != -1) {
                            setLockPrize(Integer.parseInt(lockPrize));
                            setSetterName(lockSetter);
                        }
                    } catch (NumberFormatException ignored) {

                    }
                }


                if(winnerName.isEmpty()) {
                    setWinner("");
                    setTicketWinNum(-1);
                    setNumberTicketWin(-1);
                    setTotalWinPrize(-1);
                } else {
                    setWinner(winnerName);
                    setTicketWinNum(Integer.parseInt(winnerNumber));
                    setNumberTicketWin(Integer.parseInt(winnerAmountTicket));
                    setTotalWinPrize(Integer.parseInt(winnerTotalPrize));
                }

                Bukkit.getConsoleSender().sendMessage("§x§E§7§F§F§0§0[XSCasino] Lottery loaded data from database successfully");
            }
            resultSet.close();
            statement.close();
            connection.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void saveData() {
        ArrayList<String> lotteryList = new ArrayList<>();
        for(Map.Entry<Integer,Integer> lottery : this.getLotteryList().entrySet()) {
            lotteryList.add(lottery.getKey()+":"+lottery.getValue());
        }

        if(XSHandlers.getUsingSQL()) {
            saveTOSQL(XSHandlers.getJDBC_URL(),XSHandlers.getUSER(),XSHandlers.getPASS(),lotteryList);
        } else {
            this.getCustomConfig().set("data.lottery_list",lotteryList);
            this.getCustomConfig().set("data.next_prize_time",getNextPrizeTime());

            if(!getWinner().isEmpty()) {
                this.getCustomConfig().set("data.winner.name",getWinner());
                this.getCustomConfig().set("data.winner.number",getTicketWinNum());
                this.getCustomConfig().set("data.winner.numberTicket",getNumberTicketWin());
                this.getCustomConfig().set("data.winner.totalPrize",getTotalWinPrize());
            }

            this.getCustomConfig().set("data.lockPrize.number",getLockPrize());
            this.getCustomConfig().set("data.lockPrize.setter",getSetterName());


            try {
                getCustomConfig().save(getCustomConfigFile());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void resetPlayerData(String JDBC_URL,String USER,String PASS) {
        try {
            Connection connection = DriverManager.getConnection(JDBC_URL, USER, PASS);

            String updateQuery = "UPDATE " + XSHandlers.getTableXSPlayer() + " SET lotteryList = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(updateQuery);
            preparedStatement.setString(1, "[]");
            preparedStatement.executeUpdate();

            preparedStatement.close();
            connection.close();

            Bukkit.getConsoleSender().sendMessage("§x§E§7§F§F§0§0[XSAPI Casino] Lottery Database : §x§6§0§F§F§0§0Reset!");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public void saveTOSQL(String JDBC_URL,String USER,String PASS,ArrayList<String> lotteryData) {
        try {
            Connection connection = DriverManager.getConnection(JDBC_URL, USER, PASS);
            PreparedStatement preparedStatement = null;
            if(XSHandlers.getUsingRedis()) {
                String updateQuery = "UPDATE " +XSHandlers.getTableLottery() + " SET winnerName=?, winnerNumber=?, winnerNumberTicket=?, winnerPrize=?," +
                        " lockPrize=?, lockSetter=? LIMIT 1";
                preparedStatement = connection.prepareStatement(updateQuery);
                if(!getWinner().isEmpty()) {
                    preparedStatement.setString(1, getWinner());
                    preparedStatement.setString(2, String.valueOf(getTicketWinNum()));
                    preparedStatement.setString(3, String.valueOf(getNumberTicketWin()));
                    preparedStatement.setString(4, String.valueOf(getTotalWinPrize()));
                } else {
                    preparedStatement.setString(1, "");
                    preparedStatement.setString(2, "");
                    preparedStatement.setString(3, "");
                    preparedStatement.setString(4, "");
                }
                preparedStatement.setString(5, String.valueOf(getLockPrize()));
                preparedStatement.setString(6, getSetterName());
            } else {
                String updateQuery = "UPDATE " +XSHandlers.getTableLottery() + " SET lotteryList=?, NextPrizeTime=?," +
                        " winnerName=?, winnerNumber=?, winnerNumberTicket=?, winnerPrize=?," +
                        " lockPrize=?, lockSetter=? LIMIT 1";

                preparedStatement = connection.prepareStatement(updateQuery);

                preparedStatement.setString(1, String.valueOf(lotteryData));
                preparedStatement.setLong(2, getNextPrizeTime());
                if(!getWinner().isEmpty()) {
                    preparedStatement.setString(3, getWinner());
                    preparedStatement.setString(4, String.valueOf(getTicketWinNum()));
                    preparedStatement.setString(5, String.valueOf(getNumberTicketWin()));
                    preparedStatement.setString(6, String.valueOf(getTotalWinPrize()));
                } else {
                    preparedStatement.setString(3, "");
                    preparedStatement.setString(4, "");
                    preparedStatement.setString(5, "");
                    preparedStatement.setString(6, "");
                }
                preparedStatement.setString(7, String.valueOf(getLockPrize()));
                preparedStatement.setString(8, getSetterName());
            }

            preparedStatement.executeUpdate();
            preparedStatement.close();
            connection.close();

            Bukkit.getConsoleSender().sendMessage("§x§E§7§F§F§0§0[XSAPI Casino] Lottery Database : §x§6§0§F§F§0§0Saved!");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void createSQL(String JDBC_URL,String USER,String PASS) {
        try {
            Connection connection = DriverManager.getConnection(JDBC_URL,USER,PASS);

            Statement statement = connection.createStatement();

            String createTableQuery = "CREATE TABLE IF NOT EXISTS " + XSHandlers.getTableLottery() + " ("
                    + "lotteryList TEXT DEFAULT '[]', "
                    + "NextPrizeTime BIGINT DEFAULT 0, "
                    + "winnerName VARCHAR(16) DEFAULT '', "
                    + "winnerNumber VARCHAR(2) DEFAULT '', "
                    + "winnerNumberTicket VARCHAR(10) DEFAULT '', "
                    + "winnerPrize VARCHAR(20) DEFAULT '', "
                    + "lockPrize VARCHAR(2) DEFAULT '', "
                    + "lockSetter VARCHAR(16) DEFAULT ''"
                    + ")";

            statement.executeUpdate(createTableQuery);


            Statement statementInsert = connection.createStatement();

            String insertQuery = "INSERT INTO " + XSHandlers.getTableLottery() + " (lotteryList) "
                    + "VALUES ('[]')";

            statementInsert.executeUpdate(insertQuery);
            statementInsert.close();

            statement.close();
            connection.close();

            Bukkit.getConsoleSender().sendMessage("§x§E§7§F§F§0§0[XSCasino] Lottery Database : §x§6§0§F§F§0§0Connected");
        } catch (SQLException e) {
            Bukkit.getConsoleSender().sendMessage("§x§E§7§F§F§0§0[XSasino] Lottery Database : §x§C§3§0§C§2§ANot Connected");
            e.printStackTrace();
        }
    }

    public void createTask() {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(XSCasino.getPlugin(), new Runnable() {
            @Override
            public void run() {
                //Bukkit.broadcastMessage("CURRENT: " + System.currentTimeMillis() + " NEXT: " + getNextPrizeTime() + " = " + (System.currentTimeMillis() - getNextPrizeTime()));

                if(!XSHandlers.getUsingRedis()) {
                    if(System.currentTimeMillis() - getNextPrizeTime() >= 0L) {
                        setNextPrizeTime(System.currentTimeMillis() + (getPrizeTime()*1000L));
                        int prizeNum;

                        if(getLockPrize() != -1) {
                            prizeNum = getLockPrize();
                        } else {
                            prizeNum = generatePrizeNumber();
                        }

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
                } else {
                    if(System.currentTimeMillis() - getNextPrizeTime() >= 0L) {
                        setNextPrizeTime(System.currentTimeMillis()+XSUtils.calculateTimeRedis(getPrizeString()));
                    }
                }
                for(Map.Entry<UUID,Inventory> playerOpen : xsLotteryUserOpenUI.entrySet()) {
                    updateInventory(Bukkit.getPlayer(playerOpen.getKey()));
                }
            }
        }, 0L, 20L);
    }

    public void sendPrizeWinRedis(String message) {
        String str = String.valueOf(message);
        if(str.length() == 1) {
            str = ("0" + str);
        }
        // Bukkit.broadcastMessage("Lottery Prize Out! " + str);
        /*for(Player p : Bukkit.getOnlinePlayers()) {
            String winMsg = messages.customConfig.getString("lottery_prize_annoucement")
                    .replace("%number%",str).replace("%prize%",String.valueOf(getPotPrize()));

            XSUtils.sendReplaceComponents(p,winMsg);
        }*/
        String winMsg = messages.customConfig.getString("lottery_prize_annoucement")
                .replace("%number%",str).replace("%prize%",String.valueOf(getPotPrize()));
        Bukkit.broadcastMessage(XSUtils.replacePlainToString((winMsg.replace("%prefix%"
                , Objects.requireNonNull(messages.customConfig.getString("prefix"))))).replace('&','§'));
        setTicketWinNum(Integer.parseInt(message));
        checkRewardWinRedis(Integer.parseInt(str));
    }

    public void checkRewardWinRedis(int prizeNum) {
        HashMap<String,Integer> winnerList = new HashMap<>();
        for(Map.Entry<UUID,XSUser> userList : XSHandlers.xsCasinoUser.entrySet()) {
            if(!userList.getValue().getLottery().isEmpty()) {
                if(userList.getValue().getLottery().containsKey(prizeNum)) {
                    winnerList.put(userList.getValue().getPlayer().getName(),userList.getValue().getLottery().get(prizeNum));
                }
                userList.getValue().getLottery().clear();
            }
        }

        Gson gson = new Gson();
        String winList = gson.toJson(winnerList);
        XSHandlers.sendMessageToRedisAsync("XSCasinoRedisData/XSLottery/WinnerList/"+ XSHandlers.getHostCrossServer() + "/" + XSHandlers.getLocalRedis(),winList);
    }

    public void sendReward(int prizeNum) {

        //Bukkit.broadcastMessage("STARTING CHECK");
        HashMap<UUID,Integer> lotteryWinner = new HashMap<>();
        int amountWinticket = 0;
        String winnerName = "";
        int MaxAmountOfTicket = 0;

        if(XSHandlers.getUsingSQL()) {
            //Check only online players
            for(Map.Entry<UUID,XSUser> userList : XSHandlers.xsCasinoUser.entrySet()) {
                if(!userList.getValue().getLottery().isEmpty()) {
                    if(userList.getValue().getLottery().containsKey(prizeNum)) {
                        int amt = userList.getValue().getLottery().get(prizeNum);
                        amountWinticket += amt;
                        lotteryWinner.put(userList.getKey(),amt);
                        if(amt > MaxAmountOfTicket) {
                            MaxAmountOfTicket = amt;
                            winnerName = userList.getValue().getPlayer().getName();
                        }
                    }
                    userList.getValue().getLottery().clear();
                }
            }
        } else {
            File[] allData = new File(XSCasino.getPlugin().getDataFolder() + "/data").listFiles();

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
        }

        setWinner(winnerName);
        setNumberTicketWin(amountWinticket);
        setTicketWinNum(prizeNum);
        setTotalWinPrize((int) getPotPrize());
        String winMsg = "";
        setSetterName("");
        setLockPrize(-1);
        resetPlayerData(XSHandlers.getJDBC_URL(),XSHandlers.getUSER(),XSHandlers.getPASS());
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

    public void loadDataFromRedisServer(String message) {
        Gson gson = new Gson();
        HashMap<Integer, Integer> resultMap = gson.fromJson(message, new TypeToken<HashMap<Integer, Integer>>(){}.getType());

        int currentTicket = 0;
        for(Map.Entry<Integer,Integer> data : resultMap.entrySet()) {
            currentTicket += data.getValue();
        }

        lotteryList.putAll(resultMap);
        setAmountTicket(currentTicket);
        setPotPrize((long) (getPotPrize() + getAmountTicket()*getPotExtra()));
        //Bukkit.broadcastMessage("Load Data from Redis Successfully");
    }

    public void calculatePrizeRedis(String message) {
        Gson gson = new Gson();
        HashMap<String, Integer> resultMap = gson.fromJson(message.split("XSCASINO_ESCAPE_PREFIX")[0], new TypeToken<HashMap<String, Integer>>(){}.getType());

        setWinner(message.split("XSCASINO_ESCAPE_PREFIX")[1]);
        setNumberTicketWin(Integer.parseInt(message.split("XSCASINO_ESCAPE_PREFIX")[2]));
        setTotalWinPrize((int) getPotPrize());
        setSetterName("");
        setLockPrize(-1);
        clearLotteryData();

        String winMsg = "";
        if(getWinner().isEmpty()) {
            winMsg = Objects.requireNonNull(messages.customConfig.getString("lottery_prize_win"))
                    .replace("%player_winner%", Objects.requireNonNull(messages.customConfig.getString("win_condition.no_data")));
        } else {
            winMsg = Objects.requireNonNull(messages.customConfig.getString("lottery_prize_win"))
                    .replace("%player_winner%",getWinner());
        }

        Bukkit.broadcastMessage((XSUtils.replacePlainToString(winMsg.replace("%prefix%"
                , Objects.requireNonNull(messages.customConfig.getString("prefix"))))).replace('&','§'));

        setNextPrizeTime(System.currentTimeMillis()+XSUtils.calculateTimeRedis(getPrizeString()));

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

}

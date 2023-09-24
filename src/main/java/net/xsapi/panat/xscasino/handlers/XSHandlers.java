package net.xsapi.panat.xscasino.handlers;

import com.google.gson.Gson;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import net.xsapi.panat.xscasino.configuration.config;
import net.xsapi.panat.xscasino.configuration.lotteryConfig;
import net.xsapi.panat.xscasino.configuration.messages;
import net.xsapi.panat.xscasino.core.XSCasino;
import net.xsapi.panat.xscasino.events.joinEvent;
import net.xsapi.panat.xscasino.events.leaveEvent;
import net.xsapi.panat.xscasino.gui.ui_main_lottery;
import net.xsapi.panat.xscasino.gui.ui_myticket_lottery;
import net.xsapi.panat.xscasino.gui.ui_topticket_lottery;
import net.xsapi.panat.xscasino.modules.lottery;
import net.xsapi.panat.xscasino.user.UserData;
import net.xsapi.panat.xscasino.user.XSUser;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.scheduler.BukkitRunnable;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class XSHandlers {

    //Redis Connnection
    private static boolean usingRedis = false;
    private static String hostCrossServer;
    public static String redisHost;
    public static int redisPort;
    public static String redisPass;
    private static String localRedis;
    public static ArrayList<Thread> threads = new ArrayList<>();

    //MySQL Connection
    private static boolean usingSQL = false;
    private static String JDBC_URL;
    private static String USER;
    private static String PASS;
    private static String DB_TABLE;
    private static String TABLE_LOTTERY = "xscasino_lottery";
    private static String TABLE_XSPLAYER = "xscasino_user";

    public static lottery XSLottery;
    public static HashMap<UUID, XSUser> xsCasinoUser = new HashMap<>();
    private static HashMap<UUID, UserData> userData = new HashMap<>();

    private static Economy econ = null;
    private static Permission perms = null;

    public static String getHostRedis() {
        return redisHost;
    }
    public static String getHostCrossServer() {
        return hostCrossServer;
    }
    public static String getRedisPass() {
        return redisPass;
    }

    public static int getRedisPort() {
        return redisPort;
    }

    public static boolean getUsingRedis() {
        return usingRedis;
    }

    public static String getLocalRedis() {
        return localRedis;
    }

    public static boolean getUsingSQL() { return usingSQL; }
    public static String getJDBC_URL() {
        return JDBC_URL;
    }

    public static String getUSER() {
        return USER;
    }

    public static String getPASS() {
        return PASS;
    }

    public static String getTableLottery() {
        return TABLE_LOTTERY;
    }
    public static String getTableXSPlayer() {
        return TABLE_XSPLAYER;
    }


    public static void setupDefault() {
        usingRedis = config.customConfig.getBoolean("redis.enable");
        usingSQL = config.customConfig.getBoolean("database.enable");
        hostCrossServer = config.customConfig.getString("cross-server.server-name");
        localRedis = config.customConfig.getString("cross-server.parent-name");

        if(usingRedis) {
            if(redisConnection()) {
                localRedis = config.customConfig.getString("cross-server.server-name");
                hostCrossServer = config.customConfig.getString("cross-server.parent-name");

               // createRedisTask();
                subscribeToChannelAsync("XSCasinoRedisData/XSLottery/Update/"+ getHostCrossServer());
                subscribeToChannelAsync("XSCasinoRedisData/XSLottery/EndPrizeNumber/"+ getHostCrossServer());
                subscribeToChannelAsync("XSCasinoRedisData/XSLottery/EndSendWinnerList/"+ getHostCrossServer());
                subscribeToChannelAsync("XSCasinoRedisData/XSLottery/RequestsReturn/"+ getHostCrossServer() + "/" + getLocalRedis());
                subscribeToChannelAsync("XSCasinoRedisData/XSLottery/ResponseStatus/"+ getHostCrossServer() + "/" + getLocalRedis());
            }
        }

        if(usingSQL) {
            String host = config.customConfig.getString("database.host");
            DB_TABLE = config.customConfig.getString("database.dbTable");
            JDBC_URL = "jdbc:mysql://" + host +  "/" + DB_TABLE;
            USER = config.customConfig.getString("database.user");
            PASS = config.customConfig.getString("database.password");
            createUserTable();
        }

    }

    private static void subscribeToChannelAsync(String channelName) {
        Thread thread = new Thread(() -> {
            try (Jedis jedis = new Jedis(getHostRedis(), getRedisPort())) {
                if(!getRedisPass().isEmpty()) {
                    jedis.auth(getRedisPass());
                }
                JedisPubSub jedisPubSub = new JedisPubSub() {
                    @Override
                    public void onMessage(String channel, String message) {
                        if (Thread.currentThread().isInterrupted()) {
                            return;
                        }
                        //Bukkit.getConsoleSender().sendMessage("Recieved Data (send from client) --> " + message);
                        //Bukkit.getConsoleSender().sendMessage("--------------------");
                        if(channel.equalsIgnoreCase("XSCasinoRedisData/XSLottery/Update/"+ getHostCrossServer())) {
                            XSLottery.redisConvertObject(message);
                        } else if(channel.equalsIgnoreCase("XSCasinoRedisData/XSLottery/EndPrizeNumber/"+ getHostCrossServer())) {
                            XSLottery.sendPrizeWinRedis(message);
                        } else if(channel.equalsIgnoreCase("XSCasinoRedisData/XSLottery/EndSendWinnerList/"+ getHostCrossServer())) {
                            XSLottery.calculatePrizeRedis(message);
                        } else if(channel.equalsIgnoreCase("XSCasinoRedisData/XSLottery/RequestsReturn/"+ getHostCrossServer() + "/" + getLocalRedis())) {
                            XSLottery.loadDataFromRedisServer(message);
                        } else if(channel.equalsIgnoreCase("XSCasinoRedisData/XSLottery/ResponseStatus/"+ getHostCrossServer() + "/" + getLocalRedis())) {
                            if(message.equalsIgnoreCase("true")) {
                                XSLottery.setBuyAble(true);
                            } else {
                                XSLottery.setBuyAble(false);
                            }
                        }

                    }
                };
                jedis.subscribe(jedisPubSub, channelName);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        thread.start();
        threads.add(thread);
    }

    public static void destroyAllThread() {
        for(Thread thread : threads) {
            thread.interrupt();
        }
    }


    private static boolean redisConnection() {
        redisHost = config.customConfig.getString("redis.host");
        redisPort = config.customConfig.getInt("redis.port");
        redisPass = config.customConfig.getString("redis.password");

        try {
            Jedis jedis = new Jedis(getHostRedis(), getRedisPort());
            if(!getRedisPass().isEmpty()) {
                jedis.auth(getRedisPass());
            }
            jedis.close();
            Bukkit.getConsoleSender().sendMessage("§x§E§7§F§F§0§0[XSCasino] Redis Server : §x§6§0§F§F§0§0Connected");
            return true;
        } catch (Exception e) {
            Bukkit.getConsoleSender().sendMessage("§x§E§7§F§F§0§0[XSCasino] Redis Server : §x§C§3§0§C§2§ANot Connected");
            e.printStackTrace();
        }
        return false;
    }

    public static void sendMessageToRedisAsync(String CHName, String message) {

        new Thread(() -> {
            try (Jedis jedis = new Jedis(getHostRedis(), getRedisPort())) {
                if(!getRedisPass().isEmpty()) {
                    jedis.auth(getRedisPass());
                }
                jedis.publish(CHName, message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public static void sendDataObjectRedis(String CHName,String lottery) {
        XSHandlers.sendMessageToRedisAsync(CHName,lottery);
       // Bukkit.broadcastMessage("Client Send.... From " + CHName);
    }

    private static void createUserTable() {
        try {
            Connection connection = DriverManager.getConnection(JDBC_URL,USER,PASS);

            DatabaseMetaData metaData = connection.getMetaData();
            ResultSet resultSet = metaData.getTables(null, null, getTableXSPlayer(), null);
            boolean tableExists = resultSet.next();

            if(!tableExists) {
                Statement statement = connection.createStatement();

                String createTableQuery = "CREATE TABLE " + getTableXSPlayer() + " ("
                        + "id INT PRIMARY KEY AUTO_INCREMENT, "
                        + "UUID VARCHAR(36), "
                        + "playerName VARCHAR(16), "
                        + "lotteryList TEXT"
                        + ")";

                statement.executeUpdate(createTableQuery);
                statement.close();
            }
            connection.close();

            Bukkit.getConsoleSender().sendMessage("§x§E§7§F§F§0§0[XSCasino] Player Database : §x§6§0§F§F§0§0Connected");
        } catch (SQLException e) {
            Bukkit.getConsoleSender().sendMessage("§x§E§7§F§F§0§0[XSCasino] Player Database : §x§C§3§0§C§2§ANot Connected");
            e.printStackTrace();
        }
    }

    public static void loadXSCasinoModules() {
        Bukkit.getConsoleSender().sendMessage("§x§f§f§a§c§2§f[XSCasino] trying to load data...");
        XSLottery = new lottery(lotteryConfig.customConfigFile,lotteryConfig.customConfig);

        if(getUsingSQL()) {
            XSLottery.createSQL(getJDBC_URL(),getUSER(),getPASS());
        }
        //Check is redis server available
        XSHandlers.sendMessageToRedisAsync("XSCasinoRedisData/XSLottery/CheckStatus/"+ XSHandlers.getHostCrossServer() + "/" + XSHandlers.getLocalRedis(),XSHandlers.getLocalRedis());
        Bukkit.getConsoleSender().sendMessage("§x§f§f§a§c§2§f[XSCasino] loaded §x§6§0§F§F§0§0100% §x§f§f§a§c§2§fcomplete!");

    }

    public static HashMap<UUID, UserData> getUserData() {
        return userData;
    }

    public static void saveXSCasinoModules() {
        XSLottery.saveData();
    }

    public static void saveUserData() {
        for(Player p : Bukkit.getOnlinePlayers()) {

            if(getUsingSQL()) {
                if(XSHandlers.xsCasinoUser.containsKey(p.getUniqueId())) {
                    XSUser xsUser = XSHandlers.xsCasinoUser.get(p.getUniqueId());
                    ArrayList<String> lotteryList = new ArrayList<>();
                    for(Map.Entry<Integer,Integer> lottery : xsUser.getLottery().entrySet()) {
                        lotteryList.add(lottery.getKey()+":"+lottery.getValue());
                    }
                    xsUser.saveUserSQL(lotteryList);
                    //Bukkit.getLogger().info("Saved " + p.getName() + " via SQL");
                }
            } else {
                if(XSHandlers.xsCasinoUser.containsKey(p.getUniqueId())) {

                    XSUser xsUser = XSHandlers.xsCasinoUser.get(p.getUniqueId());

                    ArrayList<String> lotteryList = new ArrayList<>();
                    for(Map.Entry<Integer,Integer> lottery : xsUser.getLottery().entrySet()) {
                        lotteryList.add(lottery.getKey()+":"+lottery.getValue());
                    }
                    xsUser.getUserConfig().set("modules.lottery.data",lotteryList);

                    xsUser.saveData();

                    XSHandlers.xsCasinoUser.remove(p.getUniqueId());
                }
            }
        }
    }

    public static void loadUserData() {
        for(Player p : Bukkit.getOnlinePlayers()) {

            if(getUsingSQL()) {
                XSUser xsUser = new XSUser(p);
                XSHandlers.xsCasinoUser.put(p.getUniqueId(),xsUser);
            } else {
                File pFile = new File(XSCasino.getPlugin().getDataFolder() + "/data", p.getUniqueId() + ".yml");

                if(pFile.exists()) {
                    XSUser xsUser = new XSUser(p);

                    if(xsUser.getUserConfig().get("modules.lottery.data") != null) {
                        xsUser.loadUserData();
                    }

                    XSHandlers.xsCasinoUser.put(p.getUniqueId(),xsUser);
                }
            }

            XSHandlers.getUserData().put(p.getUniqueId(),new UserData(p));
        }
    }

    public static void setupAPI() {
        if (XSCasino.getPlugin().getServer().getPluginManager().getPlugin("Vault") == null) {
            Bukkit.getConsoleSender().sendMessage("§x§f§f§a§c§2§f[XSCasino] Vault : §x§D§F§1§C§6§3Not Found!");
            XSCasino.getPlugin().getServer().getPluginManager().disablePlugin(XSCasino.getPlugin());
            Bukkit.getConsoleSender().sendMessage("§x§f§f§a§c§2§f[XSCasino] §x§D§F§1§C§6§3Plugin Disabled due not found vault!");
        } else {
            RegisteredServiceProvider<Economy> rsp = XSCasino.getPlugin().getServer().getServicesManager().getRegistration(Economy.class);
            econ = rsp.getProvider();
            RegisteredServiceProvider<Permission> rspPermission = XSCasino.getPlugin().getServer().getServicesManager().getRegistration(Permission.class);
            perms = rspPermission.getProvider();
            Bukkit.getConsoleSender().sendMessage("§x§f§f§a§c§2§f[XSCasino] Vault : §x§2§F§C§0§2§0Found!");
        }
    }

    public static Economy getEconomy() {
        return econ;
    }

    public static Permission getPerms() {
        return perms;
    }

    public static void registerEvents() {
        Bukkit.getPluginManager().registerEvents(new joinEvent(), XSCasino.getPlugin());
        Bukkit.getPluginManager().registerEvents(new leaveEvent(), XSCasino.getPlugin());
        Bukkit.getPluginManager().registerEvents(new ui_main_lottery(), XSCasino.getPlugin());
        Bukkit.getPluginManager().registerEvents(new ui_topticket_lottery(), XSCasino.getPlugin());
        Bukkit.getPluginManager().registerEvents(new ui_myticket_lottery(), XSCasino.getPlugin());
    }


}

package net.xsapi.panat.xscasino.user;

import net.xsapi.panat.xscasino.core.XSCasino;
import net.xsapi.panat.xscasino.handlers.XSHandlers;
import net.xsapi.panat.xscasino.types.TokenType;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class XSUser {

    private Player p;
    private HashMap<Integer,Integer> lottery = new HashMap<>();

    private TokenType tokenType;

    private File userFile;
    private FileConfiguration userConfig;

    public XSUser(Player p) {
        this.p = p;

        if(XSHandlers.getUsingSQL()) {
            createUserSQL();
            loadSQLUserData();
        } else {
            this.userFile = new File(XSCasino.getPlugin().getDataFolder() + "/data", p.getUniqueId() + ".yml");
            this.userConfig = YamlConfiguration.loadConfiguration(this.userFile);
        }
    }

    public Player getPlayer() {
        return p;
    }

    public void setTokenType(TokenType tokenType) {
        this.tokenType = tokenType;
    }

    public TokenType getTokenType() {
        return tokenType;
    }

    public void createUser() {
        this.userConfig = YamlConfiguration.loadConfiguration(this.userFile);
        if (!this.userFile.exists()) {
            this.userConfig.set("AccountName", this.p.getName());
            this.userConfig.set("modules.lottery.data", new ArrayList<String>());
        }
        saveData();
    }

    public void createUserSQL() {
        try {
            Connection connection = DriverManager.getConnection(XSHandlers.getJDBC_URL(),XSHandlers.getUSER(),XSHandlers.getPASS());

            String checkPlayerQuery = "SELECT EXISTS(SELECT * FROM " + XSHandlers.getTableXSPlayer() + " WHERE playerName = ?) AS exist";
            PreparedStatement preparedStatement = connection.prepareStatement(checkPlayerQuery);
            preparedStatement.setString(1, p.getName());
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                boolean exists = resultSet.getBoolean("exist");

                if (!exists) {
                    String insertQuery = "INSERT INTO " + XSHandlers.getTableXSPlayer() + " (UUID, playerName, lotteryList) "
                            + "VALUES (?, ?, ?)";

                    try (PreparedStatement preparedStatementInsert = connection.prepareStatement(insertQuery)) {
                        preparedStatementInsert.setString(1, String.valueOf(p.getUniqueId()));
                        preparedStatementInsert.setString(2, p.getName());
                        preparedStatementInsert.setString(3, "[]");
                        preparedStatementInsert.executeUpdate();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }

            resultSet.close();
            preparedStatement.close();
            connection.close();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void loadSQLUserData() {
        try {
            Connection connection = DriverManager.getConnection(XSHandlers.getJDBC_URL(),XSHandlers.getUSER(),XSHandlers.getPASS());

            String selectQuery = "SELECT * FROM " + XSHandlers.getTableXSPlayer() + " WHERE playername = ?";;

            try (PreparedStatement preparedStatement = connection.prepareStatement(selectQuery)) {
                preparedStatement.setString(1, p.getName());
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        String lotteryList = resultSet.getString("lotteryList");
                        if(!lotteryList.equalsIgnoreCase("[]") && !lotteryList.isEmpty()) {

                            lotteryList = lotteryList.replaceAll("\\[|\\]", "");
                            String[] dataArray = lotteryList.split(",");

                            ArrayList<String> arrayList = new ArrayList<>(Arrays.asList(dataArray));
                            for (String lottery : arrayList) {
                                int key = Integer.parseInt(lottery.trim().split(":")[0]);
                                int amount = Integer.parseInt(lottery.trim().split(":")[1]);
                                getLottery().put(key,amount);
                            }
                        }
                    }
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void loadUserData() {

        if(XSHandlers.getUsingSQL()) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(XSCasino.getPlugin(), new Runnable() {
                @Override
                public void run() {
                    loadSQLUserData();
                }
            }, 20L);
        } else {
            for(String lottery : this.userConfig.getStringList("modules.lottery.data")) {
                int ticket = Integer.parseInt(lottery.split(":")[0]);
                int amount = Integer.parseInt(lottery.split(":")[1]);

                getLottery().put(ticket,amount);
            }
        }

    }

    public void saveUserSQL(ArrayList<String> lotteryData) {
        try (Connection connection = DriverManager.getConnection(XSHandlers.getJDBC_URL(), XSHandlers.getUSER(), XSHandlers.getPASS())) {
            String updateQuery = "UPDATE " + XSHandlers.getTableXSPlayer() + " SET lotteryList = ? WHERE playername = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(updateQuery)) {
                preparedStatement.setString(1, lotteryData.toString());
                preparedStatement.setString(2, p.getName());
                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void saveModulesData() {

        ArrayList<String> lotteryList = new ArrayList<>();
        for(Map.Entry<Integer,Integer> lottery : this.getLottery().entrySet()) {
            lotteryList.add(lottery.getKey()+":"+lottery.getValue());
        }

        if(XSHandlers.getUsingSQL()) {
            saveUserSQL(lotteryList);
        } else {
            this.getUserConfig().set("modules.lottery.data",lotteryList);
            this.saveData();
        }
    }

    public void saveData() {
        try {
            getUserConfig().save(this.userFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public File getUserFile() {
        return userFile;
    }

    public FileConfiguration getUserConfig() {
        return userConfig;
    }

    public HashMap<Integer, Integer> getLottery() {
        return lottery;
    }
}

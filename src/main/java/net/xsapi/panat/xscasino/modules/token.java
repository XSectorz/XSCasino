package net.xsapi.panat.xscasino.modules;

import net.xsapi.panat.xscasino.configuration.tokenConfig;
import net.xsapi.panat.xscasino.handlers.XSHandlers;
import net.xsapi.panat.xscasino.handlers.XSUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class token {

    public static HashMap<String,ItemStack> tokenList = new HashMap<>();

    private static String TABLE = "xscasino_token";

    public static ItemStack getToken100() {
        return tokenList.get("token_100");
    }

    public static ItemStack getToken1000() {
        return tokenList.get("token_1000");
    }

    public static ItemStack getToken10000() {
        return tokenList.get("token_10000");
    }

    public static void setToken100(ItemStack token100) {
        tokenList.put("token_100",token100);
    }

    public static void setToken1000(ItemStack token1000) {
        tokenList.put("token_1000",token1000);
    }

    public static void setToken10000(ItemStack token10000) {
        tokenList.put("token_10000",token10000);
    }

    public static void setupDefault() {
        if(XSHandlers.getUsingSQL()) {
            token.setToken100(new ItemStack(Material.AIR));
            token.setToken1000(new ItemStack(Material.AIR));
            token.setToken10000(new ItemStack(Material.AIR));
            createSQL(XSHandlers.getJDBC_URL(),XSHandlers.getUSER(),XSHandlers.getPASS());
            loadSQLData();
        } else {
            if(tokenConfig.customConfig.get("items.token100") != null) {
                setToken100(XSUtils.itemStackFromBase64(tokenConfig.customConfig.getString("items.token100")));
            } else if(tokenConfig.customConfig.get("items.token1000") != null) {
                setToken1000(XSUtils.itemStackFromBase64(tokenConfig.customConfig.getString("items.token1000")));
            } else if(tokenConfig.customConfig.get("items.token10000") != null) {
                setToken10000(XSUtils.itemStackFromBase64(tokenConfig.customConfig.getString("items.token10000")));
            }
        }
    }

    public static void saveData() {
        if(XSHandlers.getUsingSQL()) {
            saveData("token_100");
            saveData("token_1000");
            saveData("token_10000");
        } else {
            tokenConfig.customConfig.set("items.token100",XSUtils.itemStackToBase64(tokenList.get("token_100")));
            tokenConfig.customConfig.set("items.token1000",XSUtils.itemStackToBase64(tokenList.get("token_1000")));
            tokenConfig.customConfig.set("items.token10000",XSUtils.itemStackToBase64(tokenList.get("token_10000")));

            tokenConfig.save();
            tokenConfig.reload();
        }
    }

    public static void saveData(String key) {
        try (Connection connection = DriverManager.getConnection(XSHandlers.getJDBC_URL(), XSHandlers.getUSER(), XSHandlers.getPASS())) {

            String updateQuery = "UPDATE " + TABLE + " SET encode = ? WHERE tokenID = ?";

            try (PreparedStatement preparedStatement = connection.prepareStatement(updateQuery)) {
                preparedStatement.setString(1, XSUtils.itemStackToBase64(tokenList.get(key)));
                preparedStatement.setString(2, key);
                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void loadSQLData() {
        loadData("token_100");
        loadData("token_1000");
        loadData("token_10000");
    }

    public static void loadData(String key) {
        try {
            Connection connection = DriverManager.getConnection(XSHandlers.getJDBC_URL(),XSHandlers.getUSER(),XSHandlers.getPASS());
            String selectQuery = "SELECT * FROM " + TABLE + " WHERE tokenID = ?";;

            try (PreparedStatement preparedStatement = connection.prepareStatement(selectQuery)) {
                preparedStatement.setString(1, key);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        String encoded = resultSet.getString("encode");

                        if(!encoded.isEmpty()) {
                            tokenList.put(key,XSUtils.itemStackFromBase64(encoded));
                        }
                       // Bukkit.broadcastMessage("UPDATE! " + key);
                    }
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    public static void createSQL(String JDBC_URL, String USER, String PASS) {
        try {
            Connection connection = DriverManager.getConnection(JDBC_URL,USER,PASS);

            Statement statement = connection.createStatement();

            String createTableQuery = "CREATE TABLE IF NOT EXISTS " + TABLE + " ("
                    + "tokenID TEXT DEFAULT '', "
                    + "encode TEXT DEFAULT ''"
                    + ")";

            statement.executeUpdate(createTableQuery);


            for(int i = 0 ; i < 3 ; i++) {

                String checkQuery = "SELECT EXISTS(SELECT * FROM " + TABLE + " WHERE tokenID = ?) AS exist";
                PreparedStatement preparedStatementCheck = connection.prepareStatement(checkQuery);
                preparedStatementCheck.setString(1, String.format("token_%d", 100 * (int) Math.pow(10, i)));
                ResultSet resultSet = preparedStatementCheck.executeQuery();

                if (resultSet.next()) {
                    boolean exists = resultSet.getBoolean("exist");
                    if (!exists) {
                        String insertQuery = "INSERT INTO " + TABLE + " (tokenID, encode) "
                                + "VALUES (?,?)";
                        try (PreparedStatement preparedStatementInsert = connection.prepareStatement(insertQuery)) {
                            preparedStatementInsert.setString(1, String.format("token_%d", 100 * (int) Math.pow(10, i)));
                            preparedStatementInsert.setString(2, "");
                            preparedStatementInsert.executeUpdate();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            statement.close();
            connection.close();

            Bukkit.getConsoleSender().sendMessage("§x§E§7§F§F§0§0[XSCasino] Token Database : §x§6§0§F§F§0§0Connected");
        } catch (SQLException e) {
            Bukkit.getConsoleSender().sendMessage("§x§E§7§F§F§0§0[XSCasino] Token Database : §x§C§3§0§C§2§ANot Connected");
            e.printStackTrace();
        }
    }
}

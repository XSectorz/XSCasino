package net.xsapi.panat.xscasino.modules;

import net.xsapi.panat.xscasino.configuration.tokenConfig;
import net.xsapi.panat.xscasino.handlers.XSUtils;
import org.bukkit.inventory.ItemStack;

public class token {

    private static ItemStack token100;
    private static ItemStack token1000;
    private static ItemStack token10000;

    public static ItemStack getToken100() {
        return token100;
    }

    public static ItemStack getToken1000() {
        return token1000;
    }

    public static ItemStack getToken10000() {
        return token10000;
    }

    public static void setToken100(ItemStack token100) {
        token.token100 = token100;
    }

    public static void setToken1000(ItemStack token1000) {
        token.token1000 = token1000;
    }

    public static void setToken10000(ItemStack token10000) {
        token.token10000 = token10000;
    }

    public static void setupDefault() {
        if(tokenConfig.customConfig.get("items.token100") != null) {
            token100 = XSUtils.itemStackFromBase64(tokenConfig.customConfig.getString("items.token100"));
        } else if(tokenConfig.customConfig.get("items.token1000") != null) {
            token1000 = XSUtils.itemStackFromBase64(tokenConfig.customConfig.getString("items.token1000"));
        } else if(tokenConfig.customConfig.get("items.token10000") != null) {
            token10000 = XSUtils.itemStackFromBase64(tokenConfig.customConfig.getString("items.token10000"));
        }
    }

    public static void saveData() {
        tokenConfig.customConfig.set("items.token100",XSUtils.itemStackToBase64(token100));
        tokenConfig.customConfig.set("items.token1000",XSUtils.itemStackToBase64(token1000));
        tokenConfig.customConfig.set("items.token10000",XSUtils.itemStackToBase64(token10000));

        tokenConfig.save();
        tokenConfig.reload();
    }
}

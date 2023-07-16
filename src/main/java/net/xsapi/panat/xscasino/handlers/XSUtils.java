package net.xsapi.panat.xscasino.handlers;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.xsapi.panat.xscasino.configuration.messages;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collections;

public class XSUtils {

    public static void sendMessages(Player p, String path) {
        Audience player = (Audience) p;

        MiniMessage miniMessage = MiniMessage.builder().build();

        Component parsedMessage = MiniMessage.miniMessage().deserialize((messages.customConfig.getString(path).replace("%prefix%"
        ,messages.customConfig.getString("prefix"))));

        player.sendMessage(parsedMessage);
    }

    public static void sendReplaceComponents(Player p,String str) {
        Audience player = (Audience) p;
        MiniMessage miniMessage = MiniMessage.builder().build();

        player.sendMessage(MiniMessage.miniMessage().deserialize(str.replace("%prefix%"
                ,messages.customConfig.getString("prefix"))));
    }

    public static ArrayList<String> messagesList(String path) {
        return (ArrayList<String>) messages.customConfig.getStringList("commands_list");
    }

    public static String getMessagesConfig(String path) {
        return messages.customConfig.getString(path).replace("&","§");
    }

    public static ItemStack createItemStack(Material mat,int amount,int customModelData,
                                            String name,ArrayList<String> lore) {

        ItemStack it = new ItemStack(mat,amount);
        ItemMeta itemMeta = it.getItemMeta();

        itemMeta.setCustomModelData(customModelData);
        itemMeta.setDisplayName(name.replace("&","§"));

        ArrayList<String> lores = new ArrayList<>();

        for(String line : lore) {
            lores.add(line.replace("&","§"));
        }

        itemMeta.setLore(lores);

        it.setItemMeta(itemMeta);
        return it;


    }

}

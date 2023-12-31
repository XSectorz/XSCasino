package net.xsapi.panat.xscasino.handlers;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.xsapi.panat.xscasino.configuration.messages;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Base64;
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

    public static long calculateTimeRedis(String time) {

        LocalTime currentTime = LocalTime.now();
        //LocalTime currentTime =LocalTime.of(0, 1);
        LocalTime targetTime = LocalTime.of(Integer.parseInt(time.split(":")[0]), Integer.parseInt(time.split(":")[1]));
        Duration duration;
        if (currentTime.isAfter(targetTime)) {
            duration = Duration.between(targetTime, currentTime);
            targetTime = LocalTime.of((int) (23-duration.toHours()), (int) (59-(duration.toMinutes()%60)),59);
            currentTime =LocalTime.of(0, 0);

        }
        duration = Duration.between(currentTime, targetTime);
        return Math.abs(duration.toMillis());

    }

    public static String convertTime(long millis) {
        int seconds = (int) (millis / 1000) % 60;
        int minutes = (int) ((millis / (1000 * 60)) % 60);
        int hours = (int) ((millis / (1000 * 60 * 60)) % 24);
        long days = millis / (1000 * 60 * 60 * 24);

        String timer = "";

        if(days >= 1) {
            timer += days;
            if(days == 1) {
                timer += " " + XSUtils.getMessagesConfig("time.day") + " ";
            } else {
                timer += " " + XSUtils.getMessagesConfig("time.days") + " ";
            }
        }
        if(hours >= 1) {
            timer += hours;
            if(hours == 1) {
                timer += " " + XSUtils.getMessagesConfig("time.hour") + " ";
            } else {
                timer += " " + XSUtils.getMessagesConfig("time.hours") + " ";
            }
        }
        if(minutes >= 1) {
            timer += minutes;
            if(minutes == 1) {
                timer += " " + XSUtils.getMessagesConfig("time.minute") + " ";
            } else {
                timer += " " + XSUtils.getMessagesConfig("time.minutes") + " ";
            }
        }

        if(seconds >= 1) {
            timer += seconds;
            if(minutes == 1) {
                timer += " " + XSUtils.getMessagesConfig("time.second") + " ";
            } else {
                timer += " " + XSUtils.getMessagesConfig("time.seconds") + " ";
            }
        }

        if(timer.isEmpty()) {
            timer += XSUtils.getMessagesConfig("time.soon");
        }

        return timer;
    }

    public static String replacePlainToString(String str) {
        Component parsedMessage = MiniMessage.builder().build().deserialize(str);

        String legacy = LegacyComponentSerializer.legacyAmpersand().serialize(parsedMessage);
        return legacy.replace('&', '§');
    }

    public static String itemStackToBase64(ItemStack itemStack) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);

            dataOutput.writeObject(itemStack);

            byte[] byteItemStack = outputStream.toByteArray();
            String base64 = Base64.getEncoder().encodeToString(byteItemStack);

            return base64;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static ItemStack itemStackFromBase64(String base64) {
        try {
            byte[] byteItemStack = Base64.getDecoder().decode(base64);
            ByteArrayInputStream inputStream = new ByteArrayInputStream(byteItemStack);
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);

            // อ่าน NBT และสร้าง ItemStack จาก inputStream
            ItemStack itemStack = (ItemStack) dataInput.readObject();

            return itemStack;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}

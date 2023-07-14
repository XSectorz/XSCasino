package net.xsapi.panat.xscasino.events;

import net.xsapi.panat.xscasino.handlers.XSHandlers;
import net.xsapi.panat.xscasino.user.XSUser;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.ArrayList;
import java.util.Map;

public class leaveEvent implements Listener {

    @EventHandler
    public void onLeave(PlayerQuitEvent e) {
        Player p = e.getPlayer();

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

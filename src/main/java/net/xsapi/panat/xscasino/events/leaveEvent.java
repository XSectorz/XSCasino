package net.xsapi.panat.xscasino.events;

import net.xsapi.panat.xscasino.handlers.XSHandlers;
import net.xsapi.panat.xscasino.user.XSUser;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class leaveEvent implements Listener {

    @EventHandler
    public void onLeave(PlayerQuitEvent e) {
        Player p = e.getPlayer();

        if(XSHandlers.xsCasinoUser.containsKey(p.getUniqueId())) {
            XSUser xsUser = XSHandlers.xsCasinoUser.get(p.getUniqueId());
            xsUser.saveModulesData();

            XSHandlers.xsCasinoUser.remove(p.getUniqueId());
        }
    }
}

package net.xsapi.panat.xscasino.events;

import net.xsapi.panat.xscasino.core.XSCasino;
import net.xsapi.panat.xscasino.handlers.XSHandlers;
import net.xsapi.panat.xscasino.user.XSUser;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.io.File;

public class joinEvent implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();

        File pFile = new File(XSCasino.getPlugin().getDataFolder() + "/data", p.getUniqueId() + ".yml");

        if(pFile.exists()) {
            XSUser xsUser = new XSUser(p);

            if(xsUser.getUserConfig().get("modules.lottery.data") != null) {
                xsUser.loadUserData();
            }

            XSHandlers.xsCasinoUser.put(p.getUniqueId(),xsUser);
        }

    }
}

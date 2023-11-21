package net.xsapi.panat.xscasino.modules;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.Inventory;

import java.io.File;
import java.util.HashMap;
import java.util.UUID;

public class roulette extends XSCasinoTemplates{

    public HashMap<UUID, Inventory> xsRouletteOpenUI = new HashMap<>();

    public HashMap<UUID, Inventory> getXsRouletteOpenUI() {
        return xsRouletteOpenUI;
    }

    public roulette(File customConfigFile, FileConfiguration customConfig) {
        setCustomConfigFile(customConfigFile);
        setCustomConfig(customConfig);

        setTitle(getCustomConfig().getString("configuration.title").replace("&","§"));
        setInvSize(getCustomConfig().getInt("configuration.inventorySize"));
    }
}

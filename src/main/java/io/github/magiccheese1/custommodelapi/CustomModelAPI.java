package io.github.magiccheese1.custommodelapi;

import co.aikar.commands.PaperCommandManager;
import io.github.magiccheese1.custommodelapi.commands.CMCommand;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;

public final class CustomModelAPI extends JavaPlugin {

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(new BukkitEventListener(null, null), this);
        ResourceManager resourceManager = new ResourceManager(this);
        PaperCommandManager paperCommandManager = new PaperCommandManager(this);
        paperCommandManager.registerCommand(new CMCommand(resourceManager));
    }
}

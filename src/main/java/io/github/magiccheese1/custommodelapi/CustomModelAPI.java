package io.github.magiccheese1.custommodelapi;

import co.aikar.commands.PaperCommandManager;
import io.github.magiccheese1.custommodelapi.commands.CMCommand;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public final class CustomModelAPI extends JavaPlugin {

    private static ResourceManager resourceManager;

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(new BukkitEventListener(null, null), this);
        resourceManager = new ResourceManager(this);
        PaperCommandManager paperCommandManager = new PaperCommandManager(this);
        paperCommandManager.registerCommand(new CMCommand(resourceManager));
        try {
            resourceManager.addModel(new File(this.getDataFolder() + File.separator + "jebaccie.json"), new Texture[]{new Texture(new NamespacedKey(this, "penis"), new File(this.getDataFolder() + File.separator + "pink.png"))}, new NamespacedKey(this, "jebaccie"));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onDisable() {
        resourceManager.clean();
    }

    public static ResourceManager getResourceManager() {
        return resourceManager;
    }
}

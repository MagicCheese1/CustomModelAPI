package io.github.magiccheese1.custommodelapi;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;

public class BukkitEventListener implements Listener {
    private String resourcePackURL, resourcePackHash;

    public BukkitEventListener(@NotNull String resourcePackURL,@NotNull String resourcePackHash){
        this.resourcePackURL = resourcePackURL;
        this.resourcePackHash = resourcePackHash;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event){
        event.getPlayer().setResourcePack(resourcePackURL, resourcePackHash);
    }
}

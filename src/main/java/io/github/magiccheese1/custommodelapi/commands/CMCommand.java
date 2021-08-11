package io.github.magiccheese1.custommodelapi.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Subcommand;
import com.google.gson.Gson;
import io.github.magiccheese1.custommodelapi.CustomModelAPI;
import io.github.magiccheese1.custommodelapi.ResourceManager;
import org.bukkit.Bukkit;

@CommandAlias("cm")
public class CMCommand extends BaseCommand {
    private final ResourceManager resourceManager;

    public CMCommand(ResourceManager resourceManager) {
        this.resourceManager = resourceManager;
    }

    @Subcommand("generate")
    public void onGenerate() {
        resourceManager.generatePack();
    }

    @Subcommand("ss")
    public void onessa() {
        Gson gson = new Gson();
    }
}

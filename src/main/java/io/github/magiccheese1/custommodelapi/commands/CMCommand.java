package io.github.magiccheese1.custommodelapi.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Subcommand;
import io.github.magiccheese1.custommodelapi.ResourceManager;

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
}

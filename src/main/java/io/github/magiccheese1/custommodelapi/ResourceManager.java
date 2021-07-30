package io.github.magiccheese1.custommodelapi;

import com.google.common.base.Charsets;
import com.google.gson.Gson;
import io.github.magiccheese1.custommodelapi.utils.ZipUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.io.*;
import java.nio.file.Files;
import java.util.*;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class ResourceManager {
    private final Plugin plugin;
    private Map<String, Model> models;
    private File modelsDirectory;
    private File texturesDirectory;

    public ResourceManager(Plugin plugin) {
        this.plugin = plugin;
        models = new HashMap<String, Model>();
        modelsDirectory = new File(plugin.getDataFolder() + "/models");
        texturesDirectory = new File(plugin.getDataFolder() + "/textures");
        modelsDirectory.mkdirs();
        texturesDirectory.mkdirs();
    }

    public ItemStack getItemStack(String modelName, int amount) {
        Model model = models.get(modelName);
        if (model == null)
            return null;
        ItemStack itemStack = new ItemStack(Material.PAPER, amount);

        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setCustomModelData(model.customModelData);

        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    public void setModel(ItemStack itemStack, String modelName) {
        Model model = models.get(modelName);
        if (model == null) {
            return;
        }

        itemStack.setType(Material.PAPER);

        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setCustomModelData(model.customModelData);

        itemStack.setItemMeta(itemMeta);
    }

    public void generatePack() {
        try {
            File tempDirectory = new File(Files.createTempDirectory("CMAPI") + File.separator + "resourcePack");
            tempDirectory.mkdirs();
            File tempModelsDirectory = new File(tempDirectory + File.separator + "assets" + File.separator + "minecraft" + File.separator + "models" + File.separator + "item");
            File tempTexturesDirectory = new File(tempDirectory + File.separator + "assets" + File.separator + "minecraft" + File.separator + "textures" + File.separator + "custom");
            tempModelsDirectory.mkdirs();
            tempTexturesDirectory.mkdirs();
            OutputStream outputStream = new FileOutputStream(tempDirectory + File.separator + "pack.mcmeta");
            this.getClass().getClassLoader().getResourceAsStream("pack.mcmeta").transferTo(outputStream);
            File paperJson = new File(tempModelsDirectory + File.separator + "paper.json");
            OutputStream outputStream2 = new FileOutputStream(paperJson);
            this.getClass().getClassLoader().getResourceAsStream("paper.json").transferTo(outputStream2);
            outputStream2.close();
            Gson gson = new Gson();
            Map<String, Object> paperJsonMap = gson.fromJson(Files.readString(paperJson.toPath(), Charsets.UTF_8), Map.class);
            List<Object> overrides = new ArrayList<>();
            File[] modelsDirectoryfiles = modelsDirectory.listFiles();
            for (int i = 0; i < modelsDirectoryfiles.length; i++) {
                Files.copy(modelsDirectoryfiles[i].toPath(), (new File(tempModelsDirectory + File.separator + modelsDirectoryfiles[i].getName())).toPath(), REPLACE_EXISTING);
                Map<String, Object> override = new HashMap<>();
                override.put("predicate", Collections.singletonMap("custom_model_data", i+1));
                override.put("model", "item/" + modelsDirectoryfiles[i].getName().split("\\.")[0]);
                overrides.add(override);
            }
            paperJsonMap.put("overrides", overrides);
            BufferedWriter writer = new BufferedWriter(new FileWriter(paperJson));
            writer.write(gson.toJson(paperJsonMap));
            writer.close();
            for (File file : texturesDirectory.listFiles()) {
                Files.copy(file.toPath(), (new File(tempTexturesDirectory + File.separator + file.getName())).toPath(), REPLACE_EXISTING);
            }
            File resourcePack = new File(plugin.getDataFolder() + File.separator + "resourcePack.zip");
            ZipUtils.zip(tempDirectory.listFiles(), resourcePack.getPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
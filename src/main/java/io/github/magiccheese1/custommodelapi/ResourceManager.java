package io.github.magiccheese1.custommodelapi;

import com.google.common.base.Charsets;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import io.github.magiccheese1.custommodelapi.utils.ZipUtils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class ResourceManager {
    private final Plugin plugin;
    private final File modelsDirectory;
    private final File texturesDirectory;
    private final File modelsJson;

    //currently used models
    private Map<NamespacedKey, Model> models;
    //Not yet applied models
    private Map<NamespacedKey, Model> modelsBuffer;

    private Map<Integer, NamespacedKey> customModelDataMap;

    protected ResourceManager(Plugin plugin) {
        this.plugin = plugin;

        modelsDirectory = new File(plugin.getDataFolder() + "/models");
        texturesDirectory = new File(plugin.getDataFolder() + "/textures");
        modelsDirectory.mkdirs();
        texturesDirectory.mkdirs();

        Gson gson = new Gson();
        modelsJson = new File(plugin.getDataFolder() + File.separator + "models.json");
        models = new HashMap<>();
        modelsBuffer = new HashMap<>();
        customModelDataMap = new HashMap<>();
        if (modelsJson.exists()) {
            try {
                List<Model> modelList = gson.fromJson(Files.readString(modelsJson.toPath()), new TypeToken<List<Model>>() {
                }.getType());
                for (Model model : modelList) {
                    models.put(model.getNamespacedKey(), model);
                    customModelDataMap.put(model.getCustomModelData(), model.getNamespacedKey());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            modelsBuffer = new HashMap<>(models);
        }
    }

    /**
     * Creates a new ItemStack with the model already set.
     *
     * @param modelNamespacedKey the key of the model you want applied to the ItemStack.
     * @param amount             the size of the ItemStack.
     * @return the created ItemStack.
     */
    public ItemStack getItemStack(@NotNull NamespacedKey modelNamespacedKey, int amount) {
        Model model = models.get(modelNamespacedKey);
        if (model == null)
            return null;
        ItemStack itemStack = new ItemStack(Material.PAPER, amount);

        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setCustomModelData(model.getCustomModelData());

        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    /**
     * Creates a new ItemStack with the model already set and a size of 1.
     *
     * @param modelNamespacedKey the key of the model you want applied to the ItemStack.
     * @return the created ItemStack.
     */
    public ItemStack getModeledItemStack(@NotNull NamespacedKey modelNamespacedKey) {
        return getItemStack(modelNamespacedKey, 1);
    }

    /**
     * Edits an existing ItemStack to use a certain model.
     *
     * @param itemStack          the ItemStack you want to be edited.
     * @param modelNamespacedKey the key of the model you want applied to the ItemStack.
     */
    public void setItemStackModel(@NotNull ItemStack itemStack, @NotNull NamespacedKey modelNamespacedKey) {
        Model model = models.get(modelNamespacedKey);
        if (model == null) {
            return;
        }

        itemStack.setType(Material.PAPER);

        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setCustomModelData(model.getCustomModelData());

        itemStack.setItemMeta(itemMeta);
    }

    public void addModel(@NotNull File file, Texture[] textures, @NotNull NamespacedKey namespacedKey) throws IOException {
        if (!file.exists() || file.isDirectory())
            return;
        Path newModelPath = new File(modelsDirectory + File.separator + file.getName()).toPath();
        int customModelData = -1;
        if (modelsBuffer.containsKey(namespacedKey)) {
            Model oldModel = modelsBuffer.get(namespacedKey);
            Path oldModelFile = Path.of(oldModel.getFilePath());
            Files.deleteIfExists(oldModelFile);
            for (Texture texture : oldModel.getTextures()) {
                Path oldTextureFile = Path.of(texture.getFilePath());
                Files.deleteIfExists(oldTextureFile);
            }
            customModelData = oldModel.getCustomModelData();
        } else {
            int x = 1;
            while (customModelDataMap.containsKey(Integer.valueOf(x))) {
                x++;
            }
            customModelData = x;
            customModelDataMap.put(x, namespacedKey);
        }
        newModelPath = resolveFileConfilict(file, newModelPath);
        Files.copy(file.toPath(), newModelPath);

        Gson gson = new Gson();
        Map<String, Object> modelJsonMap = gson.fromJson(Files.readString(newModelPath), Map.class);
        Map<String, Object> modelJsonTexturesMap = (Map<String, Object>) modelJsonMap.get("textures");

        Map<NamespacedKey, Texture> newTextures = new HashMap<>();
        for (Texture texture : textures) {
            File textureFile = new File(texture.getFilePath());
            if (!textureFile.exists() || textureFile.isDirectory())
                continue;
            Path newTexturePath = new File(texturesDirectory + File.separator + textureFile.getName()).toPath();
            newTexturePath = resolveFileConfilict(textureFile, newTexturePath);

            Files.copy(textureFile.toPath(), newTexturePath);

            Texture texture1 = new Texture(texture.getNamespacedKey(), newTexturePath.toFile());
            newTextures.put(texture1.getNamespacedKey(), texture1);

            for (Map.Entry<String, Object> entry : modelJsonTexturesMap.entrySet()) {
                if (entry.getValue().equals("custom/" + textureFile.getName().toString().split("\\.")[0])) {
                    modelJsonTexturesMap.put(entry.getKey(), "custom/" + newTexturePath.getFileName().toString().split("\\.")[0]);
                }
            }
        }

        modelJsonMap.put("textures", modelJsonTexturesMap);
        FileWriter fileWriter = new FileWriter(newModelPath.toFile());
        fileWriter.write(gson.toJson(modelJsonMap));
        fileWriter.close();

        Model model = new Model(namespacedKey, customModelData, newModelPath.toFile(), newTextures.values().toArray(new Texture[0]));
        modelsBuffer.put(model.getNamespacedKey(), model);
    }

    @NotNull
    private Path resolveFileConfilict(File textureFile, Path newPath) throws IOException {
        int x = 0;
        while (Files.exists(newPath)) {
            x++;
            String[] fileNameSplits = newPath.getFileName().toString().split("\\.");
            // extension is assumed to be the last part
            int extensionIndex = fileNameSplits.length - 1;
            // add extension to id
            newPath = new File(newPath.getParent() + "/" + newPath.getFileName().toString().split("\\.")[0] + x + "." + fileNameSplits[extensionIndex]).toPath();
        }
        return newPath;
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
            File[] modelsDirectoryFiles = modelsDirectory.listFiles();
            for (int i = 0; i < modelsDirectoryFiles.length; i++) {

                Files.copy(modelsDirectoryFiles[i].toPath(), (new File(tempModelsDirectory + File.separator + modelsDirectoryFiles[i].getName())).toPath(), REPLACE_EXISTING);

                Map<String, Object> override = new HashMap<>();
                override.put("predicate", Collections.singletonMap("custom_model_data", i + 1));
                override.put("model", "item/" + modelsDirectoryFiles[i].getName().split("\\.")[0]);
                overrides.add(override);
                for (Map.Entry<NamespacedKey, Model> entry : modelsBuffer.entrySet()) {
                    if (entry.getValue().getFilePath().equals(modelsDirectoryFiles[i].getPath())) {
                        models.put(entry.getKey(), entry.getValue());
                        break;
                    }
                }
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

    protected void clean() {
        Gson gson = new Gson();
        try {
            if (!modelsJson.exists())
                modelsJson.createNewFile();
            BufferedWriter writer = new BufferedWriter(new FileWriter(modelsJson));
            String json = gson.toJson(models.values().toArray());
            writer.write(json);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
package io.github.magiccheese1.custommodelapi;

import org.bukkit.NamespacedKey;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class Model {
    private NamespacedKey namespacedKey;
    private int customModelData;
    private String filePath;
    private Texture[] textures;

    protected Model(NamespacedKey namespacedKey, int customModelData, File filePath, Texture[] textures) {
        this.namespacedKey = namespacedKey;
        this.customModelData = customModelData;
        this.filePath = filePath.getPath();
        this.textures = textures;
    }

    public NamespacedKey getNamespacedKey() {
        return namespacedKey;
    }

    public int getCustomModelData() {
        return customModelData;
    }

    public String getFilePath() {
        return filePath;
    }

    public Texture[] getTextures() {
        return textures;
    }

    public Texture getTexture(NamespacedKey namespacedKey) {
        for(Texture texture : textures) {
            if(texture.getNamespacedKey().equals(namespacedKey))
                return texture;
        }
        return null;
    }

}

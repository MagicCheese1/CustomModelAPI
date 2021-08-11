package io.github.magiccheese1.custommodelapi;

import org.bukkit.NamespacedKey;

import java.io.File;

public class Texture {
    private NamespacedKey namespacedKey;
    private String filePath;

    public Texture(NamespacedKey namespacedKey, File file) {
        this.namespacedKey = namespacedKey;
        this.filePath = file.getPath();
    }

    public NamespacedKey getNamespacedKey() {
        return namespacedKey;
    }

    public String getFilePath() {
        return filePath;
    }

}

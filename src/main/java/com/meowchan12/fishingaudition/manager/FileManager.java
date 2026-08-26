package com.meowchan12.fishingaudition.manager;

import com.meowchan12.fishingaudition.Main;
import java.io.File;

public class FileManager {

    private final Main plugin;

    // List of required folders according to design
    private final String[] folders = {
            "backup",
            "material",
            "region",
            "shop",
            "fishingrod",
            "currency",
            "playerdata",
            "language",
            "scoreboard"
    };

    public FileManager(Main plugin) {
        this.plugin = plugin;
    }

    /**
     * Generates the directory tree inside the plugin's data folder.
     */
    public void setupFolders() {
        for (String folderName : folders) {
            File folder = new File(plugin.getDataFolder(), folderName);
            if (!folder.exists()) {
                boolean created = folder.mkdirs();
                if (created) {
                    plugin.getLogger().info("[FileManager] Successfully created folder: " + folderName);
                } else {
                    plugin.getLogger().warning("[FileManager] Failed to create folder: " + folderName);
                }
            }
        }
    }
}
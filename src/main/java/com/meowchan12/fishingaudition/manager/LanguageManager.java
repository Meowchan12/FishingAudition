package com.meowchan12.fishingaudition.manager;

import com.meowchan12.fishingaudition.Main;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class LanguageManager {

    private final Main plugin;
    private File langFile;
    private FileConfiguration langConfig;

    public LanguageManager(Main plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    public void loadConfig() {
        langFile = new File(plugin.getDataFolder(), "messages.yml");
        if (!langFile.exists()) {
            langFile.getParentFile().mkdirs();
            plugin.saveResource("messages.yml", false);
        }
        langConfig = YamlConfiguration.loadConfiguration(langFile);
    }

    public String getRawMessage(String path, String def) {
        if (langConfig == null) return def;
        return langConfig.getString(path, def);
    }
}

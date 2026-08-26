package com.meowchan12.fishingaudition.manager;

import com.meowchan12.fishingaudition.Main;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class RodManager {

    private final Main plugin;
    private File rodFile;
    private FileConfiguration rodConfig;

    // Use LinkedHashMap to preserve insertion order (useful for GUI)
    private final Map<String, RodData> rods = new LinkedHashMap<>();

    public RodManager(Main plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    public void loadConfig() {
        rodFile = new File(plugin.getDataFolder(), "rods.yml");
        if (!rodFile.exists()) {
            rodFile.getParentFile().mkdirs();
            plugin.saveResource("rods.yml", false);
        }

        rodConfig = YamlConfiguration.loadConfiguration(rodFile);
        rods.clear();

        ConfigurationSection section = rodConfig.getConfigurationSection("rods");
        if (section != null) {
            for (String key : section.getKeys(false)) {
                String name = section.getString(key + ".name", key);
                int maxDurability = section.getInt(key + ".max_durability", 50);
                double rarityBoost = section.getDouble(key + ".rarity_boost", 0.0);
                double price = section.getDouble(key + ".price", 500.0);
                double repairMultiplier = section.getDouble(key + ".repair_multiplier", 1.0);
                int requiredLevel = section.getInt(key + ".required_level", 1);
                int modelData = section.getInt(key + ".custom_model_data", 0);
                List<String> lore = section.getStringList(key + ".lore");
                List<String> catchableRarities = section.getStringList(key + ".catchable-rarities");
                double extraTime = section.getDouble(key + ".extra-time", 0.0);
                double progressBoost = section.getDouble(key + ".progress-boost", 1.0);

                RodData data = new RodData(key, name, maxDurability, rarityBoost, price, repairMultiplier, requiredLevel, modelData, lore, catchableRarities, extraTime, progressBoost);
                rods.put(key, data);
            }
        }
        plugin.getLogger().info("Loaded " + rods.size() + " custom rods.");
    }

    public RodData getRodById(String id) {
        return rods.get(id);
    }

    public List<RodData> getAllRods() {
        return new ArrayList<>(rods.values());
    }
}

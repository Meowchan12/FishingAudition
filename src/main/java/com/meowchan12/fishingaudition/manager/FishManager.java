package com.meowchan12.fishingaudition.manager;

import com.meowchan12.fishingaudition.Main;
import com.meowchan12.fishingaudition.material.FishTier;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.*;

public class FishManager {

    private final Main plugin;
    private File fishFile;
    private FileConfiguration fishConfig;

    // In-memory storage optimized for RNG: grouped by Tier
    private final Map<FishTier, List<FishData>> fishByTier = new EnumMap<>(FishTier.class);
    // Fast lookup by ID for admin command
    private final Map<String, FishData> fishById = new HashMap<>();
    private final Random random = new Random();

    public FishManager(Main plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    public void loadConfig() {
        fishFile = new File(plugin.getDataFolder(), "fish.yml");
        if (!fishFile.exists()) {
            fishFile.getParentFile().mkdirs();
            plugin.saveResource("fish.yml", false);
        }

        fishConfig = YamlConfiguration.loadConfiguration(fishFile);
        fishByTier.clear();
        fishById.clear();
        
        for (FishTier tier : FishTier.values()) {
            fishByTier.put(tier, new ArrayList<>());
        }

        ConfigurationSection section = fishConfig.getConfigurationSection("fishes");
        if (section != null) {
            for (String key : section.getKeys(false)) {
                String name = section.getString(key + ".name", key);
                String tierStr = section.getString(key + ".tier", "COMMON").toUpperCase();
                double value = section.getDouble(key + ".value", 10.0);
                int modelData = section.getInt(key + ".custom_model_data", 0);

                FishTier tier;
                try {
                    tier = FishTier.valueOf(tierStr);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid tier " + tierStr + " for fish " + key);
                    tier = FishTier.COMMON;
                }
                
                double defaultXp = tier.ordinal() * 10.0 + 5.0; // 5, 15, 25, etc.
                double xpReward = section.getDouble(key + ".xp-reward", defaultXp);

                FishData data = new FishData(key, name, tier, value, xpReward, modelData);
                fishByTier.get(tier).add(data);
                fishById.put(key, data);
            }
        }
        plugin.getLogger().info("Loaded " + fishById.size() + " custom fishes.");
    }

    /**
     * O(1) random selection from a pre-filtered list
     */
    public FishData getRandomFishByTier(FishTier tier) {
        List<FishData> list = fishByTier.get(tier);
        if (list == null || list.isEmpty()) return null;
        return list.get(random.nextInt(list.size()));
    }

    public FishData getFishById(String id) {
        return fishById.get(id);
    }

    public Set<String> getAllFishIds() {
        return fishById.keySet();
    }

    public Collection<FishData> getAllFish() {
        return fishById.values();
    }
}

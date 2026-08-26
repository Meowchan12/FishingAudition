package com.meowchan12.fishingaudition.manager;

import com.meowchan12.fishingaudition.Main;
import com.meowchan12.fishingaudition.utils.MessageUtils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

public class BaitManager {

    private final Main plugin;
    private File baitFile;
    private FileConfiguration baitConfig;
    private final Map<String, BaitData> baits = new HashMap<>();
    
    public static final NamespacedKey BAIT_ID_KEY = new NamespacedKey(Main.getInstance(), "bait_id");

    public BaitManager(Main plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    public void loadConfig() {
        baitFile = new File(plugin.getDataFolder(), "baits.yml");
        if (!baitFile.exists()) {
            baitFile.getParentFile().mkdirs();
            plugin.saveResource("baits.yml", false);
        }

        baitConfig = YamlConfiguration.loadConfiguration(baitFile);
        baits.clear();

        ConfigurationSection section = baitConfig.getConfigurationSection("baits");
        if (section != null) {
            for (String key : section.getKeys(false)) {
                String name = section.getString(key + ".name", key);
                double rarityBoost = section.getDouble(key + ".rarity_boost", 5.0);
                int maxCharges = section.getInt(key + ".max_charges", 50);
                int modelData = section.getInt(key + ".custom_model_data", 0);
                double missPenaltyReduction = section.getDouble(key + ".miss-penalty-reduction", 0.0);
                String targetRarity = section.getString(key + ".target-rarity", "");
                double price = section.getDouble(key + ".price", 1000.0);
                int slot = section.getInt(key + ".slot", -1);

                BaitData data = new BaitData(key, name, rarityBoost, maxCharges, modelData, missPenaltyReduction, targetRarity, price, slot);
                baits.put(key, data);
            }
        }
        plugin.getLogger().info("Loaded " + baits.size() + " baits.");
    }

    public BaitData getBaitById(String id) {
        return baits.get(id);
    }
    
    public java.util.Collection<BaitData> getAllBaits() {
        return baits.values();
    }
    
    public ItemStack generateBait(String id) {
        BaitData data = getBaitById(id);
        if (data == null) return null;
        
        ItemStack item = new ItemStack(Material.SLIME_BALL);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(MessageUtils.colorize(data.getName()));
            if (data.getCustomModelData() > 0) {
                meta.setCustomModelData(data.getCustomModelData());
            }
            
            PersistentDataContainer pdc = meta.getPersistentDataContainer();
            pdc.set(BAIT_ID_KEY, PersistentDataType.STRING, id);
            
            List<String> lore = new ArrayList<>();
            lore.add(MessageUtils.colorize("&7Use this bait to increase"));
            lore.add(MessageUtils.colorize("&7your fishing chances."));
            lore.add("");
            lore.add(MessageUtils.colorize("&8▶ &fCharges: &a" + data.getMaxCharges() + "&8/&a" + data.getMaxCharges()));
            if (data.getTargetRarity() != null && !data.getTargetRarity().isEmpty()) {
                lore.add(MessageUtils.colorize("&8▶ &fTarget: &e" + data.getTargetRarity()));
            }
            if (data.getMissPenaltyReduction() > 0) {
                lore.add(MessageUtils.colorize("&8▶ &fPenalty Reduction: &a-" + (int)(data.getMissPenaltyReduction() * 100) + "%"));
            }
            if (data.getRarityBoost() > 0) {
                lore.add(MessageUtils.colorize("&8▶ &fRarity Boost: &a+" + data.getRarityBoost() + "%"));
            }
            lore.add("");
            lore.add(MessageUtils.colorize("&e&l[!] &eRight-click while holding this bait to equip it!"));
            meta.setLore(lore);
            
            item.setItemMeta(meta);
        }
        return item;
    }
    
    public boolean isBait(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer().has(BAIT_ID_KEY, PersistentDataType.STRING);
    }
    
    public String getBaitId(ItemStack item) {
        if (!isBait(item)) return null;
        return item.getItemMeta().getPersistentDataContainer().get(BAIT_ID_KEY, PersistentDataType.STRING);
    }
}

package com.meowchan12.fishingaudition.material;

import com.meowchan12.fishingaudition.Main;
import com.meowchan12.fishingaudition.manager.FishData;
import com.meowchan12.fishingaudition.utils.MessageUtils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class CustomFish {

    public static final NamespacedKey FISH_TAG = new NamespacedKey(Main.getInstance(), "is_custom_fish");
    public static final NamespacedKey FISH_VALUE = new NamespacedKey(Main.getInstance(), "fish_value");
    public static final NamespacedKey FISH_ID = new NamespacedKey(Main.getInstance(), "fish_id");

    public static ItemStack generateFish(FishData data) {
        if (data == null) return null;
        
        ItemStack fish = new ItemStack(Material.COD);
        ItemMeta meta = fish.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(MessageUtils.colorize("&b" + data.getName()));

            List<String> lore = new ArrayList<>();
            lore.add(MessageUtils.colorize("&7Tier: " + data.getTier().getDisplayName()));
            lore.add(MessageUtils.colorize("&7Value: &e" + data.getValue() + " FishCoins"));
            lore.add("");
            lore.add(MessageUtils.colorize("&eUse /fish chest or /fish sellall to sell!"));
            meta.setLore(lore);

            if (data.getCustomModelData() > 0) {
                meta.setCustomModelData(data.getCustomModelData());
            }

            meta.addEnchant(org.bukkit.enchantments.Enchantment.UNBREAKING, 10, true);
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);

            meta.getPersistentDataContainer().set(FISH_TAG, PersistentDataType.BYTE, (byte) 1);
            meta.getPersistentDataContainer().set(FISH_VALUE, PersistentDataType.DOUBLE, data.getValue());
            meta.getPersistentDataContainer().set(FISH_ID, PersistentDataType.STRING, data.getId());
            meta.getPersistentDataContainer().set(new NamespacedKey(Main.getInstance(), "fa_fish_id"), PersistentDataType.STRING, data.getId());

            fish.setItemMeta(meta);
        }
        return fish;
    }

    /**
     * Checks if an item is a legitimate custom fish.
     */
    public static boolean isCustomFish(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer().has(FISH_TAG, PersistentDataType.BYTE);
    }

    /**
     * Retrieves the exact FishCoin value of the fish.
     */
    public static double getFishValue(ItemStack item) {
        if (!isCustomFish(item)) return 0.0;
        return item.getItemMeta().getPersistentDataContainer().getOrDefault(FISH_VALUE, PersistentDataType.DOUBLE, 0.0);
    }
}
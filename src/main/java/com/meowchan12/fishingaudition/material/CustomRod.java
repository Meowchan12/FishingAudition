package com.meowchan12.fishingaudition.material;

import com.meowchan12.fishingaudition.Main;
import com.meowchan12.fishingaudition.manager.RodData;
import com.meowchan12.fishingaudition.utils.MessageUtils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class CustomRod {

    public static final NamespacedKey ROD_ID = new NamespacedKey(Main.getInstance(), "rod_id");

    public static ItemStack generateRod(RodData data) {
        if (data == null) return null;

        ItemStack rod = new ItemStack(Material.FISHING_ROD);
        ItemMeta meta = rod.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(MessageUtils.colorize(data.getName()));
            meta.setUnbreakable(true);
            meta.addEnchant(org.bukkit.enchantments.Enchantment.LUCK_OF_THE_SEA, 1, true);
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);

            if (data.getCustomModelData() > 0) {
                meta.setCustomModelData(data.getCustomModelData());
            }

            PersistentDataContainer pdc = meta.getPersistentDataContainer();
            pdc.set(ROD_ID, PersistentDataType.STRING, data.getId());
            pdc.set(new NamespacedKey(Main.getInstance(), "max_durability"), PersistentDataType.INTEGER, data.getMaxDurability());
            pdc.set(new NamespacedKey(Main.getInstance(), "current_durability"), PersistentDataType.INTEGER, data.getMaxDurability());
            pdc.set(new NamespacedKey(Main.getInstance(), "rarity_boost"), PersistentDataType.DOUBLE, data.getRarityBoost());
            pdc.set(new NamespacedKey(Main.getInstance(), "required_level"), PersistentDataType.INTEGER, data.getRequiredLevel());
            pdc.set(new NamespacedKey(Main.getInstance(), "extra_time"), PersistentDataType.DOUBLE, data.getExtraTime());
            pdc.set(new NamespacedKey(Main.getInstance(), "progress_boost"), PersistentDataType.DOUBLE, data.getProgressBoost());

            if (data.getCatchableRarities() != null && !data.getCatchableRarities().isEmpty()) {
                pdc.set(new NamespacedKey(Main.getInstance(), "catchable_rarities"), PersistentDataType.STRING, String.join(",", data.getCatchableRarities()));
            }

            rod.setItemMeta(meta);
            updateLore(rod, data);
        }
        return rod;
    }

    public static void updateLore(ItemStack rod) {
        if (rod == null || !rod.hasItemMeta()) return;
        ItemMeta meta = rod.getItemMeta();
        if (meta == null) return;
        
        String id = meta.getPersistentDataContainer().get(ROD_ID, PersistentDataType.STRING);
        if (id == null) return;
        
        RodData data = Main.getInstance().getRodManager().getRodById(id);
        if (data != null) {
            updateLore(rod, data);
        }
    }

    public static void updateLore(ItemStack rod, RodData data) {
        if (rod == null || !rod.hasItemMeta()) return;
        ItemMeta meta = rod.getItemMeta();
        if (meta == null) return;

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        NamespacedKey maxKey = new NamespacedKey(Main.getInstance(), "max_durability");
        NamespacedKey curKey = new NamespacedKey(Main.getInstance(), "current_durability");
        NamespacedKey rarityKey = new NamespacedKey(Main.getInstance(), "rarity_boost");
        NamespacedKey reqLevelKey = new NamespacedKey(Main.getInstance(), "required_level");
        NamespacedKey extraTimeKey = new NamespacedKey(Main.getInstance(), "extra_time");
        NamespacedKey progressBoostKey = new NamespacedKey(Main.getInstance(), "progress_boost");
        NamespacedKey catchableKey = new NamespacedKey(Main.getInstance(), "catchable_rarities");
        
        NamespacedKey baitIdKey = com.meowchan12.fishingaudition.listener.BaitListener.APPLIED_BAIT_ID;
        NamespacedKey baitChargesKey = com.meowchan12.fishingaudition.listener.BaitListener.APPLIED_BAIT_CHARGES;
        NamespacedKey baitBoostKey = com.meowchan12.fishingaudition.listener.BaitListener.APPLIED_BAIT_BOOST;
        NamespacedKey baitMissRedKey = com.meowchan12.fishingaudition.listener.BaitListener.APPLIED_BAIT_MISS_REDUCTION;
        NamespacedKey baitTargetKey = com.meowchan12.fishingaudition.listener.BaitListener.APPLIED_BAIT_TARGET_RARITY;

        if (!pdc.has(maxKey, PersistentDataType.INTEGER)) return;

        int max = pdc.get(maxKey, PersistentDataType.INTEGER);
        int current = pdc.get(curKey, PersistentDataType.INTEGER);
        double boost = pdc.getOrDefault(rarityKey, PersistentDataType.DOUBLE, 0.0);
        int reqLevel = pdc.getOrDefault(reqLevelKey, PersistentDataType.INTEGER, 1);
        double extraTime = pdc.getOrDefault(extraTimeKey, PersistentDataType.DOUBLE, 0.0);
        double progressBoost = pdc.getOrDefault(progressBoostKey, PersistentDataType.DOUBLE, 1.0);
        String catchable = pdc.get(catchableKey, PersistentDataType.STRING);

        List<String> lore = new ArrayList<>();
        if (data.getLore() != null) {
            for (String line : data.getLore()) {
                lore.add(MessageUtils.colorize(line));
            }
        }
        lore.add("");
        
        // Rod dynamic stats
        lore.add(MessageUtils.colorize("&8▶ &fDurability: &a" + current + "&8/&a" + max));
        lore.add(MessageUtils.colorize("&8▶ &fRequired Level: &a" + reqLevel));
        
        if (catchable != null && !catchable.isEmpty()) {
            lore.add(MessageUtils.colorize("&8▶ &fCan catch: &7" + catchable.replace(",", ", ")));
        }
        if (extraTime > 0) {
            lore.add(MessageUtils.colorize("&8▶ &fMinigame Time: &b+" + extraTime + "s"));
        }
        if (progressBoost > 1.0) {
            lore.add(MessageUtils.colorize("&8▶ &fProgress Boost: &d+" + (int)((progressBoost - 1.0) * 100) + "%"));
        }
        if (boost > 0) {
            lore.add(MessageUtils.colorize("&8▶ &fRarity Boost: &e+" + boost + "%"));
        }

        lore.add("");
        
        meta.setLore(lore);
        rod.setItemMeta(meta);
    }

    public static boolean isCustomRod(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        NamespacedKey maxKey = new NamespacedKey(Main.getInstance(), "max_durability");
        return item.getItemMeta().getPersistentDataContainer().has(maxKey, PersistentDataType.INTEGER);
    }
}
package com.meowchan12.fishingaudition.shop;

import com.meowchan12.fishingaudition.Main;

import com.meowchan12.fishingaudition.material.CustomRod;
import com.meowchan12.fishingaudition.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ShopManager {

    public static String getCurrencyShopTitle() {
        return MessageUtils.colorize(Main.getInstance().getConfig().getString("gui.titles.exchange", "&8Currency Exchange"));
    }

    public static String getRodShopTitle() {
        return MessageUtils.colorize(Main.getInstance().getConfig().getString("gui.titles.rod_shop", "&8Fishing Rod Shop"));
    }

    public void openCurrencyShop(Player player) {
        org.bukkit.configuration.file.FileConfiguration config = Main.getInstance().getConfig();
        int size = config.getInt("gui.sizes.exchange", 27);
        Inventory inv = Bukkit.createInventory(null, size, getCurrencyShopTitle());

        if (config.getBoolean("items.filler.enabled", true)) {
            Material fillerMat = Material.valueOf(config.getString("items.filler.material", "GRAY_STAINED_GLASS_PANE"));
            ItemStack glass = new ItemStack(fillerMat);
            ItemMeta glassMeta = glass.getItemMeta();
            if (glassMeta != null) {
                glassMeta.setDisplayName(MessageUtils.colorize(config.getString("items.filler.name", " ")));
                glassMeta.setCustomModelData(config.getInt("items.filler.custom_model_data", 0));
                glass.setItemMeta(glassMeta);
            }
            for (int i = 0; i < size; i++) {
                inv.setItem(i, glass);
            }
        }

        org.bukkit.configuration.ConfigurationSection tiers = config.getConfigurationSection("exchange.tiers");
        if (tiers != null) {
            for (String key : tiers.getKeys(false)) {
                String matStr = tiers.getString(key + ".material", "GOLD_INGOT");
                Material mat = Material.matchMaterial(matStr);
                if (mat == null) mat = Material.GOLD_INGOT;

                ItemStack item = new ItemStack(mat);
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName(MessageUtils.colorize(tiers.getString(key + ".name", key)));
                    int cost = tiers.getInt(key + ".cost-fishcoins", 100);
                    int reward = tiers.getInt(key + ".reward-amount", 1000);
                    String currency = tiers.getString(key + ".currency_type", "VAULT");
                    double rate = (double) reward / cost;

                    List<String> lore = new ArrayList<>();
                    for (String line : tiers.getStringList(key + ".lore")) {
                        line = line.replace("%cost%", String.valueOf(cost))
                                   .replace("%reward%", String.valueOf(reward))
                                   .replace("%currency%", currency)
                                   .replace("%rate%", String.format("%.1f", rate));
                        lore.add(MessageUtils.colorize(line));
                    }
                    meta.setLore(lore);
                    meta.setCustomModelData(tiers.getInt(key + ".custom_model_data", 0));
                    
                    // Store ID to PersistentDataContainer
                    String exchangeId = tiers.getString(key + ".id", key);
                    meta.getPersistentDataContainer().set(new org.bukkit.NamespacedKey(Main.getInstance(), "exchange_id"), org.bukkit.persistence.PersistentDataType.STRING, exchangeId);
                    item.setItemMeta(meta);
                }
                int s = tiers.getInt(key + ".slot", 11);
                inv.setItem(s, item);
            }
        }

        Material closeMat = Material.valueOf(config.getString("items.close_button.material", "BARRIER"));
        ItemStack closeButton = new ItemStack(closeMat);
        ItemMeta closeMeta = closeButton.getItemMeta();
        if (closeMeta != null) {
            closeMeta.setDisplayName(MessageUtils.colorize(config.getString("items.close_button.name", "&c&lClose")));
            List<String> lore = new ArrayList<>();
            for (String line : config.getStringList("items.close_button.lore")) {
                lore.add(MessageUtils.colorize(line));
            }
            closeMeta.setLore(lore);
            closeMeta.setCustomModelData(config.getInt("items.close_button.custom_model_data", 0));
            closeButton.setItemMeta(closeMeta);
        }
        inv.setItem(size - 1, closeButton);

        ItemStack backBtn = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backBtn.getItemMeta();
        if (backMeta != null) {
            backMeta.setDisplayName(MessageUtils.colorize("&e&lReturn to Menu"));
            backBtn.setItemMeta(backMeta);
        }
        inv.setItem(size - 2, backBtn);

        player.openInventory(inv);
    }

    public void openRodShop(Player player) {
        org.bukkit.configuration.file.FileConfiguration config = Main.getInstance().getConfig();
        int size = config.getInt("gui.sizes.rod_shop", 54);
        Inventory inv = Bukkit.createInventory(null, size, getRodShopTitle());

        if (config.getBoolean("items.filler.enabled", true)) {
            Material fillerMat = Material.valueOf(config.getString("items.filler.material", "GRAY_STAINED_GLASS_PANE"));
            ItemStack glass = new ItemStack(fillerMat);
            ItemMeta glassMeta = glass.getItemMeta();
            if (glassMeta != null) {
                glassMeta.setDisplayName(MessageUtils.colorize(config.getString("items.filler.name", " ")));
                glassMeta.setCustomModelData(config.getInt("items.filler.custom_model_data", 0));
                glass.setItemMeta(glassMeta);
            }
            for (int i = 0; i < size; i++) {
                inv.setItem(i, glass);
            }
        }

        List<com.meowchan12.fishingaudition.manager.RodData> rods = Main.getInstance().getRodManager().getAllRods();
        int slot = 10;
        
        for (com.meowchan12.fishingaudition.manager.RodData data : rods) {
            if (data.getId().equals("starter_rod")) continue;
            if (slot >= size - 2) break;
            if (slot % 9 == 8) {
                slot += 2;
            }
            if (slot >= size - 2) break;

            ItemStack rodItem = CustomRod.generateRod(data);
            if (rodItem != null) {
                ItemMeta meta = rodItem.getItemMeta();
                if (meta != null) {
                    List<String> lore = meta.getLore();
                    if (lore == null) lore = new ArrayList<>();
                    lore.add("");
                    
                    int requiredLevel = data.getRequiredLevel();
                    lore.add(MessageUtils.getRawMessage("minigame.required_level", "&cRequired Level: &f" + requiredLevel));
                    
                    if (Main.getInstance().getPlayerDataManager().hasRod(player, data.getId())) {
                        lore.add(MessageUtils.colorize("&a[Left-Click] &7to EQUIP"));
                    } else {
                        lore.add(MessageUtils.colorize("&ePrice: &f" + data.getPrice() + " FishCoins"));
                        lore.add(MessageUtils.colorize("&a[Left-Click] &7to purchase"));
                    }
                    meta.setLore(lore);
                    rodItem.setItemMeta(meta);
                }
                inv.setItem(slot, rodItem);
            }
            slot++;
        }

        // Load Baits
        for (com.meowchan12.fishingaudition.manager.BaitData bData : Main.getInstance().getBaitManager().getAllBaits()) {
            if (bData != null && bData.getSlot() >= 0 && bData.getSlot() < size - 2) {
                ItemStack baitItem = Main.getInstance().getBaitManager().generateBait(bData.getId());
                if (baitItem != null) {
                    ItemMeta meta = baitItem.getItemMeta();
                    if (meta != null) {
                        List<String> lore = meta.getLore();
                        if (lore == null) lore = new ArrayList<>();
                        lore.add("");
                        lore.add(MessageUtils.colorize("&ePrice: &f" + bData.getPrice() + " FishCoins"));
                        lore.add(MessageUtils.colorize("&a[Left-Click] &7to purchase"));
                        meta.setLore(lore);
                        
                        // Mark it as a shop item with PDC so we know price
                        meta.getPersistentDataContainer().set(new org.bukkit.NamespacedKey(Main.getInstance(), "shop_price"), org.bukkit.persistence.PersistentDataType.DOUBLE, bData.getPrice());
                        
                        baitItem.setItemMeta(meta);
                    }
                    inv.setItem(bData.getSlot(), baitItem);
                }
            }
        }

        Material closeMat = Material.valueOf(config.getString("items.close_button.material", "BARRIER"));
        ItemStack closeButton = new ItemStack(closeMat);
        ItemMeta closeMeta = closeButton.getItemMeta();
        if (closeMeta != null) {
            closeMeta.setDisplayName(MessageUtils.colorize(config.getString("items.close_button.name", "&c&lClose")));
            List<String> lore = new ArrayList<>();
            for (String line : config.getStringList("items.close_button.lore")) {
                lore.add(MessageUtils.colorize(line));
            }
            closeMeta.setLore(lore);
            closeMeta.setCustomModelData(config.getInt("items.close_button.custom_model_data", 0));
            closeButton.setItemMeta(closeMeta);
        }
        inv.setItem(size - 1, closeButton);

        ItemStack backBtn = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backBtn.getItemMeta();
        if (backMeta != null) {
            backMeta.setDisplayName(MessageUtils.colorize("&e&lReturn to Menu"));
            backBtn.setItemMeta(backMeta);
        }
        inv.setItem(size - 2, backBtn);

        player.openInventory(inv);
    }
    
    /**
     * Forcefully close all shop inventories to prevent bugs on reload/disable.
     */
    public void forceCloseAll() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getOpenInventory() != null) {
                String title = player.getOpenInventory().getTitle();
                if (title.equals(getCurrencyShopTitle()) || title.equals(getRodShopTitle())) {
                    player.closeInventory();
                }
            }
        }
    }
}
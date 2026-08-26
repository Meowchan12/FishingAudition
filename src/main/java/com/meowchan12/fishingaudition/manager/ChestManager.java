package com.meowchan12.fishingaudition.manager;

import com.meowchan12.fishingaudition.Main;
import com.meowchan12.fishingaudition.material.CustomFish;
import com.meowchan12.fishingaudition.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ChestManager {

    private final Main plugin;

    public ChestManager(Main plugin) {
        this.plugin = plugin;
    }

    public void sweepFishToChest(Player player) {
        List<ItemStack> sweptItems = new ArrayList<>();
        boolean foundFish = false;

        for (int i = 0; i < player.getInventory().getSize(); i++) {
            ItemStack item = player.getInventory().getItem(i);
            if (CustomFish.isCustomFish(item)) {
                sweptItems.add(item.clone());
                player.getInventory().setItem(i, null);
                foundFish = true;
            }
        }

        if (foundFish) {
            java.util.UUID uuid = player.getUniqueId();
            com.meowchan12.fishingaudition.utils.SchedulerUtils.runAsync(plugin, () -> {
                List<ItemStack> existing = loadChestSync(uuid);
                existing.addAll(sweptItems);
                
                if (existing.size() > 53) {
                    List<ItemStack> keep = new ArrayList<>(existing.subList(0, 53));
                    List<ItemStack> overflow = new ArrayList<>(existing.subList(53, existing.size()));
                    
                    double totalEarned = 0.0;
                    int fishCount = 0;
                    for (ItemStack fish : overflow) {
                        if (CustomFish.isCustomFish(fish)) {
                            totalEarned += (CustomFish.getFishValue(fish) * fish.getAmount());
                            fishCount += fish.getAmount();
                        }
                    }
                    saveChestSync(uuid, keep);
                    
                    if (totalEarned > 0) {
                        double finalEarned = totalEarned;
                        int finalCount = fishCount;
                        com.meowchan12.fishingaudition.utils.SchedulerUtils.runAtEntity(plugin, player, () -> {
                            com.meowchan12.fishingaudition.currencymanager.FishCoinManager.addBalance(player, finalEarned);
                            player.sendMessage(MessageUtils.getMessage("success.chest_full_autosell")
                                    .replace("{count}", String.valueOf(finalCount))
                                    .replace("{earned}", String.format("%.2f", finalEarned)));
                        });
                    }
                } else {
                    saveChestSync(uuid, existing);
                    com.meowchan12.fishingaudition.utils.SchedulerUtils.runAtEntity(plugin, player, () -> {
                        player.sendMessage(MessageUtils.getMessage("success.fish_moved_to_chest"));
                    });
                }
            });
        }
    }

    public void sweepFishToChestSync(Player player) {
        List<ItemStack> sweptItems = new ArrayList<>();
        boolean foundFish = false;

        for (int i = 0; i < player.getInventory().getSize(); i++) {
            ItemStack item = player.getInventory().getItem(i);
            if (CustomFish.isCustomFish(item)) {
                sweptItems.add(item.clone());
                player.getInventory().setItem(i, null);
                foundFish = true;
            }
        }

        if (foundFish) {
            java.util.UUID uuid = player.getUniqueId();
            List<ItemStack> existing = loadChestSync(uuid);
            existing.addAll(sweptItems);
            
            if (existing.size() > 53) {
                List<ItemStack> keep = new ArrayList<>(existing.subList(0, 53));
                List<ItemStack> overflow = new ArrayList<>(existing.subList(53, existing.size()));
                
                double totalEarned = 0.0;
                for (ItemStack fish : overflow) {
                    if (CustomFish.isCustomFish(fish)) {
                        totalEarned += (CustomFish.getFishValue(fish) * fish.getAmount());
                    }
                }
                saveChestSync(uuid, keep);
                com.meowchan12.fishingaudition.currencymanager.FishCoinManager.addBalance(player, totalEarned);
            } else {
                saveChestSync(uuid, existing);
            }
        }
    }

    public void openChest(Player player) {
        java.util.UUID uuid = player.getUniqueId();
        com.meowchan12.fishingaudition.utils.SchedulerUtils.runAsync(plugin, () -> {
            List<ItemStack> chestItems = loadChestSync(uuid);
            
            com.meowchan12.fishingaudition.utils.SchedulerUtils.runAtEntity(plugin, player, () -> {
                org.bukkit.configuration.file.FileConfiguration config = plugin.getConfig();
                int size = config.getInt("gui.sizes.chest", 54);
                String titlePath = config.getString("gui.titles.chest", "&8Your Virtual Fish Chest");
                String title = MessageUtils.colorize(titlePath);

                Inventory inv = Bukkit.createInventory(null, size, title);

                if (config.getBoolean("items.filler.enabled", true)) {
                    Material fillerMat = Material.valueOf(config.getString("items.filler.material", "GRAY_STAINED_GLASS_PANE"));
                    ItemStack filler = new ItemStack(fillerMat);
                    ItemMeta fillerMeta = filler.getItemMeta();
                    if (fillerMeta != null) {
                        fillerMeta.setDisplayName(MessageUtils.colorize(config.getString("items.filler.name", " ")));
                        fillerMeta.setCustomModelData(config.getInt("items.filler.custom_model_data", 0));
                        filler.setItemMeta(fillerMeta);
                    }
                    for (int i = 0; i < size; i++) {
                        inv.setItem(i, filler);
                    }
                } else {
                    for (int i = 0; i < size; i++) inv.setItem(i, null);
                }

                for (int i = 0; i < Math.min(chestItems.size(), size - 3); i++) {
                    inv.setItem(i, chestItems.get(i));
                }

                Material sellMat = Material.valueOf(config.getString("items.sell_all.material", "EMERALD"));
                ItemStack sellAllBtn = new ItemStack(sellMat);
                ItemMeta meta = sellAllBtn.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName(MessageUtils.colorize(config.getString("items.sell_all.name", "&a&lSELL ALL FISH")));
                    List<String> lore = new ArrayList<>();
                    for (String line : config.getStringList("items.sell_all.lore")) {
                        lore.add(MessageUtils.colorize(line));
                    }
                    meta.setLore(lore);
                    meta.setCustomModelData(config.getInt("items.sell_all.custom_model_data", 0));
                    sellAllBtn.setItemMeta(meta);
                }
                inv.setItem(size - 3, sellAllBtn);

                ItemStack backBtn = new ItemStack(Material.ARROW);
                ItemMeta backMeta = backBtn.getItemMeta();
                if (backMeta != null) {
                    backMeta.setDisplayName(MessageUtils.colorize("&a&lReturn to Menu"));
                    backBtn.setItemMeta(backMeta);
                }
                inv.setItem(size - 2, backBtn);

                Material closeMat = Material.valueOf(config.getString("items.close_button.material", "BARRIER"));
                ItemStack closeBtn = new ItemStack(closeMat);
                ItemMeta closeMeta = closeBtn.getItemMeta();
                if (closeMeta != null) {
                    closeMeta.setDisplayName(MessageUtils.colorize(config.getString("items.close_button.name", "&c&lClose")));
                    closeMeta.setCustomModelData(config.getInt("items.close_button.custom_model_data", 0));
                    closeBtn.setItemMeta(closeMeta);
                }
                inv.setItem(size - 1, closeBtn);

                player.openInventory(inv);
            });
        });
    }

    // --- Synchronous Database Operations (Call these from Async Threads) ---

    public List<ItemStack> loadChestSync(java.util.UUID uuid) {
        List<ItemStack> items = new ArrayList<>();
        String sql = "SELECT item_data FROM fishing_chest WHERE uuid = ?";
        try (java.sql.Connection conn = plugin.getDatabaseManager().getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
             
            ps.setString(1, uuid.toString());
            try (java.sql.ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String data = rs.getString("item_data");
                    if (data != null && !data.isEmpty()) {
                        ItemStack[] parsed = com.meowchan12.fishingaudition.utils.ItemSerializer.itemStackArrayFromBase64(data);
                        for (ItemStack item : parsed) {
                            if (item != null) items.add(item);
                        }
                    }
                }
            }
        } catch (java.sql.SQLException e) {
            plugin.getLogger().severe("Failed to load chest for " + uuid + ": " + e.getMessage());
        }
        return items;
    }

    public void saveChestSync(java.util.UUID uuid, List<ItemStack> items) {
        String data = com.meowchan12.fishingaudition.utils.ItemSerializer.itemStackArrayToBase64(items.toArray(new ItemStack[0]));
        String dbType = plugin.getDatabaseManager().getType();
        String sql;
        if (dbType.equals("mysql")) {
            sql = "INSERT INTO fishing_chest (uuid, item_data) VALUES (?, ?) ON DUPLICATE KEY UPDATE item_data = ?";
        } else {
            sql = "INSERT OR REPLACE INTO fishing_chest (uuid, item_data) VALUES (?, ?)";
        }
        
        try (java.sql.Connection conn = plugin.getDatabaseManager().getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
             
            ps.setString(1, uuid.toString());
            ps.setString(2, data);
            if (dbType.equals("mysql")) {
                ps.setString(3, data);
            }
            ps.executeUpdate();
        } catch (java.sql.SQLException e) {
            plugin.getLogger().severe("Failed to save chest for " + uuid + ": " + e.getMessage());
        }
    }

    // Called from ChestListener when player closes the inventory
    public void saveChest(Player player, List<ItemStack> items) {
        java.util.UUID uuid = player.getUniqueId();
        com.meowchan12.fishingaudition.utils.SchedulerUtils.runAsync(plugin, () -> {
            saveChestSync(uuid, items);
        });
    }

    public void clearChest(Player player) {
        java.util.UUID uuid = player.getUniqueId();
        com.meowchan12.fishingaudition.utils.SchedulerUtils.runAsync(plugin, () -> {
            String sql = "DELETE FROM fishing_chest WHERE uuid = ?";
            try (java.sql.Connection conn = plugin.getDatabaseManager().getConnection();
                 java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, uuid.toString());
                ps.executeUpdate();
            } catch (java.sql.SQLException e) {
                plugin.getLogger().severe("Failed to clear chest for " + uuid + ": " + e.getMessage());
            }
        });
    }

    public void saveAllChests() {
        // Called synchronously in onDisable.
        for (Player player : Bukkit.getOnlinePlayers()) {
            sweepFishToChestSync(player);
        }
    }

    public void startAutoSaveTask() {
        com.meowchan12.fishingaudition.utils.SchedulerUtils.runTimerAsync(plugin, () -> {
            com.meowchan12.fishingaudition.utils.SchedulerUtils.runTask(plugin, () -> {
                for (Player player : org.bukkit.Bukkit.getOnlinePlayers()) {
                    // Logic already auto-saves on change, so this is just a fallback for mass data
                }
            });
        }, 18000L, 18000L);
    }
}
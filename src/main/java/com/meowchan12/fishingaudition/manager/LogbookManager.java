package com.meowchan12.fishingaudition.manager;

import com.meowchan12.fishingaudition.Main;
import com.meowchan12.fishingaudition.material.CustomFish;
import com.meowchan12.fishingaudition.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class LogbookManager {

    private final Main plugin;
    private final Map<UUID, Set<String>> unlockedFishes = new ConcurrentHashMap<>();
    public static String getLogbookTitle() {
        return MessageUtils.colorize(Main.getInstance().getConfig().getString("gui.titles.logbook", "&8Fish Logbook"));
    }

    public LogbookManager(Main plugin) {
        this.plugin = plugin;
    }

    public void loadData(Player player) {
        UUID uuid = player.getUniqueId();
        com.meowchan12.fishingaudition.utils.SchedulerUtils.runAsync(plugin, () -> {
            Set<String> fishes = new HashSet<>();
            String sql = "SELECT fish_id FROM fishing_logbook WHERE uuid = ?";
            try (java.sql.Connection conn = plugin.getDatabaseManager().getConnection();
                 java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, uuid.toString());
                try (java.sql.ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        fishes.add(rs.getString("fish_id"));
                    }
                }
            } catch (java.sql.SQLException e) {
                plugin.getLogger().severe("Failed to load logbook for " + player.getName() + ": " + e.getMessage());
            }
            
            // Apply back to main thread or safely set in concurrent structures
            com.meowchan12.fishingaudition.utils.SchedulerUtils.runAtEntity(plugin, player, () -> {
                unlockedFishes.put(uuid, fishes);
            });
        });
    }

    public void unloadData(Player player) {
        unlockedFishes.remove(player.getUniqueId());
    }

    public void setUnlockedFishes(Player player, Set<String> fishes) {
        unlockedFishes.put(player.getUniqueId(), fishes);
    }

    public Set<String> getUnlockedFishes(Player player) {
        return unlockedFishes.getOrDefault(player.getUniqueId(), new HashSet<>());
    }

    public void unlockFish(Player player, String fishId) {
        UUID uuid = player.getUniqueId();
        Set<String> unlocked = unlockedFishes.computeIfAbsent(uuid, k -> new HashSet<>());
        if (!unlocked.contains(fishId)) {
            unlocked.add(fishId);
        }

        com.meowchan12.fishingaudition.utils.SchedulerUtils.runAsync(plugin, () -> {
            String dbType = plugin.getDatabaseManager().getType();
            String sql;
            if (dbType.equals("mysql")) {
                sql = "INSERT INTO fishing_logbook (uuid, fish_id, catch_count) VALUES (?, ?, 1) " +
                      "ON DUPLICATE KEY UPDATE catch_count = catch_count + 1";
            } else {
                sql = "INSERT OR REPLACE INTO fishing_logbook (uuid, fish_id, catch_count) VALUES (?, ?, " +
                      "COALESCE((SELECT catch_count FROM fishing_logbook WHERE uuid = ? AND fish_id = ?) + 1, 1))";
            }
            try (java.sql.Connection conn = plugin.getDatabaseManager().getConnection();
                 java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
                 
                ps.setString(1, uuid.toString());
                ps.setString(2, fishId);
                if (dbType.equals("sqlite")) {
                    ps.setString(3, uuid.toString());
                    ps.setString(4, fishId);
                }
                ps.executeUpdate();
            } catch (java.sql.SQLException e) {
                plugin.getLogger().severe("Failed to save logbook for " + player.getName() + ": " + e.getMessage());
            }
        });
    }

    public void openGUI(Player player) {
        Collection<FishData> allFishes = plugin.getFishManager().getAllFish();
        org.bukkit.configuration.file.FileConfiguration config = plugin.getConfig();
        int size = config.getInt("gui.sizes.logbook", 54);

        Inventory inv = Bukkit.createInventory(null, size, getLogbookTitle());
        Set<String> unlocked = getUnlockedFishes(player);

        int slot = 0;
        for (FishData fish : allFishes) {
            if (slot >= size - 2) break;

            ItemStack item;
            if (unlocked.contains(fish.getId())) {
                item = CustomFish.generateFish(fish);
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    List<String> lore = meta.getLore();
                    if (lore == null) lore = new ArrayList<>();
                    lore.add("");
                    lore.add(MessageUtils.colorize("&a&lUNLOCKED"));
                    meta.setLore(lore);
                    item.setItemMeta(meta);
                }
            } else {
                item = new ItemStack(Material.COAL);
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName(MessageUtils.colorize("&8???"));
                    List<String> lore = new ArrayList<>();
                    lore.add(MessageUtils.colorize("&cYou haven't caught this fish yet!"));
                    lore.add(MessageUtils.colorize("&7Tier: " + fish.getTier().getDisplayName()));
                    meta.setLore(lore);
                    item.setItemMeta(meta);
                }
            }

            inv.setItem(slot, item);
            slot++;
        }

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
    }
}

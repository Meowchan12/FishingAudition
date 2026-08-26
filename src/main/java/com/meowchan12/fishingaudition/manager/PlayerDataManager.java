package com.meowchan12.fishingaudition.manager;

import com.meowchan12.fishingaudition.Main;
import com.meowchan12.fishingaudition.currencymanager.FishCoinManager;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerDataManager {

    private final Main plugin;
    // Lưu trữ kỷ lục (Top Round) của từng người chơi
    private final Map<UUID, Integer> topRounds = new ConcurrentHashMap<>();
    private final Map<UUID, java.util.List<String>> ownedRods = new ConcurrentHashMap<>();
    private final Map<UUID, String> equippedRods = new ConcurrentHashMap<>();
    private final Map<UUID, String> equippedBaits = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> baitCharges = new ConcurrentHashMap<>();

    public PlayerDataManager(Main plugin) {
        this.plugin = plugin;
    }

    public void loadData(Player player) {
        UUID uuid = player.getUniqueId();
        
        com.meowchan12.fishingaudition.utils.SchedulerUtils.runAsync(plugin, () -> {
            String sql = "SELECT * FROM fishing_users WHERE uuid = ?";
            try (java.sql.Connection conn = plugin.getDatabaseManager().getConnection();
                 java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
                 
                ps.setString(1, uuid.toString());
                try (java.sql.ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        double coins = rs.getDouble("fishcoins");
                        int topRound = rs.getInt("top_round");
                        int level = rs.getInt("level");
                        double xp = rs.getDouble("xp");
                        String ownedRodsStr = rs.getString("owned_rods");
                        String equippedRod = rs.getString("equipped_rod");
                        String equippedBait = null;
                        int charges = 0;
                        try {
                            equippedBait = rs.getString("equipped_bait");
                            charges = rs.getInt("bait_charges");
                        } catch (java.sql.SQLException ignored) {}
                        
                        java.util.List<String> rodsList = new java.util.ArrayList<>();
                        if (ownedRodsStr != null && !ownedRodsStr.isEmpty()) {
                            rodsList.addAll(java.util.Arrays.asList(ownedRodsStr.split(",")));
                        } else {
                            rodsList.add("starter_rod");
                        }
                        
                        if (equippedRod == null || equippedRod.isEmpty()) {
                            equippedRod = "starter_rod";
                        }
                        
                        final String finalEquipped = equippedRod;
                        final String finalBait = equippedBait;
                        final int finalCharges = charges;
                        
                        // Apply back to main thread or safely set in concurrent structures
                        com.meowchan12.fishingaudition.utils.SchedulerUtils.runAtEntity(plugin, player, () -> {
                            topRounds.put(uuid, topRound);
                            plugin.getLevelManager().setLevel(player, level);
                            plugin.getLevelManager().setXP(player, xp);
                            FishCoinManager.setBalance(player, coins);
                            ownedRods.put(uuid, rodsList);
                            equippedRods.put(uuid, finalEquipped);
                            if (finalBait != null && !finalBait.isEmpty()) {
                                equippedBaits.put(uuid, finalBait);
                                baitCharges.put(uuid, finalCharges);
                            }
                        });
                    } else {
                        com.meowchan12.fishingaudition.utils.SchedulerUtils.runAtEntity(plugin, player, () -> {
                            FishCoinManager.setBalance(player, 0.0);
                            topRounds.put(uuid, 0);
                            
                            java.util.List<String> defaultRods = new java.util.ArrayList<>();
                            defaultRods.add("starter_rod");
                            ownedRods.put(uuid, defaultRods);
                            equippedRods.put(uuid, "starter_rod");
                        });
                    }
                }
            } catch (java.sql.SQLException e) {
                plugin.getLogger().severe("Failed to load player data for " + player.getName() + ": " + e.getMessage());
            }
        });
    }

    public void saveData(Player player, boolean async) {
        UUID uuid = player.getUniqueId();
        double currentCoins = FishCoinManager.getBalance(player);
        int topRound = topRounds.getOrDefault(uuid, 0);
        int level = plugin.getLevelManager() != null ? plugin.getLevelManager().getLevel(player) : 1;
        double xp = plugin.getLevelManager() != null ? plugin.getLevelManager().getXP(player) : 0.0;
        String playerName = player.getName();
        String dbType = plugin.getDatabaseManager().getType();

        Runnable saveTask = () -> {
            String rodsStr = String.join(",", ownedRods.getOrDefault(uuid, java.util.Collections.singletonList("starter_rod")));
            String equippedRod = equippedRods.getOrDefault(uuid, "starter_rod");
            String equippedBait = equippedBaits.get(uuid);
            int charges = baitCharges.getOrDefault(uuid, 0);
            
            String sql;
            if (dbType.equals("mysql")) {
                sql = "INSERT INTO fishing_users (uuid, name, fishcoins, top_round, level, xp, owned_rods, equipped_rod, equipped_bait, bait_charges) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                      "ON DUPLICATE KEY UPDATE name=?, fishcoins=?, top_round=?, level=?, xp=?, owned_rods=?, equipped_rod=?, equipped_bait=?, bait_charges=?";
            } else {
                sql = "INSERT OR REPLACE INTO fishing_users (uuid, name, fishcoins, top_round, level, xp, owned_rods, equipped_rod, equipped_bait, bait_charges) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            }
            
            try (java.sql.Connection conn = plugin.getDatabaseManager().getConnection();
                 java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
                 
                ps.setString(1, uuid.toString());
                ps.setString(2, playerName);
                ps.setDouble(3, currentCoins);
                ps.setInt(4, topRound);
                ps.setInt(5, level);
                ps.setDouble(6, xp);
                ps.setString(7, rodsStr);
                ps.setString(8, equippedRod);
                ps.setString(9, equippedBait);
                ps.setInt(10, charges);
                
                if (dbType.equals("mysql")) {
                    ps.setString(11, playerName);
                    ps.setDouble(12, currentCoins);
                    ps.setInt(13, topRound);
                    ps.setInt(14, level);
                    ps.setDouble(15, xp);
                    ps.setString(16, rodsStr);
                    ps.setString(17, equippedRod);
                    ps.setString(18, equippedBait);
                    ps.setInt(19, charges);
                }
                
                ps.executeUpdate();
            } catch (java.sql.SQLException e) {
                plugin.getLogger().severe("Failed to save player data for " + playerName + ": " + e.getMessage());
            }
        };

        if (async) {
            com.meowchan12.fishingaudition.utils.SchedulerUtils.runAsync(plugin, saveTask);
        } else {
            saveTask.run();
        }
    }

    public void checkAndSetTopRound(Player player, int round) {
        int currentTop = topRounds.getOrDefault(player.getUniqueId(), 0);
        if (round > currentTop) {
            topRounds.put(player.getUniqueId(), round);
        }
    }

    public int getTopRound(Player player) {
        return topRounds.getOrDefault(player.getUniqueId(), 0);
    }

    public boolean hasRod(Player player, String rodId) {
        java.util.List<String> rods = ownedRods.get(player.getUniqueId());
        return rods != null && rods.contains(rodId);
    }

    public void addOwnedRod(Player player, String rodId) {
        ownedRods.computeIfAbsent(player.getUniqueId(), k -> new java.util.ArrayList<>()).add(rodId);
    }

    public String getEquippedRod(Player player) {
        return equippedRods.getOrDefault(player.getUniqueId(), "starter_rod");
    }

    public void setEquippedRod(Player player, String rodId) {
        equippedRods.put(player.getUniqueId(), rodId);
    }

    public void saveAllData() {
        for (Player player : org.bukkit.Bukkit.getOnlinePlayers()) {
            saveData(player, false);
        }
    }

    public void startAutoSaveTask() {
        com.meowchan12.fishingaudition.utils.SchedulerUtils.runTimerAsync(plugin, () -> {
            // Must be run synchronously if calling saveAllData() that accesses Bukkit APIs like getOnlinePlayers()
            com.meowchan12.fishingaudition.utils.SchedulerUtils.runTask(plugin, () -> {
                for (Player player : org.bukkit.Bukkit.getOnlinePlayers()) {
                    saveData(player, true);
                }
                plugin.getLogger().info("Auto-saved all player data (Async DB).");
            });
        }, 18000L, 18000L);
    }

    public double getOfflineBalance(String playerName) {
        String sql = "SELECT fishcoins FROM fishing_users WHERE name = ?";
        try (java.sql.Connection conn = plugin.getDatabaseManager().getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, playerName);
            try (java.sql.ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("fishcoins");
                }
            }
        } catch (java.sql.SQLException e) {
            plugin.getLogger().severe("Failed to get offline balance for " + playerName + ": " + e.getMessage());
        }
        return -1.0;
    }

    public void unloadData(Player player) {
        topRounds.remove(player.getUniqueId());
        preJoinLocations.remove(player.getUniqueId());
    }

    private final Map<UUID, org.bukkit.Location> preJoinLocations = new ConcurrentHashMap<>();

    public void setPreJoinLocation(Player player, org.bukkit.Location loc) {
        if (loc != null) {
            preJoinLocations.put(player.getUniqueId(), loc.clone());
        }
    }

    public org.bukkit.Location getPreJoinLocation(Player player) {
        return preJoinLocations.get(player.getUniqueId());
    }

    public void removePreJoinLocation(Player player) {
        preJoinLocations.remove(player.getUniqueId());
    }

    public String getEquippedBait(Player player) {
        return equippedBaits.get(player.getUniqueId());
    }

    public void setEquippedBait(Player player, String baitId, int charges) {
        equippedBaits.put(player.getUniqueId(), baitId);
        baitCharges.put(player.getUniqueId(), charges);
    }

    public int getBaitCharges(Player player) {
        return baitCharges.getOrDefault(player.getUniqueId(), 0);
    }

    public void setBaitCharges(Player player, int charges) {
        baitCharges.put(player.getUniqueId(), charges);
    }

    public void removeBait(Player player) {
        equippedBaits.remove(player.getUniqueId());
        baitCharges.remove(player.getUniqueId());
    }
}
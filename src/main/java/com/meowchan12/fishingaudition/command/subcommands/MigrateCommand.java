package com.meowchan12.fishingaudition.command.subcommands;

import com.meowchan12.fishingaudition.Main;
import com.meowchan12.fishingaudition.command.SubCommand;
import com.meowchan12.fishingaudition.utils.MessageUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.List;

public class MigrateCommand extends SubCommand {

    public MigrateCommand() {
    }

    @Override
    public String getName() {
        return "migrate";
    }

    @Override
    public String getDescription() {
        return "Migrate YAML data to Database";
    }

    @Override
    public String getSyntax() {
        return "/fish migrate";
    }

    @Override
    public String getPermission() {
        return "fishingaudition.admin";
    }

    @Override
    public void perform(Player player, String[] args) {
        Main plugin = Main.getInstance();
        File dataFolder = new File(plugin.getDataFolder(), "playerdata");
        if (!dataFolder.exists() || !dataFolder.isDirectory()) {
            player.sendMessage(MessageUtils.colorize("&cNo playerdata folder found. Migration not needed."));
            return;
        }

        player.sendMessage(MessageUtils.colorize("&aStarting data migration to Database... This may take a moment."));

        new org.bukkit.scheduler.BukkitRunnable() {
            @Override
            public void run() {
                File[] files = dataFolder.listFiles();
                if (files == null) return;

                int userCount = 0;
                int chestCount = 0;
                String dbType = plugin.getDatabaseManager().getType();

                for (File file : files) {
                    if (!file.getName().endsWith(".yml")) continue;

                    String fileName = file.getName().replace(".yml", "");
                    
                    if (fileName.endsWith("_chest")) {
                        // Chest Migration
                        String uuidStr = fileName.replace("_chest", "");
                        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
                        List<?> savedList = config.getList("chest_contents");
                        
                        if (savedList != null && !savedList.isEmpty()) {
                            List<ItemStack> items = new java.util.ArrayList<>();
                            for (Object obj : savedList) {
                                if (obj instanceof ItemStack) {
                                    items.add((ItemStack) obj);
                                }
                            }
                            
                            if (!items.isEmpty()) {
                                String base64 = com.meowchan12.fishingaudition.utils.ItemSerializer.itemStackArrayToBase64(items.toArray(new ItemStack[0]));
                                String sql = dbType.equals("mysql") ? 
                                    "INSERT INTO fishing_chest (uuid, item_data) VALUES (?, ?) ON DUPLICATE KEY UPDATE item_data = ?" :
                                    "INSERT OR REPLACE INTO fishing_chest (uuid, item_data) VALUES (?, ?)";
                                    
                                try (java.sql.Connection conn = Main.getInstance().getDatabaseManager().getConnection();
                                     java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
                                    ps.setString(1, uuidStr);
                                    ps.setString(2, base64);
                                    if (dbType.equals("mysql")) ps.setString(3, base64);
                                    ps.executeUpdate();
                                    chestCount++;
                                } catch (Exception e) {
                                    Main.getInstance().getLogger().severe("Failed to migrate chest for " + uuidStr + ": " + e.getMessage());
                                }
                            }
                        }
                    } else {
                        // User Migration
                        String uuidStr = fileName;
                        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
                        
                        String name = config.getString("name", "Unknown");
                        double coins = config.getDouble("fishcoins", 0.0);
                        int topRound = config.getInt("top_round", 0);
                        int level = config.getInt("level", 1);
                        double xp = config.getDouble("xp", 0.0);
                        List<String> unlockedFishes = config.getStringList("unlocked_fishes");
                        
                        // Insert User
                        String sqlUser = dbType.equals("mysql") ? 
                            "INSERT INTO fishing_users (uuid, name, fishcoins, top_round, level, xp) VALUES (?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE name=?, fishcoins=?, top_round=?, level=?, xp=?" :
                            "INSERT OR REPLACE INTO fishing_users (uuid, name, fishcoins, top_round, level, xp) VALUES (?, ?, ?, ?, ?, ?)";
                            
                        try (java.sql.Connection conn = Main.getInstance().getDatabaseManager().getConnection();
                             java.sql.PreparedStatement ps = conn.prepareStatement(sqlUser)) {
                            ps.setString(1, uuidStr);
                            ps.setString(2, name);
                            ps.setDouble(3, coins);
                            ps.setInt(4, topRound);
                            ps.setInt(5, level);
                            ps.setDouble(6, xp);
                            if (dbType.equals("mysql")) {
                                ps.setString(7, name);
                                ps.setDouble(8, coins);
                                ps.setInt(9, topRound);
                                ps.setInt(10, level);
                                ps.setDouble(11, xp);
                            }
                            ps.executeUpdate();
                            userCount++;
                        } catch (Exception e) {
                            Main.getInstance().getLogger().severe("Failed to migrate user " + uuidStr + ": " + e.getMessage());
                        }
                        
                        // Insert Logbook
                        if (unlockedFishes != null && !unlockedFishes.isEmpty()) {
                            String sqlLogbook = dbType.equals("mysql") ?
                                "INSERT INTO fishing_logbook (uuid, fish_id, catch_count) VALUES (?, ?, 1) ON DUPLICATE KEY UPDATE catch_count = catch_count + 1" :
                                "INSERT OR REPLACE INTO fishing_logbook (uuid, fish_id, catch_count) VALUES (?, ?, COALESCE((SELECT catch_count FROM fishing_logbook WHERE uuid = ? AND fish_id = ?) + 1, 1))";
                                
                            try (java.sql.Connection conn = Main.getInstance().getDatabaseManager().getConnection();
                                 java.sql.PreparedStatement ps = conn.prepareStatement(sqlLogbook)) {
                                 
                                for (String fishId : unlockedFishes) {
                                    ps.setString(1, uuidStr);
                                    ps.setString(2, fishId);
                                    if (dbType.equals("sqlite")) {
                                        ps.setString(3, uuidStr);
                                        ps.setString(4, fishId);
                                    }
                                    ps.addBatch();
                                }
                                ps.executeBatch();
                            } catch (Exception e) {
                                Main.getInstance().getLogger().severe("Failed to migrate logbook for " + uuidStr + ": " + e.getMessage());
                            }
                        }
                    }
                }

                // Rename folder
                File oldFolder = new File(Main.getInstance().getDataFolder(), "playerdata_old");
                if (dataFolder.renameTo(oldFolder)) {
                    Main.getInstance().getLogger().info("Successfully renamed playerdata to playerdata_old.");
                } else {
                    Main.getInstance().getLogger().warning("Could not rename playerdata folder. Please rename or delete it manually.");
                }
                
                int finalUserCount = userCount;
                int finalChestCount = chestCount;
                org.bukkit.Bukkit.getScheduler().runTask(Main.getInstance(), () -> {
                    player.sendMessage(MessageUtils.colorize("&aMigration complete! Migrated &e" + finalUserCount + " &ausers and &e" + finalChestCount + " &achests."));
                });
            }
        }.runTaskAsynchronously(Main.getInstance());
    }
}

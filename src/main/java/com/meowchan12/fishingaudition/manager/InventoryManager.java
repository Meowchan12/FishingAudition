package com.meowchan12.fishingaudition.manager;

import com.meowchan12.fishingaudition.Main;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class InventoryManager {

    private final Main plugin;

    public InventoryManager(Main plugin) {
        this.plugin = plugin;
    }

    public boolean hasBackup(Player player) {
        File backupFile = new File(plugin.getDataFolder() + "/backup", player.getUniqueId() + ".yml");
        return backupFile.exists();
    }

    /**
     * Backs up the player's inventory and clears it.
     */
    public void backupAndClear(Player player) {
        File backupFile = new File(plugin.getDataFolder() + "/backup", player.getUniqueId() + ".yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(backupFile);

        // Save inventory contents directly (Bukkit handles ItemStack serialization)
        config.set("inventory", player.getInventory().getContents());
        config.set("armor", player.getInventory().getArmorContents());
        config.set("offhand", player.getInventory().getItemInOffHand());

        try {
            config.save(backupFile);
        } catch (IOException e) {
            plugin.getLogger().log(java.util.logging.Level.SEVERE, "Could not save backup for player: " + player.getName(), e);
            return;
        }

        // Clear inventory after successful backup
        player.getInventory().clear();

        // TODO: Give the player a starter fishing rod if they don't have one in data
    }

    /**
     * Restores the player's inventory from the backup file with a 2-second delay.
     */
    public void restoreInventory(Player player) {
        // Delay 2 seconds (40 ticks) as requested to prevent glitches
        com.meowchan12.fishingaudition.utils.SchedulerUtils.runTaskLaterAtEntity(plugin, player, () -> {
            if (!player.isOnline()) return;

            File backupFile = new File(plugin.getDataFolder() + "/backup", player.getUniqueId() + ".yml");
            if (!backupFile.exists()) return;

            YamlConfiguration config = YamlConfiguration.loadConfiguration(backupFile);

            // Restore items
            List<?> inventoryList = config.getList("inventory");
            if (inventoryList != null) {
                player.getInventory().setContents(inventoryList.toArray(new ItemStack[0]));
            }

            List<?> armorList = config.getList("armor");
            if (armorList != null) {
                player.getInventory().setArmorContents(armorList.toArray(new ItemStack[0]));
            }

            ItemStack offhand = config.getItemStack("offhand");
            if (offhand != null) {
                player.getInventory().setItemInOffHand(offhand);
            }

            // Delete backup file after restoring
            backupFile.delete();

            player.sendMessage(com.meowchan12.fishingaudition.utils.MessageUtils.colorize("&aYour inventory has been restored!"));
        }, 40L);
    }

    /**
     * Restores the player's inventory immediately without delay (for synchronous shutdown).
     */
    public void restoreInventorySync(Player player) {
        if (!player.isOnline()) return;

        File backupFile = new File(plugin.getDataFolder() + "/backup", player.getUniqueId() + ".yml");
        if (!backupFile.exists()) return;

        YamlConfiguration config = YamlConfiguration.loadConfiguration(backupFile);

        List<?> inventoryList = config.getList("inventory");
        if (inventoryList != null) {
            player.getInventory().setContents(inventoryList.toArray(new ItemStack[0]));
        }

        List<?> armorList = config.getList("armor");
        if (armorList != null) {
            player.getInventory().setArmorContents(armorList.toArray(new ItemStack[0]));
        }

        ItemStack offhand = config.getItemStack("offhand");
        if (offhand != null) {
            player.getInventory().setItemInOffHand(offhand);
        }

        backupFile.delete();
    }
}
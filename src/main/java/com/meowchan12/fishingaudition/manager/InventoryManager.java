package com.meowchan12.fishingaudition.manager;

import com.meowchan12.fishingaudition.Main;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.UUID;

public class InventoryManager {

    private final Main plugin;
    private final Map<UUID, ItemStack[]> savedInventories = new ConcurrentHashMap<>();
    private final Map<UUID, ItemStack[]> savedArmors = new ConcurrentHashMap<>();
    private final Map<UUID, ItemStack> savedOffhands = new ConcurrentHashMap<>();

    public InventoryManager(Main plugin) {
        this.plugin = plugin;
    }

    public boolean hasBackup(Player player) {
        return savedInventories.containsKey(player.getUniqueId());
    }

    /**
     * Backs up the player's inventory and clears it.
     */
    public void backupAndClear(Player player) {
        UUID uuid = player.getUniqueId();
        
        savedInventories.put(uuid, player.getInventory().getContents());
        savedArmors.put(uuid, player.getInventory().getArmorContents());
        savedOffhands.put(uuid, player.getInventory().getItemInOffHand());

        // Clear inventory after successful backup
        player.getInventory().clear();
    }

    /**
     * Restores the player's inventory using Atomic Pop.
     */
    public void restoreInventory(Player player) {
        com.meowchan12.fishingaudition.utils.SchedulerUtils.runTaskLaterAtEntity(plugin, player, () -> {
            restoreInventorySync(player);
            if (player.isOnline()) {
                player.sendMessage(com.meowchan12.fishingaudition.utils.MessageUtils.colorize("&aYour inventory has been restored!"));
            }
        }, 40L);
    }

    /**
     * Restores the player's inventory immediately using Atomic Pop.
     */
    public void restoreInventorySync(Player player) {
        if (!player.isOnline()) return;

        UUID uuid = player.getUniqueId();
        
        // Atomic Pop: remove and get at the same time
        ItemStack[] savedContents = savedInventories.remove(uuid);
        ItemStack[] savedArmor = savedArmors.remove(uuid);
        ItemStack offhand = savedOffhands.remove(uuid);

        if (savedContents == null) {
            // Absolutely do not touch current inventory if backup is missing!
            return;
        }

        player.getInventory().setContents(savedContents);
        if (savedArmor != null) player.getInventory().setArmorContents(savedArmor);
        if (offhand != null) player.getInventory().setItemInOffHand(offhand);
    }
}
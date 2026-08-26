package com.meowchan12.fishingaudition.listener;

import com.meowchan12.fishingaudition.Main;
import com.meowchan12.fishingaudition.manager.PlayerDataManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.entity.Player;
import java.util.UUID;

public class DataListener implements Listener {

    private final PlayerDataManager dataManager;

    public DataListener(PlayerDataManager dataManager) {
        this.dataManager = dataManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        dataManager.loadData(player);
        Main.getInstance().getLogbookManager().loadData(player);
        
        // SAFE SHUTDOWN: If a player logs in and has a backup, it means they crashed/logged out mid-session.
        if (Main.getInstance().getInventoryManager() != null && Main.getInstance().getInventoryManager().hasBackup(player)) {
            Main.getInstance().getLogger().info("Player " + player.getName() + " logged in with an active fishing backup. Restoring and teleporting.");
            
            // Clear current inventory (which contains virtual rod)
            player.getInventory().clear();
            
            // Restore actual inventory
            Main.getInstance().getInventoryManager().restoreInventorySync(player);
            
            // Teleport to pre_join_location or fallback
            org.bukkit.Location preLoc = dataManager.getPreJoinLocation(player);
            if (preLoc != null) {
                player.teleport(preLoc);
                dataManager.removePreJoinLocation(player);
            } else if (Main.getInstance().getRegionManager() != null && Main.getInstance().getRegionManager().getLeaveLocation() != null) {
                player.teleport(Main.getInstance().getRegionManager().getLeaveLocation());
            }
            
            if (Main.getInstance().getScoreboardManager() != null) {
                Main.getInstance().getScoreboardManager().removeBoard(player);
            }
            player.sendMessage(com.meowchan12.fishingaudition.utils.MessageUtils.colorize("&cYour previous fishing session was interrupted. Your inventory has been restored."));
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        
        // Save data async to avoid lagging the server
        dataManager.saveData(player, true);
        
        // Unload all from memory to prevent Memory Leaks
        dataManager.unloadData(player);
        Main.getInstance().getLogbookManager().unloadData(player);
        Main.getInstance().getLevelManager().unloadData(player);
        com.meowchan12.fishingaudition.currencymanager.FishCoinManager.unloadData(player);
        
        // End session if stuck
        if (Main.getInstance().getSessionManager().isPlaying(player)) {
            Main.getInstance().getSessionManager().getSession(player).endSession(false);
        }
        
        // Clear chat input state
        if (Main.getInstance().getCommand("fish").getExecutor() instanceof com.meowchan12.fishingaudition.command.CommandManager) {
            // Need a way to clear ExchangeLogic
            Main.getInstance().getExchangeLogic().removeAwaitingInput(player);
        }

        // Remove FastBoard to prevent memory leaks
        if (Main.getInstance().getScoreboardManager() != null) {
            Main.getInstance().getScoreboardManager().removeBoard(player);
        }
    }
}
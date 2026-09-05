package com.meowchan12.fishingaudition.listener;

import com.meowchan12.fishingaudition.Main;
import com.meowchan12.fishingaudition.manager.SessionManager;
import com.meowchan12.fishingaudition.mechanics.AuditionSession;
import com.meowchan12.fishingaudition.utils.MessageUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import java.util.UUID;

public class FishingListener implements Listener {

    private final Main plugin;
    private final SessionManager sessionManager;

    public FishingListener(Main plugin, SessionManager sessionManager) {
        this.plugin = plugin;
        this.sessionManager = sessionManager;
    }

    @EventHandler
    public void onPlayerJoin(org.bukkit.event.player.PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (plugin.getEventManager().isEventActive()) {
            double multi = plugin.getEventManager().getCurrentMultiplier();
            String msg = plugin.getConfig().getString("events.weekend-xp.messages.on-join", "&e&l🌟 WEEKEND EVENT IS ACTIVE! &fEnjoy &a{multiplier}x XP &f!");
            msg = msg.replace("{multiplier}", String.valueOf(multi));
            player.sendMessage(MessageUtils.colorize(msg));
        }
    }

    @EventHandler
    public void onPlayerFish(PlayerFishEvent event) {
        Player player = event.getPlayer();

        // Check if region is set and hook is inside the region
        if (plugin.getRegionManager() != null && plugin.getRegionManager().isSet()) {
            if (event.getHook() != null && !plugin.getRegionManager().isInsideRegion(event.getHook().getLocation())) {
                return;
            }
        }

        if (event.getState() == PlayerFishEvent.State.BITE) {
            event.setCancelled(true);
            if (event.getHook() != null) {
                event.getHook().remove();
            }
            
            // Check Level Requirement
            org.bukkit.inventory.ItemStack rod = player.getInventory().getItemInMainHand();
            if (com.meowchan12.fishingaudition.material.CustomRod.isCustomRod(rod)) {
                org.bukkit.persistence.PersistentDataContainer pdc = rod.getItemMeta().getPersistentDataContainer();
                org.bukkit.NamespacedKey reqLevelKey = new org.bukkit.NamespacedKey(plugin, "required_level");
                int reqLevel = pdc.getOrDefault(reqLevelKey, org.bukkit.persistence.PersistentDataType.INTEGER, 1);
                
                if (plugin.getLevelManager().getLevel(player) < reqLevel) {
                    player.sendMessage(MessageUtils.colorize("&cYou need to be Level " + reqLevel + " to use this rod!"));
                    return;
                }
            }
            
            sessionManager.startSession(player);
            player.sendMessage(MessageUtils.colorize("&aA fish is biting! Pay attention to the hotbar!"));
        }
        else if (event.getState() == PlayerFishEvent.State.CAUGHT_FISH) {
            event.setCancelled(true);
            if (event.getCaught() != null) {
                event.getCaught().remove();
            }
            if (sessionManager.isPlaying(player)) {
                AuditionSession session = sessionManager.getSession(player);
                if (session != null) session.endSession(false);
                sessionManager.removeSession(player);
            }
            player.sendMessage(MessageUtils.colorize("&cYou pulled too late/unexpectedly!"));
            reduceDurability(player);
        }
        else if (event.getState() == PlayerFishEvent.State.FAILED_ATTEMPT) {
            if (sessionManager.isPlaying(player)) {
                AuditionSession session = sessionManager.getSession(player);
                if (session != null) session.endSession(false);
                sessionManager.removeSession(player);
            }
            player.sendMessage(MessageUtils.colorize("&cYou reeled in too late!"));
            reduceDurability(player);
        }
        else if (event.getState() == PlayerFishEvent.State.REEL_IN || event.getState() == PlayerFishEvent.State.IN_GROUND) {
            if (sessionManager.isPlaying(player)) {
                AuditionSession session = sessionManager.getSession(player);
                if (session != null) session.endSession(false);
                sessionManager.removeSession(player);
            }
            player.sendMessage(MessageUtils.colorize("&cYou reeled in too early or cancelled!"));
            reduceDurability(player);
        }
    }

    private void reduceDurability(Player player) {
        org.bukkit.inventory.ItemStack rod = player.getInventory().getItemInMainHand();
        if (com.meowchan12.fishingaudition.material.CustomRod.isCustomRod(rod)) {
            org.bukkit.inventory.meta.Damageable meta = (org.bukkit.inventory.meta.Damageable) rod.getItemMeta();
            if (meta != null) {
                meta.setDamage(meta.getDamage() + 1);
                rod.setItemMeta(meta);
                if (meta.getDamage() >= rod.getType().getMaxDurability()) {
                    player.getInventory().setItemInMainHand(null);
                    player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f);
                    player.sendMessage(MessageUtils.colorize("&cYour fishing rod has broken!"));
                }
            }
        }
    }

    @EventHandler
    public void onPlayerItemHeld(org.bukkit.event.player.PlayerItemHeldEvent event) {
        if (sessionManager.getActiveSessionsCount() == 0) return;
        Player player = event.getPlayer();

        if (sessionManager.isPlaying(player)) {
            event.setCancelled(true);
            int pressedSlot = event.getNewSlot();
            
            AuditionSession session = sessionManager.getSession(player);
            if (session != null) {
                session.handleInput(pressedSlot);
            }
            player.getInventory().setHeldItemSlot(4);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        if (plugin.getPlayerDataManager().isInArena(player)) {
            com.meowchan12.fishingaudition.command.CommandManager.leaveArena(player, "QUIT", true);
        }
    }

    // --- Anti-Exploit Measures ---

    @EventHandler
    public void onPlayerDropItem(org.bukkit.event.player.PlayerDropItemEvent event) {
        org.bukkit.inventory.ItemStack dropped = event.getItemDrop().getItemStack();
        if (com.meowchan12.fishingaudition.material.CustomRod.isCustomRod(dropped)) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(MessageUtils.colorize("&cYou cannot drop your virtual fishing rod!"));
        } else if (sessionManager.getActiveSessionsCount() > 0 && sessionManager.isPlaying(event.getPlayer())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(MessageUtils.colorize("&cYou cannot drop items while fishing!"));
        }
    }

    @EventHandler
    public void onInventoryClick(org.bukkit.event.inventory.InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        
        // Block personal inventory interaction if they are in active players
        if (plugin.getInventoryManager() != null && plugin.getInventoryManager().hasBackup(player)) {
            if (event.getClickedInventory() != null && event.getClickedInventory().getType() == org.bukkit.event.inventory.InventoryType.PLAYER) {
                event.setCancelled(true);
                player.sendMessage(MessageUtils.colorize("&cYou cannot modify your inventory while in the fishing area!"));
                return;
            }
        }
        
        org.bukkit.inventory.ItemStack clicked = event.getCurrentItem();
        
        if (clicked != null && clicked.getType() != org.bukkit.Material.AIR) {
            if (com.meowchan12.fishingaudition.material.CustomRod.isCustomRod(clicked)) {
                // Prevent moving the virtual rod anywhere
                if (plugin.getRegionManager() != null && plugin.getRegionManager().isInsideRegion(player.getLocation())) {
                    event.setCancelled(true);
                    player.sendMessage(MessageUtils.colorize("&cYou cannot move the virtual fishing rod!"));
                }
            }
        }
    }

    @EventHandler
    public void onPlayerCommandPreprocess(org.bukkit.event.player.PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        if (plugin.getInventoryManager() != null && plugin.getInventoryManager().hasBackup(player)) {
            String msg = event.getMessage().toLowerCase();
            if (!msg.startsWith("/fa ") && !msg.equals("/fa") && !msg.startsWith("/fish ") && !msg.equals("/fish")) {
                event.setCancelled(true);
                player.sendMessage(MessageUtils.colorize("&cYou cannot use commands while in the fishing area! Use /fish leave first."));
            }
        }
    }

    @EventHandler
    public void onPlayerDeath(org.bukkit.event.entity.PlayerDeathEvent event) {
        Player player = event.getEntity();
        if (sessionManager.getActiveSessionsCount() > 0 && sessionManager.isPlaying(player)) {
            AuditionSession session = sessionManager.getSession(player);
            if (session != null) session.endSession(false);
            sessionManager.removeSession(player);
        }
        
        // Remove the virtual rod from drops to prevent duplicating
        event.getDrops().removeIf(com.meowchan12.fishingaudition.material.CustomRod::isCustomRod);
        
        // Restore their real inventory drops!
        if (plugin.getInventoryManager() != null && plugin.getInventoryManager().hasBackup(player)) {
            java.io.File backupFile = new java.io.File(plugin.getDataFolder() + "/backup", player.getUniqueId() + ".yml");
            org.bukkit.configuration.file.YamlConfiguration config = org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(backupFile);
            
            java.util.List<?> inventoryList = config.getList("inventory");
            if (inventoryList != null) {
                for (Object obj : inventoryList) {
                    if (obj instanceof org.bukkit.inventory.ItemStack) {
                        event.getDrops().add((org.bukkit.inventory.ItemStack) obj);
                    }
                }
            }
            java.util.List<?> armorList = config.getList("armor");
            if (armorList != null) {
                for (Object obj : armorList) {
                    if (obj instanceof org.bukkit.inventory.ItemStack) {
                        event.getDrops().add((org.bukkit.inventory.ItemStack) obj);
                    }
                }
            }
            org.bukkit.inventory.ItemStack offhand = config.getItemStack("offhand");
            if (offhand != null) {
                event.getDrops().add(offhand);
            }
            
            backupFile.delete();
            
            if (plugin.getScoreboardManager() != null) {
                plugin.getScoreboardManager().removeBoard(player);
            }
        }
    }

    @EventHandler
    public void onPlayerSwapHand(org.bukkit.event.player.PlayerSwapHandItemsEvent event) {
        if (sessionManager.getActiveSessionsCount() == 0) return;
        if (sessionManager.isPlaying(event.getPlayer())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(MessageUtils.colorize("&cYou cannot swap items while fishing!"));
        }
    }

    @EventHandler
    public void onPlayerDamage(org.bukkit.event.entity.EntityDamageEvent event) {
        if (sessionManager.getActiveSessionsCount() == 0) return;
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if (sessionManager.isPlaying(player)) {
                AuditionSession session = sessionManager.getSession(player);
                if (session != null) {
                    session.endSession(false);
                }
                sessionManager.removeSession(player);
                player.sendMessage(MessageUtils.getMessage("errors.session_ended_damage", "&cSession ended because you took damage!"));
            }
        }
    }

    @EventHandler
    public void onPlayerMove(org.bukkit.event.player.PlayerMoveEvent event) {
        if (event.getFrom().getBlockX() == event.getTo().getBlockX() && 
            event.getFrom().getBlockY() == event.getTo().getBlockY() && 
            event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }

        if (sessionManager.getActiveSessionsCount() == 0) return;

        Player player = event.getPlayer();
        if (sessionManager.isPlaying(player)) {
            AuditionSession session = sessionManager.getSession(player);
            if (session != null && session.getStartLocation() != null) {
                if (event.getTo() != null && event.getTo().distanceSquared(session.getStartLocation()) > 4.0) { // 2 blocks squared
                    session.endSession(false);
                    sessionManager.removeSession(player);
                    player.sendMessage(MessageUtils.getMessage("errors.session_ended_move", "&cSession ended because you moved!"));
                }
            }
        }
    }
}
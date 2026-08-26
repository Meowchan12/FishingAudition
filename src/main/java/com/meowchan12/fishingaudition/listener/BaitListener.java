package com.meowchan12.fishingaudition.listener;

import com.meowchan12.fishingaudition.Main;
import com.meowchan12.fishingaudition.manager.BaitData;
import com.meowchan12.fishingaudition.utils.MessageUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class BaitListener implements Listener {

    private final Main plugin;
    
    // We can keep these for backwards compatibility or remove them later,
    // but the spec now says to use PlayerDataManager.
    public static final NamespacedKey APPLIED_BAIT_ID = new NamespacedKey(Main.getInstance(), "applied_bait_id");
    public static final NamespacedKey APPLIED_BAIT_CHARGES = new NamespacedKey(Main.getInstance(), "applied_bait_charges");
    public static final NamespacedKey APPLIED_BAIT_BOOST = new NamespacedKey(Main.getInstance(), "applied_bait_boost");
    public static final NamespacedKey APPLIED_BAIT_MISS_REDUCTION = new NamespacedKey(Main.getInstance(), "applied_bait_miss_reduction");
    public static final NamespacedKey APPLIED_BAIT_TARGET_RARITY = new NamespacedKey(Main.getInstance(), "applied_bait_target_rarity");

    public BaitListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        
        if (item == null) return;
        
        if (plugin.getBaitManager().isBait(item)) {
            String baitId = plugin.getBaitManager().getBaitId(item);
            BaitData bait = plugin.getBaitManager().getBaitById(baitId);
            if (bait == null) return;
            
            event.setCancelled(true);
            
            // Set in PlayerDataManager
            plugin.getPlayerDataManager().setEquippedBait(player, baitId, bait.getMaxCharges());
            
            // Consume 1
            item.setAmount(item.getAmount() - 1);
            
            player.playSound(player.getLocation(), Sound.ENTITY_FISHING_BOBBER_SPLASH, 1.0f, 1.0f);
            player.sendMessage(MessageUtils.colorize("&aSuccessfully equipped " + bait.getName() + "&a!"));
        }
    }
}

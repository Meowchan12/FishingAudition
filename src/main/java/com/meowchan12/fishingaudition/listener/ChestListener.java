package com.meowchan12.fishingaudition.listener;

import com.meowchan12.fishingaudition.currencymanager.FishCoinManager;
import com.meowchan12.fishingaudition.manager.ChestManager;
import com.meowchan12.fishingaudition.material.CustomFish;
import com.meowchan12.fishingaudition.utils.MessageUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class ChestListener implements Listener {

    private final ChestManager chestManager;

    public ChestListener(ChestManager chestManager) {
        this.chestManager = chestManager;
    }

    @EventHandler
    public void onChestClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();

        // Kiểm tra xem có đúng là GUI Rương Ảo không
        String chestTitle = MessageUtils.colorize(com.meowchan12.fishingaudition.Main.getInstance().getConfig().getString("gui.titles.chest", "&8Your Virtual Fish Chest"));
        if (title.equals(chestTitle)) {
            event.setCancelled(true); // Ngăn không cho kéo/thả vật phẩm ra khỏi rương

            // Handle buttons
            int size = com.meowchan12.fishingaudition.Main.getInstance().getConfig().getInt("gui.sizes.chest", 54);
            
            if (event.getRawSlot() == size - 1) { // Close
                player.closeInventory();
            } else if (event.getRawSlot() == size - 2) { // Back
                player.closeInventory();
                player.performCommand("fish menu");
            } else if (event.getRawSlot() == size - 3) { // Sell All
                java.util.UUID uuid = player.getUniqueId();
                player.closeInventory();
                
                com.meowchan12.fishingaudition.utils.SchedulerUtils.runAsync(com.meowchan12.fishingaudition.Main.getInstance(), () -> {
                    List<ItemStack> storedFish = chestManager.loadChestSync(uuid);

                    if (storedFish.isEmpty()) {
                        player.sendMessage(MessageUtils.getMessage("errors.chest_empty"));
                        return;
                    }

                    double totalEarned = 0.0;
                    int fishCount = 0;

                    for (ItemStack fish : storedFish) {
                        if (CustomFish.isCustomFish(fish)) {
                            totalEarned += (CustomFish.getFishValue(fish) * fish.getAmount());
                            fishCount += fish.getAmount();
                        }
                    }

                    chestManager.clearChest(player);

                    int playerLevel = com.meowchan12.fishingaudition.Main.getInstance().getLevelManager().getLevel(player);
                    double multiplierConfig = com.meowchan12.fishingaudition.Main.getInstance().getConfig().getDouble("leveling.level-multiplier-per-level", 0.01);
                    double bonusMultiplier = playerLevel * multiplierConfig;
                    double finalEarned = totalEarned * (1.0 + bonusMultiplier);
                    int bonusPercent = (int) (bonusMultiplier * 100);
                    int finalCount = fishCount;

                    com.meowchan12.fishingaudition.utils.SchedulerUtils.runAtEntity(com.meowchan12.fishingaudition.Main.getInstance(), player, () -> {
                        FishCoinManager.addBalance(player, finalEarned);
                        player.sendMessage(MessageUtils.getMessage("success.chest_sold")
                                .replace("{count}", String.valueOf(finalCount))
                                .replace("{earned}", String.format("%.2f", finalEarned))
                                + MessageUtils.colorize(" &e(+" + bonusPercent + "% Level Bonus)"));
                    });
                });
            }
        }
    }
}
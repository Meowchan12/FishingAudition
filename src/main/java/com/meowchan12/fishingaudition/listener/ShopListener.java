package com.meowchan12.fishingaudition.listener;

import com.meowchan12.fishingaudition.currencymanager.ExchangeLogic;
import com.meowchan12.fishingaudition.currencymanager.FishCoinManager;
import com.meowchan12.fishingaudition.material.CustomRod;
import com.meowchan12.fishingaudition.shop.ShopManager;
import com.meowchan12.fishingaudition.utils.MessageUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;
import com.meowchan12.fishingaudition.Main;

public class ShopListener implements Listener {

    private final Main plugin;
    private final ExchangeLogic exchangeLogic;

    public ShopListener(Main plugin, ExchangeLogic exchangeLogic) {
        this.plugin = plugin;
        this.exchangeLogic = exchangeLogic;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();

        if (title.equals(ShopManager.getCurrencyShopTitle())) {
            event.setCancelled(true);

            org.bukkit.configuration.file.FileConfiguration config = plugin.getConfig();
            int size = config.getInt("gui.sizes.exchange", 27);

            if (event.getCurrentItem() != null && event.getCurrentItem().hasItemMeta()) {
                String exchangeId = event.getCurrentItem().getItemMeta().getPersistentDataContainer().get(
                        new org.bukkit.NamespacedKey(plugin, "exchange_id"),
                        org.bukkit.persistence.PersistentDataType.STRING
                );
                
                if (exchangeId != null) {
                    if (event.getClick() == ClickType.LEFT) {
                        exchangeLogic.processFixedExchange(player, exchangeId);
                    } else if (event.getClick() == ClickType.RIGHT) {
                        exchangeLogic.startChatInputSession(player, exchangeId);
                    }
                } else if (event.getRawSlot() == size - 1) {
                    player.closeInventory();
                } else if (event.getRawSlot() == size - 2) {
                    player.closeInventory();
                    player.performCommand("fish menu");
                }
            } else if (event.getRawSlot() == size - 1) {
                player.closeInventory();
            } else if (event.getRawSlot() == size - 2) {
                player.closeInventory();
                player.performCommand("fish menu");
            }
        }
        else if (title.equals(ShopManager.getRodShopTitle())) {
            event.setCancelled(true);
            
            org.bukkit.configuration.file.FileConfiguration config = plugin.getConfig();
            int size = config.getInt("gui.sizes.rod_shop", 54);
            
            if (event.getRawSlot() == size - 1) {
                player.closeInventory();
                return;
            } else if (event.getRawSlot() == size - 2) {
                player.closeInventory();
                player.performCommand("fish menu");
                return;
            }
            
            if (event.getCurrentItem() != null && event.getCurrentItem().getType() != Material.AIR && event.getCurrentItem().hasItemMeta()) {
                if (!event.getClick().isLeftClick()) return;
                
                org.bukkit.inventory.meta.ItemMeta meta = event.getCurrentItem().getItemMeta();
                String rodId = meta.getPersistentDataContainer().get(CustomRod.ROD_ID, org.bukkit.persistence.PersistentDataType.STRING);
                String baitId = meta.getPersistentDataContainer().get(com.meowchan12.fishingaudition.manager.BaitManager.BAIT_ID_KEY, org.bukkit.persistence.PersistentDataType.STRING);
                
                if (rodId != null) {
                    com.meowchan12.fishingaudition.manager.RodData data = plugin.getRodManager().getRodById(rodId);
                    if (data != null) {
                        int playerLevel = plugin.getLevelManager().getLevel(player);
                        if (playerLevel < data.getRequiredLevel()) {
                            com.meowchan12.fishingaudition.utils.SoundUtils.playSound(player, "wrong_key");
                            player.sendMessage(MessageUtils.getRawMessage("minigame.level_too_low", "&cYou need to be Level " + data.getRequiredLevel() + " to use this rod!"));
                            return;
                        }

                        if (plugin.getPlayerDataManager().hasRod(player, rodId)) {
                            plugin.getPlayerDataManager().setEquippedRod(player, rodId);
                            com.meowchan12.fishingaudition.utils.SoundUtils.playSound(player, "purchase");
                            player.sendMessage(MessageUtils.getMessage("success.rod_equipped").replace("{rod}", data.getName()));
                            player.closeInventory();
                            return;
                        }

                        double cost = data.getPrice();
                        double currentCoins = FishCoinManager.getBalance(player);
                        
                        if (currentCoins >= cost) {
                            FishCoinManager.setBalance(player, currentCoins - cost);
                            plugin.getPlayerDataManager().addOwnedRod(player, rodId);
                            plugin.getPlayerDataManager().setEquippedRod(player, rodId);
                            
                            com.meowchan12.fishingaudition.utils.SoundUtils.playSound(player, "purchase");
                            player.sendMessage(MessageUtils.getMessage("success.rod_bought"));
                            player.closeInventory();
                        } else {
                            com.meowchan12.fishingaudition.utils.SoundUtils.playSound(player, "wrong_key");
                            player.sendMessage(MessageUtils.getMessage("errors.not_enough_coins"));
                        }
                    }
                } else if (baitId != null) {
                    com.meowchan12.fishingaudition.manager.BaitData data = plugin.getBaitManager().getBaitById(baitId);
                    if (data != null) {
                        double cost = data.getPrice();
                        double currentCoins = FishCoinManager.getBalance(player);
                        
                        if (currentCoins >= cost) {
                            FishCoinManager.setBalance(player, currentCoins - cost);
                            
                            ItemStack baitItem = plugin.getBaitManager().generateBait(data.getId());
                            if (player.getInventory().firstEmpty() == -1) {
                                player.getWorld().dropItem(player.getLocation(), baitItem);
                            } else {
                                player.getInventory().addItem(baitItem);
                            }
                            
                            com.meowchan12.fishingaudition.utils.SoundUtils.playSound(player, "purchase");
                            player.sendMessage(MessageUtils.colorize("&aSuccessfully purchased " + data.getName() + "&a!"));
                            // DO NOT close inventory to allow buying multiple
                        } else {
                            com.meowchan12.fishingaudition.utils.SoundUtils.playSound(player, "wrong_key");
                            player.sendMessage(MessageUtils.getMessage("errors.not_enough_coins"));
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();

        if (exchangeLogic.isAwaitingInput(player)) {
            event.setCancelled(true);
            String msg = event.getMessage();
            com.meowchan12.fishingaudition.utils.SchedulerUtils.runAtEntity(plugin, player, () -> {
                exchangeLogic.processChatInput(player, msg);
            });
        }
    }

    @EventHandler
    public void onPlayerCommandPreprocess(org.bukkit.event.player.PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        if (exchangeLogic.isAwaitingInput(player)) {
            event.setCancelled(true);
            player.sendMessage(MessageUtils.getMessage("errors.must_finish_action"));
        }
    }
}
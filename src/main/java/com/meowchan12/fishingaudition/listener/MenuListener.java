package com.meowchan12.fishingaudition.listener;

import com.meowchan12.fishingaudition.Main;
import com.meowchan12.fishingaudition.command.subcommands.MenuCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class MenuListener implements Listener {

    private final Main plugin;

    public MenuListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();

        if (title.equals(MenuCommand.MENU_TITLE)) {
            event.setCancelled(true); // Prevent taking items out of the menu

            int slot = event.getRawSlot();
            switch (slot) {
                case 11:
                    player.closeInventory();
                    plugin.getShopManager().openRodShop(player);
                    break;
                case 13:
                    player.closeInventory();
                    plugin.getShopManager().openCurrencyShop(player);
                    break;
                case 15:
                    player.closeInventory();
                    plugin.getChestManager().openChest(player);
                    break;
                case 4:
                    player.closeInventory();
                    openLeaderboardMenu(player);
                    break;
                case 22:
                    player.closeInventory();
                    player.performCommand("fish profile");
                    break;
                case 26:
                    player.closeInventory();
                    break;
                default:
                    // Clicked on glass pane or empty space
                    break;
            }
        } else if (title.equals(com.meowchan12.fishingaudition.utils.MessageUtils.colorize("&8✦ Leaderboards ✦"))) {
            event.setCancelled(true);
            int slot = event.getRawSlot();
            if (slot == 11) {
                player.closeInventory();
                player.performCommand("fish top coins");
            } else if (slot == 15) {
                player.closeInventory();
                player.performCommand("fish top round");
            } else if (slot == 26) {
                player.closeInventory();
            } else if (slot == 25) {
                player.closeInventory();
                player.performCommand("fish menu");
            }
        } else if (title.equals(com.meowchan12.fishingaudition.utils.MessageUtils.colorize(plugin.getConfig().getString("gui.titles.preview", "&8Fish Preview")))) {
            event.setCancelled(true);
        } else if (title.equals(com.meowchan12.fishingaudition.manager.LogbookManager.getLogbookTitle())) {
            event.setCancelled(true);
            int size = plugin.getConfig().getInt("gui.sizes.logbook", 54);
            if (event.getRawSlot() == size - 1) {
                player.closeInventory();
            } else if (event.getRawSlot() == size - 2) {
                player.closeInventory();
                player.performCommand("fish menu");
            }
        }
    }

    private void openLeaderboardMenu(Player player) {
        org.bukkit.inventory.Inventory inv = org.bukkit.Bukkit.createInventory(null, 27, com.meowchan12.fishingaudition.utils.MessageUtils.colorize("&8✦ Leaderboards ✦"));

        org.bukkit.inventory.ItemStack glass = new org.bukkit.inventory.ItemStack(org.bukkit.Material.GRAY_STAINED_GLASS_PANE);
        org.bukkit.inventory.meta.ItemMeta glassMeta = glass.getItemMeta();
        if (glassMeta != null) {
            glassMeta.setDisplayName(" ");
            glass.setItemMeta(glassMeta);
        }

        for (int i = 0; i < 27; i++) {
            inv.setItem(i, glass);
        }

        org.bukkit.inventory.ItemStack coinsBtn = new org.bukkit.inventory.ItemStack(org.bukkit.Material.SUNFLOWER);
        org.bukkit.inventory.meta.ItemMeta coinsMeta = coinsBtn.getItemMeta();
        if (coinsMeta != null) {
            coinsMeta.setDisplayName(com.meowchan12.fishingaudition.utils.MessageUtils.colorize("&e&lTop FishCoins"));
            coinsBtn.setItemMeta(coinsMeta);
        }
        inv.setItem(11, coinsBtn);

        org.bukkit.inventory.ItemStack roundsBtn = new org.bukkit.inventory.ItemStack(org.bukkit.Material.FISHING_ROD);
        org.bukkit.inventory.meta.ItemMeta roundsMeta = roundsBtn.getItemMeta();
        if (roundsMeta != null) {
            roundsMeta.setDisplayName(com.meowchan12.fishingaudition.utils.MessageUtils.colorize("&a&lTop Rounds"));
            roundsBtn.setItemMeta(roundsMeta);
        }
        inv.setItem(15, roundsBtn);

        org.bukkit.inventory.ItemStack backBtn = new org.bukkit.inventory.ItemStack(org.bukkit.Material.ARROW);
        org.bukkit.inventory.meta.ItemMeta backMeta = backBtn.getItemMeta();
        if (backMeta != null) {
            backMeta.setDisplayName(com.meowchan12.fishingaudition.utils.MessageUtils.colorize("&a&lReturn to Menu"));
            backBtn.setItemMeta(backMeta);
        }
        inv.setItem(25, backBtn);

        org.bukkit.inventory.ItemStack closeBtn = new org.bukkit.inventory.ItemStack(org.bukkit.Material.BARRIER);
        org.bukkit.inventory.meta.ItemMeta closeMeta = closeBtn.getItemMeta();
        if (closeMeta != null) {
            closeMeta.setDisplayName(com.meowchan12.fishingaudition.utils.MessageUtils.colorize("&c&lClose"));
            closeBtn.setItemMeta(closeMeta);
        }
        inv.setItem(26, closeBtn);

        player.openInventory(inv);
    }
}

package com.meowchan12.fishingaudition.command.subcommands;

import com.meowchan12.fishingaudition.command.SubCommand;
import com.meowchan12.fishingaudition.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collections;

public class MenuCommand extends SubCommand {

    public static final String MENU_TITLE = MessageUtils.colorize("&8Fishing Audition Menu");

    @Override
    public String getName() {
        return "menu";
    }

    @Override
    public String getDescription() {
        return "Open the main menu GUI.";
    }

    @Override
    public String getSyntax() {
        return "/fa menu";
    }

    @Override
    public String getPermission() {
        return "fishingaudition.menu";
    }

    @Override
    public void perform(Player player, String[] args) {
        Inventory inv = Bukkit.createInventory(null, 27, MENU_TITLE);

        ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta glassMeta = glass.getItemMeta();
        if (glassMeta != null) {
            glassMeta.setDisplayName(" ");
            glass.setItemMeta(glassMeta);
        }

        for (int i = 0; i < 27; i++) {
            inv.setItem(i, glass);
        }

        inv.setItem(11, createGuiItem(Material.FISHING_ROD, "&b&lFishing Rod Shop", "&7Click to browse and buy rods."));
        inv.setItem(13, createGuiItem(Material.GOLD_INGOT, "&e&lCurrency Exchange", "&7Click to exchange FishCoins."));
        inv.setItem(15, createGuiItem(Material.CHEST, "&a&lVirtual Fish Chest", "&7Click to open your fish storage."));
        inv.setItem(4, createGuiItem(Material.BOOK, "&d&lLeaderboard", "&7Click to view top players."));
        
        // --- PROFILE ITEM ---
        double coins = com.meowchan12.fishingaudition.currencymanager.FishCoinManager.getBalance(player);
        int topRound = com.meowchan12.fishingaudition.Main.getInstance().getPlayerDataManager().getTopRound(player);
        String coinRank = com.meowchan12.fishingaudition.Main.getInstance().getTopManager().getPlayerCoinRank(player.getUniqueId());
        String roundRank = com.meowchan12.fishingaudition.Main.getInstance().getTopManager().getPlayerRoundRank(player.getUniqueId());
        int level = com.meowchan12.fishingaudition.Main.getInstance().getLevelManager().getLevel(player);
        double xp = com.meowchan12.fishingaudition.Main.getInstance().getLevelManager().getXP(player);
        double neededXp = com.meowchan12.fishingaudition.Main.getInstance().getLevelManager().getRequiredXP(level);
        String rodId = com.meowchan12.fishingaudition.Main.getInstance().getPlayerDataManager().getEquippedRod(player);
        String rodName = "None";
        if (rodId != null) {
            com.meowchan12.fishingaudition.manager.RodData data = com.meowchan12.fishingaudition.Main.getInstance().getRodManager().getRodById(rodId);
            if (data != null) rodName = data.getName();
        }

        ItemStack profileItem = new ItemStack(Material.PLAYER_HEAD);
        org.bukkit.inventory.meta.SkullMeta skullMeta = (org.bukkit.inventory.meta.SkullMeta) profileItem.getItemMeta();
        if (skullMeta != null) {
            skullMeta.setOwningPlayer(player);
            skullMeta.setDisplayName(MessageUtils.colorize("&d&l✦ Your Profile ✦"));
            java.util.List<String> lore = new java.util.ArrayList<>();
            lore.add(MessageUtils.colorize("&7Here are your current fishing stats:"));
            lore.add("");
            lore.add(MessageUtils.colorize("&fCoins: &6" + coins + " &7(Top &e#" + coinRank + "&7)"));
            lore.add(MessageUtils.colorize("&fTop Round: &c" + topRound + " &7(Top &e#" + roundRank + "&7)"));
            lore.add("");
            lore.add(MessageUtils.colorize("&fLevel: &a" + level));
            lore.add(MessageUtils.colorize("&fXP: &b" + xp + " &7/ &b" + neededXp));
            lore.add("");
            lore.add(MessageUtils.colorize("&fEquipped Rod: &d" + rodName));
            
            String baitId = com.meowchan12.fishingaudition.Main.getInstance().getPlayerDataManager().getEquippedBait(player);
            if (baitId != null) {
                com.meowchan12.fishingaudition.manager.BaitData bData = com.meowchan12.fishingaudition.Main.getInstance().getBaitManager().getBaitById(baitId);
                if (bData != null) {
                    int charges = com.meowchan12.fishingaudition.Main.getInstance().getPlayerDataManager().getBaitCharges(player);
                    int maxCharges = bData.getMaxCharges();
                    lore.add(MessageUtils.colorize("&fEquipped Bait: &6" + bData.getName() + " &7(" + charges + "/" + maxCharges + " left)"));
                } else {
                    lore.add(MessageUtils.colorize("&fEquipped Bait: &7None"));
                }
            } else {
                lore.add(MessageUtils.colorize("&fEquipped Bait: &7None"));
            }
            
            skullMeta.setLore(lore);
            profileItem.setItemMeta(skullMeta);
        }
        inv.setItem(22, profileItem);
        // --------------------

        ItemStack closeButton = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = closeButton.getItemMeta();
        if (closeMeta != null) {
            closeMeta.setDisplayName(MessageUtils.colorize("&c&lClose"));
            closeButton.setItemMeta(closeMeta);
        }
        inv.setItem(26, closeButton);

        player.openInventory(inv);
    }

    private ItemStack createGuiItem(Material material, String name, String loreLine) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(MessageUtils.colorize(name));
            meta.setLore(Collections.singletonList(MessageUtils.colorize(loreLine)));
            item.setItemMeta(meta);
        }
        return item;
    }
}

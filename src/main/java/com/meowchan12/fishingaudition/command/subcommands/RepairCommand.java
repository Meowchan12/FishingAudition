package com.meowchan12.fishingaudition.command.subcommands;

import com.meowchan12.fishingaudition.Main;
import com.meowchan12.fishingaudition.command.SubCommand;
import com.meowchan12.fishingaudition.currencymanager.FishCoinManager;
import com.meowchan12.fishingaudition.material.CustomRod;
import com.meowchan12.fishingaudition.utils.MessageUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class RepairCommand extends SubCommand {

    @Override
    public String getName() {
        return "repair";
    }

    @Override
    public String getDescription() {
        return "Repairs your currently held fishing rod.";
    }

    @Override
    public String getSyntax() {
        return "/fa repair";
    }

    @Override
    public String getPermission() {
        return com.meowchan12.fishingaudition.constants.Permissions.USER_REPAIR;
    }

    @Override
    public void perform(Player player, String[] args) {
        ItemStack item = player.getInventory().getItemInMainHand();
        
        if (!CustomRod.isCustomRod(item)) {
            player.sendMessage(MessageUtils.getMessage("repair_not_rod"));
            return;
        }

        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        NamespacedKey maxKey = new NamespacedKey(Main.getInstance(), "max_durability");
        NamespacedKey curKey = new NamespacedKey(Main.getInstance(), "current_durability");

        int max = pdc.getOrDefault(maxKey, PersistentDataType.INTEGER, 50);
        int current = pdc.getOrDefault(curKey, PersistentDataType.INTEGER, 50);

        if (current >= max) {
            player.sendMessage(MessageUtils.getMessage("repair_max"));
            return;
        }

        int missing = max - current;
        
        double repairMultiplier = 1.0;
        String rodId = pdc.get(CustomRod.ROD_ID, PersistentDataType.STRING);
        if (rodId != null) {
            com.meowchan12.fishingaudition.manager.RodData rodData = Main.getInstance().getRodManager().getRodById(rodId);
            if (rodData != null) {
                repairMultiplier = rodData.getRepairMultiplier();
            }
        }
        
        double totalCost = missing * repairMultiplier;
        double balance = FishCoinManager.getBalance(player);

        if (balance < totalCost) {
            String msg = MessageUtils.getMessage("repair_not_enough").replace("{cost}", String.valueOf(totalCost));
            player.sendMessage(msg);
            return;
        }

        // Proceed to repair
        FishCoinManager.setBalance(player, balance - totalCost);
        pdc.set(curKey, PersistentDataType.INTEGER, max);
        item.setItemMeta(meta);
        CustomRod.updateLore(item);

        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1.0f, 1.0f);
        String msg = MessageUtils.getMessage("repair_success").replace("{cost}", String.valueOf(totalCost));
        player.sendMessage(msg);
    }
}

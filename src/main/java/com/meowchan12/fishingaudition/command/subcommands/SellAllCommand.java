package com.meowchan12.fishingaudition.command.subcommands;

import com.meowchan12.fishingaudition.command.SubCommand;
import com.meowchan12.fishingaudition.currencymanager.FishCoinManager;
import com.meowchan12.fishingaudition.material.CustomFish;
import com.meowchan12.fishingaudition.utils.MessageUtils;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class SellAllCommand extends SubCommand {

    @Override
    public String getName() {
        return "sellall";
    }

    @Override
    public String getDescription() {
        return "Sell all your caught custom fish for FishCoins.";
    }

    @Override
    public String getSyntax() {
        return "/fish sellall";
    }

    @Override
    public String getPermission() {
        return com.meowchan12.fishingaudition.constants.Permissions.USER_SELLALL;
    }

    @Override
    public void perform(Player player, String[] args) {
        double totalEarned = 0.0;
        int fishCount = 0;

        // Quét toàn bộ túi đồ
        for (int i = 0; i < player.getInventory().getSize(); i++) {
            ItemStack item = player.getInventory().getItem(i);

            if (CustomFish.isCustomFish(item)) {
                double value = CustomFish.getFishValue(item) * item.getAmount();
                totalEarned += value;
                fishCount += item.getAmount();

                // Xóa vật phẩm sau khi bán
                player.getInventory().setItem(i, null);
            }
        }

        if (fishCount == 0) {
            player.sendMessage(MessageUtils.getMessage("errors.chest_empty"));
            return;
        }

        int playerLevel = com.meowchan12.fishingaudition.Main.getInstance().getLevelManager().getLevel(player);
        double multiplierConfig = com.meowchan12.fishingaudition.Main.getInstance().getConfig().getDouble("leveling.level-multiplier-per-level", 0.01);
        double bonusMultiplier = playerLevel * multiplierConfig;
        double finalEarned = totalEarned * (1.0 + bonusMultiplier);
        int bonusPercent = (int) (bonusMultiplier * 100);

        // Cộng tiền cho người chơi
        FishCoinManager.addBalance(player, finalEarned);
        player.sendMessage(MessageUtils.getMessage("success.chest_sold")
                .replace("{count}", String.valueOf(fishCount))
                .replace("{earned}", String.format("%.2f", finalEarned))
                + MessageUtils.colorize(" &e(+" + bonusPercent + "% Level Bonus)"));
    }
}

package com.meowchan12.fishingaudition.command.subcommands;

import com.meowchan12.fishingaudition.Main;
import com.meowchan12.fishingaudition.command.SubCommand;
import com.meowchan12.fishingaudition.currencymanager.FishCoinManager;
import com.meowchan12.fishingaudition.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import java.util.UUID;

public class BalLookCommand extends SubCommand {

    @Override
    public String getName() {
        return "ballook";
    }

    @Override
    public String getDescription() {
        return "Check another player's FishCoin balance.";
    }

    @Override
    public String getSyntax() {
        return "/fish ballook <player>";
    }

    @Override
    public String getPermission() {
        return com.meowchan12.fishingaudition.constants.Permissions.USER_BALLOOK;
    }

    @Override
    public void perform(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(MessageUtils.colorize("&cUsage: " + getSyntax()));
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target != null) {
            double bal = FishCoinManager.getBalance(target);
            player.sendMessage(MessageUtils.colorize("&a" + target.getName() + "'s FishCoins balance: &e" + bal));
        } else {
            // If offline, try async fetch
            com.meowchan12.fishingaudition.utils.SchedulerUtils.runAsync(Main.getInstance(), () -> {
                double bal = Main.getInstance().getPlayerDataManager().getOfflineBalance(args[1]);
                if (bal >= 0) {
                    player.sendMessage(MessageUtils.colorize("&a" + args[1] + "'s FishCoins balance: &e" + bal));
                } else {
                    player.sendMessage(MessageUtils.colorize("&cPlayer not found in database."));
                }
            });
        }
    }
}

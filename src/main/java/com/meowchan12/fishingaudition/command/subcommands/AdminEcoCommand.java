package com.meowchan12.fishingaudition.command.subcommands;

import com.meowchan12.fishingaudition.command.SubCommand;
import com.meowchan12.fishingaudition.currencymanager.FishCoinManager;
import com.meowchan12.fishingaudition.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class AdminEcoCommand extends SubCommand {

    @Override
    public String getName() { return "eco"; }

    @Override
    public String getDescription() { return "Manage player FishCoins."; }

    @Override
    public String getSyntax() { return "/fish eco <give/take/set> <player> <amount>"; }

    @Override
    public String getPermission() { return com.meowchan12.fishingaudition.constants.Permissions.ADMIN_ECO; }

    @Override
    public void perform(Player player, String[] args) {
        if (args.length < 4) {
            player.sendMessage(MessageUtils.colorize("&cUsage: " + getSyntax()));
            return;
        }

        String action = args[1].toLowerCase();
        Player target = Bukkit.getPlayer(args[2]);
        double amount;

        if (target == null || !target.isOnline()) {
            player.sendMessage(MessageUtils.colorize("&cPlayer not found or offline."));
            return;
        }

        try {
            amount = Double.parseDouble(args[3]);
        } catch (NumberFormatException e) {
            player.sendMessage(MessageUtils.colorize("&cInvalid amount!"));
            return;
        }

        if (amount < 0) {
            player.sendMessage(MessageUtils.colorize("&cAmount cannot be negative."));
            return;
        }

        switch (action) {
            case "give":
                FishCoinManager.addBalance(target, amount);
                player.sendMessage(MessageUtils.colorize("&aGave &e" + amount + " &aFishCoins to &f" + target.getName()));
                break;
            case "take":
                if (FishCoinManager.removeBalance(target, amount)) {
                    player.sendMessage(MessageUtils.colorize("&aTook &e" + amount + " &aFishCoins from &f" + target.getName()));
                } else {
                    player.sendMessage(MessageUtils.colorize("&c" + target.getName() + " does not have enough FishCoins."));
                }
                break;
            case "set":
                FishCoinManager.setBalance(target, amount);
                player.sendMessage(MessageUtils.colorize("&aSet &f" + target.getName() + "&a's balance to &e" + amount));
                break;
            default:
                player.sendMessage(MessageUtils.colorize("&cInvalid action. Use give, take, or set."));
                break;
        }
    }
}

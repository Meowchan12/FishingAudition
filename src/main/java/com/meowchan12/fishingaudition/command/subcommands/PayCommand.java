package com.meowchan12.fishingaudition.command.subcommands;

import com.meowchan12.fishingaudition.Main;
import com.meowchan12.fishingaudition.command.SubCommand;
import com.meowchan12.fishingaudition.currencymanager.FishCoinManager;
import com.meowchan12.fishingaudition.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class PayCommand extends SubCommand {

    @Override
    public String getName() {
        return "pay";
    }

    @Override
    public String getDescription() {
        return "Pay FishCoins to another player.";
    }

    @Override
    public String getSyntax() {
        return "/fish pay <player> <amount>";
    }

    @Override
    public String getPermission() {
        return com.meowchan12.fishingaudition.constants.Permissions.USER_PAY;
    }

    @Override
    public void perform(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage(MessageUtils.colorize("&cUsage: " + getSyntax()));
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            player.sendMessage(MessageUtils.colorize("&cPlayer not found or offline."));
            return;
        }

        if (target.equals(player)) {
            player.sendMessage(MessageUtils.colorize("&cYou cannot pay yourself."));
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(args[2]);
        } catch (NumberFormatException e) {
            player.sendMessage(MessageUtils.colorize("&cInvalid amount."));
            return;
        }

        if (amount <= 0) {
            player.sendMessage(MessageUtils.colorize("&cAmount must be greater than 0."));
            return;
        }

        if (FishCoinManager.hasEnough(player, amount)) {
            FishCoinManager.removeBalance(player, amount);
            FishCoinManager.addBalance(target, amount);
            player.sendMessage(MessageUtils.colorize("&aPaid &e" + amount + " FishCoins &ato &e" + target.getName() + "&a."));
            target.sendMessage(MessageUtils.colorize("&aYou received &e" + amount + " FishCoins &afrom &e" + player.getName() + "&a."));
        } else {
            player.sendMessage(MessageUtils.colorize("&cYou do not have enough FishCoins."));
        }
    }
}

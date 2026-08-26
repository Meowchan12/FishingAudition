package com.meowchan12.fishingaudition.command.subcommands;

import com.meowchan12.fishingaudition.Main;
import com.meowchan12.fishingaudition.command.SubCommand;
import com.meowchan12.fishingaudition.currencymanager.FishCoinManager;
import com.meowchan12.fishingaudition.utils.MessageUtils;
import org.bukkit.entity.Player;

public class BalCommand extends SubCommand {

    @Override
    public String getName() {
        return "bal";
    }

    @Override
    public String getDescription() {
        return "Check your FishCoin balance.";
    }

    @Override
    public String getSyntax() {
        return "/fish bal";
    }

    @Override
    public String getPermission() {
        return "fishingaudition.user.bal";
    }

    @Override
    public void perform(Player player, String[] args) {
        double bal = FishCoinManager.getBalance(player);
        player.sendMessage(MessageUtils.colorize("&aYour FishCoins balance: &e" + bal));
    }
}

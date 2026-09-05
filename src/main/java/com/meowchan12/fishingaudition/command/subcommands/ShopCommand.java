package com.meowchan12.fishingaudition.command.subcommands;

import com.meowchan12.fishingaudition.Main;
import com.meowchan12.fishingaudition.command.SubCommand;
import com.meowchan12.fishingaudition.shop.ShopManager;
import com.meowchan12.fishingaudition.utils.MessageUtils;
import org.bukkit.entity.Player;

public class ShopCommand extends SubCommand {

    private final ShopManager shopManager;

    public ShopCommand() {
        this.shopManager = new ShopManager();
    }

    @Override
    public String getName() {
        return "shop";
    }

    @Override
    public String getDescription() {
        return "Open the Fishing Shop.";
    }

    @Override
    public String getSyntax() {
        return "/fish shop <rod/currency>";
    }

    @Override
    public String getPermission() {
        return com.meowchan12.fishingaudition.constants.Permissions.USER_SHOP;
    }

    @Override
    public void perform(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(MessageUtils.colorize("&cUsage: " + getSyntax()));
            return;
        }

        String shopType = args[1].toLowerCase();

        if (shopType.equals("currency")) {
            shopManager.openCurrencyShop(player);
        } else if (shopType.equals("rod")) {
            shopManager.openRodShop(player);
        } else {
            player.sendMessage(MessageUtils.colorize("&cInvalid shop type. Available: rod, currency."));
        }
    }
}

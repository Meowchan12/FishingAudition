package com.meowchan12.fishingaudition.command.subcommands;

import com.meowchan12.fishingaudition.Main;
import com.meowchan12.fishingaudition.command.SubCommand;
import com.meowchan12.fishingaudition.manager.FishData;
import com.meowchan12.fishingaudition.material.CustomFish;
import com.meowchan12.fishingaudition.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class GiveFishCommand extends SubCommand {

    @Override
    public String getName() {
        return "givefish";
    }

    @Override
    public String getDescription() {
        return "Give a specific custom fish to a player.";
    }

    @Override
    public String getSyntax() {
        return "/fa givefish <player> <fish_id>";
    }

    @Override
    public String getPermission() {
        return com.meowchan12.fishingaudition.constants.Permissions.ADMIN_GENERAL;
    }

    @Override
    public void perform(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage(MessageUtils.colorize("&cUsage: " + getSyntax()));
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            player.sendMessage(MessageUtils.colorize("&cPlayer not found."));
            return;
        }

        String fishId = args[2].toLowerCase();
        FishData data = Main.getInstance().getFishManager().getFishById(fishId);
        
        if (data == null) {
            player.sendMessage(MessageUtils.colorize("&cFish ID '" + fishId + "' not found in fish.yml."));
            return;
        }

        ItemStack fishItem = CustomFish.generateFish(data);
        if (fishItem != null) {
            target.getInventory().addItem(fishItem);
            player.sendMessage(MessageUtils.colorize("&aSuccessfully gave " + data.getName() + " &ato " + target.getName() + "."));
            target.sendMessage(MessageUtils.colorize("&aYou received " + data.getName() + "&a!"));
        } else {
            player.sendMessage(MessageUtils.colorize("&cFailed to generate fish item."));
        }
    }
}

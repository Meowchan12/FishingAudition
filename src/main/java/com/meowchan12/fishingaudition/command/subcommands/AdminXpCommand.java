package com.meowchan12.fishingaudition.command.subcommands;

import com.meowchan12.fishingaudition.Main;
import com.meowchan12.fishingaudition.command.SubCommand;
import com.meowchan12.fishingaudition.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class AdminXpCommand extends SubCommand {

    @Override
    public String getName() {
        return "adminxp";
    }

    @Override
    public String getDescription() {
        return "Manage player XP and Level.";
    }

    @Override
    public String getSyntax() {
        return "/fish adminxp <give|set|take> <player> <amount>";
    }

    @Override
    public String getPermission() {
        return com.meowchan12.fishingaudition.constants.Permissions.ADMIN_XP;
    }

    @Override
    public void perform(Player player, String[] args) {
        if (args.length < 4) {
            player.sendMessage(MessageUtils.colorize("&cUsage: " + getSyntax()));
            return;
        }

        String action = args[1].toLowerCase();
        Player target = Bukkit.getPlayer(args[2]);

        if (target == null) {
            player.sendMessage(MessageUtils.colorize("&cPlayer not found."));
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(args[3]);
        } catch (NumberFormatException e) {
            player.sendMessage(MessageUtils.colorize("&cInvalid amount."));
            return;
        }

        switch (action) {
            case "give":
                Main.getInstance().getLevelManager().addXP(target, amount);
                player.sendMessage(MessageUtils.colorize("&aGave &e" + amount + " XP &ato &e" + target.getName()));
                break;
            case "set":
                Main.getInstance().getLevelManager().setXP(target, amount);
                Main.getInstance().getLevelManager().checkLevelUp(target);
                player.sendMessage(MessageUtils.colorize("&aSet &e" + target.getName() + "'s XP &ato &e" + amount));
                break;
            case "take":
                double current = Main.getInstance().getLevelManager().getXP(target);
                Main.getInstance().getLevelManager().setXP(target, Math.max(0, current - amount));
                player.sendMessage(MessageUtils.colorize("&aTook &e" + amount + " XP &afrom &e" + target.getName()));
                break;
            default:
                player.sendMessage(MessageUtils.colorize("&cUsage: " + getSyntax()));
                break;
        }
    }
}

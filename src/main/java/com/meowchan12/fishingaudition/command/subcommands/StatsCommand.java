package com.meowchan12.fishingaudition.command.subcommands;

import com.meowchan12.fishingaudition.Main;
import com.meowchan12.fishingaudition.command.SubCommand;
import com.meowchan12.fishingaudition.utils.MessageUtils;
import org.bukkit.entity.Player;

public class StatsCommand extends SubCommand {

    @Override
    public String getName() {
        return "stats";
    }

    @Override
    public String getDescription() {
        return "View your fishing profile and stats.";
    }

    @Override
    public String getSyntax() {
        return "/fish stats";
    }

    @Override
    public String getPermission() {
        return com.meowchan12.fishingaudition.constants.Permissions.USER_STATS;
    }

    @Override
    public void perform(Player player, String[] args) {
        int level = Main.getInstance().getLevelManager().getLevel(player);
        double currentXp = Main.getInstance().getLevelManager().getXP(player);
        double neededXp = Main.getInstance().getLevelManager().getRequiredXP(level);
        
        int percent = (int) ((currentXp / neededXp) * 100);
        if (percent > 100) percent = 100;
        
        int bars = (int) (percent / 10.0);
        StringBuilder progressStr = new StringBuilder("&8[&a");
        for (int i = 0; i < 10; i++) {
            if (i < bars) {
                progressStr.append("|");
            } else if (i == bars) {
                progressStr.append("&c|");
            } else {
                progressStr.append("|");
            }
        }
        progressStr.append("&8]");
        
        double multiplierConfig = Main.getInstance().getConfig().getDouble("leveling.level-multiplier-per-level", 0.01);
        int bonusPercent = (int) (level * multiplierConfig * 100);
        
        int topRound = Main.getInstance().getPlayerDataManager().getTopRound(player);

        player.sendMessage(MessageUtils.colorize("&b&l=== ✦ FISHING PROFILE ✦ ==="));
        player.sendMessage(MessageUtils.colorize("&fPlayer: &a" + player.getName()));
        player.sendMessage(MessageUtils.colorize("&fLevel: &e" + level));
        player.sendMessage(MessageUtils.colorize("&fXP: &d" + String.format("%.1f", currentXp) + " / " + String.format("%.1f", neededXp)));
        player.sendMessage(MessageUtils.colorize("&fProgress: " + progressStr.toString() + " &7(" + percent + "%)"));
        player.sendMessage(MessageUtils.colorize("&fSell Bonus: &6+" + bonusPercent + "%"));
        player.sendMessage(MessageUtils.colorize("&fTop Round: &c" + topRound));
        player.sendMessage(MessageUtils.colorize("&b&l========================="));
    }
}

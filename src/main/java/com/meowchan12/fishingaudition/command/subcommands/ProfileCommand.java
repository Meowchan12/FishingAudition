package com.meowchan12.fishingaudition.command.subcommands;

import com.meowchan12.fishingaudition.Main;
import com.meowchan12.fishingaudition.command.SubCommand;
import com.meowchan12.fishingaudition.utils.MessageUtils;
import org.bukkit.entity.Player;

public class ProfileCommand extends SubCommand {

    @Override
    public String getName() {
        return "profile";
    }

    @Override
    public String getDescription() {
        return "View your fishing profile.";
    }

    @Override
    public String getSyntax() {
        return "/fa profile";
    }

    @Override
    public String getPermission() {
        return com.meowchan12.fishingaudition.constants.Permissions.USER_PROFILE;
    }

    @Override
    public void perform(Player player, String[] args) {
        double coins = com.meowchan12.fishingaudition.currencymanager.FishCoinManager.getBalance(player);
        int topRound = Main.getInstance().getPlayerDataManager().getTopRound(player);
        String coinRank = Main.getInstance().getTopManager().getPlayerCoinRank(player.getUniqueId());
        String roundRank = Main.getInstance().getTopManager().getPlayerRoundRank(player.getUniqueId());
        
        int level = Main.getInstance().getLevelManager().getLevel(player);
        double xp = Main.getInstance().getLevelManager().getXP(player);
        double neededXp = Main.getInstance().getLevelManager().getRequiredXP(level);
        
        String rodId = Main.getInstance().getPlayerDataManager().getEquippedRod(player);
        String rodName = "None";
        if (rodId != null) {
            com.meowchan12.fishingaudition.manager.RodData data = Main.getInstance().getRodManager().getRodById(rodId);
            if (data != null) rodName = data.getName();
        }

        double percentDouble = 0.0;
        if (neededXp > 0) {
            percentDouble = (xp / neededXp) * 100.0;
        }
        int percent = (int) Math.min(100, Math.max(0, percentDouble));
        
        // Progress bar (10 chars)
        int filled = (int) (percent / 10.0);
        int unfilled = 10 - filled;
        StringBuilder bar = new StringBuilder();
        bar.append("&8[&a");
        for(int i=0; i<filled; i++) bar.append("|");
        bar.append("&c");
        for(int i=0; i<unfilled; i++) bar.append("|");
        bar.append("&8]");

        player.sendMessage(MessageUtils.colorize("&b&l=== ✦ FISHING PROFILE ✦ ==="));
        player.sendMessage(MessageUtils.colorize("&fPlayer: &a" + player.getName()));
        player.sendMessage(MessageUtils.colorize("&fLevel: &e" + level));
        player.sendMessage(MessageUtils.colorize("&fXP: &d" + xp + " &7/ &d" + neededXp));
        player.sendMessage(MessageUtils.colorize("&fProgress: " + bar.toString() + " &7(" + percent + "%)"));
        player.sendMessage(MessageUtils.colorize(""));
        player.sendMessage(MessageUtils.colorize("&fCoins: &6" + coins + " &7(Rank: &e#" + coinRank + "&7)"));
        player.sendMessage(MessageUtils.colorize("&fTop Round: &c" + topRound + " &7(Rank: &e#" + roundRank + "&7)"));
        player.sendMessage(MessageUtils.colorize("&fEquipped Rod: &d" + rodName));
        
        String baitId = Main.getInstance().getPlayerDataManager().getEquippedBait(player);
        if (baitId != null) {
            com.meowchan12.fishingaudition.manager.BaitData bData = Main.getInstance().getBaitManager().getBaitById(baitId);
            if (bData != null) {
                int charges = Main.getInstance().getPlayerDataManager().getBaitCharges(player);
                int maxCharges = bData.getMaxCharges();
                player.sendMessage(MessageUtils.colorize("&fEquipped Bait: &6" + bData.getName() + " &7(" + charges + "/" + maxCharges + " left)"));
            } else {
                player.sendMessage(MessageUtils.colorize("&fEquipped Bait: &7None"));
            }
        } else {
            player.sendMessage(MessageUtils.colorize("&fEquipped Bait: &7None"));
        }
        
        player.sendMessage(MessageUtils.colorize("&b&l========================="));
    }
}

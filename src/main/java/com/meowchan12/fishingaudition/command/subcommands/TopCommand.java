package com.meowchan12.fishingaudition.command.subcommands;

import com.meowchan12.fishingaudition.Main;
import com.meowchan12.fishingaudition.command.SubCommand;
import com.meowchan12.fishingaudition.top.TopEntry;
import com.meowchan12.fishingaudition.utils.MessageUtils;
import org.bukkit.entity.Player;

import java.util.List;

public class TopCommand extends SubCommand {

    @Override
    public String getName() {
        return "top";
    }

    @Override
    public String getDescription() {
        return "View the leaderboards.";
    }

    @Override
    public String getSyntax() {
        return "/fish top <coins|round> [page]";
    }

    @Override
    public String getPermission() {
        return com.meowchan12.fishingaudition.constants.Permissions.USER_TOP;
    }

    @Override
    public void perform(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(MessageUtils.colorize("&cUsage: " + getSyntax()));
            return;
        }

        String type = args[1].toLowerCase();
        int page = 1;
        if (args.length >= 3) {
            try {
                page = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                player.sendMessage(MessageUtils.colorize("&cInvalid page number."));
                return;
            }
        }

        List<TopEntry> entries;
        String title;
        if (type.equals("coins")) {
            entries = Main.getInstance().getTopManager().getTopCoins();
            title = "&e&lTop FishCoins";
        } else if (type.equals("round")) {
            entries = Main.getInstance().getTopManager().getTopRounds();
            title = "&a&lTop Rounds";
        } else {
            player.sendMessage(MessageUtils.colorize("&cUsage: " + getSyntax()));
            return;
        }

        if (entries.isEmpty()) {
            player.sendMessage(MessageUtils.colorize("&cNo data available yet."));
            return;
        }

        int itemsPerPage = 10;
        int maxPages = (int) Math.ceil((double) entries.size() / itemsPerPage);
        
        if (page < 1) page = 1;
        if (page > maxPages) page = maxPages;

        player.sendMessage(MessageUtils.colorize("&8=========[ " + title + " &8]========="));
        
        int start = (page - 1) * itemsPerPage;
        int end = Math.min(start + itemsPerPage, entries.size());

        for (int i = start; i < end; i++) {
            TopEntry entry = entries.get(i);
            String valueStr = type.equals("coins") ? String.format("%.2f", entry.getValue()) : String.valueOf((int) entry.getValue());
            player.sendMessage(MessageUtils.colorize("&7" + (i + 1) + ". &f" + entry.getName() + " &8- &e" + valueStr));
        }

        player.sendMessage(MessageUtils.colorize("&8Page " + page + " / " + maxPages));
    }
}

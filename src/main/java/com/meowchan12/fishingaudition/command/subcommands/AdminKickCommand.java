package com.meowchan12.fishingaudition.command.subcommands;

import com.meowchan12.fishingaudition.Main;
import com.meowchan12.fishingaudition.command.SubCommand;
import com.meowchan12.fishingaudition.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class AdminKickCommand extends SubCommand {

    @Override
    public String getName() { return "kick"; }

    @Override
    public String getDescription() { return "Kick a player from the fishing session."; }

    @Override
    public String getSyntax() { return "/fish kick <player>"; }

    @Override
    public String getPermission() { return "fishingaudition.admin.kick"; }

    @Override
    public void perform(Player admin, String[] args) {
        if (args.length < 2) {
            admin.sendMessage(MessageUtils.colorize("&cUsage: " + getSyntax()));
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);

        if (target == null || !target.isOnline()) {
            admin.sendMessage(MessageUtils.colorize("&cPlayer not found or offline."));
            return;
        }

        // Ép chạy lệnh Leave
        target.performCommand("fish leave");

        admin.sendMessage(MessageUtils.colorize("&aSuccessfully forced &f" + target.getName() + " &ato leave the fishing area."));
        target.sendMessage(MessageUtils.colorize("&cYou were kicked from the fishing area by an Admin!"));
    }
}
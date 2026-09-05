package com.meowchan12.fishingaudition.command.subcommands;

import com.meowchan12.fishingaudition.Main;
import com.meowchan12.fishingaudition.command.SubCommand;
import com.meowchan12.fishingaudition.utils.MessageUtils;
import org.bukkit.entity.Player;

public class ReloadCommand extends SubCommand {

    @Override
    public String getName() { return "reload"; }

    @Override
    public String getDescription() { return "Reloads the plugin configuration."; }

    @Override
    public String getSyntax() { return "/fish reload"; }

    @Override
    public String getPermission() { return com.meowchan12.fishingaudition.constants.Permissions.ADMIN_RELOAD; }

    @Override
    public void perform(Player player, String[] args) {
        performConsole(player, args);
    }

    @Override
    public void performConsole(org.bukkit.command.CommandSender sender, String[] args) {
        Main.getInstance().reloadConfig();
        Main.getInstance().getRegionManager().loadRegion();

        sender.sendMessage(MessageUtils.colorize("&aFishingAudition configurations reloaded successfully!"));
    }
}

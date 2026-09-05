package com.meowchan12.fishingaudition.command.subcommands;

import com.meowchan12.fishingaudition.Main;
import com.meowchan12.fishingaudition.command.SubCommand;
import org.bukkit.entity.Player;

public class LogbookCommand extends SubCommand {

    @Override
    public String getName() {
        return "logbook";
    }

    @Override
    public String getDescription() {
        return "Open the Fish Logbook.";
    }

    @Override
    public String getSyntax() {
        return "/fish logbook";
    }

    @Override
    public String getPermission() {
        return com.meowchan12.fishingaudition.constants.Permissions.USER_LOGBOOK;
    }

    @Override
    public void perform(Player player, String[] args) {
        Main.getInstance().getLogbookManager().openGUI(player);
    }
}

package com.meowchan12.fishingaudition.command.subcommands;

import com.meowchan12.fishingaudition.Main;
import com.meowchan12.fishingaudition.command.SubCommand;
import com.meowchan12.fishingaudition.utils.MessageUtils;
import org.bukkit.entity.Player;

public class EventCommand extends SubCommand {

    @Override
    public String getName() {
        return "event";
    }

    @Override
    public String getDescription() {
        return "Check active events.";
    }

    @Override
    public String getSyntax() {
        return "/fa event";
    }

    @Override
    public String getPermission() {
        return com.meowchan12.fishingaudition.constants.Permissions.USER_EVENT;
    }

    @Override
    public void perform(Player player, String[] args) {
        if (Main.getInstance().getEventManager().isEventActive()) {
            double multi = Main.getInstance().getEventManager().getCurrentMultiplier();
            player.sendMessage(MessageUtils.colorize("&a&l🌟 Event is Active! &fCurrent Multiplier: &e" + multi + "x"));
        } else {
            player.sendMessage(MessageUtils.colorize("&cNo active events at the moment. Events usually run on Weekends!"));
        }
    }
}

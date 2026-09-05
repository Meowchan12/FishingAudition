package com.meowchan12.fishingaudition.command.subcommands;

import com.meowchan12.fishingaudition.Main;
import com.meowchan12.fishingaudition.command.SubCommand;
import com.meowchan12.fishingaudition.utils.MessageUtils;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class SetJoinCommand extends SubCommand {

    @Override
    public String getName() { return "setjoin"; }

    @Override
    public String getDescription() { return "Set the spawn location for the fishing area."; }

    @Override
    public String getSyntax() { return "/fish setjoin"; }

    @Override
    public String getPermission() { return com.meowchan12.fishingaudition.constants.Permissions.ADMIN_SETUP; }

    @Override
    public void perform(Player player, String[] args) {
        Location loc = player.getLocation();

        Main.getInstance().getConfig().set("region.join_location.world", loc.getWorld().getName());
        Main.getInstance().getConfig().set("region.join_location.x", loc.getX());
        Main.getInstance().getConfig().set("region.join_location.y", loc.getY());
        Main.getInstance().getConfig().set("region.join_location.z", loc.getZ());
        Main.getInstance().getConfig().set("region.join_location.yaw", loc.getYaw());
        Main.getInstance().getConfig().set("region.join_location.pitch", loc.getPitch());

        Main.getInstance().saveConfig();

        player.sendMessage(MessageUtils.colorize("&aJoin location has been successfully set!"));
    }
}

package com.meowchan12.fishingaudition.command.subcommands;

import com.meowchan12.fishingaudition.Main;
import com.meowchan12.fishingaudition.command.SubCommand;
import com.meowchan12.fishingaudition.utils.MessageUtils;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class SetLeaveCommand extends SubCommand {

    @Override
    public String getName() { return "setleave"; }

    @Override
    public String getDescription() { return "Set the exit location when leaving the fishing area."; }

    @Override
    public String getSyntax() { return "/fish setleave"; }

    @Override
    public String getPermission() { return "fishingaudition.admin.setup"; }

    @Override
    public void perform(Player player, String[] args) {
        Location loc = player.getLocation();

        Main.getInstance().getConfig().set("region.leave_location.world", loc.getWorld().getName());
        Main.getInstance().getConfig().set("region.leave_location.x", loc.getX());
        Main.getInstance().getConfig().set("region.leave_location.y", loc.getY());
        Main.getInstance().getConfig().set("region.leave_location.z", loc.getZ());
        Main.getInstance().getConfig().set("region.leave_location.yaw", loc.getYaw());
        Main.getInstance().getConfig().set("region.leave_location.pitch", loc.getPitch());

        Main.getInstance().saveConfig();

        player.sendMessage(MessageUtils.colorize("&aLeave location has been successfully set!"));
    }
}
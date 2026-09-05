package com.meowchan12.fishingaudition.command.subcommands;

import com.meowchan12.fishingaudition.Main;

import com.meowchan12.fishingaudition.command.SubCommand;
import com.meowchan12.fishingaudition.manager.InventoryManager;
import com.meowchan12.fishingaudition.utils.MessageUtils;
import org.bukkit.entity.Player;

public class LeaveCommand extends SubCommand {

    private final InventoryManager inventoryManager;

    public LeaveCommand(InventoryManager inventoryManager) {
        this.inventoryManager = inventoryManager;
    }

    @Override
    public String getName() {
        return "leave";
    }

    @Override
    public String getDescription() {
        return "Leave the fishing area and restore inventory.";
    }

    @Override
    public String getSyntax() {
        return "/fish leave";
    }

    @Override
    public String getPermission() {
        return com.meowchan12.fishingaudition.constants.Permissions.USER_LEAVE;
    }

    @Override
    public void perform(Player player, String[] args) {
        org.bukkit.configuration.file.FileConfiguration config = Main.getInstance().getConfig();
        if (!Main.getInstance().getRegionManager().isSet() || !config.contains("region.join_location.world") || !config.contains("region.leave_location.world")) {
            player.sendMessage(MessageUtils.colorize("&cThe fishing area has not been set up by an admin yet!"));
            return;
        }

        com.meowchan12.fishingaudition.command.CommandManager.leaveArena(player, "SELF", false);
    }
}

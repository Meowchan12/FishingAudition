package com.meowchan12.fishingaudition.command.subcommands;

import com.meowchan12.fishingaudition.command.SubCommand;
import com.meowchan12.fishingaudition.manager.ChestManager;
import org.bukkit.entity.Player;

public class ChestCommand extends SubCommand {

    private final ChestManager chestManager;

    public ChestCommand(ChestManager chestManager) {
        this.chestManager = chestManager;
    }

    @Override
    public String getName() { return "chest"; }

    @Override
    public String getDescription() { return "Open your virtual fish storage."; }

    @Override
    public String getSyntax() { return "/fish chest"; }

    @Override
    public String getPermission() { return com.meowchan12.fishingaudition.constants.Permissions.USER_CHEST; }

    @Override
    public void perform(Player player, String[] args) {
        chestManager.openChest(player);
    }
}

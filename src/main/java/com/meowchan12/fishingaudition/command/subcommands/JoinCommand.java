package com.meowchan12.fishingaudition.command.subcommands;

import com.meowchan12.fishingaudition.Main;
import com.meowchan12.fishingaudition.command.SubCommand;
import com.meowchan12.fishingaudition.manager.InventoryManager;
import com.meowchan12.fishingaudition.material.CustomRod;
import com.meowchan12.fishingaudition.utils.MessageUtils;
import org.bukkit.entity.Player;

public class JoinCommand extends SubCommand {

    private final InventoryManager inventoryManager;

    public JoinCommand(InventoryManager inventoryManager) {
        this.inventoryManager = inventoryManager;
    }

    @Override
    public String getName() {
        return "join";
    }

    @Override
    public String getDescription() {
        return "Join the fishing area.";
    }

    @Override
    public String getSyntax() {
        return "/fish join";
    }

    @Override
    public String getPermission() {
        return com.meowchan12.fishingaudition.constants.Permissions.USER_JOIN;
    }

    @Override
    public void perform(Player player, String[] args) {
        org.bukkit.configuration.file.FileConfiguration config = Main.getInstance().getConfig();
        if (!Main.getInstance().getRegionManager().isSet() || !config.contains("region.join_location.world") || !config.contains("region.leave_location.world")) {
            player.sendMessage(MessageUtils.colorize("&cThe fishing area has not been set up by an admin yet!"));
            return;
        }

        // Check if player is already in a session or region
        if (Main.getInstance().getPlayerDataManager().isInArena(player)) {
            player.sendMessage(MessageUtils.colorize("&cYou are already in the fishing area!"));
            return;
        }
        
        // Backup and clear inventory
        inventoryManager.backupAndClear(player);
        Main.getInstance().getPlayerDataManager().addPlayerToArena(player);

        // Give Equipped Rod
        String equippedRodId = Main.getInstance().getPlayerDataManager().getEquippedRod(player);
        com.meowchan12.fishingaudition.manager.RodData equippedData = Main.getInstance().getRodManager().getRodById(equippedRodId);
        
        if (equippedData == null) {
            equippedData = Main.getInstance().getRodManager().getRodById("starter_rod");
            Main.getInstance().getPlayerDataManager().setEquippedRod(player, "starter_rod");
            player.sendMessage(MessageUtils.colorize("&aYou didn't have a rod equipped, so we gave you a Starter Rod!"));
        }
        
        if (equippedData != null) {
            player.getInventory().setItem(4, CustomRod.generateRod(equippedData));
            player.getInventory().setHeldItemSlot(4);
        }

        Main.getInstance().getPlayerDataManager().setPreJoinLocation(player, player.getLocation());
        org.bukkit.Location joinLoc = Main.getInstance().getRegionManager().getJoinLocation();
        if (joinLoc != null) {
            player.teleport(joinLoc);
        }
        Main.getInstance().getScoreboardManager().createBoard(player);

        player.sendMessage(MessageUtils.colorize("&aYou have joined the fishing area! Your inventory has been saved."));
    }
}

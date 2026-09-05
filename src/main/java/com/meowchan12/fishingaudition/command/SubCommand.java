package com.meowchan12.fishingaudition.command;

import org.bukkit.entity.Player;

public abstract class SubCommand {

    public abstract String getName();
    public abstract String getDescription();
    public abstract String getSyntax();
    public abstract String getPermission();

    public abstract void perform(Player player, String[] args);

    public void performConsole(org.bukkit.command.CommandSender sender, String[] args) {
        if (sender instanceof Player) {
            perform((Player) sender, args);
        } else {
            sender.sendMessage("§cThis command can only be executed by players!");
        }
    }
}
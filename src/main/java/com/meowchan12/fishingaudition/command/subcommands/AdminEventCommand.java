package com.meowchan12.fishingaudition.command.subcommands;

import com.meowchan12.fishingaudition.Main;
import com.meowchan12.fishingaudition.command.SubCommand;
import com.meowchan12.fishingaudition.utils.MessageUtils;
import org.bukkit.entity.Player;

import java.time.ZoneId;
import java.time.ZonedDateTime;

public class AdminEventCommand extends SubCommand {

    @Override
    public String getName() {
        return "adminevent";
    }

    @Override
    public String getDescription() {
        return "Manage and debug automated events.";
    }

    @Override
    public String getSyntax() {
        return "/fa adminevent <toggle|debug>";
    }

    @Override
    public String getPermission() {
        return com.meowchan12.fishingaudition.constants.Permissions.ADMIN_EVENT;
    }

    @Override
    public void perform(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(MessageUtils.colorize("&cUsage: " + getSyntax()));
            return;
        }

        String action = args[1].toLowerCase();
        
        if (action.equals("toggle")) {
            boolean current = Main.getInstance().getEventManager().isForceOverride();
            Main.getInstance().getEventManager().setForceOverride(!current);
            player.sendMessage(MessageUtils.colorize("&aEvent Force Override set to: &e" + !current));
        } 
        else if (action.equals("debug")) {
            ZonedDateTime now = ZonedDateTime.now(ZoneId.systemDefault());
            String time = now.toLocalTime().toString();
            String day = now.getDayOfWeek().name();
            boolean configEnabled = Main.getInstance().getConfig().getBoolean("events.weekend-xp.enabled", false);
            boolean isActive = Main.getInstance().getEventManager().isEventActive();
            boolean forceOverride = Main.getInstance().getEventManager().isForceOverride();
            double multi = Main.getInstance().getEventManager().getCurrentMultiplier();
            
            player.sendMessage(MessageUtils.colorize("&b&l=== ✦ EVENT DEBUG ✦ ==="));
            player.sendMessage(MessageUtils.colorize("&fServer Time: &e" + time));
            player.sendMessage(MessageUtils.colorize("&fDay of Week: &e" + day));
            player.sendMessage(MessageUtils.colorize("&fConfig Enabled: &e" + configEnabled));
            player.sendMessage(MessageUtils.colorize("&fForce Override: &e" + forceOverride));
            player.sendMessage(MessageUtils.colorize("&fEvent Active: &a" + isActive));
            player.sendMessage(MessageUtils.colorize("&fCurrent Multiplier: &a" + multi + "x"));
            player.sendMessage(MessageUtils.colorize("&b&l======================="));
            
            Main.getInstance().getLogger().info("=== EVENT DEBUG ===");
            Main.getInstance().getLogger().info("Server Time: " + time);
            Main.getInstance().getLogger().info("Day of Week: " + day);
            Main.getInstance().getLogger().info("Config Enabled: " + configEnabled);
            Main.getInstance().getLogger().info("Force Override: " + forceOverride);
            Main.getInstance().getLogger().info("Event Active: " + isActive);
            Main.getInstance().getLogger().info("Current Multiplier: " + multi + "x");
            Main.getInstance().getLogger().info("===================");
        } else {
            player.sendMessage(MessageUtils.colorize("&cUnknown action. Use: toggle, debug"));
        }
    }
}

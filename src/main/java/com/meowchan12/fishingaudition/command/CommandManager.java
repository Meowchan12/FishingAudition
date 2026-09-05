package com.meowchan12.fishingaudition.command;

import com.meowchan12.fishingaudition.Main;
import com.meowchan12.fishingaudition.command.subcommands.*;
import com.meowchan12.fishingaudition.utils.MessageUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.TabCompleter;
import java.util.Collections;

public class CommandManager implements CommandExecutor, TabCompleter {

    private final List<SubCommand> subCommands = new ArrayList<>();

    public CommandManager(Main plugin) {
        // Register SubCommands here
        subCommands.add(new JoinCommand(plugin.getInventoryManager()));
        subCommands.add(new LeaveCommand(plugin.getInventoryManager()));
        subCommands.add(new SetRegionCommand());
        subCommands.add(new SetJoinCommand());
        subCommands.add(new SetLeaveCommand());
        subCommands.add(new ReloadCommand());
        subCommands.add(new AdminEcoCommand());
        subCommands.add(new AdminKickCommand());
        subCommands.add(new RepairCommand());
        subCommands.add(new MenuCommand());
        subCommands.add(new GiveFishCommand());
        subCommands.add(new LogbookCommand());
        subCommands.add(new MigrateCommand());
        subCommands.add(new ChestCommand(plugin.getChestManager()));
        subCommands.add(new SellAllCommand());
        subCommands.add(new ShopCommand());
        subCommands.add(new PreviewCommand());
        subCommands.add(new BalCommand());
        subCommands.add(new BalLookCommand());
        subCommands.add(new PayCommand());
        subCommands.add(new TopCommand());
        subCommands.add(new StatsCommand());
        subCommands.add(new AdminXpCommand());
        subCommands.add(new ProfileCommand());
        subCommands.add(new EventCommand());
        subCommands.add(new AdminEventCommand());
        subCommands.add(new DebugCommand());
    }

    public static final java.util.Set<java.util.UUID> leavingPlayers = java.util.concurrent.ConcurrentHashMap.newKeySet();

    public static void leaveArena(Player player, String reason, boolean sync) {
        Main plugin = Main.getInstance();
        if (plugin == null) return;

        if (plugin.getPlayerDataManager() == null || !plugin.getPlayerDataManager().isInArena(player)) {
            player.sendMessage(MessageUtils.colorize("&cYou are not in the fishing area!"));
            return;
        }

        if (!leavingPlayers.add(player.getUniqueId())) {
            player.sendMessage(MessageUtils.colorize("&cPlease wait, you are already leaving the area..."));
            return;
        }

        try {
            // 1. Remove from active sessions (QTE)
            if (plugin.getSessionManager() != null && plugin.getSessionManager().isPlaying(player)) {
                com.meowchan12.fishingaudition.mechanics.AuditionSession session = plugin.getSessionManager().getSession(player);
                if (session != null) session.endSession(false);
                plugin.getSessionManager().removeSession(player);
            }
            
            // 2 & 3. Virtual items -> Chest -> Auto-sell
            if (plugin.getChestManager() != null) {
                if (sync) {
                    plugin.getChestManager().sweepFishToChestSync(player);
                } else {
                    plugin.getChestManager().sweepFishToChest(player);
                }
            }
            
            // Clear virtual inventory
            player.getInventory().clear();
            
            // 4. Restore original inventory (Atomic)
            if (plugin.getInventoryManager() != null && plugin.getInventoryManager().hasBackup(player)) {
                if (sync) {
                    plugin.getInventoryManager().restoreInventorySync(player);
                } else {
                    plugin.getInventoryManager().restoreInventory(player);
                }
            }
            
            // 5. Restore Main Scoreboard & Arena State
            if (plugin.getScoreboardManager() != null) {
                plugin.getScoreboardManager().removeBoard(player);
            }
            plugin.getPlayerDataManager().removePlayerFromArena(player);
            
            // 6. Safe Teleport
            if (plugin.getPlayerDataManager() != null) {
                org.bukkit.Location preLoc = plugin.getPlayerDataManager().getPreJoinLocation(player);
                if (preLoc != null) {
                    player.teleport(preLoc);
                    plugin.getPlayerDataManager().removePreJoinLocation(player);
                } else if (plugin.getRegionManager() != null && plugin.getRegionManager().getLeaveLocation() != null) {
                    player.teleport(plugin.getRegionManager().getLeaveLocation());
                }
            }

            if ("KICKED".equals(reason)) {
                player.sendMessage(MessageUtils.colorize("&cYou were kicked from the fishing area!"));
            } else if ("SELF".equals(reason)) {
                player.sendMessage(MessageUtils.colorize("&aYou have left the fishing area."));
            }
        } finally {
            leavingPlayers.remove(player.getUniqueId());
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (args.length > 0 && !args[0].equalsIgnoreCase("help")) {
            for (SubCommand subCommand : subCommands) {
                if (args[0].equalsIgnoreCase(subCommand.getName())) {
                    if (sender.hasPermission(subCommand.getPermission())) {
                        subCommand.performConsole(sender, args);
                    } else {
                        sender.sendMessage(MessageUtils.colorize("&cYou do not have permission to use this command."));
                    }
                    return true;
                }
            }
        }

        // Help Menu Pagination
        int page = 1;
        if (args.length > 0 && args[0].equalsIgnoreCase("help")) {
            if (args.length > 1) {
                try {
                    page = Integer.parseInt(args[1]);
                } catch (NumberFormatException ignored) {}
            }
        } else if (args.length == 1) {
            try {
                page = Integer.parseInt(args[0]);
            } catch (NumberFormatException ignored) {}
        }

        List<SubCommand> permittedCommands = new ArrayList<>();
        for (SubCommand subCommand : subCommands) {
            if (sender.hasPermission(subCommand.getPermission())) {
                permittedCommands.add(subCommand);
            }
        }

        int maxCommandsPerPage = 5;
        int maxPages = (int) Math.ceil((double) permittedCommands.size() / maxCommandsPerPage);
        if (page < 1) page = 1;
        if (page > maxPages) page = maxPages;

        sender.sendMessage(MessageUtils.colorize("<#3498db>&l=== FishingAudition Help (" + page + "/" + maxPages + ") ==="));
        
        int startIndex = (page - 1) * maxCommandsPerPage;
        int endIndex = Math.min(startIndex + maxCommandsPerPage, permittedCommands.size());

        for (int i = startIndex; i < endIndex; i++) {
            SubCommand subCommand = permittedCommands.get(i);
            sender.sendMessage(MessageUtils.colorize("&e" + subCommand.getSyntax() + " &8- &7" + subCommand.getDescription()));
        }
        
        if (page < maxPages) {
            sender.sendMessage(MessageUtils.colorize("&aType &e/fish help " + (page + 1) + " &ato see the next page."));
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        List<String> list = new ArrayList<>();

        if (args.length == 1) {
            for (SubCommand subCommand : subCommands) {
                if (sender.hasPermission(subCommand.getPermission())) {
                    list.add(subCommand.getName());
                }
            }
        } else if (args.length == 2) {
            String subCmd = args[0].toLowerCase();
            if (subCmd.equals("shop")) {
                list.add("currency");
                list.add("rod");
            } else if (subCmd.equals("top")) {
                list.add("fishing");
                list.add("currency");
            } else if (subCmd.equals("setregion")) {
                list.add("1");
                list.add("2");
            } else if (subCmd.equals("eco")) {
                list.add("give");
                list.add("take");
                list.add("set");
            } else if (subCmd.equals("kick") || subCmd.equals("givefish")) {
                for (Player p : org.bukkit.Bukkit.getOnlinePlayers()) {
                    list.add(p.getName());
                }
            }
        } else if (args.length == 3) {
            String subCmd = args[0].toLowerCase();
            if (subCmd.equals("eco")) {
                for (Player p : org.bukkit.Bukkit.getOnlinePlayers()) {
                    list.add(p.getName());
                }
            } else if (subCmd.equals("givefish")) {
                list.addAll(Main.getInstance().getFishManager().getAllFishIds());
            }
        }

        org.bukkit.util.StringUtil.copyPartialMatches(args[args.length - 1], list, completions);
        Collections.sort(completions);
        return completions;
    }
}
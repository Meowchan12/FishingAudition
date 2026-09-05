package com.meowchan12.fishingaudition.command.subcommands;

import com.meowchan12.fishingaudition.Main;
import com.meowchan12.fishingaudition.command.SubCommand;
import com.meowchan12.fishingaudition.utils.MessageUtils;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Set;
import java.util.UUID;

public class DebugCommand extends SubCommand {

    @Override
    public String getName() { return "debug"; }

    @Override
    public String getDescription() { return "Show system telemetry dashboard."; }

    @Override
    public String getSyntax() { return "/fish debug"; }

    @Override
    public String getPermission() { return com.meowchan12.fishingaudition.constants.Permissions.ADMIN_DEBUG; }

    @Override
    public void perform(Player player, String[] args) {
        performConsole(player, args);
    }

    @Override
    public void performConsole(org.bukkit.command.CommandSender sender, String[] args) {
        Main plugin = Main.getInstance();
        
        sender.sendMessage(MessageUtils.colorize("&b&l================ [ FishingAudition Debug ] ================"));
        
        // Arena Population
        int arenaCount = plugin.getPlayerDataManager().getArenaPlayerCount();
        Set<UUID> arenaPlayers = plugin.getPlayerDataManager().getPlayersInArena();
        StringBuilder arenaNames = new StringBuilder();
        if (arenaCount > 0 && arenaCount <= 5) {
            for (UUID uuid : arenaPlayers) {
                Player p = Bukkit.getPlayer(uuid);
                if (p != null) {
                    if (arenaNames.length() > 0) arenaNames.append(", ");
                    arenaNames.append(p.getName());
                }
            }
        }
        String arenaDetail = arenaCount > 0 && arenaCount <= 5 ? " (" + arenaNames + ")" : "";
        sender.sendMessage(MessageUtils.colorize("&e * Arena Population: &f" + arenaCount + " players" + arenaDetail));
        
        // Active Minigames (QTE)
        int activeSessions = plugin.getSessionManager() != null ? plugin.getSessionManager().getActiveSessionsCount() : 0;
        sender.sendMessage(MessageUtils.colorize("&e * Active Minigames: &f" + activeSessions + " active QTE sessions"));
        
        // Economy Hook
        String econHook = "None";
        if (plugin.getEconomyManager() != null) {
            econHook = plugin.getEconomyManager().getProviderName();
        }
        sender.sendMessage(MessageUtils.colorize("&e * Economy Hook: &f" + econHook));
        
        // Database Engine
        String dbEngine = plugin.getDatabaseManager().getType().toUpperCase();
        sender.sendMessage(MessageUtils.colorize("&e * Database Engine: &f" + dbEngine));
        
        // HikariCP Pool
        HikariDataSource ds = plugin.getDatabaseManager().getDataSource();
        if (ds != null && !ds.isClosed()) {
            HikariPoolMXBean poolBean = ds.getHikariPoolMXBean();
            if (poolBean != null) {
                sender.sendMessage(MessageUtils.colorize("&e * HikariCP Pool: &fActive: " + poolBean.getActiveConnections() + 
                    " | Idle: " + poolBean.getIdleConnections() + 
                    " | Total: " + poolBean.getTotalConnections()));
            } else {
                sender.sendMessage(MessageUtils.colorize("&e * HikariCP Pool: &7MXBean unavailable"));
            }
        } else {
            sender.sendMessage(MessageUtils.colorize("&e * HikariCP Pool: &cClosed"));
        }
        
        // Memory Cache
        int cachedProfiles = plugin.getPlayerDataManager().getCachedProfileCount();
        int dirtyRecords = plugin.getPlayerDataManager().getDirtyCount();
        sender.sendMessage(MessageUtils.colorize("&e * Memory Cache: &f" + cachedProfiles + " cached profiles | &cDirty: " + dirtyRecords + " pending save"));
        
        // JVM Memory
        Runtime rt = Runtime.getRuntime();
        long freeMB = rt.freeMemory() / 1024 / 1024;
        long maxMB = rt.maxMemory() / 1024 / 1024;
        long usedMB = maxMB - freeMB;
        sender.sendMessage(MessageUtils.colorize("&e * JVM Memory: &fUsed: " + usedMB + " MB / Max: " + maxMB + " MB (Free: " + freeMB + " MB)"));
        
        sender.sendMessage(MessageUtils.colorize("&b&l==========================================================="));
    }
}

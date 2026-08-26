package com.meowchan12.fishingaudition.scoreboard;

import com.meowchan12.fishingaudition.Main;
import com.meowchan12.fishingaudition.currencymanager.FishCoinManager;
import com.meowchan12.fishingaudition.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.UUID;

public class ScoreboardManager {

    private final Main plugin;
    private final Map<UUID, Scoreboard> boards = new ConcurrentHashMap<>();

    public ScoreboardManager(Main plugin) {
        this.plugin = plugin;
        startUpdateTask();
    }

    public void createBoard(Player player) {
        Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective obj = board.registerNewObjective("fa_board", "dummy", MessageUtils.colorize("&#00b4db&lFISHING &#0083b0&lAUDITION"));
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);

        setLine(board, obj, 9, "§1", "");
        setLine(board, obj, 8, "§2", "");
        setLine(board, obj, 7, "§3", "");
        setLine(board, obj, 6, "§4", "");
        setLine(board, obj, 5, "§5", "");
        setLine(board, obj, 4, "§6", "");
        setLine(board, obj, 3, "§7", "");
        setLine(board, obj, 2, "§8", MessageUtils.colorize("&eType /fa leave"));
        setLine(board, obj, 1, "§9", MessageUtils.colorize("&eto exit the game!"));

        boards.put(player.getUniqueId(), board);
        player.setScoreboard(board);
        updateBoard(player); 
    }

    private void setLine(Scoreboard board, Objective obj, int score, String entry, String text) {
        Team team = board.getTeam("line_" + score);
        if (team == null) {
            team = board.registerNewTeam("line_" + score);
            team.addEntry(entry);
        }
        team.setPrefix(text);
        obj.getScore(entry).setScore(score);
    }

    public void removeBoard(Player player) {
        boards.remove(player.getUniqueId());
        if (player.isOnline()) {
            player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
        }
    }

    public void updateBoard(Player player) {
        Scoreboard board = boards.get(player.getUniqueId());
        if (board == null) return;

        double coins = FishCoinManager.getBalance(player);
        int topRound = plugin.getPlayerDataManager().getTopRound(player);
        
        String equippedRodId = plugin.getPlayerDataManager().getEquippedRod(player);
        com.meowchan12.fishingaudition.manager.RodData equippedData = plugin.getRodManager().getRodById(equippedRodId);
        String rodName = (equippedData != null) ? equippedData.getName() : "Starter Rod";

        int level = plugin.getLevelManager().getLevel(player);
        double currentXp = plugin.getLevelManager().getXP(player);
        double neededXp = plugin.getLevelManager().getRequiredXP(level);

        Team tLevel = board.getTeam("line_8");
        if (tLevel != null) tLevel.setPrefix(MessageUtils.colorize("&#f8b500✦ Level: &f" + level));

        Team tXp = board.getTeam("line_7");
        if (tXp != null) tXp.setPrefix(MessageUtils.colorize("&#f8b500✦ XP: &f" + String.format("%.1f", currentXp) + "/" + String.format("%.1f", neededXp)));

        Team tCoins = board.getTeam("line_6");
        if (tCoins != null) tCoins.setPrefix(MessageUtils.colorize("&#f8b500✦ Coins: &f" + String.format("%.1f", coins)));

        Team tTop = board.getTeam("line_5");
        if (tTop != null) tTop.setPrefix(MessageUtils.colorize("&#f8b500✦ Top Round: &f" + topRound));

        Team tRod = board.getTeam("line_4");
        if (tRod != null) tRod.setPrefix(MessageUtils.colorize("&#f8b500✦ Rod: &f" + rodName));
    }

    private void startUpdateTask() {
        com.meowchan12.fishingaudition.utils.SchedulerUtils.runTimerAsync(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (boards.containsKey(player.getUniqueId())) {
                    com.meowchan12.fishingaudition.utils.SchedulerUtils.runAtEntity(plugin, player, () -> {
                        updateBoard(player);
                    });
                }
            }
        }, 20L, 20L);
    }

    public void cleanup() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (boards.containsKey(player.getUniqueId())) {
                player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
            }
        }
        boards.clear();
    }
}
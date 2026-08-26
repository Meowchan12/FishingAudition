package com.meowchan12.fishingaudition.top;

import com.meowchan12.fishingaudition.Main;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TopManager {

    private final Main plugin;
    private final List<TopEntry> topCoins = new ArrayList<>();
    private final List<TopEntry> topRounds = new ArrayList<>();

    public TopManager(Main plugin) {
        this.plugin = plugin;
        startAutoUpdate();
    }

    public void startAutoUpdate() {
        com.meowchan12.fishingaudition.utils.SchedulerUtils.runTimerAsync(plugin, this::calculateTop, 100L, 12000L);
    }

    public void calculateTop() {
        List<TopEntry> tempCoins = new ArrayList<>();
        List<TopEntry> tempRounds = new ArrayList<>();

        String dbType = plugin.getDatabaseManager().getType();
        String sqlCoins = "SELECT uuid, name, fishcoins FROM fishing_users ORDER BY fishcoins DESC LIMIT 10";
        String sqlRounds = "SELECT uuid, name, top_round FROM fishing_users ORDER BY top_round DESC LIMIT 10";

        try (java.sql.Connection conn = plugin.getDatabaseManager().getConnection()) {
            
            // Top Coins
            try (java.sql.PreparedStatement ps = conn.prepareStatement(sqlCoins);
                 java.sql.ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    try {
                        UUID uuid = UUID.fromString(rs.getString("uuid"));
                        String name = rs.getString("name");
                        double coins = rs.getDouble("fishcoins");
                        tempCoins.add(new TopEntry(uuid, name, coins));
                    } catch (Exception e) {}
                }
            }

            // Top Rounds
            try (java.sql.PreparedStatement ps = conn.prepareStatement(sqlRounds);
                 java.sql.ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    try {
                        UUID uuid = UUID.fromString(rs.getString("uuid"));
                        String name = rs.getString("name");
                        int round = rs.getInt("top_round");
                        tempRounds.add(new TopEntry(uuid, name, round));
                    } catch (Exception e) {}
                }
            }

        } catch (java.sql.SQLException e) {
            plugin.getLogger().severe("Failed to load top leaderboards: " + e.getMessage());
            return;
        }

        // Sắp xếp giảm dần
        tempCoins.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));
        tempRounds.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));

        // Cập nhật list chính an toàn
        synchronized (this) {
            topCoins.clear();
            topRounds.clear();
            topCoins.addAll(tempCoins);
            topRounds.addAll(tempRounds);
        }
    }

    public List<TopEntry> getTopCoins() {
        synchronized (this) { return new ArrayList<>(topCoins); }
    }

    public List<TopEntry> getTopRounds() {
        synchronized (this) { return new ArrayList<>(topRounds); }
    }

    public String getPlayerCoinRank(UUID uuid) {
        synchronized (this) {
            for (int i = 0; i < topCoins.size(); i++) {
                if (topCoins.get(i).getUuid().equals(uuid)) {
                    return String.valueOf(i + 1);
                }
            }
        }
        return "Unranked";
    }

    public String getPlayerRoundRank(UUID uuid) {
        synchronized (this) {
            for (int i = 0; i < topRounds.size(); i++) {
                if (topRounds.get(i).getUuid().equals(uuid)) {
                    return String.valueOf(i + 1);
                }
            }
        }
        return "Unranked";
    }
}
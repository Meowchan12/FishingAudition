package com.meowchan12.fishingaudition.utils;

import com.meowchan12.fishingaudition.Main;
import com.meowchan12.fishingaudition.currencymanager.FishCoinManager;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class FishingPlaceholder extends PlaceholderExpansion {

    private final Main plugin;

    public FishingPlaceholder(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getIdentifier() {
        return "fa";
    }

    @Override
    public String getAuthor() {
        return "Meowchan12";
    }

    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer offlinePlayer, String params) {
        if (params.equalsIgnoreCase("coins")) {
            if (offlinePlayer != null && offlinePlayer.isOnline()) {
                return String.format("%.2f", FishCoinManager.getBalance(offlinePlayer.getPlayer()));
            }
            return "0.00";
        }

        if (params.startsWith("top_name_")) {
            try {
                int pos = Integer.parseInt(params.split("_")[2]);
                java.util.List<com.meowchan12.fishingaudition.top.TopEntry> topCoins = plugin.getTopManager().getTopCoins();
                if (pos > 0 && pos <= topCoins.size()) {
                    return topCoins.get(pos - 1).getName();
                }
            } catch (Exception e) {}
            return "N/A";
        }

        if (params.startsWith("top_round_")) {
            try {
                int pos = Integer.parseInt(params.split("_")[2]);
                java.util.List<com.meowchan12.fishingaudition.top.TopEntry> topRounds = plugin.getTopManager().getTopRounds();
                if (pos > 0 && pos <= topRounds.size()) {
                    return String.valueOf((int) topRounds.get(pos - 1).getValue());
                }
            } catch (Exception e) {}
            return "0";
        }

        return null;
    }
}
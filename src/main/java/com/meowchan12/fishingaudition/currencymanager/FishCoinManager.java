package com.meowchan12.fishingaudition.currencymanager;

import org.bukkit.entity.Player;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.UUID;

public class FishCoinManager {

    // Balance Cache: Player UUID -> Balance
    private static final Map<UUID, Double> balances = new ConcurrentHashMap<>();

    public static double getBalance(Player player) {
        return balances.getOrDefault(player.getUniqueId(), 0.0);
    }

    public static void setBalance(Player player, double amount) {
        balances.put(player.getUniqueId(), Math.max(0.0, amount));
    }

    public static void addBalance(Player player, double amount) {
        setBalance(player, getBalance(player) + amount);
    }

    public static boolean removeBalance(Player player, double amount) {
        double current = getBalance(player);
        if (current >= amount) {
            setBalance(player, current - amount);
            return true;
        }
        return false;
    }

    public static boolean hasEnough(Player player, double amount) {
        return getBalance(player) >= amount;
    }

    public static void unloadData(Player player) {
        balances.remove(player.getUniqueId());
    }
}
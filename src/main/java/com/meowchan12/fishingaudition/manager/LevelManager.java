package com.meowchan12.fishingaudition.manager;

import com.meowchan12.fishingaudition.Main;
import com.meowchan12.fishingaudition.material.FishTier;
import com.meowchan12.fishingaudition.utils.MessageUtils;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.UUID;

public class LevelManager {

    private final Main plugin;
    private final Map<UUID, Integer> playerLevels = new ConcurrentHashMap<>();
    private final Map<UUID, Double> playerXP = new ConcurrentHashMap<>();

    public LevelManager(Main plugin) {
        this.plugin = plugin;
    }

    public void setLevel(Player player, int level) {
        playerLevels.put(player.getUniqueId(), level);
    }

    public void setXP(Player player, double xp) {
        playerXP.put(player.getUniqueId(), xp);
    }

    public int getLevel(Player player) {
        return playerLevels.getOrDefault(player.getUniqueId(), 1);
    }

    public double getXP(Player player) {
        return playerXP.getOrDefault(player.getUniqueId(), 0.0);
    }

    public double getRequiredXP(int level) {
        org.bukkit.configuration.file.FileConfiguration config = plugin.getConfig();
        double base = config.getDouble("level-system.base-xp", 100.0);
        double multiplier = config.getDouble("level-system.multiplier", 1.5);
        return Math.round(base * Math.pow(level, multiplier));
    }

    public void addXP(Player player, double amount) {
        double multiplier = plugin.getEventManager().getCurrentMultiplier();
        double currentXP = getXP(player) + (amount * multiplier);
        setXP(player, currentXP);
        checkLevelUp(player);
    }
    
    public void addXP(Player player, com.meowchan12.fishingaudition.manager.FishData fish) {
        addXP(player, fish.getXpReward());
    }

    public void checkLevelUp(Player player) {
        double currentXP = getXP(player);
        int currentLevel = getLevel(player);
        double requiredXP = getRequiredXP(currentLevel);

        boolean leveledUp = false;
        while (currentXP >= requiredXP) {
            currentXP -= requiredXP;
            currentLevel++;
            requiredXP = getRequiredXP(currentLevel);
            leveledUp = true;
        }

        if (leveledUp) {
            setXP(player, currentXP);
            setLevel(player, currentLevel);
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
            MessageUtils.sendTitle(player, "&a&lLEVEL UP!", "&fYou reached Level &e" + currentLevel);
            player.spawnParticle(org.bukkit.Particle.FIREWORK, player.getLocation().add(0, 1, 0), 100, 0.5, 0.5, 0.5, 0.1);
        }
    }

    public double getPassiveRarityBoost(Player player) {
        // 0.1% per level
        return getLevel(player) * 0.1;
    }

    public void unloadData(Player player) {
        playerLevels.remove(player.getUniqueId());
        playerXP.remove(player.getUniqueId());
    }
}

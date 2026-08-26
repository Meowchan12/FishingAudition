package com.meowchan12.fishingaudition.mechanics;

import com.meowchan12.fishingaudition.Main;
import com.meowchan12.fishingaudition.material.CustomFish;
import com.meowchan12.fishingaudition.material.CustomRod;
import com.meowchan12.fishingaudition.material.FishTier;
import com.meowchan12.fishingaudition.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class AuditionSession {

    private final Main plugin;
    private final UUID playerUUID;
    private final org.bukkit.Location startLocation;
    private BossBar bossBar;
    
    // Single task for the whole session
    private com.meowchan12.fishingaudition.utils.SchedulerUtils.ScheduledTask sessionTask;

    private int currentRound = 1;
    private int currentLevel = 0;
    private double bossBarProgress;
    private int targetNumber;
    private double keyTimer; // in seconds
    private final Random random = new Random();
    
    // Config values cache
    private double drainPerSecond;
    private double penaltyLoss;
    private double correctGain;
    
    private final List<ItemStack> caughtFish = new ArrayList<>();
    private boolean isEnding = false;

    public AuditionSession(Main plugin, Player player) {
        this.plugin = plugin;
        this.playerUUID = player.getUniqueId();
        this.startLocation = player.getLocation().clone();

        this.bossBar = Bukkit.createBossBar(
                MessageUtils.getRawMessage("minigame.start_title", "&eStarting Fishing Audition..."),
                BarColor.BLUE,
                BarStyle.SOLID
        );
        this.bossBar.addPlayer(player);
        this.bossBar.setVisible(true);
        
        loadConfigValues();
        resetForNextRound(true); // first round init
        startSessionTask();
    }

    private void loadConfigValues() {
        FileConfiguration config = plugin.getConfig();
        drainPerSecond = config.getDouble("gameplay.drain-per-second", 0.02);
        penaltyLoss = config.getDouble("gameplay.penalty-progress-loss", 0.3);
        correctGain = config.getDouble("gameplay.correct-progress-gain", 0.2);
    }

    private void startSessionTask() {
        Player player = Bukkit.getPlayer(playerUUID);
        if (player == null) return;

        sessionTask = com.meowchan12.fishingaudition.utils.SchedulerUtils.runTimerAsync(plugin, () -> {
            if (isEnding) return;
            
            Player p = Bukkit.getPlayer(playerUUID);
            if (p == null) {
                endSession(false);
                return;
            }

            // Must update UI and state sync or async depending on what's safe. BossBar update is thread-safe in paper usually, but logic should ideally be synced to entity.
            com.meowchan12.fishingaudition.utils.SchedulerUtils.runAtEntity(plugin, p, () -> {
                if (isEnding) return;

                // Passive Drain
                bossBarProgress -= (drainPerSecond / 20.0);
                
                // Key Timer Drain
                keyTimer -= 0.05; // 1 tick = 0.05 seconds
                
                if (bossBarProgress <= 0.0) {
                    endSession(false);
                    return;
                }

                if (keyTimer <= 0) {
                    // Timeout -> Miss penalty
                    double missReduction = getMissPenaltyReduction(p);
                    bossBarProgress -= (penaltyLoss * (1.0 - missReduction));
                    com.meowchan12.fishingaudition.utils.SoundUtils.playSound(p, "wrong_key");
                    MessageUtils.sendTitle(p, MessageUtils.getRawMessage("minigame.miss_title", "&cMISS!"), MessageUtils.getRawMessage("minigame.miss_subtitle", ""));
                    
                    if (bossBarProgress <= 0.0) {
                        endSession(false);
                        return;
                    }
                    startNextNumber();
                }

                updateBossBarUI();
            });
        }, 1L, 1L);
    }

    public void resetForNextRound(boolean isFirstRound) {
        if (!isFirstRound) {
            currentRound++;
        }
        
        if (currentRound >= 10 && currentRound < 20) currentLevel = 1;
        else if (currentRound >= 20) currentLevel = 2; // Can default to level 1 config if level 2 doesn't exist
        else currentLevel = 0;

        FileConfiguration config = plugin.getConfig();
        String levelPath = "gameplay.levels.level-" + currentLevel;
        if (!config.contains(levelPath)) {
            levelPath = "gameplay.levels.level-1"; // fallback
        }
        
        bossBarProgress = config.getDouble(levelPath + ".start-progress", 0.5);
        startNextNumber();
        updateBossBarUI();
    }

    private void startNextNumber() {
        int[] validSlots = {0, 1, 2, 3, 5, 6, 7, 8};
        targetNumber = validSlots[random.nextInt(validSlots.length)];
        
        FileConfiguration config = plugin.getConfig();
        String levelPath = "gameplay.levels.level-" + currentLevel;
        if (!config.contains(levelPath)) {
            levelPath = "gameplay.levels.level-1";
        }
        
        double timeMin = config.getDouble(levelPath + ".time-min", 2.0);
        double timeMax = config.getDouble(levelPath + ".time-max", 2.5);
        
        Player p = Bukkit.getPlayer(playerUUID);
        double extra = p != null ? getExtraTime(p.getInventory().getItemInMainHand()) : 0.0;
        
        if (timeMax > timeMin) {
            keyTimer = timeMin + (timeMax - timeMin) * random.nextDouble() + extra;
        } else {
            keyTimer = timeMin + extra;
        }
    }

    public void handleInput(int inputNumber) {
        if (isEnding) return;

        Player player = Bukkit.getPlayer(playerUUID);
        if (player == null) return;
        
        com.meowchan12.fishingaudition.utils.SchedulerUtils.runAtEntity(plugin, player, () -> {
            if (isEnding) return;

            if (inputNumber == targetNumber) {
                // Correct
                double pBoost = getProgressBoost(player.getInventory().getItemInMainHand());
                bossBarProgress += (correctGain * pBoost);
                com.meowchan12.fishingaudition.utils.SoundUtils.playSound(player, "correct_key");
                
                if (bossBarProgress >= 1.0D) {
                    winRound(player);
                    return;
                }
            } else {
                // Wrong
                double missReduction = getMissPenaltyReduction(player);
                bossBarProgress -= (penaltyLoss * (1.0 - missReduction));
                com.meowchan12.fishingaudition.utils.SoundUtils.playSound(player, "wrong_key");
                MessageUtils.sendTitle(player, MessageUtils.getRawMessage("minigame.miss_title", "&cMISS!"), MessageUtils.getRawMessage("minigame.miss_subtitle", ""));
                
                if (bossBarProgress <= 0.0D) {
                    endSession(false);
                    return;
                }
            }

            startNextNumber();
            updateBossBarUI();
        });
    }
    
    private void winRound(Player player) {
        player.sendMessage(MessageUtils.getMessage("minigame.round_cleared").replace("{round}", String.valueOf(currentRound)));
        com.meowchan12.fishingaudition.utils.SoundUtils.playSound(player, "win_round");
        
        // QoL: Restore health and food
        player.setHealth(20.0);
        player.setFoodLevel(20);
        player.setSaturation(20.0f);
        
        // Add random fish to buffer
        ItemStack rod = player.getInventory().getItemInMainHand();
        double rarityBoost = getRarityBoostFromRod(rod, player);
        FishTier caughtTier = generateRandomFishTier(rarityBoost + plugin.getLevelManager().getPassiveRarityBoost(player), rod, player);
        com.meowchan12.fishingaudition.manager.FishData fishData = plugin.getFishManager().getRandomFishByTier(caughtTier);
        
        // Add Base XP
        double baseXP = plugin.getConfig().getDouble("leveling.base_xp_per_round", 20.0);
        plugin.getLevelManager().addXP(player, baseXP);
        
        if (fishData != null) {
            caughtFish.add(CustomFish.generateFish(fishData));
            MessageUtils.sendActionBar(player, "&aCaught: " + fishData.getName() + " &7(Stored in net)");
        }
        
        resetForNextRound(false);
    }

    private void updateBossBarUI() {
        double displayProgress = Math.max(0.0, Math.min(1.0, bossBarProgress));
        bossBar.setTitle(MessageUtils.getRawMessage("minigame.bossbar_title", "&bPress number: &e&l{target} &8| &fRound: &a{round} &8| &fLevel: &c{level} &8| &7{time}s")
                .replace("{target}", String.valueOf(targetNumber + 1))
                .replace("{round}", String.valueOf(currentRound))
                .replace("{level}", String.valueOf(currentLevel))
                .replace("{time}", String.format("%.1f", keyTimer)));
        bossBar.setProgress(displayProgress);

        if (displayProgress > 0.7) {
            bossBar.setColor(BarColor.GREEN);
        } else if (displayProgress >= 0.3) {
            bossBar.setColor(BarColor.YELLOW);
        } else {
            bossBar.setColor(BarColor.RED);
        }
    }

    public void endSession(boolean success) {
        if (isEnding) return;
        isEnding = true;
        
        if (sessionTask != null) {
            sessionTask.cancel();
            sessionTask = null;
        }
        bossBar.removeAll();

        int reachedRound = currentRound - 1;
        Player player = Bukkit.getPlayer(playerUUID);
        if (player == null) return;

        // 1. Fire Session End Event
        com.meowchan12.fishingaudition.api.events.AuditionSessionEndEvent endEvent = new com.meowchan12.fishingaudition.api.events.AuditionSessionEndEvent(player, success, reachedRound);
        Bukkit.getPluginManager().callEvent(endEvent);

        if (reachedRound > 0 && !caughtFish.isEmpty()) {
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
            player.spawnParticle(Particle.SPLASH, player.getLocation().add(0, 1, 0), 50, 0.5, 0.5, 0.5, 0.1);
            
            ItemStack rod = player.getInventory().getItemInMainHand();
            boolean rodBroke = handleRodDurability(player, rod, reachedRound);
            
            // Give all caught fish
            List<ItemStack> overflow = new ArrayList<>();
            for (ItemStack fish : caughtFish) {
                java.util.Map<Integer, ItemStack> left = player.getInventory().addItem(fish);
                if (!left.isEmpty()) {
                    overflow.addAll(left.values());
                }
                
                // Unlock and XP
                if (CustomFish.isCustomFish(fish)) {
                    org.bukkit.persistence.PersistentDataContainer pdc = fish.getItemMeta().getPersistentDataContainer();
                    String fId = pdc.get(CustomFish.FISH_ID, PersistentDataType.STRING);
                    com.meowchan12.fishingaudition.manager.FishData fd = plugin.getFishManager().getFishById(fId);
                    if (fd != null) {
                        plugin.getLogbookManager().unlockFish(player, fd.getId());
                        plugin.getLevelManager().addXP(player, fd);
                        
                        com.meowchan12.fishingaudition.api.events.PlayerCatchCustomFishEvent catchEvent = new com.meowchan12.fishingaudition.api.events.PlayerCatchCustomFishEvent(player, fd);
                        Bukkit.getPluginManager().callEvent(catchEvent);
                    }
                }
            }
            
            if (!overflow.isEmpty()) {
                // Send to Virtual Chest
                player.sendMessage(MessageUtils.getMessage("minigame.inv_full_to_chest").replace("{count}", String.valueOf(overflow.size())));
                plugin.getChestManager().saveChest(player, overflow);
                
                com.meowchan12.fishingaudition.utils.SchedulerUtils.runAsync(plugin, () -> {
                    List<ItemStack> existing = plugin.getChestManager().loadChestSync(player.getUniqueId());
                    existing.addAll(overflow);
                    plugin.getChestManager().saveChestSync(player.getUniqueId(), existing);
                });
            }

            player.sendMessage(MessageUtils.getMessage("minigame.session_ended").replace("{count}", String.valueOf(caughtFish.size())));
        } else {
            player.sendMessage(MessageUtils.getMessage("errors.fishing_too_late"));
        }

        plugin.getPlayerDataManager().checkAndSetTopRound(player, reachedRound);
        plugin.getPlayerDataManager().saveData(player, true);
        plugin.getScoreboardManager().updateBoard(player);
    }
    
    private double getRarityBoostFromRod(ItemStack rod, Player player) {
        if (CustomRod.isCustomRod(rod)) {
            ItemMeta meta = rod.getItemMeta();
            PersistentDataContainer pdc = meta.getPersistentDataContainer();
            NamespacedKey boostKey = new NamespacedKey(plugin, "rarity_boost");
            double rarityBoost = pdc.getOrDefault(boostKey, PersistentDataType.DOUBLE, 0.0);
            
            // Bait Logic from PlayerDataManager
            String baitId = plugin.getPlayerDataManager().getEquippedBait(player);
            if (baitId != null) {
                com.meowchan12.fishingaudition.manager.BaitData bait = plugin.getBaitManager().getBaitById(baitId);
                if (bait != null) {
                    rarityBoost += bait.getRarityBoost();
                }
            }
            return rarityBoost;
        }
        return 0.0;
    }
    
    private boolean handleRodDurability(Player player, ItemStack rod, int totalCaught) {
        if (totalCaught <= 0) return false;
        
        // Deduct Bait charges from PlayerDataManager
        String baitId = plugin.getPlayerDataManager().getEquippedBait(player);
        if (baitId != null) {
            int charges = plugin.getPlayerDataManager().getBaitCharges(player);
            charges -= totalCaught;
            if (charges <= 0) {
                plugin.getPlayerDataManager().removeBait(player);
                player.sendMessage(MessageUtils.getMessage("errors.bait_run_out", "&cYour bait has run out!"));
            } else {
                plugin.getPlayerDataManager().setBaitCharges(player, charges);
            }
        }
        
        if (CustomRod.isCustomRod(rod)) {
            ItemMeta meta = rod.getItemMeta();
            PersistentDataContainer pdc = meta.getPersistentDataContainer();
            
            NamespacedKey curDurKey = new NamespacedKey(plugin, "current_durability");
            int currentDurability = pdc.getOrDefault(curDurKey, PersistentDataType.INTEGER, 50);
            currentDurability -= totalCaught;
            
            if (currentDurability <= 0) {
                player.getInventory().setItemInMainHand(null);
                com.meowchan12.fishingaudition.utils.SoundUtils.playSound(player, "rod_break");
                MessageUtils.sendTitle(player, MessageUtils.getRawMessage("minigame.rod_broken_title", "&c&lROD BROKEN!"), MessageUtils.getRawMessage("minigame.rod_broken_subtitle", "&7Your fishing rod has been destroyed."));
                return true;
            } else {
                pdc.set(curDurKey, PersistentDataType.INTEGER, currentDurability);
                rod.setItemMeta(meta);
                CustomRod.updateLore(rod);
            }
        }
        return false;
    }
    
    private FishTier generateRandomFishTier(double boost, ItemStack rod, Player player) {
        FileConfiguration config = plugin.getConfig();
        double legRate = config.getDouble("rng.rates.legendary", 1.0);
        double epiRate = config.getDouble("rng.rates.epic", 5.0);
        double rarRate = config.getDouble("rng.rates.rare", 15.0);
        double uncRate = config.getDouble("rng.rates.uncommon", 40.0);
    
        double chance = random.nextDouble() * 100.0;
        FishTier rolledTier = FishTier.COMMON;
        
        if (chance <= (legRate + boost)) rolledTier = FishTier.LEGENDARY;
        else if (chance <= (epiRate + boost * 2)) rolledTier = FishTier.EPIC;
        else if (chance <= (rarRate + boost * 3)) rolledTier = FishTier.RARE;
        else if (chance <= (uncRate + boost * 4)) rolledTier = FishTier.UNCOMMON;

        if (com.meowchan12.fishingaudition.material.CustomRod.isCustomRod(rod)) {
            PersistentDataContainer pdc = rod.getItemMeta().getPersistentDataContainer();
            // Check Target Rarity from PlayerDataManager Bait
            String baitId = plugin.getPlayerDataManager().getEquippedBait(player);
            if (baitId != null) {
                com.meowchan12.fishingaudition.manager.BaitData bait = plugin.getBaitManager().getBaitById(baitId);
                if (bait != null && bait.getTargetRarity() != null && !bait.getTargetRarity().isEmpty() && random.nextDouble() < 0.3) {
                    try {
                        rolledTier = FishTier.valueOf(bait.getTargetRarity().toUpperCase());
                    } catch(Exception e) {}
                }
            }
            
            // Check Catchable Rarities (Rod Gating)
            String catchableStr = pdc.get(new NamespacedKey(plugin, "catchable_rarities"), PersistentDataType.STRING);
            if (catchableStr != null && !catchableStr.isEmpty()) {
                java.util.List<String> allowed = java.util.Arrays.asList(catchableStr.split(","));
                while (rolledTier != FishTier.COMMON && !allowed.contains(rolledTier.name())) {
                    if (rolledTier == FishTier.LEGENDARY) rolledTier = FishTier.EPIC;
                    else if (rolledTier == FishTier.EPIC) rolledTier = FishTier.RARE;
                    else if (rolledTier == FishTier.RARE) rolledTier = FishTier.UNCOMMON;
                    else if (rolledTier == FishTier.UNCOMMON) rolledTier = FishTier.COMMON;
                }
            }
        }
        
        return rolledTier;
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public org.bukkit.Location getStartLocation() {
        return startLocation;
    }

    private double getExtraTime(ItemStack rod) {
        if (!com.meowchan12.fishingaudition.material.CustomRod.isCustomRod(rod)) return 0.0;
        org.bukkit.NamespacedKey key = new org.bukkit.NamespacedKey(plugin, "extra_time");
        return rod.getItemMeta().getPersistentDataContainer().getOrDefault(key, PersistentDataType.DOUBLE, 0.0);
    }

    private double getProgressBoost(ItemStack rod) {
        if (!com.meowchan12.fishingaudition.material.CustomRod.isCustomRod(rod)) return 1.0;
        org.bukkit.NamespacedKey key = new org.bukkit.NamespacedKey(plugin, "progress_boost");
        return rod.getItemMeta().getPersistentDataContainer().getOrDefault(key, PersistentDataType.DOUBLE, 1.0);
    }

    private double getMissPenaltyReduction(Player player) {
        String baitId = plugin.getPlayerDataManager().getEquippedBait(player);
        if (baitId != null) {
            com.meowchan12.fishingaudition.manager.BaitData bait = plugin.getBaitManager().getBaitById(baitId);
            if (bait != null) {
                return bait.getMissPenaltyReduction();
            }
        }
        return 0.0;
    }
}
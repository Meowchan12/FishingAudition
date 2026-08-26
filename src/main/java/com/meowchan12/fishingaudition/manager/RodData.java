package com.meowchan12.fishingaudition.manager;

import java.util.List;

public class RodData {
    private final String id;
    private final String name;
    private final int maxDurability;
    private final double rarityBoost;
    private final double price;
    private final double repairMultiplier;
    private final int requiredLevel;
    private final int customModelData;
    private final List<String> lore;
    private final List<String> catchableRarities;
    private final double extraTime;
    private final double progressBoost;

    public RodData(String id, String name, int maxDurability, double rarityBoost, double price, double repairMultiplier, int requiredLevel, int customModelData, List<String> lore, List<String> catchableRarities, double extraTime, double progressBoost) {
        this.id = id;
        this.name = name;
        this.maxDurability = maxDurability;
        this.rarityBoost = rarityBoost;
        this.price = price;
        this.repairMultiplier = repairMultiplier;
        this.requiredLevel = requiredLevel;
        this.customModelData = customModelData;
        this.lore = lore;
        this.catchableRarities = catchableRarities;
        this.extraTime = extraTime;
        this.progressBoost = progressBoost;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public int getMaxDurability() { return maxDurability; }
    public double getRarityBoost() { return rarityBoost; }
    public double getPrice() { return price; }
    public double getRepairMultiplier() { return repairMultiplier; }
    public int getRequiredLevel() { return requiredLevel; }
    public int getCustomModelData() { return customModelData; }
    public List<String> getLore() { return lore; }
    public List<String> getCatchableRarities() { return catchableRarities; }
    public double getExtraTime() { return extraTime; }
    public double getProgressBoost() { return progressBoost; }
}

package com.meowchan12.fishingaudition.manager;

public class BaitData {
    private final String id;
    private final String name;
    private final double rarityBoost;
    private final int maxCharges;
    private final int customModelData;
    private final double missPenaltyReduction;
    private final String targetRarity;
    private final double price;
    private final int slot;

    public BaitData(String id, String name, double rarityBoost, int maxCharges, int customModelData, double missPenaltyReduction, String targetRarity, double price, int slot) {
        this.id = id;
        this.name = name;
        this.rarityBoost = rarityBoost;
        this.maxCharges = maxCharges;
        this.customModelData = customModelData;
        this.missPenaltyReduction = missPenaltyReduction;
        this.targetRarity = targetRarity;
        this.price = price;
        this.slot = slot;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public double getRarityBoost() { return rarityBoost; }
    public int getMaxCharges() { return maxCharges; }
    public int getCustomModelData() { return customModelData; }
    public double getMissPenaltyReduction() { return missPenaltyReduction; }
    public String getTargetRarity() { return targetRarity; }
    public double getPrice() { return price; }
    public int getSlot() { return slot; }
}

package com.meowchan12.fishingaudition.manager;

import com.meowchan12.fishingaudition.material.FishTier;

public class FishData {
    private final String id;
    private final String name;
    private final FishTier tier;
    private final double value;
    private final double xpReward;
    private final int customModelData;

    public FishData(String id, String name, FishTier tier, double value, double xpReward, int customModelData) {
        this.id = id;
        this.name = name;
        this.tier = tier;
        this.value = value;
        this.xpReward = xpReward;
        this.customModelData = customModelData;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public FishTier getTier() { return tier; }
    public double getValue() { return value; }
    public double getXpReward() { return xpReward; }
    public int getCustomModelData() { return customModelData; }
}

package com.meowchan12.fishingaudition.material;

public enum FishTier {
    COMMON("&fCommon", 10.0),
    UNCOMMON("&aUncommon", 25.0),
    RARE("&9Rare", 100.0),
    EPIC("&5Epic", 500.0),
    LEGENDARY("&6Legendary", 1000.0);

    private final String displayName;
    private final double basePrice;

    FishTier(String displayName, double basePrice) {
        this.displayName = displayName;
        this.basePrice = basePrice;
    }

    public String getDisplayName() {
        return displayName;
    }

    public double getBasePrice() {
        return basePrice;
    }
}
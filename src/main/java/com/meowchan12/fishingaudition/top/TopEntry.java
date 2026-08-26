package com.meowchan12.fishingaudition.top;

import java.util.UUID;

public class TopEntry {
    private final UUID uuid;
    private final String name;
    private final double value; // Dùng chung cho cả Coin (double) và Round (int ép sang double)

    public TopEntry(UUID uuid, String name, double value) {
        this.uuid = uuid;
        this.name = name;
        this.value = value;
    }

    public String getName() { return name; }
    public double getValue() { return value; }
    public UUID getUuid() { return uuid; }
}
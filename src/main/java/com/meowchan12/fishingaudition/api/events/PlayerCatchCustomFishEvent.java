package com.meowchan12.fishingaudition.api.events;

import com.meowchan12.fishingaudition.manager.FishData;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerCatchCustomFishEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final FishData fishData;

    public PlayerCatchCustomFishEvent(Player player, FishData fishData) {
        this.player = player;
        this.fishData = fishData;
    }

    public Player getPlayer() {
        return player;
    }

    public FishData getFishData() {
        return fishData;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}

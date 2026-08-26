package com.meowchan12.fishingaudition.api.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class AuditionSessionEndEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final boolean isWin;
    private final int reachedRound;

    public AuditionSessionEndEvent(Player player, boolean isWin, int reachedRound) {
        this.player = player;
        this.isWin = isWin;
        this.reachedRound = reachedRound;
    }

    public Player getPlayer() {
        return player;
    }

    public boolean isWin() {
        return isWin;
    }

    public int getReachedRound() {
        return reachedRound;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}

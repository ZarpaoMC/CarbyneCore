package com.medievallords.carbyne.utils.glaedr.events;

import com.medievallords.carbyne.utils.glaedr.scoreboards.Entry;
import com.medievallords.carbyne.utils.glaedr.scoreboards.PlayerScoreboard;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@Getter
@Setter
/**
 * This event is called when an entry is abruptly cancelled by entry#cancel
 */
public class EntryCancelEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private Entry entry;
    private PlayerScoreboard scoreboard;
    private Player player;

    public EntryCancelEvent(Entry entry, PlayerScoreboard scoreboard) {
        this.entry = entry;
        this.scoreboard = scoreboard;
        this.player = scoreboard.getPlayer();
    }
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}

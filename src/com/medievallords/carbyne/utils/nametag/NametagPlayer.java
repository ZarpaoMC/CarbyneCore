package com.medievallords.carbyne.utils.nametag;

import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The NametagPlayer class
 * Used to manage NMS-Nametags that are per-player.
 * All nametags and "player nametags" in a NametagPlayer object are visible to that object's player only.
 */
public class NametagPlayer {

    private final Player player;
    private final String name;
    private List<Nametag> registeredNametags;
    private Map<NametagPlayer,Nametag> playerNametags;

    public NametagPlayer(Player player) {
        this.name = player.getName();
        this.player = player;
        this.registeredNametags = new ArrayList<>();
        this.playerNametags = new HashMap<>();
    }

    /**
     * Get the name of this player
     * @return The player's name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the Bukkit Player
     * @return The player
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Get all nametags registered for this player
     * @return A list of nametags registered for this player
     */
    public List<Nametag> getRegisteredNametags() {
        return registeredNametags;
    }

    /**
     * Get if a Nametag is registered for this player
     * @param nametag The nametag
     * @return True if registered, false if not
     */
    public boolean hasRegisteredNametag(Nametag nametag){
        return registeredNametags.contains(nametag);
    }

    /**
     * Gets a registerNametag from this player
     * @param name The name of the nametag to get; case insensitive
     * @return The Nametag, can be null if not found/registered
     */
    public Nametag getRegisteredNametag(String name) {
        for (Nametag nametag : registeredNametags) {
            if (nametag.getName().equalsIgnoreCase(name)) {
                return nametag;
            }
        }

        return null;
    }

    /**
     * Registers a nametag for this player, only if it is not already registered.
     * @param nametag The nametag to register
     */
    public void registerNametag(Nametag nametag) {
        if (!hasRegisteredNametag(nametag)) {
            registeredNametags.add(nametag);
            NametagEdit nametagEdit = new NametagEdit();
            nametagEdit.setNametag(nametag);
            nametagEdit.setParam(NametagEdit.NametagParam.CREATE);
            nametagEdit.setCanSeeFriendlyInvisibles(false);
            nametagEdit.sendToPlayer(player);
        } else {
            //Unregister and re-register, to ensure it gets registered
            unregisterNametag(nametag);
            registerNametag(nametag);
        }
    }

    /**
     * Unregisters a nametag for this player, only if it is already registered.
     * @param nametag The nametag to unregister
     */
    public void unregisterNametag(Nametag nametag) {
        if (hasRegisteredNametag(nametag) ){
            NametagEdit nametagEdit = new NametagEdit();
            nametagEdit.setNametag(nametag);
            nametagEdit.setParam(NametagEdit.NametagParam.REMOVE);
            nametagEdit.setCanSeeFriendlyInvisibles(false);
            nametagEdit.sendToPlayer(player);
            registeredNametags.remove(nametag);
        }
    }

    /**
     * Get all player nametags for this player
     * @return A list of player nametags for this player
     */
    public Map<NametagPlayer, Nametag> getPlayerNametags() {
        return playerNametags;
    }

    /**
     * Gets a nametag that a player has for this player
     * @param player The player to get the nametag for
     * @return The nametag
     */
    public Nametag getPlayerNametag(NametagPlayer player){
        return playerNametags.get(player);
    }

    /**
     * Gets if a player has a nametag for this player
     * @param player The player to check if they have a nametag
     * @return True if they have a nametag, false if not
     */
    public boolean hasPlayerNametag(NametagPlayer player){
        return playerNametags.containsKey(player);
    }

    /**
     * Removes a player's nametag for this player
     * @param who The player who's nametag we should remove
     * @param nametag The nametag to remove
     */
    public void removePlayerNametag(NametagPlayer who, Nametag nametag) {
        if (playerNametags.containsKey(who)) {
            if (!hasRegisteredNametag(nametag)) {
                registerNametag(nametag);
            }

            NametagEdit nametagEdit = new NametagEdit();
            nametagEdit.setNametag(nametag);
            nametagEdit.setParam(NametagEdit.NametagParam.REMOVE_PLAYER);
            nametagEdit.setCanSeeFriendlyInvisibles(false);
            nametagEdit.removePlayer(who.getPlayer()); // Not tested, maybe remove this if it breaks.
            nametagEdit.sendToPlayer(player);
            playerNametags.remove(who);
        }
    }

    /**
     * Set a player (who)'s Nametag to a certain nametag for this (this object) player
     * @param who The player to set the nametag for
     * @param nametag The nametag to set
     */
    public void setPlayerNametag(NametagPlayer who, Nametag nametag) {
        if (!hasRegisteredNametag(nametag)) {
            registerNametag(nametag);
        }

        if (playerNametags.containsKey(who)) {
            //Remove the nametag to prevent conflicts
            removePlayerNametag(who,nametag);
        }

        NametagEdit nametagEdit = new NametagEdit();
        nametagEdit.setNametag(nametag);
        nametagEdit.addPlayer(who.getPlayer());
        nametagEdit.setParam(NametagEdit.NametagParam.ADD_PLAYER);
        nametagEdit.setCanSeeFriendlyInvisibles(false);
        nametagEdit.sendToPlayer(player);
        playerNametags.put(who,nametag);
    }

    /**
     * Re-register and set all nametags for this player
     */
    public void refresh() {
        for (Nametag nametag : registeredNametags) {
            registerNametag(nametag);
        }

        for (NametagPlayer p : playerNametags.keySet()) {
            Nametag nametag = playerNametags.get(p);
            setPlayerNametag(p, nametag);
        }
    }

    /**
     * Remove all nametags for player and unregister all nametags
     */
    public void reset() {
        for (NametagPlayer nametagPlayer : playerNametags.keySet()) {
            Nametag nametag = playerNametags.get(nametagPlayer);
            removePlayerNametag(nametagPlayer, nametag);
        }

        for (Nametag nametag : registeredNametags) {
            NametagEdit nametagEdit = new NametagEdit();
            nametagEdit.setNametag(nametag);
            nametagEdit.setParam(NametagEdit.NametagParam.REMOVE);
            nametagEdit.setCanSeeFriendlyInvisibles(false);
            nametagEdit.sendToPlayer(player);
        }

        registeredNametags.clear();
    }

    public void update(NametagPlayer who, Nametag nametag) {
        if (hasPlayerNametag(who)) {
            NametagEdit nametagEdit = new NametagEdit();
            nametagEdit.setNametag(nametag);
            nametagEdit.addPlayer(who.getPlayer());
            nametagEdit.setParam(NametagEdit.NametagParam.UPDATE);
            nametagEdit.setCanSeeFriendlyInvisibles(false);
            nametagEdit.sendToPlayer(player);
        }
    }
}

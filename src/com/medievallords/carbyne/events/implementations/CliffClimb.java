package com.medievallords.carbyne.events.implementations;

import com.medievallords.carbyne.Carbyne;
import com.medievallords.carbyne.events.Event;
import com.medievallords.carbyne.events.EventManager;
import com.medievallords.carbyne.events.EventProperties;
import com.medievallords.carbyne.events.implementations.commands.CliffClimbCommands;
import com.medievallords.carbyne.events.implementations.listeners.CliffClimbListeners;
import com.medievallords.carbyne.gates.Gate;
import com.medievallords.carbyne.utils.DateUtil;
import com.medievallords.carbyne.utils.MessageManager;
import com.medievallords.carbyne.utils.command.BaseCommand;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.scheduler.BukkitRunnable;
import org.github.paperspigot.Title;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 * Created by Dalton on 7/5/2017.
 */
@Getter
public class CliffClimb extends Event
{

    private Location cliffClimbSpawn = new Location(Bukkit.getWorld("world"), -365, 18, -1077);
    private Location winningLocation = new Location(Bukkit.getWorld("world"), -366, 105, -1072);
    private Gate eventGate;
    @Setter
    private Player winner = null;

    @Getter
    private boolean afterCountdown = false;
    private String countDownString = "5m1s";
    private long startTime = -1;
    private int ticks = 0;

    private Map<Player, BukkitRunnable> waitingTasks = new HashMap<>();

    private CliffClimbListeners cliffClimbListeners;

    public CliffClimb(EventManager eventManager, String timeString)
    {
        super(eventManager, timeString);
        properties.add(EventProperties.PVP_DISABLED);
        properties.add(EventProperties.SPELLS_DISABLED);
        properties.add(EventProperties.PLUGIN_TELEPORT_DISABLED);
        properties.add(EventProperties.ENDERPEARL_TELEPORT_DISABLED);
        commands.add(new CliffClimbCommands(this));
        for(BaseCommand command : commands)
            Carbyne.getInstance().getCommandFramework().unregisterCommands(command);
        cliffClimbListeners = new CliffClimbListeners(this);
        Location gateBlock = new Location(Bukkit.getWorld("world"), -353, 20, -1077);
        eventGate = main.getGateManager().getGate(gateBlock);
    }

    @Override
    public void tick()
    {
        if(!active)
        {
            if(isItTimeToActivate()) start();
        }
        else
        {
            if(!afterCountdown)
            {
                long time = System.currentTimeMillis();
                if(time > startTime)
                {
                    afterCountdown = true;
                    eventGate.setKeepClosed(false);
                    eventGate.setKeepOpen(true);
                    eventGate.openGate();
                    for(Player p : participants)
                        MessageManager.sendMessage(p, "&2The walls are down, climb the mountain!");
                }
                else
                {
                    ticks++;
                    if(ticks == 10)
                    {
                        int secondsUntilStart = (int)Math.floor(((startTime - System.currentTimeMillis()) / 1000));
                        ticks = 0;
                        for(Player p : participants)
                            MessageManager.sendMessage(p, "&2Cliff Climb will begin in " + secondsUntilStart + " seconds!");
                    }
                }
            }
            else {
                if (winner != null)
                {
                    MessageManager.broadcastMessage("The winner of Cliff Climb is " + winner.getName() + "!");
                    for(Player player : Bukkit.getOnlinePlayers())
                        player.sendTitle(new Title.Builder().title(winner.getDisplayName()).subtitle(ChatColor.translateAlternateColorCodes('&', "&fis the victor of Cliff Climb!")).stay(55).build());
                    stop();
                }
            }
        }
    }

    @Override
    public void start()
    {
        super.start();
        Carbyne.getInstance().getLogger().log(Level.INFO, "The Cliff Climb event is now active!");
        Bukkit.getPluginManager().registerEvents(cliffClimbListeners, main);
        Title title = new Title.Builder().title(ChatColor.translateAlternateColorCodes('&', "&2Cliff &fClimb")).subtitle(ChatColor.translateAlternateColorCodes('&', "&fCliff Climb is starting! /cliffclimb join!")).stay(55).build();
        for(Player player : Bukkit.getOnlinePlayers()) {
            player.sendTitle(title);
            MessageManager.sendMessage(player, "The Cliff Climb event is starting. /cliffclimb join");
        }
        try { startTime = DateUtil.parseDateDiff(countDownString, true); } catch(Exception e) {}
    }

    @Override
    public void stop()
    {
        super.stop();
        HandlerList.unregisterAll(cliffClimbListeners);
        afterCountdown = false;
        winner = null;
        eventGate.setKeepClosed(true);
        eventGate.setKeepOpen(false);
        eventGate.closeGate();
    }

}

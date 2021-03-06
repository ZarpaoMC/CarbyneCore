package com.medievallords.carbyne.commands;

import com.medievallords.carbyne.utils.JSONMessage;
import com.medievallords.carbyne.utils.command.BaseCommand;
import com.medievallords.carbyne.utils.command.Command;
import com.medievallords.carbyne.utils.command.CommandArgs;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class VoteCommand extends BaseCommand {

    @Command(name = "vote")
    public void onCommand(CommandArgs commandArgs) {
        Player player = commandArgs.getPlayer();

        JSONMessage.create(ChatColor.translateAlternateColorCodes('&', "&f[&3Voting&f]: &aYou can vote on our website @ &b&l["))
                .then("Link")
                .color(ChatColor.AQUA)
                .style(ChatColor.BOLD)
                .style(ChatColor.UNDERLINE)
                .openURL("http://www.playminecraft.org/vote")
                .tooltip(ChatColor.translateAlternateColorCodes('&', "&aThis will take you to our website where you can vote."))
                .then(ChatColor.translateAlternateColorCodes('&', "&r&b&l] &aand &areceive &aup to 3 random crate keys!"))
                .send(player);
    }
}

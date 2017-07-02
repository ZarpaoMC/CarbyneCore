package com.medievallords.carbyne.gear.commands;

import com.medievallords.carbyne.Carbyne;
import com.medievallords.carbyne.utils.MessageManager;
import com.medievallords.carbyne.utils.command.BaseCommand;
import com.medievallords.carbyne.utils.command.Command;
import com.medievallords.carbyne.utils.command.CommandArgs;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;

public class GearCommands extends BaseCommand {

	@Command(name = "carbyne", aliases = {"cg"}, inGameOnly = true)
	public void onCommand(CommandArgs commandArgs) {
        CommandSender sender = commandArgs.getSender();
        String[] args = commandArgs.getArgs();

        if (args.length == 0) {
            MessageManager.sendMessage(sender, "&cUsage: /carbyne store");
        } else if (args.length == 1) {
            if (args[0].equalsIgnoreCase("store")) {
                ((Player) sender).openInventory(getGearManager().getGearGuiManager().getStoreGui());
            } else if (args[0].equalsIgnoreCase("reload") && sender.hasPermission("carbyne.administrator")) {
                getGearManager().getCarbyneGear().clear();
                getGearManager().getDefaultArmors().clear();
                getGearManager().getDefaultWeapons().clear();

                getGearManager().load(YamlConfiguration.loadConfiguration(new File(getCarbyne().getDataFolder(), "gear.yml")));
                getGearManager().loadStoreOptions(YamlConfiguration.loadConfiguration(new File(getCarbyne().getDataFolder(), "store.yml")));

                getCarbyne().getGearManager().getGearGuiManager().reloadStoreGuis();

                MessageManager.sendMessage(sender, "&aSuccessfully reloaded Carbyne configurations.");
            } else {
                MessageManager.sendMessage(sender, "&cUsage: /carbyne store");
            }
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("money") && sender.hasPermission("carbyne.administrator")) {
                try {
                    int amount = Integer.parseInt(args[1]);

                    ItemStack tokenItem = getGearManager().getTokenItem();
                    tokenItem.setAmount(amount);
                    ((Player) sender).getInventory().addItem(tokenItem);
                    MessageManager.sendMessage(sender, "&aSuccessfully received &c" + amount + " &aof &b" + ChatColor.stripColor(Carbyne.getInstance().getGearManager().getTokenItem().getItemMeta().getDisplayName()) + "&a.");
                } catch (NumberFormatException e) {
                    MessageManager.sendMessage(sender, "&cPlease enter a valid amount.");
                }
            }
        } else {
            MessageManager.sendMessage(sender, "&cUsage: /carbyne store");
        }
	}
}

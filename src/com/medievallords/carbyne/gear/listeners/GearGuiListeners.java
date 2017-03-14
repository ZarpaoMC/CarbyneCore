package com.medievallords.carbyne.gear.listeners;

import com.medievallords.carbyne.gear.GearGuiManager;
import com.medievallords.carbyne.gear.GearManager;
import com.medievallords.carbyne.gear.types.carbyne.CarbyneArmor;
import com.medievallords.carbyne.gear.types.carbyne.CarbyneWeapon;
import com.medievallords.carbyne.utils.MessageManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class GearGuiListeners implements Listener {

    private GearManager gearManager;
    private GearGuiManager gearGuiManager;

    public GearGuiListeners(GearManager gearManager) {
        this.gearManager = gearManager;
        this.gearGuiManager = gearManager.getGearGuiManager();
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        Player p = (Player) e.getWhoClicked();

        if (gearGuiManager.isCustomInventory(e.getInventory())) {
            e.setCancelled(true);
        }

        if (e.getInventory().getSize() == 9) {
            if (!(e.getRawSlot() < e.getView().getTopInventory().getSize())) {
                return;
            }
        } else {
            if (e.getRawSlot() != e.getSlot()) {
                return;
            }
        }

        if (e.getCurrentItem() == null) {
            return;
        }

        if (e.getInventory().getTitle().equalsIgnoreCase(gearGuiManager.getStoreGui().getTitle())) {
            e.setCancelled(true);

            if (e.getCurrentItem().getType() == Material.DIAMOND_SWORD) {
                p.openInventory(gearGuiManager.getWeaponsGui());
            } else if (e.getCurrentItem().getType() == Material.LEATHER_CHESTPLATE) {
                p.openInventory(gearGuiManager.getArmorGui());
            }
        } else if (e.getInventory().getTitle().equalsIgnoreCase(gearGuiManager.getWeaponsGui().getTitle())) {
            e.setCancelled(true);

            if (e.getCurrentItem().getType() == Material.BARRIER) {
                p.openInventory(gearGuiManager.getStoreGui());
            } else {
                if (e.getCurrentItem() != null && e.getCurrentItem().getType() != Material.AIR) {
                    CarbyneWeapon carbyneWeapon = gearManager.getCarbyneWeapon(e.getCurrentItem());

                    if (carbyneWeapon == null) {
                        MessageManager.sendMessage(p, "&cAn error has occurred while purchasing this item. (Error code: 1)");
                        p.closeInventory();
                        return;
                    }

                    if (p.getInventory().firstEmpty() == -1) {
                        MessageManager.sendMessage(p, "&cYour inventory is full.");
                        p.closeInventory();
                        return;
                    }

                    if (p.hasPermission("carbyne.admin") || p.isOp()) {
                        p.getInventory().addItem(carbyneWeapon.getItem(false).clone());
                        MessageManager.sendMessage(p, "&aSuccessfully purchased a &4" + carbyneWeapon.getDisplayName() + "&a.");
                        return;
                    }

                    if (canBuy(p, carbyneWeapon.getCost())) {
                        int total = 0;

                        for (ItemStack item : p.getInventory().all(gearManager.getMoneyItem()).values()) {
                            if (item.hasItemMeta() && item.getItemMeta().hasLore()) {
                                total += item.getAmount();
                            }
                        }

                        if (total < carbyneWeapon.getCost()) {
                            MessageManager.sendMessage(p, "&cYou do not have enough armor tokens to purchase this item.");
                            p.closeInventory();
                            return;
                        }

                        removeItems(p.getInventory(), gearManager.getMoneyItem(), carbyneWeapon.getCost());

                        p.getInventory().addItem(carbyneWeapon.getItem(false).clone());
                        MessageManager.sendMessage(p, "&aSuccessfully purchased a &5" + carbyneWeapon.getDisplayName() + " &afor &c" + carbyneWeapon.getCost() + " &aof &b" + gearManager.getMoneyItem() + "&a.");
                    } else {
                        MessageManager.sendMessage(p, "&cYou do not have enough armor tokens to purchase this item.");
                        p.closeInventory();
                    }
                }
            }
        } else if (e.getInventory().getTitle().equalsIgnoreCase(gearGuiManager.getArmorGui().getTitle())) {
            e.setCancelled(true);

            if (e.getCurrentItem().getType() == Material.BARRIER) {
                p.openInventory(gearGuiManager.getStoreGui());
            } else {
                if (e.getCurrentItem() != null && e.getCurrentItem().getType() != Material.AIR) {
                    CarbyneArmor carbyneArmor = gearManager.getCarbyneArmor(e.getCurrentItem());

                    if (carbyneArmor == null) {
                        MessageManager.sendMessage(p, "&cAn error has occurred while purchasing this item. (Error code: 1)");
                        p.closeInventory();
                        return;
                    }

                    if (gearGuiManager.getArmorGuiList().keySet().contains(carbyneArmor.getDisplayName())) {
                        p.openInventory(gearGuiManager.getArmorGuiList().get(carbyneArmor.getDisplayName()));
                    }
                }
            }
        } else if (ChatColor.stripColor(e.getInventory().getTitle()).contains("Purchase Armor") || ChatColor.stripColor(e.getInventory().getTitle()).equalsIgnoreCase("Purchase Armor")) {
            e.setCancelled(true);

            if (e.getCurrentItem().getType() == Material.BARRIER) {
                p.openInventory(gearGuiManager.getArmorGui());
            } else {
                if (e.getCurrentItem() != null && e.getCurrentItem().getType() != Material.AIR) {
                    CarbyneArmor carbyneArmor = gearManager.getCarbyneArmor(e.getCurrentItem());

                    if (carbyneArmor == null) {
                        MessageManager.sendMessage(p, "&cAn error has occurred while purchasing this item. (Error code: 1)");
                        p.closeInventory();
                        return;
                    }

                    if (p.getInventory().firstEmpty() == -1) {
                        MessageManager.sendMessage(p, "&cYour inventory is full.");
                        p.closeInventory();
                        return;
                    }

                    if (p.hasPermission("carbyne.admin") || p.isOp()) {
                        p.getInventory().addItem(carbyneArmor.getItem(false).clone());
                        MessageManager.sendMessage(p, "&aSuccessfully purchased a &5" + carbyneArmor.getDisplayName() + "&a.");
                        return;
                    }

                    if (canBuy(p, carbyneArmor.getCost())) {
                        int total = 0;

                        for (ItemStack item : p.getInventory().all(gearManager.getMoneyItem()).values()) {
                            if (item.hasItemMeta() && item.getItemMeta().hasLore()) {
                                total += item.getAmount();
                            }
                        }

                        if (total < carbyneArmor.getCost()) {
                            MessageManager.sendMessage(p, "&cYou do not have enough armor tokens to purchase this item.");
                            p.closeInventory();
                            return;
                        }

                        removeItems(p.getInventory(), gearManager.getMoneyItem(), carbyneArmor.getCost());

                        p.getInventory().addItem(carbyneArmor.getItem(false).clone());
                        MessageManager.sendMessage(p, "&aSuccessfully purchased a &5" + carbyneArmor.getDisplayName() + " &afor &c" + carbyneArmor.getCost() + " &aof &b" + gearManager.getMoneyItem() + "&a.");
                    } else {
                        MessageManager.sendMessage(p, "&cYou do not have enough armor tokens to purchase this item.");
                        p.closeInventory();
                    }
                }
            }
        }
    }

    public boolean canBuy(Player player, int cost) {
        return player.getInventory().containsAtLeast(gearManager.getMoney(), cost);
    }

    public void removeItems(Inventory inventory, Material type, int amount) {
        if (amount <= 0) {
            return;
        }

        int size = inventory.getSize();

        for (int slot = 0; slot < size; slot++) {
            ItemStack is = inventory.getItem(slot);

            if (is == null) {
                continue;
            }

            if (type == is.getType()) {
                int newAmount = is.getAmount() - amount;

                if (newAmount > 0) {
                    is.setAmount(newAmount);
                    break;
                } else {
                    inventory.clear(slot);
                    amount = -newAmount;
                    if (amount == 0) break;
                }
            }
        }
    }
}
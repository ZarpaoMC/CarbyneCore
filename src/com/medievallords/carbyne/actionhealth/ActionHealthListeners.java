package com.medievallords.carbyne.actionhealth;

import org.bukkit.event.Listener;

/**
 * Created by William on 7/13/2017.
 */
public class ActionHealthListeners implements Listener {

    /*
    @EventHandler(priority = EventPriority.MONITOR)
    public void onDamage(EntityDamageByEntityEvent event) {
        if (plugin.settingsManager.checkPvP && event.isCancelled()) {
            return;
        }

        if (plugin.healthUtil.isDisabled(event.getDamager().getLocation())) {
            return;
        }

        if (plugin.settingsManager.worlds.contains(event.getDamager().getWorld().getName())) {
            return;
        }

        if (plugin.settingsManager.usePerms && !event.getDamager().hasPermission("ActionHealth.Health")) {
            return;
        }


        Entity damaged = event.getEntity();
        if (damaged.getType().name().equals("ARMOR_STAND")) return;

        if (event.getDamager() instanceof Projectile) {
            Projectile projectile = (Projectile) event.getDamager();

            if (projectile.getShooter() instanceof Player) {
                Player player = (Player) projectile.getShooter();

                // Check if the setting 'Show Player' is enabled
                if (event.getEntity() instanceof Player) {
                    if (!plugin.settingsManager.showPlayers) {
                        return;
                    }
                }

                if (!plugin.settingsManager.showMobs) {
                    return;
                }

                if (player.getUniqueId() == damaged.getUniqueId()) {
                    return;
                }

                if (plugin.toggle.contains(player.getUniqueId())) {
                    if (plugin.settingsManager.toggleMessage != null && !plugin.settingsManager.toggleMessage.equals("")) {
                        plugin.healthUtil.sendActionBar(player, plugin.settingsManager.toggleMessage.replace("{name}", player.getName()));
                    }
                    return;
                }

                // Send health
                if (damaged instanceof LivingEntity) {
                    LivingEntity livingEntity = (LivingEntity) damaged;
                    plugin.healthUtil.sendHealth(player, livingEntity, livingEntity.getHealth() - event.getFinalDamage());
                }
            }
        }

        if (event.getDamager() instanceof Player) {
            Player player = (Player) event.getDamager();

            if (player.getUniqueId() == damaged.getUniqueId()) {
                return;
            }

            // Check if the setting 'Show Player' is enabled
            if (event.getEntity() instanceof Player) {
                if (!plugin.settingsManager.showPlayers) {
                    return;
                }

                if (player.hasMetadata("NPC")) {
                    return;
                }
            }

            if (!plugin.settingsManager.showMobs) {
                return;
            }

            if (plugin.toggle.contains(player.getUniqueId())) {
                if (plugin.settingsManager.toggleMessage != null && !plugin.settingsManager.toggleMessage.equals("")) {
                    plugin.healthUtil.sendActionBar(player, plugin.settingsManager.toggleMessage.replace("{name}", player.getName()));
                }
                return;
            }

            // Send health
            if (damaged instanceof LivingEntity) {
                LivingEntity livingEntity = (LivingEntity) damaged;
                plugin.healthUtil.sendHealth(player, livingEntity, livingEntity.getHealth() - event.getFinalDamage());
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (plugin.settingsManager.rememberToggle) {
            FileHandler fileHandler = new FileHandler("plugins/ActionHealth/players/" + player.getUniqueId() + ".yml");

            if (fileHandler.getBoolean("toggle")) {
                plugin.toggle.add(player.getUniqueId());
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onLeave(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        if (plugin.toggle.contains(player.getUniqueId())) {
            plugin.toggle.remove(player.getUniqueId());
        }
    }*/
}

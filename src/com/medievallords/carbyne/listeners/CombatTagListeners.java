package com.medievallords.carbyne.listeners;

import com.bizarrealex.aether.scoreboard.Board;
import com.bizarrealex.aether.scoreboard.cooldown.BoardCooldown;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.medievallords.carbyne.Carbyne;
import com.medievallords.carbyne.customevents.CombatTaggedEvent;
import com.medievallords.carbyne.profiles.Profile;
import com.medievallords.carbyne.profiles.ProfileManager;
import com.medievallords.carbyne.utils.ItemBuilder;
import com.medievallords.carbyne.utils.MessageManager;
import com.medievallords.carbyne.utils.PlayerUtility;
import com.medievallords.carbyne.utils.ReflectionUtils;
import com.nisovin.magicspells.events.SpellTargetEvent;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Coord;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.TownyUniverse;
import com.palmergames.bukkit.towny.object.TownyWorld;
import com.palmergames.bukkit.towny.utils.CombatUtil;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

/**
 * Created by Calvin on 3/22/2017
 * for the Carbyne project.
 */
public class CombatTagListeners implements Listener {

    private Carbyne main = Carbyne.getInstance();
    private ProfileManager profileManager = main.getProfileManager();
    private static ConcurrentHashMap<UUID, Long> tagged = new ConcurrentHashMap<>();
    private static final List<BlockFace> ALL_DIRECTIONS = ImmutableList.of(BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST);
    private final Map<UUID, Set<Location>> previousUpdates = new HashMap<>();
    private final ExecutorService executorService = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setNameFormat("ForceField Thread").build());

    private List<UUID> removeInventories = new ArrayList<>();

    public CombatTagListeners() {
        ConfigurationSection section = main.getWeteFileConfiguration().getConfigurationSection("Removes");
        if (section == null) {
            section = main.getWeteFileConfiguration().createSection("Removes");
        }

        if (section.getStringList("remove") == null) {
            return;
        }

        for (String s : section.getStringList("remove")) {
            removeInventories.add(UUID.fromString(s));
        }

    }

    //=================[ Tag listeners ]=================

    @EventHandler
    public void onEnderchestOpen(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (event.hasBlock() && event.getClickedBlock().getType() == Material.ENDER_CHEST && isInCombat(player.getUniqueId())) {
                event.setCancelled(true);
                MessageManager.sendMessage(player, "&7You cannot open your enderchest during combat.");
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();

        if (event.getEntity() instanceof Villager && (!(event.getDamager() instanceof Player) || event.getDamager() instanceof Projectile)) {
            if (event.getDamager() instanceof Projectile && ((Projectile) event.getDamager()).getShooter() != null &&  ((Projectile) event.getDamager()).getShooter() instanceof Player) {
                return;
            }

            Villager villager = (Villager) event.getEntity();

            if (villager.hasMetadata("logger")) {
                UUID id = UUID.fromString(villager.getMetadata("logger").get(0).value().toString());

                if (loggers.containsKey(id) && loggers.get(id) == villager) {
                    event.setCancelled(true);
                    return;
                }
            }
        }

        if (damager instanceof Player && event.getEntity() instanceof Player) {
            Player playerDamager = (Player) damager;
            Player damaged = (Player) event.getEntity();

            Profile damagerProfile = main.getProfileManager().getProfile(playerDamager.getUniqueId());
            Profile damagedProfile = main.getProfileManager().getProfile(damaged.getUniqueId());

            if (damagedProfile != null && damagerProfile != null) {
                if (damagerProfile.getRemainingPvPTime() > 1) {
                    event.setCancelled(true);
                    MessageManager.sendMessage(playerDamager, "&cYou cannot attack while your PvPTimer is active. Use &a/pvptimer");
                    return;
                } else if (damagedProfile.getRemainingPvPTime() > 1) {
                    event.setCancelled(true);
                    MessageManager.sendMessage(playerDamager, "&cYou cannot attack someone while their PvPTimer is active.");
                    return;
                }
            }
        }

        if (damager instanceof Projectile && event.getEntity() instanceof Player) {
            if (((Projectile) damager).getShooter() != null) {
                damager = (Entity) ((Projectile) damager).getShooter();
                Player damaged = (Player) event.getEntity();

                if (damager instanceof Player) {
                    Profile damagerProfile = main.getProfileManager().getProfile(damager.getUniqueId());
                    Profile damagedProfile = main.getProfileManager().getProfile(damaged.getUniqueId());

                    if (damagedProfile != null && damagerProfile != null) {
                        if (damagedProfile.getRemainingPvPTime() > 1) {
                            event.setCancelled(true);
                            MessageManager.sendMessage((Player) damager, "&cYou cannot attack someone while their PvPTimer is active.");
                            return;
                        }

                        if (damagerProfile.getRemainingPvPTime() > 1) {
                            event.setCancelled(true);
                            MessageManager.sendMessage((Player) damager, "&cYou cannot attack someone while your PvPTimer is active. Use &a/pvptimer");
                            return;
                        }
                    }
                }
            }
        }


        if (damager instanceof Projectile) {
            if (((Projectile) damager).getShooter() != null) {
                damager = (Entity) ((Projectile) damager).getShooter();
            }
        }

        if (event.getEntity() instanceof Player) {
            Player tagged = (Player) event.getEntity();

            if (damager instanceof Player) {
                Player tagger = (Player) damager;

                if (tagger != tagged) {
                    if (isPvpEnabledAt(tagged.getLocation()) && isPvpEnabledAt(tagger.getLocation())) {
                        setCombatTag(tagger);
                        setCombatTag(tagged);

                        Bukkit.getServer().getPluginManager().callEvent(new CombatTaggedEvent(tagged));
                    }
                }
            }
        }

    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        removeCombatTag(event.getEntity().getUniqueId());
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        removeCombatTag(event.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();

        if (isInCombat(player.getUniqueId())) {
            String command = event.getMessage();

            for (String disabledCommand : main.getConfig().getStringList("combat-tag.blocked-cmds")) {
                if (command.indexOf(" ") == disabledCommand.length()) {
                    if (command.substring(0, command.indexOf(" ")).equalsIgnoreCase(disabledCommand)) {
                        MessageManager.sendMessage(player, "&7This command is disabled while in combat.");
                        event.setCancelled(true);
                        return;
                    }
                } else if (disabledCommand.indexOf(" ") > 0) {
                    if (command.toLowerCase().startsWith(disabledCommand.toLowerCase())) {
                        MessageManager.sendMessage(player, "&7This command is disabled while in combat.");
                        event.setCancelled(true);
                        return;
                    }
                } else if (!command.contains(" ") && command.equalsIgnoreCase(disabledCommand)) {
                    MessageManager.sendMessage(player, "&7This command is disabled while in combat.");
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

    @EventHandler
    public void shutdown(PluginDisableEvent event) {
        if (event.getPlugin() != main)
            return;

        // Shutdown executor service and clean up threads
        executorService.shutdown();
        try {
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException ignore) {}

        // Go through all previous updates and revert spoofed blocks
        for (UUID uuid : previousUpdates.keySet()) {
            Player player = Bukkit.getPlayer(uuid);

            if (player == null)
                continue;

            for (Location location : previousUpdates.get(uuid)) {
                Block block = location.getBlock();
                player.sendBlockChange(location, block.getType(), block.getData());
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void updateViewedBlocks(PlayerMoveEvent event) {
        // Do nothing if player hasn't moved over a whole block
        Location t = event.getTo();
        Location f = event.getFrom();

        if (t.getBlockX() == f.getBlockX() && t.getBlockY() == f.getBlockY() && t.getBlockZ() == f.getBlockZ()) {
            return;
        }

        final Player player = event.getPlayer();

        // Asynchronously send block changes around player
        executorService.submit(() -> {
            // Stop processing if player has logged off
            UUID uuid = player.getUniqueId();

            if (Bukkit.getPlayer(uuid) == null) {
                return;
            }

            // Update the players force field perspective and find all blocks to stop spoofing
            Set<Location> changedBlocks = getChangedBlocks(player);
            Material forceFieldMaterial = Material.STAINED_GLASS;
            byte forceFieldMaterialDamage = 14;

            Set<Location> removeBlocks;
            if (previousUpdates.containsKey(uuid)) {
                removeBlocks = previousUpdates.get(uuid);
            } else {
                removeBlocks = new HashSet<>();
            }

            for (Location location : changedBlocks) {
                player.sendBlockChange(location, forceFieldMaterial, forceFieldMaterialDamage);
                removeBlocks.remove(location);
            }

            // Remove no longer used spoofed blocks
            for (Location location : removeBlocks) {
                Block block = location.getBlock();
                player.sendBlockChange(location, block.getType(), block.getData());
            }

            previousUpdates.put(uuid, changedBlocks);
        });
    }

    @EventHandler(ignoreCancelled = true)
    public void denySafeZoneEntry(PlayerTeleportEvent event) {
        if (isInCombat(event.getPlayer().getUniqueId()) && event.getCause() == PlayerTeleportEvent.TeleportCause.ENDER_PEARL && !isPvpEnabledAt(event.getTo()) && isPvpEnabledAt(event.getFrom())) {
            event.setCancelled(true);
            MessageManager.sendMessage(event.getPlayer(), "&cYou cannot use ender pearls while in combat. You have been refunded.");
            event.getPlayer().getInventory().addItem(new ItemBuilder(Material.ENDER_PEARL).amount(1).build());
        }
    }

    //=================[ Villager listeners ]=================

    public static HashMap<UUID, Villager> loggers = new HashMap<>();
    private Multimap<UUID, ItemStack[]> inventories = ArrayListMultimap.create();
    private HashMap<Villager, BukkitRunnable> tasks = new HashMap<>();

    private static HashMap<UUID, BukkitRunnable> disabled = new HashMap<>();
    private static HashMap<UUID, BukkitRunnable> counters = new HashMap<>();
    private static HashMap<UUID, Integer> count = new HashMap<>();

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        if (player.isDead() || player.getHealth() <= 0) {
            removeCombatTag(player.getUniqueId());
            return;
        }

        Profile profile = main.getProfileManager().getProfile(player.getUniqueId());

        if (profile == null) {
            removeCombatTag(player.getUniqueId());
            return;
        }

        if (player.getGameMode() == GameMode.SURVIVAL) {
            if (!profile.isSafelyLogged()) {
                if (isInCombat(player.getUniqueId()) || (TownyUniverse.getTownBlock(player.getLocation()) != null && TownyUniverse.getTownBlock(player.getLocation()).getPermissions().pvp)) {
                    Location loggedLocation = player.getWorld().getHighestBlockAt(player.getLocation()).getLocation();
                    Villager villager = (Villager) loggedLocation.getWorld().spawnCreature(loggedLocation, EntityType.VILLAGER);
                    villager.setAdult();
                    villager.setMaxHealth(player.getMaxHealth());
                    villager.setHealth(player.getHealth());
                    villager.setCustomName(ChatColor.translateAlternateColorCodes('&', player.getDisplayName()));
                    villager.setCustomNameVisible(true);
                    villager.setFireTicks(player.getFireTicks());
                    player.getActivePotionEffects().forEach(villager::addPotionEffect);
                    villager.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, Integer.MAX_VALUE, 7, false, false));
                    villager.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, 128, false, false));
                    villager.setCanPickupItems(false);
                    villager.setMetadata("logger", new FixedMetadataValue(main, player.getUniqueId().toString()));

                    ReflectionUtils.setCollidable(villager, false);

                    loggers.put(player.getUniqueId(), villager);
                    inventories.get(player.getUniqueId()).add(player.getInventory().getContents());
                    inventories.get(player.getUniqueId()).add(player.getInventory().getArmorContents());

                    if (tasks.containsKey(villager)) {
                        tasks.get(villager).cancel();
                    }

                    tasks.put(villager, new BukkitRunnable() {
                        public void run() {
                            loggers.remove(UUID.fromString(villager.getMetadata("logger").get(0).value().toString()));
                            villager.removeMetadata("logger", main);
                            tasks.remove(villager);
                            villager.remove();
                        }
                    });

                    tasks.get(villager).runTaskLater(main, 10 * 20L);
                }
            } else {
                if (player.getWorld().getName().equalsIgnoreCase("world")) {
                    Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "spawn " + player.getName());
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Profile profile = main.getProfileManager().getProfile(player.getUniqueId());

        if (profile == null) {
            if (loggers.get(player.getUniqueId()) != null && loggers.get(player.getUniqueId()).getHealth() > 0.0) {
                player.teleport(loggers.get(player.getUniqueId()).getLocation());
                player.setHealth(loggers.get(player.getUniqueId()).getHealth());
                player.setFireTicks(loggers.get(player.getUniqueId()).getFireTicks());
                tasks.remove(loggers.get(player.getUniqueId()));
                loggers.get(player.getUniqueId()).remove();
                loggers.remove(player.getUniqueId());
                inventories.removeAll(player.getUniqueId());
            }

            return;
        }

        if (removeInventories.contains(player.getUniqueId())) {
            ConfigurationSection section = main.getWeteFileConfiguration().getConfigurationSection("Removes");
            if (section == null) {
                main.getWeteFileConfiguration().createSection("Removes");
            }

            removeInventories.remove(player.getUniqueId());

            List<String> list = new ArrayList<>();
            removeInventories.forEach(uuid -> list.add(uuid.toString()));
            section.set("remove", list);

            try {
                main.getWeteFileConfiguration().save(main.getWeteFile());
                main.setWeteFileConfiguration(YamlConfiguration.loadConfiguration(main.getWeteFile()));
            } catch (IOException e) {
                Bukkit.getLogger().log(Level.WARNING, "Could not save and load wete.yml");
            }

            player.getInventory().clear();
        }

        if (profile.isSafelyLogged()) {
            if (loggers.containsKey(player.getUniqueId()) && loggers.get(player.getUniqueId()) != null && loggers.get(player.getUniqueId()).getHealth() > 0.0) {
                tasks.remove(loggers.get(player.getUniqueId()));
                loggers.get(player.getUniqueId()).remove();
                loggers.remove(player.getUniqueId());
                inventories.removeAll(player.getUniqueId());
            } else {
                if (player.getWorld().getName().equalsIgnoreCase("world")) {
                    Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "spawn " + player.getName());
                }
            }

            profile.setSafelyLogged(false);
        } else {
            if (loggers.containsKey(player.getUniqueId())) {
                if (loggers.get(player.getUniqueId()) != null && loggers.get(player.getUniqueId()).getHealth() > 0.0) {
                    player.teleport(loggers.get(player.getUniqueId()).getLocation());
                    player.setHealth(loggers.get(player.getUniqueId()).getHealth());
                    player.setFireTicks(loggers.get(player.getUniqueId()).getFireTicks());

                    tasks.remove(loggers.get(player.getUniqueId()));
                    loggers.get(player.getUniqueId()).remove();
                    loggers.remove(player.getUniqueId());
                    inventories.removeAll(player.getUniqueId());
                } else {
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            player.getInventory().clear();
                            player.getInventory().setHelmet(null);
                            player.getInventory().setChestplate(null);
                            player.getInventory().setLeggings(null);
                            player.getInventory().setBoots(null);
                            player.setHealth(0.0);
                        }
                    }.runTaskLater(main, 5L);
                }
            } else {
                if (player.getWorld().getName().equalsIgnoreCase("world")) {
                    Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "spawn " + player.getName());
                }
            }
        }
        //Idk if this is right or if im missing something but i think this is right lol
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity() instanceof Villager) {
            Villager villager = (Villager) event.getEntity();

            if (villager.hasMetadata("logger")) {
                UUID id = UUID.fromString(villager.getMetadata("logger").get(0).value().toString());

                if (loggers.containsKey(id) && loggers.get(id) == villager) {
                    for (ItemStack[] itemStacks : inventories.get(id)) {
                        for (ItemStack items : itemStacks) {
                            if (items != null && items.getType() != Material.AIR) {
                                villager.getLocation().getWorld().dropItemNaturally(villager.getLocation(), items);
                            }
                        }
                    }

                    ConfigurationSection section = main.getWeteFileConfiguration().getConfigurationSection("Removes");
                    if (section == null) {
                        main.getWeteFileConfiguration().createSection("Removes");
                    }

                    removeInventories.add(id);

                    List<String> list = new ArrayList<>();
                    removeInventories.forEach(uuid -> list.add(uuid.toString()));
                    section.set("remove", list);

                    try {
                        main.getWeteFileConfiguration().save(main.getWeteFile());
                        main.setWeteFileConfiguration(YamlConfiguration.loadConfiguration(main.getWeteFile()));
                    } catch (IOException e) {
                        Bukkit.getLogger().log(Level.WARNING, "Could not save and load wete.yml");
                    }
                }

                tasks.remove(loggers.get(id));
            }
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (disabled.containsKey(event.getPlayer().getUniqueId())) {
            if (event.getFrom().getBlockX() != event.getTo().getBlockX() || event.getFrom().getBlockY() != event.getTo().getBlockY() || event.getFrom().getBlockZ() != event.getTo().getBlockZ()) {
                disabled.get(event.getPlayer().getUniqueId()).cancel();
                disabled.remove(event.getPlayer().getUniqueId());
                counters.get(event.getPlayer().getUniqueId()).cancel();
                counters.remove(event.getPlayer().getUniqueId());
                count.remove(event.getPlayer().getUniqueId());
                MessageManager.sendMessage(event.getPlayer(), "&cYou moved! Logout has been cancelled!");

                Board board = Board.getByPlayer(event.getPlayer());

                if (board.getCooldown("logout") != null) {
                    board.getCooldown("logout").cancel();
                }
            }
        }
    }

    @EventHandler
    public void onClick(PlayerInteractEntityEvent event) {
        if (event.getRightClicked() instanceof Villager) {
            Villager villager = (Villager) event.getRightClicked();

            if (villager.hasMetadata("logger")) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onUnload(ChunkUnloadEvent event) {
        for (Entity entity : event.getChunk().getEntities()) {
            if (entity instanceof Villager) {
                Villager villager = (Villager) entity;

                if (villager.hasMetadata("logger")) {
                    villager.remove();
                }
            }
        }
    }

    //===============[ Methods ]================

    private Set<Location> getChangedBlocks(Player player) {
        Set<Location> locations = new HashSet<>();

        // Do nothing if player is not tagged
        if (!isInCombat(player.getUniqueId()))
            return locations;

        // Find the radius around the player
        int r = 10;
        Location l = player.getLocation();
        Location loc1 = l.clone().add(r, 0, r);
        Location loc2 = l.clone().subtract(r, 0, r);
        int topBlockX = loc1.getBlockX() < loc2.getBlockX() ? loc2.getBlockX() : loc1.getBlockX();
        int bottomBlockX = loc1.getBlockX() > loc2.getBlockX() ? loc2.getBlockX() : loc1.getBlockX();
        int topBlockZ = loc1.getBlockZ() < loc2.getBlockZ() ? loc2.getBlockZ() : loc1.getBlockZ();
        int bottomBlockZ = loc1.getBlockZ() > loc2.getBlockZ() ? loc2.getBlockZ() : loc1.getBlockZ();

        // Iterate through all blocks surrounding the player
        for (int x = bottomBlockX; x <= topBlockX; x++) {
            for (int z = bottomBlockZ; z <= topBlockZ; z++) {
                // Location corresponding to current loop
                Location location = new Location(l.getWorld(), (double) x, l.getY(), (double) z);

                // PvP is enabled here, no need to do anything else
                if (isPvpEnabledAt(location)) continue;

                // Check if PvP is enabled in a location surrounding this
                if (!isPvpSurrounding(location)) continue;

                for (int i = -r; i < r; i++) {
                    Location loc = new Location(location.getWorld(), location.getX(), location.getY(), location.getZ());

                    loc.setY(loc.getY() + i);

                    // Do nothing if the block at the location is not air
                    if (!loc.getBlock().getType().equals(Material.AIR)) continue;

                    // Add this location to locations
                    locations.add(new Location(loc.getWorld(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
                }
            }
        }

        return locations;
    }

    private boolean isPvpSurrounding(Location loc) {
        for (BlockFace direction : ALL_DIRECTIONS) {
            if (isPvpEnabledAt(loc.getBlock().getRelative(direction).getLocation())) {
                return true;
            }
        }

        return false;
    }

    public static boolean isPvpEnabledAt(Location loc) {
        TownyWorld world = null;
        TownBlock townBlock = null;
        try {
            world = TownyUniverse.getDataSource().getWorld(loc.getWorld().getName());
            townBlock = world.getTownBlock(Coord.parseCoord(loc));
        } catch (NotRegisteredException ignore) {}

        return !CombatUtil.preventPvP(world, townBlock);
    }

    public boolean setCombatTag(Player p) {
        tagged.put(p.getUniqueId(), PvPTimeout(main.getConfig().getInt("combat-tag.tag-length")));

        Board board = Board.getByPlayer(p);
        if (board != null) {
            BoardCooldown boardCooldown = board.getCooldown("combattag");

            if (boardCooldown == null) {
                new BoardCooldown(board, "combattag", main.getConfig().getInt("combat-tag.tag-length"));
            } else {
                boardCooldown.cancel();
                new BoardCooldown(board, "combattag", main.getConfig().getInt("combat-tag.tag-length"));
            }
        }

        return false;
    }

    public long removeCombatTag(UUID id) {
        if (isInCombat(id)) {
            if (Bukkit.getPlayer(id) != null) {
                Player player = Bukkit.getPlayer(id);
                Board board = Board.getByPlayer(player);

                if (board != null) {
                    BoardCooldown boardCooldown = board.getCooldown("combattag");

                    if (boardCooldown != null) {
                        board.getCooldown("combattag").cancel();
                    }
                }
            }

            return tagged.remove(id);
        }

        return -1;
    }

    public long PvPTimeout(int seconds) {
        return System.currentTimeMillis() + (seconds * 1000);
    }

    public static boolean isInCombat(UUID id) {
        if (getRemainingTime(id) <= 0) {
            if (tagged.containsKey(id)) {
                tagged.remove(id);
            }

            return false;
        } else {
            return true;
        }
    }

    public static long getRemainingTime(UUID id) {
        Long tag = tagged.get(id);

        if (tag == null) {
            return -1;
        }

        return tag - System.currentTimeMillis();
    }

    public static String formatTime(long difference) {
        Calendar call = Calendar.getInstance();
        call.setTimeInMillis(difference);
        return new SimpleDateFormat("ss.S").format(difference);
    }

    public ImmutableSet<UUID> listTagged() {
        return ImmutableSet.copyOf(tagged.keySet());
    }

    public static class ForceFieldTask extends BukkitRunnable {

        private final Carbyne plugin;

        private final Map<UUID, Location> validLocations = new HashMap<>();

        private ForceFieldTask(Carbyne plugin) {
            this.plugin = plugin;
        }

        @Override
        public void run() {
            for (Player player : PlayerUtility.getOnlinePlayers()) {
                UUID playerId = player.getUniqueId();

                // Do nothing if player isn't even tagged.
                if (!CombatTagListeners.isInCombat(playerId))
                    continue;

                Location loc = player.getLocation();
                if (CombatTagListeners.isPvpEnabledAt(loc)) {
                    // Track the last PVP-enabled location that the player was in.
                    validLocations.put(playerId, loc);
                } else if (validLocations.containsKey(playerId)) {
                    // Teleport the player to the last valid PVP-enabled location.
                    player.teleport(validLocations.get(playerId));
                }
            }
        }

        public static void run(Carbyne plugin) {
            new ForceFieldTask(plugin).runTaskTimer(plugin, 1, 1);
        }
    }

    public static HashMap<UUID, BukkitRunnable> getDisabled() {
        return disabled;
    }

    public static HashMap<UUID, BukkitRunnable> getCounters() {
        return counters;
    }

    public static HashMap<UUID, Integer> getCount() {
        return count;
    }

    @EventHandler
    public void onSpell(SpellTargetEvent event) {
        if (event.getTarget() instanceof Player) {
            Profile target = main.getProfileManager().getProfile(event.getTarget().getUniqueId());
            Profile caster = main.getProfileManager().getProfile(event.getCaster().getUniqueId());

            if (target != null && caster != null) {
                if (caster.getRemainingPvPTime() > 1) {
                    event.setCancelled(true);
                    MessageManager.sendMessage(event.getCaster(), "&cYou cannot cast spells on someone whilst you have a pvp-protection timer");
                } else if (target.getRemainingPvPTime() > 1) {
                    event.setCancelled(true);
                    MessageManager.sendMessage(event.getCaster(), "&cYou cannot cast spells on someone whilst they have a pvp-protection timer");
                }
            }
        }
    }
}
package com.medievallords.carbyne.staff;

import com.medievallords.carbyne.Carbyne;
import com.medievallords.carbyne.utils.*;
import com.mongodb.client.MongoCollection;
import lombok.Getter;
import lombok.Setter;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.inventivetalent.mapmanager.controller.MapController;
import org.inventivetalent.mapmanager.wrapper.MapWrapper;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

/**
 * Created by Calvin on 6/11/2017
 * for the Carbyne project.
 */
@Getter
public class StaffManager {

    private final ItemStack randomTeleportTool, toggleVanishTool, freezeTool, inspectInventoryTool, thruTool, air, wand;
    private Carbyne main = Carbyne.getInstance();
    private HashSet<UUID> vanish = new HashSet<>(), frozen = new HashSet<>(), frozenStaff = new HashSet<>();
    private HashSet<ServerPicture> serverPictures = new HashSet<>();
    private List<UUID> staffModePlayers = new ArrayList<>(), staffChatPlayers = new ArrayList<>();
    private Set<UUID> staff = new HashSet<>();
    @Setter
    private boolean chatMuted = false;
    @Setter
    private int slowChatTime = 0;
    @Setter
    private int serverSlots = 60;
    private Map<String, Boolean> falsePerms = new HashMap<>(), truePerms = new HashMap<>();

    private File staffWhitelistCommandsFile;
    private FileConfiguration staffWhitelistCommandConfiguration;

    private List<String> staffmodeCommandWhitelist;

    public StaffManager() {
        staffWhitelistCommandsFile = new File(main.getDataFolder(), "staffmodewhitelist.yml");
        staffWhitelistCommandConfiguration = YamlConfiguration.loadConfiguration(staffWhitelistCommandsFile);

        staffmodeCommandWhitelist = staffWhitelistCommandConfiguration.getStringList("Commands");

        falsePerms.put(new String("mv.bypass.gamemode.*"), false);
        falsePerms.put(new String("CreativeControl.*"), false);
        truePerms.put(new String("mv.bypass.gamemode.*"), true);
        truePerms.put(new String("CreativeControl.*"), true);

        randomTeleportTool = new ItemBuilder(Material.WATCH).name("&5Random Teleport").addLore("&fRight click to teleport to a random player").addLore("&fLeft click to teleport to a random player underground").build();
        toggleVanishTool = new ItemBuilder(Material.INK_SACK).durability(10).name("&5Vanish").addLore("&fToggle vanish").build();
        freezeTool = new ItemBuilder(Material.ICE).name("&1Freeze").addLore("&fFreeze a player").build();
        inspectInventoryTool = new ItemBuilder(Material.BOOK).name("&2Inspect Inventory").addLore("&fView the contents of a player\'s inventory").build();
        thruTool = new ItemBuilder(Material.COMPASS).name("&4Thru Tool").addLore("&fWarp through walls and doors").build();
        air = new ItemBuilder(Material.AIR).build();
        wand = new ItemBuilder(Material.WOOD_AXE).name("&6World Edit").build();

        new BukkitRunnable() {
            @Override
            public void run() {
                int amount = PlayerUtility.getOnlinePlayers().size();
                logToFile(new SimpleDateFormat("MM/dd/yyyy hh:mm a").format(new Date()) + " CST - Online: " + amount);
            }
        }.runTaskTimerAsynchronously(Carbyne.getInstance(), 0, 20 * 60 * 30);

        new BukkitRunnable() {
            @Override
            public void run() {
                for (UUID id : frozen) {
                    MessageManager.sendMessage(id, "&f\u2588\u2588\u2588\u2588&c\u2588&f\u2588\u2588\u2588\u2588");
                    MessageManager.sendMessage(id, "&f\u2588\u2588\u2588&c\u2588&6\u2588&c\u2588&f\u2588\u2588\u2588");
                    MessageManager.sendMessage(id, "&f\u2588\u2588&c\u2588&6\u2588&0\u2588&6\u2588&c\u2588&f\u2588\u2588");
                    MessageManager.sendMessage(id, "&f\u2588\u2588&c\u2588&6\u2588&0\u2588&6\u2588&c\u2588&f\u2588\u2588");
                    MessageManager.sendMessage(id, "&f\u2588&c\u2588&6\u2588\u2588&0\u2588&6\u2588\u2588&c\u2588&f\u2588");
                    MessageManager.sendMessage(id, "&f\u2588&c\u2588&6\u2588\u2588\u2588\u2588\u2588&c\u2588&f\u2588");
                    MessageManager.sendMessage(id, "&c\u2588&6\u2588\u2588\u2588&0\u2588&6\u2588\u2588\u2588&c\u2588");
                    MessageManager.sendMessage(id, "&c\u2588\u2588\u2588\u2588\u2588\u2588\u2588\u2588\u2588");
                    MessageManager.sendMessage(id, "&4&l[&c&l!&4&l] &6You have been frozen! Do not log out or you will be banned!");
                    MessageManager.sendMessage(id, "&4&l[&c&l!&4&l] &6You have &c5 minutes &6to join our Discord.");
                    MessageManager.sendMessage(id, "&4&l[&c&l!&4&l] &6Join the Discord using: /discord");
                }
            }
        }.runTaskTimerAsynchronously(Carbyne.getInstance(), 0L, 3 * 25L);

//        new BukkitRunnable() {
//            @Override
//            public void run() {
//                loadPictureMap();
//            }
//        }.runTaskLater(main, 100);
    }

//    public void reloadImages(CommandSender sender) {
//        saveServeImagesFile();
//        for (ServerPicture picture : serverPictures) {
//            picture.cancel();
//        }
//        loadPictureMap();
//        MessageManager.sendMessage(sender, "&cImages have been reloaded.");
//    }
//
//    public void loadPictureMap() {
//        ConfigurationSection cs = main.getServerImagesFileConfiguration().getConfigurationSection("Images");
//        if (cs == null) {
//            cs = main.getServerImagesFileConfiguration().createSection("Images");
//            saveServeImagesFile();
//            return;
//        }
//
//        serverPictures.clear();
//
//        for (String key : cs.getKeys(false)) {
//            String url = cs.getString(key + ".URL");
//            ConfigurationSection frameSection = cs.getConfigurationSection(key + ".Frames");
//            int x = cs.getInt(key + ".X");
//            int y = cs.getInt(key + ".Y");
//            ItemFrame[][] frames = new ItemFrame[x][y];
//            int i = 0, l = 0;
//            for (String secOne : frameSection.getKeys(false)) {
//                Location ser = deserializeLocation(frameSection.getString(secOne + ".main"));
//                for (Entity entity : ser.getWorld().getNearbyEntities(ser, .1, .1, .1)) {
//                    if (entity instanceof ItemFrame) {
//                        frames[i][l++] = (ItemFrame) entity;
//                        break;
//                    }
//                }
//                for (String loca : frameSection.getStringList(secOne + ".list")) {
//                    Location location = deserializeLocation(loca);
//                    for (Entity entity : location.getWorld().getNearbyEntities(location, .1, .1, .1)) {
//                        if (entity instanceof ItemFrame) {
//                            frames[i][l++] = (ItemFrame) entity;
//                            break;
//                        }
//                    }
//                }
//                i++;
//                l = 0;
//            }
//
//            ServerPicture picture = new ServerPicture(key, url, frames, x, y);
//            serverPictures.add(picture);
//        }
//    }
//
//    private void saveServeImagesFile() {
//        main.setServerImagesFile(new File(main.getDataFolder(), "serverimages.yml"));
//        main.setServerImagesFileConfiguration(YamlConfiguration.loadConfiguration(main.getServerImagesFile()));
//    }

    /**
     * PRECONDITION: Player has permission carbyne.staff.staffmode
     *
     * @param player Player to toggle staff mode for
     */
    public void toggleStaffMode(Player player) {
        UUID pUUID = player.getUniqueId();
        if (staffModePlayers.contains(pUUID)) {
            toggleGamemode(player, false);
            staffModePlayers.remove(pUUID);
            player.getInventory().clear();
            showPlayer(player);
            //Carbyne.getInstance().getServer().dispatchCommand(Carbyne.getInstance().getServer().getConsoleSender(), "pex user " + player.getName() + " remove mv.bypass.gamemode.*");
            PermissionUtils.setPermissions(player.addAttachment(main), falsePerms, true);
            MessageManager.sendMessage(player, "&cYou have disabled staff mode and are visible!");
        } else {
            if (PlayerUtility.isInventoryEmpty(player.getInventory())) {
                toggleGamemode(player, true);
                staffModePlayers.add(pUUID);
                player.getInventory().setContents(new ItemStack[]{thruTool, inspectInventoryTool, freezeTool, air, wand, air, toggleVanishTool, randomTeleportTool});
                vanishPlayer(player);
                //Carbyne.getInstance().getServer().dispatchCommand(Carbyne.getInstance().getServer().getConsoleSender(), "pex user " + player.getName() + " add mv.bypass.gamemode.*");
                PermissionUtils.setPermissions(player.addAttachment(main), truePerms, true);
                MessageManager.sendMessage(player, "&cYou have enabled staff mode and have vanished!");
                if (main.getTrailManager().getAdvancedEffects().containsKey(player.getUniqueId())) {
                    main.getTrailManager().getAdvancedEffects().remove(player.getUniqueId());
                }

                if (main.getTrailManager().getActivePlayerEffects().containsKey(player.getUniqueId())) {
                    main.getTrailManager().getActivePlayerEffects().remove(player.getUniqueId());
                }

            } else MessageManager.sendMessage(player, "&cYou need an empty inventory to enter staff mode!");
        }
    }

    /**
     * Method can be used to find players x-raying. Note that 30 is the gold spawn height.
     *
     * @param player Player to teleport
     */
    public void teleportToPlayerUnderY30(Player player) {
        List<Player> players = new ArrayList<>();

        for (Player p : Bukkit.getOnlinePlayers())

            if (p.getLocation().getY() <= 30 && !player.equals(p))
                players.add(p);

        if (players.size() == 0)
            return;

        player.teleport(players.get(Maths.randomNumberBetween(players.size(), 0)));
    }

    /**
     * Randomly inspect players on the server. Eliminates staff bias.
     *
     * @param player Player to teleport
     */
    public void teleportToRandomPlayer(Player player) {
        List<Player> players = new ArrayList<>();

        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!p.getUniqueId().equals(player.getUniqueId()) && (!p.hasPermission("carbyne.staff") || !p.isOp())) {
                players.add(p);
            }
        }

        if (players.size() == 0) {
            MessageManager.sendMessage(player, "&cThere are no available players to teleport to.");
            return;
        }

        player.teleport(players.get(Maths.randomNumberBetween(players.size(), 0)));
    }

    /**
     * PRECONDITION: Player is in staff mode and has permissions to vanish
     *
     * @param player Player to toggle vanish
     */
    public void toggleVanish(Player player) {
        if (vanish.contains(player.getUniqueId())) {
            showPlayer(player);
            MessageManager.sendMessage(player, "&cYou have been un-vanished!");
        } else {
            vanishPlayer(player);
            MessageManager.sendMessage(player, "&cYou are now vanished!");
        }
    }

    /**
     * Toggle method for freeze
     *
     * @param player Player to freeze
     */
    public void toggleFreeze(Player player) {
        if (frozen.contains(player.getUniqueId()))
            unfreezePlayer(player);
        else
            freezePlayer(player);
    }

    public void toggleFreeze(Player player, Player freezer) {
        if (frozen.contains(player.getUniqueId())) {
            unfreezePlayer(player);
            MessageManager.sendMessage(freezer, "&9You have unfrozen " + player.getName() + "!");
        } else {
            freezePlayer(player);
            MessageManager.sendMessage(freezer, "&9You have frozen " + player.getName() + "!");
        }
    }

    public void vanishPlayer(Player player) {
        for (Player all : PlayerUtility.getOnlinePlayers()) {
            if (!all.getUniqueId().equals(player.getUniqueId())) {
                if (!all.hasPermission("carbyne.staff.canseevanished")) {
                    all.hidePlayer(player);
                }
            }
        }

        staff.remove(player.getUniqueId());
        if (!vanish.contains(player.getUniqueId()))
            vanish.add(player.getUniqueId());
    }

    public static Location deserializeLocation(String s) {
        Location l = new Location(Bukkit.getWorlds().get(0), 0.0D, 0.0D, 0.0D);
        String[] att = s.split("a");

        for (String attribute : att) {
            String[] split = attribute.split("b");
            if (split[0].equalsIgnoreCase("w")) {
                l.setWorld(Bukkit.getWorld(split[1]));
            }

            if (split[0].equalsIgnoreCase("x")) {
                l.setX(Double.parseDouble(split[1]));
            }

            if (split[0].equalsIgnoreCase("y")) {
                l.setY(Double.parseDouble(split[1]));
            }

            if (split[0].equalsIgnoreCase("z")) {
                l.setZ(Double.parseDouble(split[1]));
            }

            if (split[0].equalsIgnoreCase("p")) {
                l.setPitch(Float.parseFloat(split[1]));
            }

            if (split[0].equalsIgnoreCase("yl")) {
                l.setYaw(Float.parseFloat(split[1]));
            }
        }

        return l;
    }

    public void showPlayer(Player player) {
        for (Player all : PlayerUtility.getOnlinePlayers()) {
            if (!all.getUniqueId().equals(player.getUniqueId())) {
                //if (!all.canSee(player)) {
                all.showPlayer(player);
                //}
            }
        }

        staff.add(player.getUniqueId());
        vanish.remove(player.getUniqueId());
    }

    public void freezePlayer(Player player) {
        frozen.add(player.getUniqueId());
        player.setWalkSpeed(0.0F);
        MessageManager.sendMessage(player, "&cYou have been frozen.");
    }

    public void unfreezePlayer(Player player) {
        frozen.remove(player.getUniqueId());
        player.setWalkSpeed(0.2F);
        MessageManager.sendMessage(player, "&aYou are no longer frozen.");
    }

    public void shutdown() {
        for (UUID id : frozen) {
            unfreezePlayer(Bukkit.getPlayer(id));
        }
        for (UUID id : staffModePlayers) {
            Bukkit.getPlayer(id).getInventory().clear();
            //Carbyne.getInstance().getServer().dispatchCommand(Carbyne.getInstance().getServer().getConsoleSender(), "pex user " + Bukkit.getPlayer(id).getName() + " remove mv.bypass.gamemode.*");
            PermissionUtils.setPermissions(Bukkit.getPlayer(id).addAttachment(main), falsePerms, true);
        }
    }

    private void toggleGamemode(Player player, boolean flag) {
        if (flag) {
            player.setGameMode(GameMode.CREATIVE);
        } else {
            player.setGameMode(GameMode.SURVIVAL);
        }
    }

    public boolean isVanished(Player player) {
        return vanish.contains(player.getUniqueId());
    }

    public void logToFile(String message) {
        try {
            File saveTo = new File(Carbyne.getInstance().getDataFolder(), "playersLog.txt");

            if (!saveTo.exists()) {
                saveTo.createNewFile();
            }

            FileWriter fw = new FileWriter(saveTo, true);

            PrintWriter pw = new PrintWriter(fw);
            pw.println(message);
            pw.flush();
            pw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Getter
    @Setter
    public static class ServerPicture {

        private Carbyne main = Carbyne.getInstance();
        private MongoCollection<Document> serverPictureCollection = main.getMongoDatabase().getCollection("serverpictures");

        private String id, imageUrl;
        private int x, y;
        private ItemFrame[][] frames;
        private BukkitTask checkRunnable;
        private BufferedImage[][] image;
        private Image[] splitImages;

        public ServerPicture(String id, String imageUrl, ItemFrame[][] frames, int x, int y) {
            this.x = x;
            this.y = y;
            this.id = id;
            this.imageUrl = imageUrl;
            this.frames = frames;

            establishPicture();

            checkRunnable = new BukkitRunnable() {
                public void run() {
                    int c = 0, r = 0;
                    for (ItemFrame[] frame1 : frames) {
                        for (ItemFrame itemFrame : frame1) {
                            for (Player player : Bukkit.getOnlinePlayers()) {
                                MapWrapper mapWrapper = main.getMapManager().wrapImage(image[c][r]);
                                MapController mapController = mapWrapper.getController();

                                mapController.addViewer(player);
                                mapController.sendContent(player);

                                mapController.showInFrame(player, itemFrame);
                                mapController.showInHand(player);
                            }

                            r++;
                        }

                        r = 0;
                        c++;
                    }
                }
            }.runTaskTimerAsynchronously(Carbyne.getInstance(), 20L, 100L);
        }

        public void establishPicture() {
            if (frames == null) {
                return;
            }

            BufferedImage image;
            try {
                image = ImageIO.read(new URL(imageUrl).openStream());
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }

            this.image = new BufferedImage[x][y];

            int dWidth = image.getWidth() / x, dHeight = image.getHeight() / y;

            int c = 0, r = 0;
            for (ItemFrame[] frame1 : frames) {
                for (ItemFrame itemFrame : frame1) {
                    this.image[c][r] = makeSubImage(image, dWidth, dHeight, (dWidth * c), (dHeight * r));
                    r++;
                }
                r = 0;
                c++;
            }
        }

        private BufferedImage makeSubImage(BufferedImage originalImage, int width, int height, int x, int y) {
            return originalImage.getSubimage(x, y, width, height);
            /*BufferedImage newImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

            Graphics2D graphics = (Graphics2D) newImage.getGraphics();
            AffineTransform at = new AffineTransform();
            at.setToRotation(Math.PI);
            graphics.drawImage(originalImage.getSubimage(-x, -y, width, height), at, null);
            //graphics.drawImage(originalImage, at,-x, -y, null);
            //graphics.drawImage(newImage, at, 1, 1, 1, 1, 1);
            graphics.dispose();

            return newImage;*/
        }

        public void cancel() {
            checkRunnable.cancel();
        }
    }


    public enum PictureType {
        SERVER, INDIVIDUAL
    }
}

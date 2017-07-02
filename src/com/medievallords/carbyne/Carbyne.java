package com.medievallords.carbyne;

import com.bizarrealex.aether.Aether;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.keenant.tabbed.Tabbed;
import com.medievallords.carbyne.bounty.BountyCommand;
import com.medievallords.carbyne.bounty.BountyManager;
import com.medievallords.carbyne.commands.*;
import com.medievallords.carbyne.controlpoints.ControlPointManager;
import com.medievallords.carbyne.controlpoints.commands.*;
import com.medievallords.carbyne.controlpoints.listeners.CaptureListener;
import com.medievallords.carbyne.crates.CrateManager;
import com.medievallords.carbyne.crates.commands.*;
import com.medievallords.carbyne.crates.listeners.CrateListeners;
import com.medievallords.carbyne.donator.GamemodeManager;
import com.medievallords.carbyne.donator.TrailManager;
import com.medievallords.carbyne.donator.commands.FlyCommand;
import com.medievallords.carbyne.donator.commands.GamemodeCommand;
import com.medievallords.carbyne.donator.commands.TrailCommand;
import com.medievallords.carbyne.donator.listeners.GameModeListener;
import com.medievallords.carbyne.donator.listeners.TrailListener;
import com.medievallords.carbyne.duels.arena.commands.*;
import com.medievallords.carbyne.duels.arena.listeners.ArenaListeners;
import com.medievallords.carbyne.duels.duel.DuelListeners;
import com.medievallords.carbyne.duels.duel.DuelManager;
import com.medievallords.carbyne.duels.duel.commands.*;
import com.medievallords.carbyne.economy.MarketManager;
import com.medievallords.carbyne.economy.account.Account;
import com.medievallords.carbyne.economy.commands.administrator.MarketSetTaxCommand;
import com.medievallords.carbyne.economy.commands.player.*;
import com.medievallords.carbyne.events.EventManager;
import com.medievallords.carbyne.events.commands.AddBlockCommand;
import com.medievallords.carbyne.events.commands.TempCommand;
import com.medievallords.carbyne.events.hohengroth.HohengrothSchedule;
import com.medievallords.carbyne.gates.GateManager;
import com.medievallords.carbyne.gates.commands.*;
import com.medievallords.carbyne.gates.listeners.GateListeners;
import com.medievallords.carbyne.gates.listeners.GateMobListeners;
import com.medievallords.carbyne.gear.GearManager;
import com.medievallords.carbyne.gear.commands.GearCommands;
import com.medievallords.carbyne.gear.commands.GearGiveCommand;
import com.medievallords.carbyne.gear.commands.GearSetChargeCommand;
import com.medievallords.carbyne.gear.listeners.GearListeners;
import com.medievallords.carbyne.heartbeat.HeartbeatRunnable;
import com.medievallords.carbyne.leaderboards.LeaderboardListeners;
import com.medievallords.carbyne.leaderboards.LeaderboardManager;
import com.medievallords.carbyne.leaderboards.commands.*;
import com.medievallords.carbyne.listeners.*;
import com.medievallords.carbyne.lootchests.LootChestListeners;
import com.medievallords.carbyne.lootchests.LootChestManager;
import com.medievallords.carbyne.lootchests.commands.LootChestCommand;
import com.medievallords.carbyne.profiles.ProfileListeners;
import com.medievallords.carbyne.profiles.ProfileManager;
import com.medievallords.carbyne.regeneration.RegenerationHandler;
import com.medievallords.carbyne.regeneration.RegenerationListeners;
import com.medievallords.carbyne.regeneration.commands.RegenerationBypassCommand;
import com.medievallords.carbyne.spawners.commands.SpawnerCommand;
import com.medievallords.carbyne.spawners.commands.SpawnerCreateCommand;
import com.medievallords.carbyne.spawners.listeners.SpawnerListeners;
import com.medievallords.carbyne.spellmods.SpellModsCommand;
import com.medievallords.carbyne.spellmods.SpellModsListener;
import com.medievallords.carbyne.squads.SquadManager;
import com.medievallords.carbyne.squads.commands.*;
import com.medievallords.carbyne.staff.StaffManager;
import com.medievallords.carbyne.staff.commands.*;
import com.medievallords.carbyne.staff.listeners.*;
import com.medievallords.carbyne.staff.tickets.TicketListener;
import com.medievallords.carbyne.staff.tickets.TicketManager;
import com.medievallords.carbyne.staff.tickets.commands.TicketCommand;
import com.medievallords.carbyne.utils.CarbyneBoardAdapter;
import com.medievallords.carbyne.utils.ItemDb;
import com.medievallords.carbyne.utils.Lang;
import com.medievallords.carbyne.utils.PlayerUtility;
import com.medievallords.carbyne.utils.combatindicators.PacketManager;
import com.medievallords.carbyne.utils.combatindicators.PacketManagerImpl;
import com.medievallords.carbyne.utils.command.CommandFramework;
import com.medievallords.carbyne.utils.nametag.NametagManager;
import com.medievallords.carbyne.utils.signgui.SignGUI;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.client.MongoDatabase;
import com.palmergames.bukkit.towny.Towny;
import de.slikey.effectlib.EffectManager;
import lombok.Getter;
import lombok.Setter;
import net.milkbowl.vault.permission.Permission;
import net.minelink.ctplus.CombatTagPlus;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.tyrannyofheaven.bukkit.zPermissions.ZPermissionsService;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

@Getter
@Setter
public class Carbyne extends JavaPlugin {

    public static Carbyne instance;

    private MongoClient mongoClient;
    private MongoDatabase mongoDatabase;

    private Towny towny;
    private boolean townyEnabled = false;

    private CombatTagPlus combatTagPlus;
    private boolean combatTagPlusEnabled = false;

    private boolean mythicMobsEnabled = false;

    private ZPermissionsService service = null;

    private File gearFile;
    private FileConfiguration gearFileConfiguration;
    private File duelFile;
    private FileConfiguration duelFileConfiguration;
    private File gateFile;
    private FileConfiguration gateFileConfiguration;
    private File crateFile;
    private FileConfiguration crateFileConfiguration;
    private File arenaFile;
    private FileConfiguration arenaFileConfiguration;
    private File leaderboardFile;
    private FileConfiguration leaderboardFileConfiguration;
    private File lootChestFile;
    private FileConfiguration lootChestFileConfiguration;
    private File controlPointsFile;
    private FileConfiguration controlPointsFileConfiguration;
    private File weteFile;
    private FileConfiguration weteFileConfiguration;
    private File gamemodeTownsFile;
    private FileConfiguration gamemodeTownsConfiguration;
    private File eventsFile;
    private FileConfiguration eventsFileConfiguration;
    private File immunePlayersFile;
    private FileConfiguration immunePlayersFileConfiguration;
    private File rulesFile;
    private FileConfiguration rulesFileCongfiguration;

    private Permission permissions = null;

    private CommandFramework commandFramework;

    private HeartbeatRunnable heartbeatRunnable;

    private Tabbed tabbed;
    private Aether aether;
    private SignGUI signGUI;

    private ProfileManager profileManager;
    private StaffManager staffManager;
    private RegenerationHandler regenerationHandler;
    private MarketManager marketManager;
    private GearManager gearManager;
    private EffectManager effectManager;
    private GateManager gateManager;
    private SquadManager squadManager;
    private CrateManager crateManager;
    private DuelManager duelManager;
    private LeaderboardManager leaderboardManager;
    private TicketManager ticketManager;
    private LootChestManager lootChestManager;
    private ControlPointManager controlPointManager;
    private GamemodeManager gamemodeManager;
    private ItemDb itemDb;
    private PacketManager packetManager;
    private TrailManager trailManager;
    private EventManager eventManager;
    private BountyManager bountyManager;

    private HohengrothSchedule hohengrothSchedule;


    public void onEnable() {
        instance = this;

        getConfig().options().copyDefaults(true);
        saveDefaultConfig();

        try {
            service = Bukkit.getServicesManager().load(ZPermissionsService.class);
        }
        catch (NoClassDefFoundError e) {
        }
        if (service == null) {

        }

        registerConfigurations();

        registerMongoConnection();

        PluginManager pm = Bukkit.getServer().getPluginManager();

        setupPermissions();

        if (pm.isPluginEnabled("Towny")) {
            towny = (Towny) pm.getPlugin("Towny");
            townyEnabled = true;
        }

        if (pm.isPluginEnabled("CombatTagPlus")) {
            combatTagPlus = (CombatTagPlus) pm.getPlugin("CombatTagPlus");
            combatTagPlusEnabled = true;
        }

        if (pm.isPluginEnabled("MythicMobs")) {
            mythicMobsEnabled = true;
        }

        heartbeatRunnable = new HeartbeatRunnable();
        heartbeatRunnable.runTaskTimer(Carbyne.getInstance(), 0L, 2L);

        itemDb = new ItemDb();

        for (Player all : PlayerUtility.getOnlinePlayers()) {
            all.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
        }

        profileManager = new ProfileManager();
        staffManager = new StaffManager();
        marketManager = new MarketManager();
        gearManager = new GearManager();
        effectManager = new EffectManager(this);
        gateManager = new GateManager();
        squadManager = new SquadManager();
        crateManager = new CrateManager();
        duelManager = new DuelManager();
        leaderboardManager = new LeaderboardManager();
        regenerationHandler = new RegenerationHandler();
        ticketManager = new TicketManager();
        controlPointManager = new ControlPointManager();
        lootChestManager = new LootChestManager();
        gamemodeManager = new GamemodeManager();
        trailManager = new TrailManager();
        packetManager = new PacketManagerImpl(this);
        eventManager = new EventManager();
        bountyManager = new BountyManager();
        hohengrothSchedule = new HohengrothSchedule();

        hohengrothSchedule.load();

        tabbed = new Tabbed(this);
        aether = new Aether(this, new CarbyneBoardAdapter(this));
        signGUI = new SignGUI();

        commandFramework = new CommandFramework(this);

        registerCommands();
        registerEvents(pm);
        registerPackets();

        CombatTagListeners.ForceFieldTask.run(this);

        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : PlayerUtility.getOnlinePlayers()) {
                    NametagManager.updateNametag(player);
                }
            }
        }.runTaskTimerAsynchronously(this, 0L, 10L);

        clearVillagers();
    }

    public void onDisable() {
        profileManager.saveProfiles(false);
        Account.saveAccounts(false);
        staffManager.shutdown();
        marketManager.saveSales(false);
        ticketManager.saveStats();
        gateManager.saveGates();
        effectManager.dispose();
        crateManager.save(crateFileConfiguration);
        leaderboardManager.stopAllLeaderboardTasks();
        duelManager.cancelAll();
        mongoClient.close();
        hohengrothSchedule.save();
        staffManager.shutdown();

        clearVillagers();
    }

    public void registerMongoConnection() {
        MongoClientOptions options = MongoClientOptions.builder().connectionsPerHost(200000).build();
        mongoClient = new MongoClient(getConfig().getString("database.host"), options);
        mongoDatabase = mongoClient.getDatabase(getConfig().getString("database.database-name"));
    }

    private void registerEvents(PluginManager pm) {
        pm.registerEvents(new ProfileListeners(), this);
        pm.registerEvents(new CombatTagListeners(), this);
        pm.registerEvents(new GearListeners(), this);
        pm.registerEvents(new CooldownListeners(), this);
        pm.registerEvents(new OptimizationListeners(), this);
        pm.registerEvents(new ChatListener(), this);
        pm.registerEvents(new GateListeners(), this);
        pm.registerEvents(new SpawnerListeners(), this);
        pm.registerEvents(new RegenerationListeners(), this);
        pm.registerEvents(new CrateListeners(), this);
        pm.registerEvents(new ArenaListeners(), this);
        pm.registerEvents(new DuelListeners(), this);
        pm.registerEvents(new LeaderboardListeners(), this);
        pm.registerEvents(new TicketListener(), this);
        pm.registerEvents(new LootChestListeners(), this);
        pm.registerEvents(new SetSlotsCommand(), this);
        pm.registerEvents(new SpellModsListener(), this);
        pm.registerEvents(new GameModeListener(gamemodeManager), this);
        pm.registerEvents(new OnLoginCommand(), this);
        pm.registerEvents(new CaptureListener(), this);
        pm.registerEvents(new AddBlockCommand(), this);
        pm.registerEvents(hohengrothSchedule, this);
        pm.registerEvents(new PlayerListeners(), this);
        pm.registerEvents(new FreezeListeners(), this);
        pm.registerEvents(new SpambotListener(), this);
        pm.registerEvents(new StaffModeListeners(), this);
        pm.registerEvents(new PinListeners(), this);
        pm.registerEvents(new VanishListeners(), this);
        pm.registerEvents(new TrailListener(), this);

        if (mythicMobsEnabled)
            pm.registerEvents(new GateMobListeners(), this);

        if (townyEnabled)
            pm.registerEvents(new DamageListener(), this);
    }

    private void registerCommands() {
        //General Commands
        new StatsCommand();
        new ChatCommand();
        new ToggleCommand();
        new RegenerationBypassCommand();
        new LogoutCommand();
        new PvpTimerCommand();

        //Gate Commands
        new GearCommands();
        new GearGiveCommand();
        new GearSetChargeCommand();
        new GateCommand();
        new GateAddBCommand();
        new GateAddPPCommand();
        new GateAddRSBCommand();
        new GateAddSpawnerCommand();
        new GateDelBCommand();
        new GateDelPPCommand();
        new GateDelRSBCommand();
        new GateDelSpawnerCommand();
        new GateCreateCommand();
        new GateActiveCommand();
        new GateRemoveCommand();
        new GateRenameCommand();
        new GateStatusCommand();
        new GateListCommand();

        //Market Commands
        new BalanceCommand();
        new MarketBuyCommand();
        new MarketSellCommand();
        new MarketPriceCommand();
        new MarketSalesCommand();
        new MarketSetTaxCommand();
        new MarketTaxCommand();
        new DepositCommand();
        new WithdrawCommand();

        //Squad Commands
        new SquadCommand();
        new SquadJoinCommand();
        new SquadCreateCommand();
        new SquadInviteCommand();
        new SquadLeaveCommand();
        new SquadDisbandCommand();
        new SquadFriendlyFireCommand();
        new SquadSetCommand();
        new SquadKickCommand();
        new SquadChatCommand();
        new SquadListCommand();
        new FocusCommand();

        //Spawner Commands
        new SpawnerCreateCommand();
        new SpawnerCommand();

        //Crate Commands
        new CrateCommand();
        new CrateCreateCommand();
        new CrateEditCommand();
        new CrateKeyCommand();
        new CrateListCommand();
        new CrateReloadCommand();
        new CrateRemoveCommand();
        new CrateRenameCommand();
        new CrateSetLocationCommand();

        //Arena Commands
        new ArenaCommand();
        new ArenaCreateCommand();
        new ArenaRemoveCommand();
        new ArenaListCommand();
        new ArenaSetLobbyCommand();
        new ArenaAddSpawnCommand();
        new ArenaAddPedastoolCommand();
        new ArenaRemoveSpawnCommand();
        new ArenaRemovePedastoolCommand();
        new ArenaReloadCommand();

        //Duel Commands
        new DuelAcceptCommand();
        new DuelSetSquadFightCommand();
        new DuelCommand();
        new DuelBetCommand();
        new DuelDeclineCommand();

        //Leaderboard Commands
        new LeaderboardCommand();
        new LeaderboardCreateCommand();
        new LeaderboardRemoveCommand();
        new LeaderboardSetPrimarySignCommand();
        new LeaderboardDelPrimarySignCommand();
        new LeaderboardAddSignCommand();
        new LeaderboardDelSignCommand();
        new LeaderboardAddHeadCommand();
        new LeaderboardDelHeadCommand();
        new LeaderboardListCommand();

        //Ticket Commands
        new TicketCommand();

        //Loot Chest Commands
        new LootChestCommand();

        //Staff Commands
        new ClearChatCommand();
        new HelpopCommand();
        new MuteChatCommand();
        new ReportCommand();
        new SetSlotsCommand();
        new SlowChatCommand();
        new VanishCommand();
        new StaffCommand();
        new FreezeCommand();
        new SetPinCommand();
        new ResetPinCommand();
        new InvseeCommand();
        new ReviveCommand();

        //Spell Mods Commands
        new SpellModsCommand();

        //Control Point Commands
        new CreateControlPointCommand();
        new RemoveControlPointCommand();
        new ReloadControlPointCommand();
        new ControlPointCommand();
        new ListControlPointCommand();
        new AddRewardControlPointCommand();

        //Gamemode Commands
        new FlyCommand();
        new GamemodeCommand();

        new TrailCommand();
        new BountyCommand();
        new RulesCommand();

        new TempCommand();
    }

    private void registerPackets() {
        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(this, PacketType.Play.Client.CLIENT_COMMAND) {
            @Override
            public void onPacketReceiving(PacketEvent event){
                if (event.getPacket().getClientCommands().read(0) == EnumWrappers.ClientCommand.OPEN_INVENTORY_ACHIEVEMENT) {
                    Player player = event.getPlayer();

                    PlayerUtility.checkForIllegalItems(player, player.getInventory());

                    if (player.getGameMode() == GameMode.CREATIVE) {
                        player.getInventory().setArmorContents(new ItemStack[] {null, null, null, null});
                    }
                }
            }
        });
    }

    private boolean setupPermissions() {
        RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
        permissions = rsp.getProvider();
        return permissions != null;
    }

    public void registerConfigurations() {
        saveResource("gear.yml", false);
        saveResource("duel.yml", false);
        saveResource("gates.yml", false);
        saveResource("item.csv", false);
        saveResource("crates.yml", false);
        saveResource("arenas.yml", false);
        saveResource("leaderboards.yml", false);
        saveResource("lang.yml", false);
        saveResource("lootchests.yml", false);
        saveResource("wete.yml", false);
        saveResource("controlpoints.yml", false);
        saveResource("gamemodetowns.yml", false);
        saveResource("events.yml", false);
        saveResource("rules.yml", false);

        gearFile = new File(getDataFolder(), "gear.yml");
        gearFileConfiguration = YamlConfiguration.loadConfiguration(gearFile);

        duelFile = new File(getDataFolder(), "duel.yml");
        duelFileConfiguration = YamlConfiguration.loadConfiguration(duelFile);

        gateFile = new File(getDataFolder(), "gates.yml");
        gateFileConfiguration = YamlConfiguration.loadConfiguration(gateFile);

        crateFile = new File(getDataFolder(), "crates.yml");
        crateFileConfiguration = YamlConfiguration.loadConfiguration(crateFile);

        arenaFile = new File(getDataFolder(), "arenas.yml");
        arenaFileConfiguration = YamlConfiguration.loadConfiguration(arenaFile);

        leaderboardFile = new File(getDataFolder(), "leaderboards.yml");
        leaderboardFileConfiguration = YamlConfiguration.loadConfiguration(leaderboardFile);

        lootChestFile = new File(getDataFolder(), "lootchests.yml");
        lootChestFileConfiguration = YamlConfiguration.loadConfiguration(lootChestFile);

        controlPointsFile = new File(getDataFolder(), "controlpoints.yml");
        controlPointsFileConfiguration = YamlConfiguration.loadConfiguration(controlPointsFile);

        weteFile = new File(getDataFolder(), "wete.yml");
        weteFileConfiguration = YamlConfiguration.loadConfiguration(weteFile);

        gamemodeTownsFile = new File(getDataFolder(), "gamemodetowns.yml");
        gamemodeTownsConfiguration = YamlConfiguration.loadConfiguration(gamemodeTownsFile);

        eventsFile = new File(getDataFolder(), "events.yml");
        eventsFileConfiguration = YamlConfiguration.loadConfiguration(eventsFile);

        immunePlayersFile = new File(getDataFolder(), "immuneplayers.yml");
        immunePlayersFileConfiguration = YamlConfiguration.loadConfiguration(immunePlayersFile);

        rulesFile = new File(getDataFolder(), "rules.yml");
        rulesFileCongfiguration = YamlConfiguration.loadConfiguration(rulesFile);

        File langFile = new File(getDataFolder(), "lang.yml");
        FileConfiguration langFileConfiguration = YamlConfiguration.loadConfiguration(langFile);

        for (Lang item : Lang.values()) {
            if (langFileConfiguration.getString(item.getPath()) == null) {
                if (item.getAllMessages().length > 1) {
                    langFileConfiguration.set(item.getPath(), item.getAllMessages());
                } else {
                    langFileConfiguration.set(item.getPath(), item.getFirstMessage());
                }
            }
        }

        Lang.setFile(langFileConfiguration);

        try {
            langFileConfiguration.save(langFile);
        } catch (IOException e) {
            e.printStackTrace();
            getLogger().log(Level.WARNING, "Failed to save lang.yml!");
            getLogger().log(Level.WARNING, "Please report this stacktrace to Young.");
        }
    }

    public void clearVillagers() {
        for (World w : Bukkit.getWorlds()) {
            w.getEntities().stream().filter(ent -> ent instanceof Villager).forEach(ent -> {
                Villager villager = (Villager) ent;

                if (villager.getMetadata("logger") != null && villager.hasMetadata("logger")) {
                    villager.remove();
                }
            });
        }
    }

    public static Carbyne getInstance() {
        return instance;
    }
}

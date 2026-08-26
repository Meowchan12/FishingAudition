package com.meowchan12.fishingaudition;

import com.meowchan12.fishingaudition.command.CommandManager;
import com.meowchan12.fishingaudition.listener.*;
import com.meowchan12.fishingaudition.manager.*;
import com.meowchan12.fishingaudition.scoreboard.ScoreboardManager;
import com.meowchan12.fishingaudition.shop.ShopManager;
import com.meowchan12.fishingaudition.top.TopManager;
import com.meowchan12.fishingaudition.utils.FishingPlaceholder;
import com.meowchan12.fishingaudition.hooks.economy.EconomyManager;
import org.black_ixx.playerpoints.PlayerPoints;
import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private static Main instance;

    // Dependencies
    private EconomyManager economyManager;
    private PlayerPointsAPI playerPointsAPI = null;

    // Managers
    private FileManager fileManager;
    private InventoryManager inventoryManager;
    private SessionManager sessionManager;
    private PlayerDataManager playerDataManager;
    private ChestManager chestManager;
    private RegionManager regionManager;
    private ShopManager shopManager;
    private TopManager topManager;
    private ScoreboardManager scoreboardManager;
    private CommandManager commandManager;
    private FishManager fishManager;
    private RodManager rodManager;
    private LanguageManager languageManager;
    private com.meowchan12.fishingaudition.currencymanager.ExchangeLogic exchangeLogic;
    private LevelManager levelManager;
    private LogbookManager logbookManager;
    private BaitManager baitManager;
    private com.meowchan12.fishingaudition.database.DatabaseManager databaseManager;
    private EventManager eventManager;

    @Override
    public void onEnable() {
        instance = this;

        // 1. Config
        saveDefaultConfig();
        getConfig().options().copyDefaults(true);
        saveConfig();
        
        // Initialize Database
        this.databaseManager = new com.meowchan12.fishingaudition.database.DatabaseManager(this);

        // 2. Dependencies
        this.economyManager = new EconomyManager();
        if (!economyManager.setupEconomy()) {
            getLogger().severe("Vault dependency not found! Disabling plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        setupPlayerPoints();

        // 3. Managers
        this.fileManager = new FileManager(this);
        this.inventoryManager = new InventoryManager(this);
        this.sessionManager = new SessionManager(this);
        
        // Initialize these before PlayerDataManager
        this.levelManager = new LevelManager(this);
        this.fishManager = new FishManager(this);
        this.logbookManager = new LogbookManager(this);
        this.rodManager = new RodManager(this);
        this.baitManager = new BaitManager(this);
        this.eventManager = new EventManager(this);
        
        this.playerDataManager = new PlayerDataManager(this);
        this.chestManager = new ChestManager(this);
        this.regionManager = new RegionManager(this);
        this.shopManager = new ShopManager();
        this.topManager = new TopManager(this);
        this.scoreboardManager = new ScoreboardManager(this);
        
        this.languageManager = new LanguageManager(this);
        this.exchangeLogic = new com.meowchan12.fishingaudition.currencymanager.ExchangeLogic(this);

        this.playerDataManager.startAutoSaveTask();
        this.chestManager.startAutoSaveTask();
        
        // 4. Listeners
        getServer().getPluginManager().registerEvents(new com.meowchan12.fishingaudition.listener.FishingListener(this, sessionManager), this);
        getServer().getPluginManager().registerEvents(new com.meowchan12.fishingaudition.listener.ShopListener(this, exchangeLogic), this);
        getServer().getPluginManager().registerEvents(new com.meowchan12.fishingaudition.listener.ChestListener(chestManager), this);
        getServer().getPluginManager().registerEvents(new com.meowchan12.fishingaudition.listener.BaitListener(this), this);
        getServer().getPluginManager().registerEvents(new MenuListener(this), this);
        getServer().getPluginManager().registerEvents(new DataListener(playerDataManager), this);

        // 5. Commands
        this.commandManager = new CommandManager(this);
        if (getCommand("fish") != null) {
            getCommand("fish").setExecutor(commandManager);
            getCommand("fish").setTabCompleter(commandManager);
        }

        // 6. PlaceholderAPI
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new FishingPlaceholder(this).register();
        }

        getLogger().info("FishingAudition enabled successfully.");
    }

    @Override
    public void onDisable() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getOpenInventory() != null) {
                player.closeInventory();
            }
            
            // CRITICAL SHUTDOWN: Force leave if active
            if (inventoryManager != null && inventoryManager.hasBackup(player)) {
                com.meowchan12.fishingaudition.command.CommandManager.processLeave(player, true);
            }
        }

        if (sessionManager != null) {
            sessionManager.endAllSessions();
        }

        if (scoreboardManager != null) {
            scoreboardManager.cleanup();
        }

        if (playerDataManager != null) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                playerDataManager.saveData(player, false); // false = SYNC
            }
        }
        
        if (chestManager != null) {
            chestManager.saveAllChests(); // saveAllChests now uses sweepFishToChestSync
        }
        
        if (databaseManager != null) {
            databaseManager.close();
        }
        getLogger().info("FishingAudition Disabled!");
    }

    private void setupPlayerPoints() {
        if (Bukkit.getPluginManager().isPluginEnabled("PlayerPoints")) {
            this.playerPointsAPI = PlayerPoints.getInstance().getAPI();
        }
    }

    public static Main getInstance() { return instance; }
    public EconomyManager getEconomyManager() { return economyManager; }
    public PlayerPointsAPI getPlayerPointsAPI() { return playerPointsAPI; }
    
    public FileManager getFileManager() { return fileManager; }
    public InventoryManager getInventoryManager() { return inventoryManager; }
    public SessionManager getSessionManager() { return sessionManager; }
    public PlayerDataManager getPlayerDataManager() { return playerDataManager; }
    public ChestManager getChestManager() { return chestManager; }
    public RegionManager getRegionManager() { return regionManager; }
    public ShopManager getShopManager() { return shopManager; }
    public TopManager getTopManager() { return topManager; }
    public ScoreboardManager getScoreboardManager() { return scoreboardManager; }
    public FishManager getFishManager() { return fishManager; }
    public RodManager getRodManager() { return rodManager; }
    public LanguageManager getLanguageManager() { return languageManager; }
    public LevelManager getLevelManager() { return levelManager; }
    public LogbookManager getLogbookManager() { return logbookManager; }
    public BaitManager getBaitManager() { return baitManager; }
    public com.meowchan12.fishingaudition.currencymanager.ExchangeLogic getExchangeLogic() { return exchangeLogic; }
    public com.meowchan12.fishingaudition.database.DatabaseManager getDatabaseManager() { return databaseManager; }
    public EventManager getEventManager() { return eventManager; }
}

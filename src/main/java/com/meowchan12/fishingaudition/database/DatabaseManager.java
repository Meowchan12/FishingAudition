package com.meowchan12.fishingaudition.database;

import com.meowchan12.fishingaudition.Main;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {

    private final Main plugin;
    private HikariDataSource dataSource;
    private final String type;

    public DatabaseManager(Main plugin) {
        this.plugin = plugin;
        FileConfiguration config = plugin.getConfig();
        this.type = config.getString("database.type", "sqlite").toLowerCase();
        setupPool(config);
        createTables();
    }

    private void setupPool(FileConfiguration config) {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setPoolName("FishingAuditionPool");
        hikariConfig.setMaximumPoolSize(config.getInt("database.pool_size", 10));

        if (type.equals("mysql")) {
            String host = config.getString("database.host", "localhost");
            int port = config.getInt("database.port", 3306);
            String db = config.getString("database.database_name", "fishing_audition");
            
            hikariConfig.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + db + "?useSSL=false&autoReconnect=true");
            hikariConfig.setUsername(config.getString("database.username", "root"));
            hikariConfig.setPassword(config.getString("database.password", ""));
            hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
            hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
            hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        } else {
            File dbFile = new File(plugin.getDataFolder(), "database.db");
            hikariConfig.setJdbcUrl("jdbc:sqlite:" + dbFile.getAbsolutePath());
            hikariConfig.setDriverClassName("org.sqlite.JDBC");
        }

        this.dataSource = new HikariDataSource(hikariConfig);
    }

    private void createTables() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            
            // Chest Table (item_data is Base64 LONGTEXT)
            String textType = type.equals("mysql") ? "LONGTEXT" : "TEXT";

            // Users Table
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS fishing_users (" +
                    "uuid VARCHAR(36) PRIMARY KEY, " +
                    "name VARCHAR(16), " +
                    "fishcoins DOUBLE, " +
                    "top_round INT, " +
                    "level INT, " +
                    "xp DOUBLE, " +
                    "owned_rods " + textType + ", " +
                    "equipped_rod VARCHAR(64))");

            try {
                stmt.executeUpdate("ALTER TABLE fishing_users ADD COLUMN owned_rods " + textType);
            } catch (SQLException ignored) {}
            try {
                stmt.executeUpdate("ALTER TABLE fishing_users ADD COLUMN equipped_rod VARCHAR(64)");
            } catch (SQLException ignored) {}
            try {
                stmt.executeUpdate("ALTER TABLE fishing_users ADD COLUMN equipped_bait VARCHAR(64)");
            } catch (SQLException ignored) {}
            try {
                stmt.executeUpdate("ALTER TABLE fishing_users ADD COLUMN bait_charges INT");
            } catch (SQLException ignored) {}

            // Chest Table (item_data is Base64 LONGTEXT)
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS fishing_chest (" +
                    "uuid VARCHAR(36) PRIMARY KEY, " +
                    "item_data " + textType + ")");

            // Logbook Table
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS fishing_logbook (" +
                    "uuid VARCHAR(36), " +
                    "fish_id VARCHAR(64), " +
                    "catch_count INT, " +
                    "PRIMARY KEY(uuid, fish_id))");

        } catch (SQLException e) {
            plugin.getLogger().severe("Could not create database tables: " + e.getMessage());
        }
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }

    public String getType() {
        return type;
    }
}

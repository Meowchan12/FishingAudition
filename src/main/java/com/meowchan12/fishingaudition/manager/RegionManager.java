package com.meowchan12.fishingaudition.manager;

import com.meowchan12.fishingaudition.Main;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;

public class RegionManager {

    private final Main plugin;
    private String worldName;
    private double minX, minY, minZ;
    private double maxX, maxY, maxZ;
    private boolean isRegionSet = false;

    public RegionManager(Main plugin) {
        this.plugin = plugin;
        loadRegion();
    }

    public void loadRegion() {
        FileConfiguration config = plugin.getConfig();
        if (config.contains("region.world")) {
            this.worldName = config.getString("region.world");
            this.minX = Math.min(config.getDouble("region.pos1.x"), config.getDouble("region.pos2.x"));
            this.minY = Math.min(config.getDouble("region.pos1.y"), config.getDouble("region.pos2.y"));
            this.minZ = Math.min(config.getDouble("region.pos1.z"), config.getDouble("region.pos2.z"));

            this.maxX = Math.max(config.getDouble("region.pos1.x"), config.getDouble("region.pos2.x"));
            this.maxY = Math.max(config.getDouble("region.pos1.y"), config.getDouble("region.pos2.y"));
            this.maxZ = Math.max(config.getDouble("region.pos1.z"), config.getDouble("region.pos2.z"));

            this.isRegionSet = true;
        }
    }

    public void saveRegion(Location pos1, Location pos2) {
        FileConfiguration config = plugin.getConfig();
        config.set("region.world", pos1.getWorld().getName());
        config.set("region.pos1.x", pos1.getX());
        config.set("region.pos1.y", pos1.getY());
        config.set("region.pos1.z", pos1.getZ());

        config.set("region.pos2.x", pos2.getX());
        config.set("region.pos2.y", pos2.getY());
        config.set("region.pos2.z", pos2.getZ());

        plugin.saveConfig();
        loadRegion();
    }

    public boolean isInsideRegion(Location loc) {
        if (!isRegionSet) return false;
        if (loc.getWorld() == null || !loc.getWorld().getName().equals(worldName)) return false;

        return loc.getX() >= minX && loc.getX() <= maxX &&
                loc.getY() >= minY && loc.getY() <= maxY &&
                loc.getZ() >= minZ && loc.getZ() <= maxZ;
    }

    public boolean isSet() {
        if (!plugin.getConfig().isSet("region.join_location.world")) {
            return false;
        }
        return isRegionSet;
    }

    public Location getJoinLocation() {
        FileConfiguration config = plugin.getConfig();
        if (config.contains("region.join_location.world")) {
            World world = org.bukkit.Bukkit.getWorld(config.getString("region.join_location.world"));
            if (world != null) {
                double x = config.getDouble("region.join_location.x");
                double y = config.getDouble("region.join_location.y");
                double z = config.getDouble("region.join_location.z");
                float yaw = (float) config.getDouble("region.join_location.yaw");
                float pitch = (float) config.getDouble("region.join_location.pitch");
                return new Location(world, x, y, z, yaw, pitch);
            }
        }
        return null;
    }

    public Location getLeaveLocation() {
        FileConfiguration config = plugin.getConfig();
        if (config.contains("region.leave_location.world")) {
            World world = org.bukkit.Bukkit.getWorld(config.getString("region.leave_location.world"));
            if (world != null) {
                double x = config.getDouble("region.leave_location.x");
                double y = config.getDouble("region.leave_location.y");
                double z = config.getDouble("region.leave_location.z");
                float yaw = (float) config.getDouble("region.leave_location.yaw");
                float pitch = (float) config.getDouble("region.leave_location.pitch");
                return new Location(world, x, y, z, yaw, pitch);
            }
        }
        return null;
    }
}
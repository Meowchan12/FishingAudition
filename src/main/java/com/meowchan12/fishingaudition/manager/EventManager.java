package com.meowchan12.fishingaudition.manager;

import com.meowchan12.fishingaudition.Main;
import org.bukkit.configuration.file.FileConfiguration;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class EventManager {

    private final Main plugin;
    private boolean forceOverride = false;

    public EventManager(Main plugin) {
        this.plugin = plugin;
    }

    public boolean isForceOverride() {
        return forceOverride;
    }

    public void setForceOverride(boolean forceOverride) {
        this.forceOverride = forceOverride;
    }

    public boolean isEventActive() {
        if (forceOverride) return true;

        FileConfiguration config = plugin.getConfig();
        if (!config.getBoolean("events.weekend-xp.enabled", false)) {
            return false;
        }

        List<String> activeDays = config.getStringList("events.weekend-xp.active-days");
        if (activeDays == null || activeDays.isEmpty()) {
            return false;
        }

        ZonedDateTime now = ZonedDateTime.now(ZoneId.systemDefault());
        String currentDayName = now.getDayOfWeek().name();

        boolean dayMatch = false;
        for (String day : activeDays) {
            if (day.equalsIgnoreCase(currentDayName)) {
                dayMatch = true;
                break;
            }
        }
        if (!dayMatch) return false;

        String startTimeStr = config.getString("events.weekend-xp.start-time", "00:00");
        String endTimeStr = config.getString("events.weekend-xp.end-time", "23:59");

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
            LocalTime startTime = LocalTime.parse(startTimeStr, formatter);
            LocalTime endTime = LocalTime.parse(endTimeStr, formatter);
            LocalTime currentTime = now.toLocalTime();

            if (currentTime.isBefore(startTime) || currentTime.isAfter(endTime)) {
                return false;
            }

            return true;
        } catch (Exception e) {
            plugin.getLogger().warning("Invalid time format in events.weekend-xp config! Use HH:mm");
            return false;
        }
    }

    public double getCurrentMultiplier() {
        if (isEventActive()) {
            return plugin.getConfig().getDouble("events.weekend-xp.multiplier", 2.0);
        }
        return 1.0;
    }
}

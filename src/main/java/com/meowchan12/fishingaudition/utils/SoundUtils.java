package com.meowchan12.fishingaudition.utils;

import com.meowchan12.fishingaudition.Main;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class SoundUtils {

    public static void playSound(Player player, String configPath) {
        FileConfiguration config = Main.getInstance().getConfig();
        String soundName = config.getString("sounds." + configPath + ".sound", "NONE");
        
        if (soundName.equalsIgnoreCase("NONE")) {
            return;
        }

        try {
            Sound sound = Sound.valueOf(soundName.toUpperCase());
            float volume = (float) config.getDouble("sounds." + configPath + ".volume", 1.0);
            float pitch = (float) config.getDouble("sounds." + configPath + ".pitch", 1.0);
            player.playSound(player.getLocation(), sound, volume, pitch);
        } catch (IllegalArgumentException e) {
            Main.getInstance().getLogger().warning("Invalid sound configured for " + configPath + ": " + soundName);
        }
    }
}

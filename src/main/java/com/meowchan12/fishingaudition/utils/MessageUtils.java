package com.meowchan12.fishingaudition.utils;

import com.meowchan12.fishingaudition.Main;
import net.md_5.bungee.api.ChatColor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageUtils {

    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})|<#([A-Fa-f0-9]{6})>");

    public static String colorize(String message) {
        if (message == null || message.isEmpty()) return "";

        Matcher matcher = HEX_PATTERN.matcher(message);
        StringBuffer buffer = new StringBuffer();

        while (matcher.find()) {
            String color = matcher.group(1) != null ? matcher.group(1) : matcher.group(2);
            matcher.appendReplacement(buffer, ChatColor.of("#" + color).toString());
        }

        return ChatColor.translateAlternateColorCodes('&', matcher.appendTail(buffer).toString());
    }

    public static String getRawMessage(String path, String def) {
        if (Main.getInstance() == null || Main.getInstance().getLanguageManager() == null) {
            return colorize(def);
        }
        String msg = Main.getInstance().getLanguageManager().getRawMessage(path, def);
        return colorize(msg);
    }

    public static String getMessage(String path) {
        return getMessage(path, path);
    }

    public static String getMessage(String path, String def) {
        if (Main.getInstance() == null || Main.getInstance().getLanguageManager() == null) {
            return colorize(def);
        }
        
        String prefix = Main.getInstance().getLanguageManager().getRawMessage("prefix", "&8[&bFishingAudition&8] ");
        String msg = Main.getInstance().getLanguageManager().getRawMessage(path, def);
        
        return colorize(prefix + msg);
    }

    public static void sendActionBar(org.bukkit.entity.Player player, String message) {
        player.spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR, net.md_5.bungee.api.chat.TextComponent.fromLegacyText(colorize(message)));
    }

    public static void sendTitle(org.bukkit.entity.Player player, String title, String subtitle) {
        player.sendTitle(colorize(title), colorize(subtitle), 10, 70, 20);
    }
}
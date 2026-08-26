package com.meowchan12.fishingaudition.command.subcommands;

import com.meowchan12.fishingaudition.Main;
import com.meowchan12.fishingaudition.command.SubCommand;
import com.meowchan12.fishingaudition.utils.MessageUtils;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SetRegionCommand extends SubCommand {

    // Bộ nhớ tạm để lưu Point 1 trước khi set Point 2
    private static final Map<UUID, Location> tempPos1 = new HashMap<>();

    @Override
    public String getName() { return "setregion"; }

    @Override
    public String getDescription() { return "Set the fishing region boundary."; }

    @Override
    public String getSyntax() { return "/fish setregion <1/2>"; }

    @Override
    public String getPermission() { return "fishingaudition.admin.setup"; }

    @Override
    public void perform(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(MessageUtils.colorize("&cUsage: " + getSyntax()));
            return;
        }

        String point = args[1];
        Location loc = player.getLocation();

        if (point.equals("1")) {
            tempPos1.put(player.getUniqueId(), loc);
            player.sendMessage(MessageUtils.colorize("&aRegion Point 1 set at your location. Now use /fish setregion 2."));
        }
        else if (point.equals("2")) {
            if (!tempPos1.containsKey(player.getUniqueId())) {
                player.sendMessage(MessageUtils.colorize("&cYou must set Point 1 first using /fish setregion 1."));
                return;
            }
            Location pos1 = tempPos1.get(player.getUniqueId());

            // Lưu vào RegionManager và Config
            Main.getInstance().getRegionManager().saveRegion(pos1, loc);
            tempPos1.remove(player.getUniqueId());

            player.sendMessage(MessageUtils.colorize("&aFishing region successfully saved!"));
        }
        else {
            player.sendMessage(MessageUtils.colorize("&cInvalid point. Use 1 or 2."));
        }
    }
}
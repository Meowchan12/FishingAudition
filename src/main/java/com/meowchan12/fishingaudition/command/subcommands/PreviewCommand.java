package com.meowchan12.fishingaudition.command.subcommands;

import com.meowchan12.fishingaudition.Main;
import com.meowchan12.fishingaudition.command.SubCommand;
import com.meowchan12.fishingaudition.manager.FishData;
import com.meowchan12.fishingaudition.material.CustomFish;
import com.meowchan12.fishingaudition.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class PreviewCommand extends SubCommand {

    @Override
    public String getName() {
        return "preview";
    }

    @Override
    public String getDescription() {
        return "Preview all custom fish.";
    }

    @Override
    public String getSyntax() {
        return "/fish preview";
    }

    @Override
    public String getPermission() {
        return "fishingaudition.user.preview";
    }

    @Override
    public void perform(Player player, String[] args) {
        java.util.Set<String> fishIds = Main.getInstance().getFishManager().getAllFishIds();
        
        int size = Main.getInstance().getConfig().getInt("gui.sizes.preview", 54);
        String title = MessageUtils.colorize(Main.getInstance().getConfig().getString("gui.titles.preview", "&8Fish Preview"));
        Inventory inv = Bukkit.createInventory(null, size, title);
        
        for (String id : fishIds) {
            FishData data = Main.getInstance().getFishManager().getFishById(id);
            if (data != null) {
                inv.addItem(CustomFish.generateFish(data));
            }
        }
        
        player.openInventory(inv);
    }
}

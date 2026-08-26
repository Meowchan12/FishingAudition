package com.meowchan12.fishingaudition.hooks.economy;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;

import java.util.logging.Level;

/**
 * Traditional Vault hook (synchronous, single-currency).
 * This is the default fallback when VaultX is not detected.
 */
public class VaultHook implements EconomyProvider {

    private final Economy economy;

    public VaultHook(Economy economy) {
        this.economy = economy;
    }

    @Override
    public boolean isEnabled() {
        return economy != null && economy.isEnabled();
    }

    @Override
    public double getBalance(Player player, String currency) {
        // Traditional Vault does not support multi-currency; currency param is ignored.
        return economy.getBalance(player);
    }

    @Override
    public void withdraw(Player player, double amount, String currency) {
        economy.withdrawPlayer(player, amount);
    }

    @Override
    public void deposit(Player player, double amount, String currency) {
        economy.depositPlayer(player, amount);
    }
}

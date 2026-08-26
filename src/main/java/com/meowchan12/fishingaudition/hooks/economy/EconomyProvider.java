package com.meowchan12.fishingaudition.hooks.economy;

import org.bukkit.entity.Player;

/**
 * Adapter interface for external economy plugins (Vault, VaultX, etc.).
 * All economy interactions in FishingAudition MUST go through this contract.
 */
public interface EconomyProvider {

    /**
     * Checks if this economy provider is ready and available.
     */
    boolean isEnabled();

    /**
     * Gets the balance of a player.
     * @param player   the player
     * @param currency the currency identifier (e.g. "default", "gems"). Ignored if the provider does not support multi-currency.
     * @return the player's balance in the specified currency
     */
    double getBalance(Player player, String currency);

    /**
     * Withdraws money from a player's account.
     * @param player   the player
     * @param amount   the amount to withdraw
     * @param currency the currency identifier. Ignored if the provider does not support multi-currency.
     */
    void withdraw(Player player, double amount, String currency);

    /**
     * Deposits money into a player's account.
     * @param player   the player
     * @param amount   the amount to deposit
     * @param currency the currency identifier. Ignored if the provider does not support multi-currency.
     */
    void deposit(Player player, double amount, String currency);
}

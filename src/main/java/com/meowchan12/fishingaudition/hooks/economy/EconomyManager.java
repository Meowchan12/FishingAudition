package com.meowchan12.fishingaudition.hooks.economy;

import com.meowchan12.fishingaudition.Main;
import com.meowchan12.fishingaudition.hooks.economy.vaultx.VaultXHook;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.logging.Level;

/**
 * Smart router that detects the server's economy backend and creates
 * the optimal EconomyProvider implementation.
 *
 * Detection algorithm:
 * 1. Fetch the Vault Economy service provider (standard Bukkit RSP).
 * 2. Check if the provider is an instance of VaultAsyncEconomy or MultiCurrencyEconomy
 *    (VaultX registers under the Vault service name, so plugin name detection won't work).
 * 3. If VaultX classes are detected -> use VaultXHook (async + multi-currency).
 * 4. Otherwise -> fallback to VaultHook (traditional sync).
 */
public class EconomyManager {

    private EconomyProvider provider;
    private Economy rawEconomy;

    /**
     * Detects and initializes the appropriate economy provider.
     * @return true if an economy provider was successfully set up
     */
    public boolean setupEconomy() {
        if (Bukkit.getServer().getPluginManager().getPlugin("Vault") == null) {
            Main.getInstance().getLogger().severe("[EconomyManager] Vault plugin not found!");
            return false;
        }

        RegisteredServiceProvider<Economy> rsp = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            Main.getInstance().getLogger().severe("[EconomyManager] No Economy service provider registered!");
            return false;
        }

        rawEconomy = rsp.getProvider();
        if (rawEconomy == null) {
            Main.getInstance().getLogger().severe("[EconomyManager] Economy provider is null!");
            return false;
        }

        // Smart Detection: VaultX registers as a drop-in replacement under Vault's service name.
        // We detect it by checking if the provider implements VaultX-specific interfaces.
        if (isVaultX(rawEconomy)) {
            provider = new VaultXHook(rawEconomy);
            Main.getInstance().getLogger().info("[EconomyManager] VaultX detected -> Using VaultXHook (Async + MultiCurrency)");
        } else {
            provider = new VaultHook(rawEconomy);
            Main.getInstance().getLogger().info("[EconomyManager] Traditional Vault detected -> Using VaultHook (Sync)");
        }

        return true;
    }

    /**
     * Returns the active economy provider.
     */
    public EconomyProvider getProvider() {
        return provider;
    }

    /**
     * Returns the raw Vault Economy object for legacy compatibility.
     */
    public Economy getRawEconomy() {
        return rawEconomy;
    }

    /**
     * Checks if the Economy provider is a VaultX implementation
     * by testing for VaultAsyncEconomy or MultiCurrencyEconomy interfaces.
     */
    private boolean isVaultX(Economy econ) {
        try {
            Class<?> asyncClass = Class.forName("net.milkbowl.vault.economy.VaultAsyncEconomy");
            if (asyncClass.isInstance(econ)) {
                return true;
            }
        } catch (ClassNotFoundException ignored) {
            // VaultAsyncEconomy class not on classpath -> not VaultX
        }

        try {
            Class<?> multiClass = Class.forName("net.milkbowl.vault.economy.MultiCurrencyEconomy");
            if (multiClass.isInstance(econ)) {
                return true;
            }
        } catch (ClassNotFoundException ignored) {
            // MultiCurrencyEconomy class not on classpath -> not VaultX
        }

        return false;
    }
}

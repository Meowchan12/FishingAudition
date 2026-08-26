package com.meowchan12.fishingaudition.hooks.economy.vaultx;

import com.meowchan12.fishingaudition.Main;
import com.meowchan12.fishingaudition.hooks.economy.EconomyProvider;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.util.logging.Level;

/**
 * VaultX-optimized hook that leverages:
 * - Async deposit/withdraw via VaultAsyncEconomy
 * - Multi-currency support via MultiCurrencyEconomy
 *
 * Uses reflection to avoid hard compile-time dependency on VaultX classes,
 * since VaultX registers itself under the same Vault service provider.
 */
public class VaultXHook implements EconomyProvider {

    private final Economy economy;
    private final boolean hasAsync;
    private final boolean hasMultiCurrency;

    // Cached reflection methods for VaultAsyncEconomy
    private Method depositAsyncMethod;
    private Method withdrawAsyncMethod;

    // Cached reflection methods for MultiCurrencyEconomy
    private Method getBalanceMultiMethod;
    private Method depositMultiMethod;
    private Method withdrawMultiMethod;

    public VaultXHook(Economy economy) {
        this.economy = economy;
        this.hasAsync = isInstanceOf(economy, "net.milkbowl.vault.economy.VaultAsyncEconomy");
        this.hasMultiCurrency = isInstanceOf(economy, "net.milkbowl.vault.economy.MultiCurrencyEconomy");

        // Cache async methods
        if (hasAsync) {
            try {
                Class<?> asyncClass = Class.forName("net.milkbowl.vault.economy.VaultAsyncEconomy");
                depositAsyncMethod = asyncClass.getMethod("depositPlayerAsync", org.bukkit.OfflinePlayer.class, double.class);
                withdrawAsyncMethod = asyncClass.getMethod("withdrawPlayerAsync", org.bukkit.OfflinePlayer.class, double.class);
            } catch (Exception e) {
                Main.getInstance().getLogger().log(Level.WARNING, "Failed to cache VaultX async methods", e);
            }
        }

        // Cache multi-currency methods
        if (hasMultiCurrency) {
            try {
                Class<?> multiClass = Class.forName("net.milkbowl.vault.economy.MultiCurrencyEconomy");
                getBalanceMultiMethod = multiClass.getMethod("getBalance", org.bukkit.OfflinePlayer.class, String.class);
                depositMultiMethod = multiClass.getMethod("depositPlayer", org.bukkit.OfflinePlayer.class, double.class, String.class);
                withdrawMultiMethod = multiClass.getMethod("withdrawPlayer", org.bukkit.OfflinePlayer.class, double.class, String.class);
            } catch (Exception e) {
                Main.getInstance().getLogger().log(Level.WARNING, "Failed to cache VaultX multi-currency methods", e);
            }
        }

        Main.getInstance().getLogger().info("[EconomyHook] VaultX detected! Async=" + hasAsync + ", MultiCurrency=" + hasMultiCurrency);
    }

    @Override
    public boolean isEnabled() {
        return economy != null && economy.isEnabled();
    }

    @Override
    public double getBalance(Player player, String currency) {
        if (hasMultiCurrency && currency != null && !currency.isEmpty() && !currency.equalsIgnoreCase("default")) {
            try {
                if (getBalanceMultiMethod != null) {
                    Object result = getBalanceMultiMethod.invoke(economy, player, currency);
                    return (double) result;
                }
            } catch (Exception e) {
                Main.getInstance().getLogger().log(Level.WARNING, "VaultX multi-currency getBalance failed, falling back", e);
            }
        }
        return economy.getBalance(player);
    }

    @Override
    public void withdraw(Player player, double amount, String currency) {
        // Try multi-currency first
        if (hasMultiCurrency && currency != null && !currency.isEmpty() && !currency.equalsIgnoreCase("default")) {
            try {
                if (withdrawMultiMethod != null) {
                    withdrawMultiMethod.invoke(economy, player, amount, currency);
                    return;
                }
            } catch (Exception e) {
                Main.getInstance().getLogger().log(Level.WARNING, "VaultX multi-currency withdraw failed, falling back", e);
            }
        }

        // Try async
        if (hasAsync) {
            try {
                if (withdrawAsyncMethod != null) {
                    withdrawAsyncMethod.invoke(economy, player, amount);
                    return;
                }
            } catch (Exception e) {
                Main.getInstance().getLogger().log(Level.WARNING, "VaultX async withdraw failed, falling back to sync", e);
            }
        }

        // Sync fallback
        economy.withdrawPlayer(player, amount);
    }

    @Override
    public void deposit(Player player, double amount, String currency) {
        // Try multi-currency first
        if (hasMultiCurrency && currency != null && !currency.isEmpty() && !currency.equalsIgnoreCase("default")) {
            try {
                if (depositMultiMethod != null) {
                    depositMultiMethod.invoke(economy, player, amount, currency);
                    return;
                }
            } catch (Exception e) {
                Main.getInstance().getLogger().log(Level.WARNING, "VaultX multi-currency deposit failed, falling back", e);
            }
        }

        // Try async
        if (hasAsync) {
            try {
                if (depositAsyncMethod != null) {
                    depositAsyncMethod.invoke(economy, player, amount);
                    return;
                }
            } catch (Exception e) {
                Main.getInstance().getLogger().log(Level.WARNING, "VaultX async deposit failed, falling back to sync", e);
            }
        }

        // Sync fallback
        economy.depositPlayer(player, amount);
    }

    /**
     * Safely checks instanceof without requiring compile-time dependency.
     */
    private static boolean isInstanceOf(Object obj, String className) {
        try {
            Class<?> clazz = Class.forName(className);
            return clazz.isInstance(obj);
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}

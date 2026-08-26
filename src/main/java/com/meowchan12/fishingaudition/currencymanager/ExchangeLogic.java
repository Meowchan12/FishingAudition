package com.meowchan12.fishingaudition.currencymanager;

import com.meowchan12.fishingaudition.Main;
import com.meowchan12.fishingaudition.utils.MessageUtils;
import org.bukkit.entity.Player;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.UUID;

public class ExchangeLogic {

    private final Main plugin;
    // Lưu trạng thái những người đang gõ số lượng đổi tiền vào chat (Lưu lại exchangeId)
    private final Map<UUID, String> awaitingInput = new ConcurrentHashMap<>();

    public ExchangeLogic(Main plugin) {
        this.plugin = plugin;
    }

    private org.bukkit.configuration.ConfigurationSection getTierById(String exchangeId) {
        org.bukkit.configuration.ConfigurationSection tiers = plugin.getConfig().getConfigurationSection("exchange.tiers");
        if (tiers != null) {
            for (String key : tiers.getKeys(false)) {
                String id = tiers.getString(key + ".id", key);
                if (id.equalsIgnoreCase(exchangeId)) {
                    return tiers.getConfigurationSection(key);
                }
            }
        }
        return null;
    }

    /**
     * Xử lý click chuột trái: Đổi 1 lần với số lượng cố định theo cost-fishcoins.
     */
    public void processFixedExchange(Player player, String exchangeId) {
        org.bukkit.configuration.ConfigurationSection tier = getTierById(exchangeId);
        if (tier == null) return;

        double cost = tier.getDouble("cost-fishcoins", 100.0);
        if (cost <= 0) return;

        if (!FishCoinManager.hasEnough(player, cost)) {
            player.sendMessage(MessageUtils.colorize(MessageUtils.getMessage("errors.not_enough_coins", "&cYou do not have enough FishCoins!")));
            return;
        }

        executeExchange(player, exchangeId, cost, tier);
    }

    /**
     * Chuẩn bị cho click chuột phải: Bắt người chơi nhập số vào chat.
     */
    public void startChatInputSession(Player player, String exchangeId) {
        player.closeInventory();
        awaitingInput.put(player.getUniqueId(), exchangeId);
        player.sendMessage(MessageUtils.colorize("&ePlease type the amount of FishCoin you want to exchange in chat, or type &a'all' &eto exchange everything. Type &c'cancel' &eto abort."));
    }

    public boolean isAwaitingInput(Player player) {
        return awaitingInput.containsKey(player.getUniqueId());
    }

    public void removeAwaitingInput(Player player) {
        awaitingInput.remove(player.getUniqueId());
    }

    /**
     * Nhận dữ liệu từ chat và tính toán.
     */
    public void processChatInput(Player player, String input) {
        String exchangeId = awaitingInput.get(player.getUniqueId());
        removeAwaitingInput(player);

        if (input.equalsIgnoreCase("cancel")) {
            player.sendMessage(MessageUtils.colorize("&cExchange cancelled."));
            return;
        }

        org.bukkit.configuration.ConfigurationSection tier = getTierById(exchangeId);
        if (tier == null) return;

        double amountToExchange = 0.0;
        double currentBalance = FishCoinManager.getBalance(player);

        if (input.equalsIgnoreCase("all")) {
            amountToExchange = currentBalance;
        } else {
            try {
                amountToExchange = Double.parseDouble(input);
            } catch (NumberFormatException e) {
                player.sendMessage(MessageUtils.colorize(MessageUtils.getMessage("errors.invalid_number", "&cInvalid number format. Exchange cancelled.")));
                return;
            }
        }

        if (amountToExchange <= 0) {
            player.sendMessage(MessageUtils.colorize(MessageUtils.getMessage("errors.invalid_amount", "&cAmount must be greater than 0!")));
            return;
        }

        if (!FishCoinManager.hasEnough(player, amountToExchange)) {
            player.sendMessage(MessageUtils.colorize(MessageUtils.getMessage("errors.not_enough_coins", "&cYou do not have enough FishCoins!")));
            return;
        }

        executeExchange(player, exchangeId, amountToExchange, tier);
    }

    /**
     * Logic trừ tiền gốc và cộng tiền Vault/PlayerPoints bằng thuật toán Dynamic Rate.
     */
    private void executeExchange(Player player, String exchangeId, double fishCoinAmount, org.bukkit.configuration.ConfigurationSection tier) {
        double cost = tier.getDouble("cost-fishcoins", 100.0);
        double reward = tier.getDouble("reward-amount", 1000.0);
        String currencyType = tier.getString("currency_type", "VAULT");
        
        if (cost <= 0) return;
        double rate = reward / cost;
        double convertedAmount = fishCoinAmount * rate;

        FishCoinManager.removeBalance(player, fishCoinAmount);

        if (currencyType.equalsIgnoreCase("VAULT")) {
            if (plugin.getEconomyManager() != null && plugin.getEconomyManager().getProvider() != null && plugin.getEconomyManager().getProvider().isEnabled()) {
                plugin.getEconomyManager().getProvider().deposit(player, convertedAmount, "default");
                String msg = MessageUtils.getMessage("success.exchange_success", "&aExchanged &e{coins} FishCoins &afor &e{vault} Vault Money&a!");
                msg = msg.replace("{coins}", String.valueOf(fishCoinAmount)).replace("{vault}", String.valueOf(convertedAmount));
                player.sendMessage(MessageUtils.colorize(msg));
            } else {
                player.sendMessage(MessageUtils.colorize(MessageUtils.getMessage("errors.vault_not_loaded", "&cVault economy is not loaded on this server.")));
                FishCoinManager.addBalance(player, fishCoinAmount);
            }
        } else if (currencyType.equalsIgnoreCase("PLAYERPOINTS")) {
            if (plugin.getPlayerPointsAPI() != null) {
                plugin.getPlayerPointsAPI().give(player.getUniqueId(), (int) convertedAmount);
                player.sendMessage(MessageUtils.colorize("&aExchanged &e" + fishCoinAmount + " FishCoins &afor &b" + (int) convertedAmount + " PlayerPoints&a!"));
            } else {
                player.sendMessage(MessageUtils.colorize("&cPlayerPoints is not loaded on this server."));
                FishCoinManager.addBalance(player, fishCoinAmount);
            }
        }
    }
}
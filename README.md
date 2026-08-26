<div align="center">
  
# 🎣 FishingAudition

**Advanced RPG-Style Fishing Minigame for Modern Minecraft Servers**

![Java](https://img.shields.io/badge/Java-21+-orange?style=for-the-badge&logo=java)
![PaperMC](https://img.shields.io/badge/PaperMC-1.21+-blue?style=for-the-badge&logo=papermc)
![Folia](https://img.shields.io/badge/Folia-26.2-success?style=for-the-badge)
![Economy](https://img.shields.io/badge/Economy-Vault_|_VaultX-yellow?style=for-the-badge)

</div>

## 1. HEADER & OVERVIEW
**FishingAudition** is a highly optimized, feature-rich fishing minigame plugin designed for high-performance servers. It transforms the mundane vanilla fishing mechanic into an interactive, audition-style rhythm minigame enriched with deep RPG progression systems. Built from the ground up for modern architectures, the plugin natively supports **Folia 26.2** multithreading, advanced SQLite/MySQL data management via HikariCP, and dynamic economy adapters for both legacy Vault and modern VaultX.

---

## 2. CORE FEATURES

- **RPG Progression:** Features an exponential leveling system configured in `config.yml` (e.g., base XP with a multiplier). Players progress to unlock rarity-gated fishing rods and employ tactical baits. Baits feature charge mechanics and unique buffs like `miss-penalty reduction` and `target-rarity`.
- **Dynamic GUI & Lore Builder:** Administrative burden is eliminated. The plugin dynamically parses rod and bait stats from `rods.yml` and `baits.yml`, injecting them directly into the item's lore at runtime (e.g., displaying exact charges left or specific extra time).
- **Automated Events:** Includes a built-in real-time event scheduler. Server owners can configure weekend events (e.g., 2.0x XP multipliers on SATURDAY and SUNDAY) that trigger automatically based on the server clock.
- **Scoreboard System:** A dynamic, asynchronous internal sidebar scoreboard powered by Bukkit's Scoreboard API. During the minigame, it tracks and displays real-time Level, XP, Coins, Top Round, and the currently equipped Rod without lagging the server.

---

## 3. FOLIA 26.2 & PLATFORM COMPATIBILITY

FishingAudition is meticulously engineered for the modern era of Minecraft server software.
- **Fully Folia Compatible:** Traditional plugins fail on Folia due to strict concurrent threading rules. FishingAudition utilizes a custom `SchedulerUtils` abstraction layer that safely routes tasks to Folia's `GlobalRegionScheduler`, `EntityScheduler`, or `AsyncScheduler` depending on the context. 
- **Zero Main-Thread Blocking:** Data saving, scoreboard updates, and database transactions are fully asynchronous, ensuring TPS remains flawless even with hundreds of concurrent players fishing in different regions.

---

## 4. TECHNICAL SHOWCASE

### A. Ultra-Optimized Event Listening (Fast-Fail)
The plugin strictly guards server performance. The `PlayerMoveEvent` is notoriously heavy, but FishingAudition implements a Fast-Fail mechanism that instantly ignores camera pitch/yaw movements in `O(1)` time, preventing unnecessary session lookups.

```java
@EventHandler
public void onPlayerMove(org.bukkit.event.player.PlayerMoveEvent event) {
    // Fast-Fail: Ignore pitch/yaw camera movements instantly
    if (event.getFrom().getBlockX() == event.getTo().getBlockX() && 
        event.getFrom().getBlockY() == event.getTo().getBlockY() && 
        event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
        return;
    }

    if (sessionManager.getActiveSessionsCount() == 0) return;

    Player player = event.getPlayer();
    if (sessionManager.isPlaying(player)) {
        AuditionSession session = sessionManager.getSession(player);
        if (session != null && session.getStartLocation() != null) {
            if (event.getTo() != null && event.getTo().distanceSquared(session.getStartLocation()) > 4.0) {
                session.endSession(false);
                sessionManager.removeSession(player);
                player.sendMessage(MessageUtils.getMessage("errors.session_ended_move", "&cSession ended because you moved!"));
            }
        }
    }
}
```

### B. Modern Economy Adapter (Smart Detection)
FishingAudition features a custom `EconomyManager` that safely bridges traditional synchronous `Vault` setups and modern asynchronous `VaultX` implementations using Reflection. It automatically detects `VaultAsyncEconomy` or `MultiCurrencyEconomy` to utilize async transactions where possible.

```java
// Inside EconomyManager.java Smart Router
private boolean isVaultX(Economy econ) {
    try {
        Class<?> asyncClass = Class.forName("net.milkbowl.vault.economy.VaultAsyncEconomy");
        if (asyncClass.isInstance(econ)) {
            return true;
        }
    } catch (ClassNotFoundException ignored) {}

    try {
        Class<?> multiClass = Class.forName("net.milkbowl.vault.economy.MultiCurrencyEconomy");
        if (multiClass.isInstance(econ)) {
            return true;
        }
    } catch (ClassNotFoundException ignored) {}

    return false;
}

// Router Logic
if (isVaultX(rawEconomy)) {
    provider = new VaultXHook(rawEconomy);
    Main.getInstance().getLogger().info("[EconomyManager] VaultX detected -> Using VaultXHook (Async + MultiCurrency)");
} else {
    provider = new VaultHook(rawEconomy);
    Main.getInstance().getLogger().info("[EconomyManager] Traditional Vault detected -> Using VaultHook (Sync)");
}
```

---

## 5. COMMANDS & PERMISSIONS

Base Command: `/fish` or `/fa`

### User Commands
Requires permission: `fishingaudition.user.*` (Default: true)
| Command | Description |
|---|---|
| `/fish menu` | Opens the main GUI menu. |
| `/fish shop` | Opens the dynamic Rod & Bait shop. |
| `/fish profile` | Views your personal RPG profile and equipped gear. |
| `/fish join` | Enters the audition fishing minigame. |
| `/fish leave` | Exits the minigame safely. |
| `/fish top` | Views the server-wide leaderboards. |

### Admin Commands
Requires permission: `fishingaudition.admin.*` (Default: op)
| Command | Description |
|---|---|
| `/fish adminevent` | Manually toggle or manage XP events. |
| `/fish setregion` | Set the active fishing region. |
| `/fish setjoin` | Set the minigame spawn/join location. |
| `/fish setleave` | Set the minigame exit/leave location. |
| `/fish reload` | Hot-reloads all `yml` configuration files. |

---

## 6. PLACEHOLDERS (PlaceholderAPI)

| Placeholder | Returns | Description |
|---|---|---|
| `%fa_coins%` | `100.00` | The player's current FishCoin balance formatted to 2 decimals. |
| `%fa_top_name_<pos>%` | `Meowchan12` | The name of the player at the specific leaderboard position for FishCoins. |
| `%fa_top_round_<pos>%` | `50` | The highest round achieved by the player at the specific leaderboard position. |

---

## 7. DATA & FILE MANAGEMENT

### Configuration Files
- `config.yml`: Core settings including RPG level multipliers, weekend events, GUI sizes, and exchange logic.
- `rods.yml` & `baits.yml`: Dynamic item databases defining gear stats (e.g., `miss-penalty-reduction`, `target-rarity`, `charges`).
- `messages.yml`: Comprehensive locale file for fully translating the plugin.

### Database Architecture
- Supports both **SQLite** (local flat-file) and **MySQL** (remote).
- Uses **HikariCP** for high-performance connection pooling.
- **Async Auto-Saving:** The `PlayerDataManager` utilizes a dedicated asynchronous ticking task to routinely batch-save player data (XP, Level, Equipped Bait, FishCoins) without interrupting the main server thread, guaranteeing robust data integrity even during crashes.

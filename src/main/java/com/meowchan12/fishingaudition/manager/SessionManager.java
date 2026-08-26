package com.meowchan12.fishingaudition.manager;

import com.meowchan12.fishingaudition.Main;
import com.meowchan12.fishingaudition.mechanics.AuditionSession;
import org.bukkit.entity.Player;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.UUID;

public class SessionManager {

    private final Main plugin;
    // Sử dụng UUID làm key thay vì Player object để tránh Memory Leak
    private final Map<UUID, AuditionSession> activeSessions = new ConcurrentHashMap<>();

    public SessionManager(Main plugin) {
        this.plugin = plugin;
    }

    /**
     * Bắt đầu một session mới cho người chơi.
     */
    public void startSession(Player player) {
        UUID uuid = player.getUniqueId();
        if (activeSessions.containsKey(uuid)) {
            activeSessions.get(uuid).endSession(false); // Kết thúc session cũ nếu có lỗi kẹt
        }

        AuditionSession session = new AuditionSession(plugin, player);
        activeSessions.put(uuid, session);
    }

    /**
     * Lấy session hiện tại của người chơi.
     */
    public AuditionSession getSession(Player player) {
        return activeSessions.get(player.getUniqueId());
    }

    /**
     * Kiểm tra xem người chơi có đang trong minigame không.
     */
    public boolean isPlaying(Player player) {
        return activeSessions.containsKey(player.getUniqueId());
    }

    /**
     * Lấy số lượng session đang hoạt động.
     */
    public int getActiveSessionsCount() {
        return activeSessions.size();
    }

    /**
     * Xóa session khỏi danh sách quản lý.
     */
    public void removeSession(Player player) {
        activeSessions.remove(player.getUniqueId());
    }

    /**
     * Dọn dẹp toàn bộ session khi server tắt để tránh lỗi BossBar kẹt trên màn hình.
     */
    public void endAllSessions() {
        for (AuditionSession session : activeSessions.values()) {
            session.endSession(false);
        }
        activeSessions.clear();
    }
}
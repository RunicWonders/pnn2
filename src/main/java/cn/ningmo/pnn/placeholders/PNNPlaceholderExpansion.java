package cn.ningmo.pnn.placeholders;

import cn.ningmo.pnn.PNN;
import cn.ningmo.pnn.managers.NicknameManager;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

/**
 * 此类将自动注册占位符到PlaceholderAPI
 * 支持的占位符:
 * %pnn_nickname% - 玩家的昵称
 * %pnn_has_nickname% - 玩家是否有昵称
 * %pnn_real_name% - 玩家的真实名称
 */
public class PNNPlaceholderExpansion extends PlaceholderExpansion {

    private final PNN plugin;
    private final NicknameManager nicknameManager;

    public PNNPlaceholderExpansion(PNN plugin) {
        this.plugin = plugin;
        this.nicknameManager = plugin.getNicknameManager();
    }

    @Override
    public String getIdentifier() {
        return "pnn";
    }

    @Override
    public String getAuthor() {
        return "柠枺";
    }

    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }
    
    @Override
    public boolean persist() {
        return true; // 重启服务器时不需要重新注册
    }

    @Override
    public String onRequest(OfflinePlayer player, String params) {
        if (player == null) {
            return "";
        }
        
        // 获取玩家对象
        Player onlinePlayer = player.getPlayer();
        
        // 处理离线玩家的情况
        if (onlinePlayer == null) {
            // 离线玩家仅支持少数占位符
            if (params.equals("real_name")) {
                return player.getName();
            }
            return "";
        }
        
        // 处理在线玩家的占位符
        switch (params.toLowerCase()) {
            case "nickname":
                // 使用配置的格式返回玩家的昵称
                return nicknameManager.formatPNNNickname(onlinePlayer);
                
            case "has_nickname":
                // 返回玩家是否有昵称
                String nickname = nicknameManager.getRawNickname(onlinePlayer);
                return (nickname.equals(onlinePlayer.getName())) ? "false" : "true";
                
            case "real_name":
                // 返回玩家的真实名称
                return onlinePlayer.getName();
        }
        
        return null; // 不支持的占位符返回null
    }
} 
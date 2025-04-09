package cn.ningmo.pnn.listeners;

import cn.ningmo.pnn.PNN;
import cn.ningmo.pnn.managers.NicknameManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {

    private final PNN plugin;
    private final NicknameManager nicknameManager;
    private boolean placeholderEnabled;
    private boolean overrideChatFormat;
    private String joinMessage;
    private String quitMessage;
    
    public PlayerListener(PNN plugin) {
        this.plugin = plugin;
        this.nicknameManager = plugin.getNicknameManager();
        loadConfig();
    }
    
    /**
     * 从配置文件加载相关配置
     */
    public void loadConfig() {
        this.placeholderEnabled = plugin.getConfig().getBoolean("placeholder", false);
        this.overrideChatFormat = plugin.getConfig().getBoolean("override-chat-format", false);
        this.joinMessage = plugin.getConfig().getString("chat-format.join-message", "&e%pnn% 加入了游戏");
        this.quitMessage = plugin.getConfig().getString("chat-format.quit-message", "&e%pnn% 离开了游戏");
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String nickname = nicknameManager.getNickname(player);
        
        // 只有当启用了覆盖模式，或者玩家有昵称且聊天中包含昵称，才设置显示名和Tab名
        if (overrideChatFormat) {
            // 覆盖模式下，总是设置显示名称和Tab列表名称
            if (!nickname.equals(player.getName())) {
                setPlayerDisplayAndTabName(player, nickname);
            }
        } else {
            // 非覆盖模式下，只设置显示名称，不设置Tab列表名称
            if (!nickname.equals(player.getName())) {
                player.setDisplayName(nickname);
            }
        }
        
        // 只有当启用了自定义聊天格式且配置了加入消息时，才完全覆盖加入消息
        if (overrideChatFormat && joinMessage != null && !joinMessage.isEmpty()) {
            String customJoinMessage = joinMessage
                    .replace("%pnn%", nickname)
                    .replace("%player%", player.getName())
                    .replace("%world%", player.getWorld().getName());
            
            // 如果启用了PlaceholderAPI，处理其他占位符
            if (placeholderEnabled && Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                try {
                    customJoinMessage = me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, customJoinMessage);
                } catch (Exception e) {
                    plugin.getLogger().warning("处理加入消息中的占位符时出错: " + e.getMessage());
                }
            }
            
            // 转换颜色代码并设置加入消息
            event.setJoinMessage(ChatColor.translateAlternateColorCodes('&', customJoinMessage));
        }
        // 使用默认格式但替换玩家名为昵称 - 仅当有昵称时才替换
        else if (!nickname.equals(player.getName()) && event.getJoinMessage() != null) {
            String joinMsg = event.getJoinMessage();
            
            // 检查是否包含pnn占位符
            if (joinMsg.contains("%pnn%")) {
                joinMsg = joinMsg.replace("%pnn%", nickname);
            } else {
                joinMsg = joinMsg.replace(player.getName(), nickname);
            }
            
            event.setJoinMessage(joinMsg);
        }
        // 如果有pnn占位符，替换为玩家真实名称
        else if (event.getJoinMessage() != null && event.getJoinMessage().contains("%pnn%")) {
            String joinMsg = event.getJoinMessage();
            joinMsg = joinMsg.replace("%pnn%", player.getName());
            event.setJoinMessage(joinMsg);
        }
        // 其他情况，不修改原始加入消息
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        String nickname = nicknameManager.getNickname(player);
        
        // 只有当启用了自定义聊天格式且配置了退出消息时，才完全覆盖退出消息
        if (overrideChatFormat && quitMessage != null && !quitMessage.isEmpty()) {
            String customQuitMessage = quitMessage
                    .replace("%pnn%", nickname)
                    .replace("%player%", player.getName())
                    .replace("%world%", player.getWorld().getName());
            
            // 如果启用了PlaceholderAPI，处理其他占位符
            if (placeholderEnabled && Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                try {
                    customQuitMessage = me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, customQuitMessage);
                } catch (Exception e) {
                    plugin.getLogger().warning("处理退出消息中的占位符时出错: " + e.getMessage());
                }
            }
            
            // 转换颜色代码并设置退出消息
            event.setQuitMessage(ChatColor.translateAlternateColorCodes('&', customQuitMessage));
        }
        // 使用默认格式但替换玩家名为昵称 - 仅当有昵称时才替换
        else if (!nickname.equals(player.getName()) && event.getQuitMessage() != null) {
            String quitMsg = event.getQuitMessage();
            
            // 检查是否包含pnn占位符
            if (quitMsg.contains("%pnn%")) {
                quitMsg = quitMsg.replace("%pnn%", nickname);
            } else {
                quitMsg = quitMsg.replace(player.getName(), nickname);
            }
            
            event.setQuitMessage(quitMsg);
        }
        // 如果有pnn占位符，替换为玩家真实名称
        else if (event.getQuitMessage() != null && event.getQuitMessage().contains("%pnn%")) {
            String quitMsg = event.getQuitMessage();
            quitMsg = quitMsg.replace("%pnn%", player.getName());
            event.setQuitMessage(quitMsg);
        }
        // 其他情况，不修改原始退出消息
    }
    
    /**
     * 设置玩家的显示名称和Tab列表名称
     */
    private void setPlayerDisplayAndTabName(Player player, String nickname) {
        player.setDisplayName(nickname);
        try {
            player.setPlayerListName(nickname);
        } catch (Exception e) {
            plugin.getLogger().warning("无法设置玩家" + player.getName() + "的Tab列表名称：" + e.getMessage());
        }
    }
    
    /**
     * 重新加载配置
     */
    public void reloadConfig() {
        loadConfig();
    }
} 
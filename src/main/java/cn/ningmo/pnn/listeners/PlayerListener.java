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
    private boolean overrideTabFormat;
    private boolean setDisplayName;
    private String joinMessage;
    private String quitMessage;
    private String tabFormat;
    
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
        this.overrideTabFormat = plugin.getConfig().getBoolean("override-tab-format", false);
        this.setDisplayName = plugin.getConfig().getBoolean("set-display-name", true);
        this.joinMessage = plugin.getConfig().getString("chat-format.join-message", "&e%pnn% 加入了游戏");
        this.quitMessage = plugin.getConfig().getString("chat-format.quit-message", "&e%pnn% 离开了游戏");
        this.tabFormat = plugin.getConfig().getString("tab-format", "&7[&r%pnn%&7] %player%");
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String nickname = nicknameManager.getNickname(player);
        
        // 只在玩家有昵称且昵称不等于玩家名时更新显示
        if (!nickname.equals(player.getName())) {
            // 设置玩家显示名称
            if (setDisplayName || overrideChatFormat) {
                player.setDisplayName(nickname);
            }
            
            // 单独控制是否覆盖TAB列表
            if (overrideTabFormat) {
                setPlayerTabName(player, nickname);
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
     * 只设置玩家的Tab列表名称
     */
    private void setPlayerTabName(Player player, String nickname) {
        String formattedTabName = tabFormat
                .replace("%pnn%", nickname)
                .replace("%player%", player.getName());
        
        // 如果启用了PlaceholderAPI，处理其他占位符
        if (placeholderEnabled && Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            try {
                formattedTabName = me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, formattedTabName);
            } catch (Exception e) {
                plugin.getLogger().warning("处理玩家" + player.getName() + "的TAB列表名称占位符时出错: " + e.getMessage());
            }
        }
        
        // 转换颜色代码
        formattedTabName = ChatColor.translateAlternateColorCodes('&', formattedTabName);
        
        try {
            player.setPlayerListName(formattedTabName);
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
package cn.ningmo.pnn.listeners;

import cn.ningmo.pnn.PNN;
import cn.ningmo.pnn.managers.NicknameManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatListener implements Listener {

    private final PNN plugin;
    private final NicknameManager nicknameManager;
    private boolean placeholderEnabled;
    private boolean overrideChatFormat;
    private String chatFormat;
    private boolean colorPermission;
    
    public ChatListener(PNN plugin) {
        this.plugin = plugin;
        this.nicknameManager = plugin.getNicknameManager();
        loadConfig();
    }
    
    /**
     * 从配置文件加载聊天相关配置
     */
    public void loadConfig() {
        this.placeholderEnabled = plugin.getConfig().getBoolean("placeholder", false);
        this.overrideChatFormat = plugin.getConfig().getBoolean("override-chat-format", false);
        this.chatFormat = plugin.getConfig().getString("chat-format.format", "&7[&r%pnn%&7] &f%msg%");
        this.colorPermission = plugin.getConfig().getBoolean("chat-format.color-permission", true);
    }
    
    /**
     * 处理玩家聊天事件
     * 使用NORMAL优先级，避免过度干扰其他插件的聊天处理
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String nickname = nicknameManager.getNickname(player);
        
        // 如果启用了自定义聊天格式 - 完全接管聊天格式
        if (overrideChatFormat) {
            String format = chatFormat;
            
            // 替换玩家相关占位符
            format = format.replace("%pnn%", nickname);
            format = format.replace("%player%", player.getName());
            format = format.replace("%msg%", "%2$s"); // 保留消息占位符供Bukkit处理
            format = format.replace("%world%", player.getWorld().getName());
            
            // 如果启用了PlaceholderAPI，处理其他占位符
            if (placeholderEnabled && Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                try {
                    format = me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, format);
                } catch (Exception e) {
                    plugin.getLogger().warning("处理聊天格式中的占位符时出错: " + e.getMessage());
                }
            }
            
            // 转换颜色代码
            format = ChatColor.translateAlternateColorCodes('&', format);
            
            // 设置聊天格式
            event.setFormat(format);
            
            // 处理聊天消息中的颜色代码权限
            String message = event.getMessage();
            if (message.contains("&") && (!colorPermission || player.hasPermission("pnn.chatcolor"))) {
                message = ChatColor.translateAlternateColorCodes('&', message);
                event.setMessage(message);
            }
        } 
        // 非覆盖模式，只有在有昵称不等于玩家名时，才 "轻微干预"
        else if (!nickname.equals(player.getName())) {
            // 默认情况下，只通过DisplayName来影响聊天显示，不直接修改聊天格式
            // 什么都不做，依赖DisplayName机制
            // 注意：大多数聊天格式使用玩家的DisplayName，而不是直接从事件格式中获取
        }
        
        // 不再直接修改事件的格式，避免覆盖其他插件的聊天格式
    }
} 
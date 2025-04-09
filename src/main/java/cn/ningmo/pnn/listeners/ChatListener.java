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
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String nickname = nicknameManager.getNickname(player);
        
        // 如果启用了自定义聊天格式
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
        // 使用默认格式但替换玩家名为昵称
        else if (!nickname.equals(player.getName())) {
            String format = event.getFormat();
            
            // 检查格式中是否包含pnn占位符
            if (format.contains("%pnn%")) {
                // 直接替换pnn占位符为昵称
                format = format.replace("%pnn%", nickname);
            } else {
                // 保持格式不变，只替换玩家名
                format = format.replace("%1$s", nickname);
            }
            
            event.setFormat(format);
            
            // 在非覆盖模式下，不处理消息内容中的颜色代码
            // 这样可以让服务器自己的聊天插件处理颜色代码
        } 
        // 默认格式但有pnn占位符
        else if (event.getFormat().contains("%pnn%")) {
            String format = event.getFormat();
            format = format.replace("%pnn%", player.getName());
            event.setFormat(format);
        }
        // 其他情况下，保持原始格式和消息不变
    }
} 
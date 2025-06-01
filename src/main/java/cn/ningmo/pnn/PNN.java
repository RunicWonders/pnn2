package cn.ningmo.pnn;

import cn.ningmo.pnn.commands.PNNCommand;
import cn.ningmo.pnn.listeners.ChatListener;
import cn.ningmo.pnn.listeners.PlayerListener;
import cn.ningmo.pnn.managers.EconomyManager;
import cn.ningmo.pnn.managers.NicknameManager;
import cn.ningmo.pnn.placeholders.PNNPlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class PNN extends JavaPlugin {
    
    private static PNN instance;
    private NicknameManager nicknameManager;
    private EconomyManager economyManager;
    private PNNPlaceholderExpansion placeholderExpansion;
    private ChatListener chatListener;
    private PlayerListener playerListener;
    
    @Override
    public void onEnable() {
        instance = this;
        
        // 保存默认配置
        saveDefaultConfig();
        
        // 初始化管理器
        nicknameManager = new NicknameManager(this);
        economyManager = new EconomyManager(this);
        
        // 注册命令
        getCommand("pnn").setExecutor(new PNNCommand(this));
        
        // 注册监听器
        chatListener = new ChatListener(this);
        playerListener = new PlayerListener(this);
        Bukkit.getPluginManager().registerEvents(chatListener, this);
        Bukkit.getPluginManager().registerEvents(playerListener, this);
        
        // 如果PlaceholderAPI存在，注册我们的占位符
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            getLogger().info("已检测到PlaceholderAPI，正在注册占位符...");
            placeholderExpansion = new PNNPlaceholderExpansion(this);
            if (placeholderExpansion.register()) {
                getLogger().info("PNN占位符注册成功！可用占位符: %pnn_nickname%, %pnn_has_nickname%, %pnn_real_name%");
            } else {
                getLogger().warning("PNN占位符注册失败！");
            }
        }
        
        getLogger().info("PNN插件已启用！作者: 柠枺");
    }
    
    @Override
    public void onDisable() {
        // 保存所有数据
        if (nicknameManager != null) {
            nicknameManager.saveAllNicknames();
        }
        
        // 注销占位符
        if (placeholderExpansion != null) {
            placeholderExpansion.unregister();
        }
        
        getLogger().info("PNN插件已禁用！");
    }
    
    @Override
    public void reloadConfig() {
        super.reloadConfig();
        
        // 重载各组件的配置
        if (nicknameManager != null) {
            nicknameManager.saveAllNicknames();
            nicknameManager.reloadConfig();
        }
        
        if (economyManager != null) {
            economyManager.reloadConfig();
        }
        
        if (chatListener != null) {
            chatListener.loadConfig();
        }
        
        if (playerListener != null) {
            playerListener.reloadConfig();
        }
        
        getLogger().info("PNN插件配置已重载！");
    }
    
    public static PNN getInstance() {
        return instance;
    }
    
    public NicknameManager getNicknameManager() {
        return nicknameManager;
    }
    
    public EconomyManager getEconomyManager() {
        return economyManager;
    }
} 
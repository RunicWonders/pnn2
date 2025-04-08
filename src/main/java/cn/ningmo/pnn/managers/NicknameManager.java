package cn.ningmo.pnn.managers;

import cn.ningmo.pnn.PNN;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.Bukkit;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

public class NicknameManager {
    
    private final PNN plugin;
    private final Map<UUID, String> nicknames = new HashMap<>();
    private final Map<String, UUID> nicknameToUUID = new HashMap<>();
    private File nicknamesFile;
    private FileConfiguration nicknamesConfig;
    
    // 配置选项
    private boolean placeholderEnabled;
    private int maxNicknameLength;
    private boolean allowColors;
    private boolean allowFormats;
    
    public NicknameManager(PNN plugin) {
        this.plugin = plugin;
        loadConfig();
        setupFiles();
        loadNicknames();
    }
    
    /**
     * 从配置文件加载设置
     */
    private void loadConfig() {
        this.placeholderEnabled = plugin.getConfig().getBoolean("placeholder", false);
        this.maxNicknameLength = plugin.getConfig().getInt("max-nickname-length", 32);
        this.allowColors = plugin.getConfig().getBoolean("allow-colors", true);
        this.allowFormats = plugin.getConfig().getBoolean("allow-formats", true);
    }
    
    private void setupFiles() {
        nicknamesFile = new File(plugin.getDataFolder(), "nicknames.yml");
        
        if (!nicknamesFile.exists()) {
            try {
                if (!nicknamesFile.createNewFile()) {
                    plugin.getLogger().severe("无法创建nicknames.yml文件！");
                }
            } catch (IOException e) {
                plugin.getLogger().severe("创建nicknames.yml文件时出错：" + e.getMessage());
            }
        }
        
        nicknamesConfig = YamlConfiguration.loadConfiguration(nicknamesFile);
    }
    
    private void loadNicknames() {
        nicknames.clear();
        nicknameToUUID.clear();
        
        if (nicknamesConfig.contains("nicknames")) {
            for (String uuidStr : nicknamesConfig.getConfigurationSection("nicknames").getKeys(false)) {
                UUID uuid = UUID.fromString(uuidStr);
                String nickname = nicknamesConfig.getString("nicknames." + uuidStr);
                nicknames.put(uuid, nickname);
                nicknameToUUID.put(nickname.toLowerCase(), uuid);
            }
        }
    }
    
    public void saveAllNicknames() {
        nicknamesConfig.set("nicknames", null);
        
        for (Map.Entry<UUID, String> entry : nicknames.entrySet()) {
            nicknamesConfig.set("nicknames." + entry.getKey().toString(), entry.getValue());
        }
        
        try {
            nicknamesConfig.save(nicknamesFile);
        } catch (IOException e) {
            plugin.getLogger().severe("保存nicknames.yml文件时出错：" + e.getMessage());
        }
    }
    
    public boolean setNickname(Player player, String nickname) {
        // 检查昵称长度
        if (nickname.length() > maxNicknameLength) {
            return false;
        }
        
        // 检查是否包含屏蔽词
        if (containsBlockedWord(nickname)) {
            return false;
        }
        
        // 处理颜色和格式代码
        String processedNickname = nickname;
        
        // 如果允许颜色代码
        if (allowColors && nickname.contains("&")) {
            // 移除不允许的格式代码
            if (!allowFormats) {
                processedNickname = removeFormatCodes(processedNickname);
            }
            // 翻译颜色代码
            processedNickname = ChatColor.translateAlternateColorCodes('&', processedNickname);
        } else if (nickname.contains("&")) {
            // 如果不允许颜色代码，移除所有颜色代码
            processedNickname = removeAllColorCodes(processedNickname);
        }
        
        // 如果有其他玩家已经使用了这个昵称，返回失败
        UUID existingUUID = nicknameToUUID.get(processedNickname.toLowerCase());
        if (existingUUID != null && !existingUUID.equals(player.getUniqueId())) {
            return false;
        }
        
        // 如果玩家已经有昵称，先从映射中移除
        String oldNickname = nicknames.get(player.getUniqueId());
        if (oldNickname != null) {
            nicknameToUUID.remove(oldNickname.toLowerCase());
        }
        
        // 设置新昵称
        nicknames.put(player.getUniqueId(), processedNickname);
        nicknameToUUID.put(processedNickname.toLowerCase(), player.getUniqueId());
        
        // 保存到配置
        nicknamesConfig.set("nicknames." + player.getUniqueId().toString(), processedNickname);
        try {
            nicknamesConfig.save(nicknamesFile);
        } catch (IOException e) {
            plugin.getLogger().severe("保存nickname时出错：" + e.getMessage());
        }
        
        return true;
    }
    
    /**
     * 移除所有颜色和格式代码
     */
    private String removeAllColorCodes(String text) {
        return text.replaceAll("&[0-9a-fk-orA-FK-OR]", "");
    }
    
    /**
     * 仅保留颜色代码，移除格式代码
     */
    private String removeFormatCodes(String text) {
        return text.replaceAll("&[k-orK-OR]", "");
    }
    
    public boolean removeNickname(Player player) {
        String nickname = nicknames.get(player.getUniqueId());
        if (nickname != null) {
            nicknames.remove(player.getUniqueId());
            nicknameToUUID.remove(nickname.toLowerCase());
            
            nicknamesConfig.set("nicknames." + player.getUniqueId().toString(), null);
            try {
                nicknamesConfig.save(nicknamesFile);
            } catch (IOException e) {
                plugin.getLogger().severe("移除nickname时出错：" + e.getMessage());
            }
            
            return true;
        }
        return false;
    }
    
    public String getNickname(Player player) {
        String nickname = nicknames.getOrDefault(player.getUniqueId(), player.getName());
        
        // 如果启用了占位符支持，并且安装了PlaceholderAPI
        if (placeholderEnabled && Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            try {
                // 使用PlaceholderAPI解析昵称中的占位符
                nickname = me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, nickname);
            } catch (Exception e) {
                plugin.getLogger().warning("处理玩家" + player.getName() + "的昵称占位符时出错: " + e.getMessage());
            }
        }
        
        return nickname;
    }
    
    public String getRawNickname(Player player) {
        return nicknames.getOrDefault(player.getUniqueId(), player.getName());
    }
    
    public Player getPlayerByNickname(String nickname) {
        UUID uuid = nicknameToUUID.get(nickname.toLowerCase());
        if (uuid != null) {
            return plugin.getServer().getPlayer(uuid);
        }
        return null;
    }
    
    public Map<UUID, String> getAllNicknames() {
        return new HashMap<>(nicknames);
    }
    
    private boolean containsBlockedWord(String nickname) {
        List<String> blockedWords = plugin.getConfig().getStringList("Block-words");
        String lowerNickname = nickname.toLowerCase();
        
        for (String word : blockedWords) {
            if (lowerNickname.contains(word.toLowerCase())) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 重新加载配置
     */
    public void reloadConfig() {
        loadConfig();
    }
} 
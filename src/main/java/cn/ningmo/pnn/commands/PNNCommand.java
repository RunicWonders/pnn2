package cn.ningmo.pnn.commands;

import cn.ningmo.pnn.PNN;
import cn.ningmo.pnn.managers.NicknameManager;
import cn.ningmo.pnn.managers.EconomyManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class PNNCommand implements CommandExecutor, TabCompleter {
    
    private final PNN plugin;
    private final NicknameManager nicknameManager;
    
    // 命令别名映射，用于支持多种命令输入方式
    private final Map<String, String> commandAliases = new HashMap<>();
    
    public PNNCommand(PNN plugin) {
        this.plugin = plugin;
        this.nicknameManager = plugin.getNicknameManager();
        
        // 初始化命令别名
        initCommandAliases();
    }
    
    /**
     * 初始化命令别名映射
     */
    private void initCommandAliases() {
        // 设置昵称命令别名
        commandAliases.put("s", "set");
        commandAliases.put("设置", "set");
        
        // 重置昵称命令别名
        commandAliases.put("r", "reset");
        commandAliases.put("重置", "reset");
        
        // 查询昵称命令别名
        commandAliases.put("g", "get");
        commandAliases.put("查询", "get");
        
        // 列表命令别名
        commandAliases.put("l", "list");
        commandAliases.put("列表", "list");
        
        // 重载命令别名
        commandAliases.put("重载", "reload");
        
        // 管理员命令别名
        commandAliases.put("a", "admin");
        commandAliases.put("管理", "admin");
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // 检查参数长度
        if (args.length == 0) {
            sendHelpMessage(sender);
            return true;
        }
        
        // 获取子命令并解析别名
        String subCommand = args[0].toLowerCase();
        if (commandAliases.containsKey(subCommand)) {
            subCommand = commandAliases.get(subCommand);
        }
        
        // 执行对应的子命令
        switch (subCommand) {
            case "set":
                return handleSetCommand(sender, args);
            case "reset":
                return handleResetCommand(sender);
            case "get":
                return handleGetCommand(sender, args);
            case "list":
                return handleListCommand(sender, args);
            case "reload":
                return handleReloadCommand(sender);
            case "admin":
                return handleAdminCommand(sender, args);
            case "help":
                sendHelpMessage(sender);
                return true;
            default:
                sender.sendMessage(ChatColor.RED + "未知命令。使用 /pnn help 查看可用命令。");
                return true;
        }
    }
    
    /**
     * 处理设置昵称命令
     */
    private boolean handleSetCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "只有玩家才能使用此命令！");
            return true;
        }

        Player player = (Player) sender;
        if (!player.hasPermission("pnn.set")) {
            player.sendMessage(ChatColor.RED + "你没有权限设置昵称！");
            return true;
        }

        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "用法: /pnn set <昵称>");
            return true;
        }

        String nickname = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        
        // 检查经济系统
        EconomyManager economyManager = plugin.getEconomyManager();
        if (economyManager.isEnabled()) {
            double cost = economyManager.getSetNicknameCost();
            if (!economyManager.hasEnoughMoney(player, cost)) {
                player.sendMessage(ChatColor.RED + "你没有足够的金钱！需要: " + 
                    economyManager.formatMoney(cost));
                return true;
            }
        }

        if (nicknameManager.setNickname(player, nickname)) {
            // 扣除费用
            if (economyManager.isEnabled()) {
                double cost = economyManager.getSetNicknameCost();
                if (economyManager.withdrawMoney(player, cost)) {
                    player.sendMessage(ChatColor.GREEN + "成功设置昵称为: " + ChatColor.RESET + nickname);
                    player.sendMessage(ChatColor.GRAY + "花费: " + economyManager.formatMoney(cost));
                } else {
                    player.sendMessage(ChatColor.RED + "扣除费用失败，昵称设置已取消！");
                    nicknameManager.removeNickname(player);
                    return true;
                }
            } else {
                player.sendMessage(ChatColor.GREEN + "成功设置昵称为: " + ChatColor.RESET + nickname);
            }
            
            // 更新玩家显示名称
            updatePlayerDisplayNames(player);
        } else {
            player.sendMessage(ChatColor.RED + "设置昵称失败！可能原因：昵称已被使用、包含屏蔽词或长度超出限制。");
        }

        return true;
    }
    
    /**
     * 处理重置昵称命令
     */
    private boolean handleResetCommand(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "只有玩家才能使用此命令！");
            return true;
        }

        Player player = (Player) sender;
        if (!player.hasPermission("pnn.reset")) {
            player.sendMessage(ChatColor.RED + "你没有权限重置昵称！");
            return true;
        }

        // 检查经济系统
        EconomyManager economyManager = plugin.getEconomyManager();
        if (economyManager.isEnabled()) {
            double cost = economyManager.getResetNicknameCost();
            if (!economyManager.hasEnoughMoney(player, cost)) {
                player.sendMessage(ChatColor.RED + "你没有足够的金钱！需要: " + 
                    economyManager.formatMoney(cost));
                return true;
            }
        }

        String oldNickname = nicknameManager.getRawNickname(player);
        if (nicknameManager.removeNickname(player)) {
            // 扣除费用
            if (economyManager.isEnabled()) {
                double cost = economyManager.getResetNicknameCost();
                if (economyManager.withdrawMoney(player, cost)) {
                    player.sendMessage(ChatColor.GREEN + "成功重置昵称！");
                    player.sendMessage(ChatColor.GRAY + "花费: " + economyManager.formatMoney(cost));
                    
                    // 如果启用了返还功能，返还部分设置费用
                    if (economyManager.isRefundOnReset() && !oldNickname.equals(player.getName())) {
                        double refund = economyManager.getSetNicknameCost() * economyManager.getRefundRatio();
                        if (economyManager.depositMoney(player, refund)) {
                            player.sendMessage(ChatColor.GRAY + "返还设置费用: " + 
                                economyManager.formatMoney(refund));
                        }
                    }
                } else {
                    player.sendMessage(ChatColor.RED + "扣除费用失败，昵称重置已取消！");
                    return true;
                }
            } else {
                player.sendMessage(ChatColor.GREEN + "成功重置昵称！");
            }
            
            // 更新玩家显示名称
            updatePlayerDisplayNames(player);
        } else {
            player.sendMessage(ChatColor.RED + "重置昵称失败！你可能没有设置昵称。");
        }

        return true;
    }
    
    /**
     * 处理查询昵称命令
     */
    private boolean handleGetCommand(CommandSender sender, String[] args) {
        // 检查权限
        if (!sender.hasPermission("pnn.get")) {
            sender.sendMessage(ChatColor.RED + "你没有权限查询昵称！");
            return true;
        }
        
        // 如果没有指定昵称，显示所有玩家的昵称
        if (args.length < 2) {
            sender.sendMessage(ChatColor.YELLOW + "提示：使用 /pnn list 查看所有玩家昵称，或 /pnn get <昵称> 查询特定昵称对应的玩家。");
            return true;
        }
        
        // 获取昵称参数
        String nickname = args[1];
        for (int i = 2; i < args.length; i++) {
            nickname += " " + args[i];
        }
        
        // 查询玩家
        Player target = nicknameManager.getPlayerByNickname(nickname);
        
        if (target != null) {
            sender.sendMessage(ChatColor.GREEN + "昵称 " + ChatColor.RESET + nickname + ChatColor.GREEN + " 对应的玩家是：" + ChatColor.YELLOW + target.getName());
        } else {
            sender.sendMessage(ChatColor.RED + "找不到使用该昵称的玩家！");
        }
        
        return true;
    }
    
    /**
     * 处理列表命令
     */
    private boolean handleListCommand(CommandSender sender, String[] args) {
        // 检查权限
        if (!sender.hasPermission("pnn.list")) {
            sender.sendMessage(ChatColor.RED + "你没有权限查看昵称列表！");
            return true;
        }
        
        Map<UUID, String> allNicknames = nicknameManager.getAllNicknames();
        
        if (allNicknames.isEmpty()) {
            sender.sendMessage(ChatColor.YELLOW + "目前没有玩家设置昵称。");
            return true;
        }
        
        // 准备分页显示
        int pageSize = 10; // 每页显示条目数
        int totalPages = (allNicknames.size() + pageSize - 1) / pageSize; // 计算总页数
        int page = 1; // 默认显示第一页
        
        // 如果指定了页码参数，解析页码
        if (args.length > 1) {
            try {
                page = Integer.parseInt(args[1]);
                if (page < 1) page = 1;
                if (page > totalPages) page = totalPages;
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "无效的页码！使用 /pnn list [页码] 查看指定页的昵称列表。");
                return true;
            }
        }
        
        // 构建显示内容
        List<Map.Entry<UUID, String>> entries = new ArrayList<>(allNicknames.entrySet());
        int startIndex = (page - 1) * pageSize;
        int endIndex = Math.min(startIndex + pageSize, entries.size());
        
        sender.sendMessage(ChatColor.GREEN + "===== 玩家昵称列表 (" + page + "/" + totalPages + ") =====");
        
        for (int i = startIndex; i < endIndex; i++) {
            Map.Entry<UUID, String> entry = entries.get(i);
            String playerName = plugin.getServer().getOfflinePlayer(entry.getKey()).getName();
            if (playerName != null) {
                sender.sendMessage(ChatColor.YELLOW + playerName + ChatColor.WHITE + " → " + ChatColor.RESET + entry.getValue());
            }
        }
        
        // 如果有多页，显示翻页提示
        if (totalPages > 1) {
            sender.sendMessage(ChatColor.GRAY + "使用 /pnn list <页码> 查看更多昵称。");
        }
        
        return true;
    }
    
    /**
     * 处理重载命令
     */
    private boolean handleReloadCommand(CommandSender sender) {
        // 检查权限
        if (!sender.hasPermission("pnn.reload")) {
            sender.sendMessage(ChatColor.RED + "你没有权限重载插件！");
            return true;
        }
        
        // 重载插件配置
        plugin.reloadConfig();
        sender.sendMessage(ChatColor.GREEN + "PNN插件配置已重载！");
        
        return true;
    }
    
    /**
     * 处理管理员命令
     */
    private boolean handleAdminCommand(CommandSender sender, String[] args) {
        // 检查权限
        if (!sender.hasPermission("pnn.admin")) {
            sender.sendMessage(ChatColor.RED + "你没有权限使用管理员命令！");
            return true;
        }
        
        // 检查参数数量
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "用法: /pnn admin <set|reset> <玩家> [昵称]");
            return true;
        }
        
        String adminSubCommand = args[1].toLowerCase();
        
        // 检查是否提供了玩家参数
        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "请指定玩家名称！");
            return true;
        }
        
        // 获取目标玩家
        String targetName = args[2];
        Player targetPlayer = Bukkit.getPlayer(targetName);
        
        if (targetPlayer == null) {
            // 尝试通过昵称查找玩家
            for (Player online : Bukkit.getOnlinePlayers()) {
                if (nicknameManager.getNickname(online).equalsIgnoreCase(targetName)) {
                    targetPlayer = online;
                    break;
                }
            }
            
            // 如果仍未找到玩家
            if (targetPlayer == null) {
                sender.sendMessage(ChatColor.RED + "找不到玩家 " + targetName + "！请确保玩家在线。");
                return true;
            }
        }
        
        // 根据子命令执行相应操作
        switch (adminSubCommand) {
            case "set":
                if (args.length < 4) {
                    sender.sendMessage(ChatColor.RED + "请指定要设置的昵称！");
                    return true;
                }
                
                // 获取昵称
                String nickname = args[3];
                for (int i = 4; i < args.length; i++) {
                    nickname += " " + args[i];
                }
                
                // 设置昵称
                boolean setSuccess = nicknameManager.setNickname(targetPlayer, nickname);
                
                if (setSuccess) {
                    sender.sendMessage(ChatColor.GREEN + "成功为玩家 " + targetPlayer.getName() + " 设置昵称：" + ChatColor.RESET + nicknameManager.getNickname(targetPlayer));
                    targetPlayer.sendMessage(ChatColor.GREEN + "管理员为你设置了新昵称：" + ChatColor.RESET + nicknameManager.getNickname(targetPlayer));
                    
                    // 更新玩家显示名称和Tab名称
                    updatePlayerDisplayNames(targetPlayer);
                } else {
                    sender.sendMessage(ChatColor.RED + "为玩家 " + targetPlayer.getName() + " 设置昵称失败！请检查昵称是否符合要求。");
                }
                break;
                
            case "reset":
                // 重置昵称
                boolean resetSuccess = nicknameManager.removeNickname(targetPlayer);
                
                if (resetSuccess) {
                    sender.sendMessage(ChatColor.GREEN + "成功重置玩家 " + targetPlayer.getName() + " 的昵称。");
                    targetPlayer.sendMessage(ChatColor.YELLOW + "管理员重置了你的昵称。");
                    
                    boolean setDisplayName = plugin.getConfig().getBoolean("set-display-name", true);
                    boolean overrideChatFormat = plugin.getConfig().getBoolean("override-chat-format", false);
                    boolean overrideTabFormat = plugin.getConfig().getBoolean("override-tab-format", false);
                    
                    // 根据配置决定是否重置显示名称
                    if (setDisplayName || overrideChatFormat) {
                        targetPlayer.setDisplayName(targetPlayer.getName());
                    }
                    
                    // 根据配置决定是否重置TAB列表名称
                    if (overrideTabFormat) {
                        try {
                            targetPlayer.setPlayerListName(targetPlayer.getName());
                        } catch (Exception e) {
                            plugin.getLogger().warning("无法重置玩家" + targetPlayer.getName() + "的Tab列表名称：" + e.getMessage());
                        }
                    }
                } else {
                    sender.sendMessage(ChatColor.RED + "玩家 " + targetPlayer.getName() + " 没有设置昵称！");
                }
                break;
                
            default:
                sender.sendMessage(ChatColor.RED + "未知的管理员子命令：" + adminSubCommand);
                sender.sendMessage(ChatColor.RED + "可用子命令：set, reset");
                return true;
        }
        
        return true;
    }
    
    /**
     * 更新玩家的显示名称和Tab列表名称
     */
    private void updatePlayerDisplayNames(Player player) {
        String nickname = nicknameManager.getNickname(player);
        boolean overrideChatFormat = plugin.getConfig().getBoolean("override-chat-format", false);
        boolean overrideTabFormat = plugin.getConfig().getBoolean("override-tab-format", false);
        boolean setDisplayName = plugin.getConfig().getBoolean("set-display-name", true);
        
        // 根据配置决定是否设置显示名称
        if (setDisplayName || overrideChatFormat) {
            player.setDisplayName(nickname);
        }
        
        // 处理TAB列表名称 - 只在override-tab-format为true时设置
        if (overrideTabFormat) {
            String tabFormat = plugin.getConfig().getString("tab-format", "&7[&r%pnn%&7] %player%");
            try {
                String formattedTabName = tabFormat
                        .replace("%pnn%", nickname)
                        .replace("%player%", player.getName());
                
                // 如果启用了PlaceholderAPI，处理其他占位符
                boolean placeholderEnabled = plugin.getConfig().getBoolean("placeholder", false);
                if (placeholderEnabled && Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                    try {
                        formattedTabName = me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, formattedTabName);
                    } catch (Exception e) {
                        plugin.getLogger().warning("处理玩家" + player.getName() + "的TAB列表名称占位符时出错: " + e.getMessage());
                    }
                }
                
                // 转换颜色代码
                formattedTabName = ChatColor.translateAlternateColorCodes('&', formattedTabName);
                
                player.setPlayerListName(formattedTabName);
            } catch (Exception e) {
                plugin.getLogger().warning("无法设置玩家" + player.getName() + "的Tab列表名称：" + e.getMessage());
            }
        }
    }
    
    /**
     * 发送帮助信息
     */
    private void sendHelpMessage(CommandSender sender) {
        sender.sendMessage(ChatColor.GREEN + "===== PNN 玩家昵称插件帮助 =====");
        
        boolean isPlayer = sender instanceof Player;
        
        // 基础命令
        if (isPlayer && sender.hasPermission("pnn.set")) {
            sender.sendMessage(ChatColor.YELLOW + "/pnn set <昵称>" + ChatColor.WHITE + " - 设置你的昵称");
        }
        
        if (isPlayer && sender.hasPermission("pnn.reset")) {
            sender.sendMessage(ChatColor.YELLOW + "/pnn reset" + ChatColor.WHITE + " - 重置你的昵称");
        }
        
        if (sender.hasPermission("pnn.get")) {
            sender.sendMessage(ChatColor.YELLOW + "/pnn get <昵称>" + ChatColor.WHITE + " - 根据昵称查询玩家真实ID");
        }
        
        if (sender.hasPermission("pnn.list")) {
            sender.sendMessage(ChatColor.YELLOW + "/pnn list [页码]" + ChatColor.WHITE + " - 查看所有玩家昵称");
        }
        
        // 管理员命令
        if (sender.hasPermission("pnn.admin")) {
            sender.sendMessage(ChatColor.GREEN + "===== 管理员命令 =====");
            sender.sendMessage(ChatColor.YELLOW + "/pnn admin set <玩家> <昵称>" + ChatColor.WHITE + " - 设置指定玩家的昵称");
            sender.sendMessage(ChatColor.YELLOW + "/pnn admin reset <玩家>" + ChatColor.WHITE + " - 重置指定玩家的昵称");
        }
        
        if (sender.hasPermission("pnn.reload")) {
            sender.sendMessage(ChatColor.YELLOW + "/pnn reload" + ChatColor.WHITE + " - 重载插件配置");
        }
        
        // 命令别名提示
        sender.sendMessage(ChatColor.GRAY + "提示：命令支持别名，例如 /pnn s 等同于 /pnn set");
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            // 主命令补全
            List<String> commands = new ArrayList<>();
            
            // 添加基础命令
            if (sender instanceof Player) {
                if (sender.hasPermission("pnn.set")) commands.add("set");
                if (sender.hasPermission("pnn.reset")) commands.add("reset");
            }
            
            if (sender.hasPermission("pnn.get")) commands.add("get");
            if (sender.hasPermission("pnn.list")) commands.add("list");
            if (sender.hasPermission("pnn.admin")) commands.add("admin");
            if (sender.hasPermission("pnn.reload")) commands.add("reload");
            
            commands.add("help");
            
            // 添加命令别名
            for (Map.Entry<String, String> entry : commandAliases.entrySet()) {
                if (commands.contains(entry.getValue())) {
                    commands.add(entry.getKey());
                }
            }
            
            // 过滤匹配前缀的命令
            for (String cmd : commands) {
                if (cmd.toLowerCase().startsWith(args[0].toLowerCase())) {
                    completions.add(cmd);
                }
            }
            
            return completions;
        } else if (args.length == 2) {
            // 子命令补全
            String subCommand = args[0].toLowerCase();
            
            // 处理命令别名
            if (commandAliases.containsKey(subCommand)) {
                subCommand = commandAliases.get(subCommand);
            }
            
            if (subCommand.equals("get") && sender.hasPermission("pnn.get")) {
                // 提供所有玩家的昵称作为补全
                for (Map.Entry<UUID, String> entry : nicknameManager.getAllNicknames().entrySet()) {
                    String nickname = entry.getValue();
                    if (nickname.toLowerCase().startsWith(args[1].toLowerCase())) {
                        completions.add(nickname);
                    }
                }
            } else if (subCommand.equals("list") && sender.hasPermission("pnn.list")) {
                // 提供页码作为补全
                int pageCount = (nicknameManager.getAllNicknames().size() + 9) / 10;
                for (int i = 1; i <= pageCount; i++) {
                    if (String.valueOf(i).startsWith(args[1])) {
                        completions.add(String.valueOf(i));
                    }
                }
            } else if (subCommand.equals("admin") && sender.hasPermission("pnn.admin")) {
                // 提供管理员子命令作为补全
                List<String> adminCommands = Arrays.asList("set", "reset");
                for (String cmd : adminCommands) {
                    if (cmd.startsWith(args[1].toLowerCase())) {
                        completions.add(cmd);
                    }
                }
            }
            
            return completions;
        } else if (args.length == 3) {
            // 第三个参数补全
            String subCommand = args[0].toLowerCase();
            
            // 处理命令别名
            if (commandAliases.containsKey(subCommand)) {
                subCommand = commandAliases.get(subCommand);
            }
            
            if (subCommand.equals("admin") && sender.hasPermission("pnn.admin")) {
                String adminSubCommand = args[1].toLowerCase();
                
                if (adminSubCommand.equals("set") || adminSubCommand.equals("reset")) {
                    // 提供在线玩家列表
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        if (player.getName().toLowerCase().startsWith(args[2].toLowerCase())) {
                            completions.add(player.getName());
                        }
                    }
                    
                    // 添加使用昵称的玩家
                    for (Map.Entry<UUID, String> entry : nicknameManager.getAllNicknames().entrySet()) {
                        Player player = Bukkit.getPlayer(entry.getKey());
                        if (player != null) {
                            String nickname = entry.getValue();
                            if (nickname.toLowerCase().startsWith(args[2].toLowerCase())) {
                                completions.add(nickname);
                            }
                        }
                    }
                }
            }
            
            return completions;
        }
        
        return null;
    }
} 
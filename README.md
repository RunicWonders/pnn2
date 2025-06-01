# PNN (Player Nickname)
一个轻量级的玩家昵称插件，不需要任何前置插件。支持PlaceholderAPI（可选）。

## 功能特点

- 自定义玩家昵称设置
- 在聊天、TAB列表和玩家头顶显示昵称
- 支持颜色代码和格式代码
- 支持多语言
- 支持昵称屏蔽词过滤
- 支持 PlaceholderAPI 占位符
- 支持 Vault 经济系统集成
- 自定义聊天、加入、退出消息格式
- 丰富的命令系统
- 完善的权限系统

## 命令

### 基础命令
- `/pnn set <昵称>` - 设置你的昵称
- `/pnn reset` - 重置你的昵称
- `/pnn get <昵称>` - 查询昵称对应的玩家
- `/pnn list` - 查看所有玩家的昵称
- `/pnn reload` - 重载插件配置
- `/pnn help` - 显示帮助信息

### 管理员命令
- `/pnn admin <set|reset|get> <玩家> [昵称]` - 管理员命令

### 命令别名
插件支持多种命令别名，方便快速输入：
- `/pnn s <昵称>` = `/pnn set <昵称>`
- `/pnn r` = `/pnn reset`
- `/pnn g <昵称>` = `/pnn get <昵称>`
- `/pnn l` = `/pnn list`
- `/pnn a <set|reset|get> <玩家> [昵称]` = `/pnn admin <set|reset|get> <玩家> [昵称]`

还支持中文别名，如 `/pnn 设置`、`/pnn 重置`、`/pnn 查询` 等。

## 权限

- `pnn.set` - 允许设置昵称
- `pnn.reset` - 允许重置昵称
- `pnn.get` - 允许查询昵称
- `pnn.list` - 允许查看所有昵称
- `pnn.reload` - 允许重载插件
- `pnn.chatcolor` - 允许在聊天中使用颜色代码
- `pnn.admin` - 允许使用管理员命令

## 经济系统

PNN 支持与 Vault 经济系统集成，可以为设置和重置昵称设置费用：

- 设置昵称费用：100.0
- 重置昵称费用：50.0
- 支持重置时返还部分费用
- 可在配置文件中自定义费用和返还比例

## 占位符

当安装 PlaceholderAPI 并在配置中启用 `placeholder: true` 时，可以使用以下占位符：

- `%pnn_nickname%` - 玩家的昵称
- `%pnn_has_nickname%` - 玩家是否有昵称
- `%pnn_real_name%` - 玩家的真实ID

在聊天信息和加入/退出消息中可以使用：
- `%pnn%` - 玩家的昵称（如果没有昵称则显示真实ID）
- `%player%` - 玩家的真实ID
- `%msg%` - 聊天内容
- `%world%` - 玩家所在世界

## 配置文件

```yaml
# 是否启用昵称占位符支持
placeholder: false

# 占位符显示格式配置
placeholder-format:
  # %pnn% 占位符的默认显示格式
  pnn-format: "{nickname}"
  # %pnn_nickname% 占位符的默认显示格式
  pnn-nickname-format: "{nickname}"

# 屏蔽词列表
Block-words:
  - sb
  - 傻逼

# 是否覆盖默认聊天格式
override-chat-format: false

# 是否覆盖TAB列表名称格式
override-tab-format: false

# TAB列表名称格式
tab-format: "&7[&r%pnn%&7] %player%"

# 聊天格式配置
chat-format:
  # 普通聊天格式
  format: "&7[&r%pnn%&7] &f%msg%"
  # 加入服务器消息格式
  join-message: "&e%pnn% 加入了游戏"
  # 离开服务器消息格式
  quit-message: "&e%pnn% 离开了游戏"
  # 聊天颜色权限
  color-permission: true

# 昵称设置
max-nickname-length: 32
allow-colors: true
allow-formats: true

# 数据保存模式（目前仅支持yaml）
data-saving-mode: yaml
```

## 安装与下载

### 下载最新版本
从[GitHub Releases](https://github.com/RunicWonders/pnn/releases/latest)下载最新版本

### 最新构建版本
从GitHub Actions的[构建记录](https://github.com/RunicWonders/pnn/actions/workflows/ci.yml)中获取最新构建版本

## 构建方法

如果你想自己构建插件：

```bash
# 克隆仓库
git clone https://github.com/RunicWonders/pnn2.git pnn
cd pnn

# 使用Maven构建
mvn clean package
```

构建完成后，JAR文件将位于`target`目录。

## 更新日志

### v1.1.0
- 添加 Vault 经济系统集成
- 支持设置和重置昵称收费
- 支持重置时返还部分费用
- 优化配置文件结构
- 修复已知问题

### v1.0.0
- 初始版本发布
- 基础昵称管理功能
- PlaceholderAPI 支持
- 自定义聊天格式

## 安装要求

- Minecraft 1.21+
- Java 21+
- Vault (可选，用于经济系统)
- PlaceholderAPI (可选，用于占位符)

## 下载

- [GitHub Releases](https://github.com/RunicWonders/pnn/releases)
- [SpigotMC](https://www.spigotmc.org/resources/pnn.xxxxx)

## 支持

- 问题反馈：[GitHub Issues](https://github.com/RunicWonders/pnn/issues)
- 功能建议：[GitHub Discussions](https://github.com/RunicWonders/pnn/discussions)

## 许可证

本项目采用 MIT 许可证。详见 [LICENSE](LICENSE) 文件。

## 作者

- 柠枺
- 源代码由 AI 提供，经过人工优化和测试

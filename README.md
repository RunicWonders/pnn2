# PNN (Player Nickname)
一个轻量级的玩家昵称插件，不需要任何前置插件。支持PlaceholderAPI（可选）。

## 功能

- 可选择性直接覆盖原版ID显示
- 显示在聊天栏，tab栏以及玩家头部
- 玩家可以自行设置昵称
- 支持昵称颜色代码和多语言
- 昵称屏蔽词过滤
- 支持PlaceholderAPI占位符
- 自定义聊天、加入和退出消息格式
- 命令系统支持别名和Tab补全

## 命令

### 基础命令
- `/pnn set <昵称>` - 设置昵称
- `/pnn reset` - 重置昵称
- `/pnn get <昵称>` - 查询昵称对应的真实玩家ID
- `/pnn list [页码]` - 查看所有玩家昵称（支持分页）
- `/pnn reload` - 重载插件配置
- `/pnn help` - 显示帮助信息

### 管理员命令
- `/pnn admin set <玩家> <昵称>` - 为指定玩家设置昵称
- `/pnn admin reset <玩家>` - 重置指定玩家的昵称

### 命令别名
插件支持多种命令别名，方便快速输入：
- `/pnn s <昵称>` = `/pnn set <昵称>`
- `/pnn r` = `/pnn reset`
- `/pnn g <昵称>` = `/pnn get <昵称>`
- `/pnn l [页码]` = `/pnn list [页码]`
- `/pnn a <set/reset> <玩家> [昵称]` = `/pnn admin <set/reset> <玩家> [昵称]`

还支持中文别名，如 `/pnn 设置`、`/pnn 重置` 等。

## 权限

- `pnn.set` - 允许玩家设置自己的昵称（默认所有人）
- `pnn.reset` - 允许玩家重置自己的昵称（默认所有人）
- `pnn.get` - 允许玩家查询昵称对应的真实ID（默认所有人）
- `pnn.list` - 允许玩家查看所有昵称（默认OP）
- `pnn.reload` - 允许玩家重载插件配置（默认OP）
- `pnn.chatcolor` - 允许玩家在聊天中使用颜色代码（默认OP）
- `pnn.admin` - 允许玩家使用管理员命令（默认OP）

## 占位符

当安装 PlaceholderAPI 并在配置中启用 `placeholder: true` 时，可以使用以下占位符：

- `%pnn_nickname%` - 返回玩家的昵称
- `%pnn_has_nickname%` - 返回玩家是否有昵称（true 或 false）
- `%pnn_real_name%` - 返回玩家的真实ID

在聊天信息和加入/退出消息中可以使用：
- `%pnn%` - 玩家的昵称（如果没有昵称则显示真实ID）
- `%player%` - 玩家的真实ID
- `%msg%` - 聊天内容
- `%world%` - 玩家所在世界

## 配置文件

```yaml
# 是否启用昵称占位符支持
placeholder: false

# 屏蔽词列表
Block-words:
  - sb
  - 傻逼

# 是否覆盖默认聊天格式
override-chat-format: false

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
git clone https://github.com/RunicWonders/pnn.git
cd pnn

# 使用Maven构建
mvn clean package
```

构建完成后，JAR文件将位于`target`目录。

## 作者

- 柠枺 
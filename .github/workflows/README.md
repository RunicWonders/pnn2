# PNN 插件自动构建流程

本项目使用GitHub Actions自动构建系统，包含两个主要工作流程：

## 1. CI构建流程 (ci.yml)

当推送代码到`main`分支或对`main`分支创建Pull Request时，此工作流会自动运行。

**功能：**
- 使用JDK 21构建项目
- 以Git提交的短哈希值作为版本标识
- 将构建结果作为构件(Artifacts)上传到Actions运行记录中

**获取构建产物：**
1. 进入GitHub Actions页面
2. 选择最新的CI构建流程运行记录
3. 在"Artifacts"部分下载构建产物

## 2. 发布流程 (release.yml)

当创建新的版本标签(格式为`v*`，如`v1.0.0`)时，此工作流会自动运行。

**功能：**
- 使用JDK 21构建项目
- 创建正式GitHub Release
- 将构建产物上传到Release中

**创建新版本：**
```bash
# 创建本地标签
git tag v1.0.0

# 推送标签到GitHub
git push origin v1.0.0
```

## 权限设置

如果自动构建失败，请确保已正确配置仓库权限：

1. 进入仓库的"Settings" > "Actions" > "General"
2. 在"Workflow permissions"部分，选择"Read and write permissions"
3. 保存更改

## 注意事项

- CI构建产物仅保留90天
- 发布版本会永久保存在GitHub Releases中
- 请确保标签版本(v1.0.0)与pom.xml中的版本(1.0.0)保持一致 
# GitSage for Android Studio / IntelliJ IDEA

一个使用 AI 生成 Git Commit 消息的 Android Studio / IntelliJ IDEA 插件。

## 功能特性

- 🤖 **AI 驱动**：使用 OpenAI 兼容的 API 生成高质量的 Commit 消息
- 🔧 **自定义提供商**：支持任何 OpenAI 兼容的 API（OpenAI、Azure、本地部署等）
- 📝 **多种规范**：支持 Conventional Commits、Angular、Emoji 等多种提交规范
- 🌐 **多语言支持**：支持英文、中文或自动检测语言
- 🔌 **模型列表**：自动拉取提供商的可用模型列表
- 🎨 **IDE 集成**：无缝集成到 Git Commit 工具窗口

## 安装

### 从磁盘安装

1. 下载插件压缩包（.zip）
2. 打开 Android Studio / IntelliJ IDEA
3. 进入 `Settings/Preferences` → `Plugins` → `Install from disk...`
4. 选择下载的 .zip 文件
5. 重启 IDE

### 从 JetBrains Marketplace（待发布）

1. 打开 `Settings/Preferences` → `Plugins` → `Marketplace`
2. 搜索 "GitSage"
3. 点击 Install

## 配置

### 1. 打开设置

`Settings/Preferences` → `Tools` → `GitSage`

### 2. 配置 AI 提供商

- **Base URL**: API 的基础 URL（例如：`https://api.openai.com/v1`）
- **API Key**: 你的 API 密钥
- **Model**: 模型名称（可以点击 "Fetch Models" 拉取可用模型列表）
- **Temperature**: 生成温度（0.0 - 2.0）
- **Max Tokens**: 最大生成 token 数

### 3. 配置生成选项

- **Commit Convention**: 选择提交规范格式
  - Conventional Commits: `type(scope): description`
  - Angular: `type(scope): subject`
  - Emoji: `✨ description`
  - Simple: `description`
- **Language**: 生成语言（英文、中文、自动）

## 使用

### 生成 Commit 消息

1. 打开 Git 工具窗口（`View` → `Tool Windows` → `Commit`）
2. 勾选要提交的文件（不勾选则使用所有待提交文件）
3. 点击 Commit 消息框上方的 "Generate Commit Message" 按钮
4. 等待 AI 生成消息

### 快捷键

插件会添加一个操作按钮到 Git Commit 工具窗口的工具栏。

## 支持的提供商

任何 OpenAI 兼容的 API 都可以使用：

- **OpenAI**: `https://api.openai.com/v1`
- **Azure OpenAI**: `https://{your-resource}.openai.azure.com/openai/deployments/{deployment-id}`
- **LocalAI**: `http://localhost:8080/v1`
- **Ollama**: `http://localhost:11434/v1`（需要 OpenAI 兼容层）
- **其他**: 任何 OpenAI API 兼容的服务

## 开发

### 环境要求

- JDK 17+
- Kotlin 2.0.0+
- Android Studio 2024.1+ 或 IntelliJ IDEA

### 构建

```bash
./gradlew build
```

### 运行调试

```bash
./gradlew runIde
```

### 打包

```bash
./gradlew buildPlugin
```

插件包将生成在 `build/distributions/` 目录。

## 技术栈

- **Kotlin**: 主要开发语言
- **IntelliJ Platform SDK**: 插件框架
- **OkHttp**: HTTP 客户端
- **Gson**: JSON 处理
- **Kotlin Coroutines**: 异步处理

## 项目结构

```
gitsage/
├── src/main/kotlin/com/example/gitsage/
│   ├── GitSagePlugin.kt               # 插件入口
│   ├── settings/                      # 设置相关
│   │   ├── GitSageSettings.kt
│   │   ├── GitSageConfigurable.kt
│   │   ├── GitSageSettingsState.kt
│   │   └── CredentialsManager.kt
│   ├── ai/                            # AI 相关
│   │   ├── AIProvider.kt
│   │   ├── OpenAICompatibleProvider.kt
│   │   ├── ModelListFetcher.kt
│   │   ├── CommitMessageGenerator.kt
│   │   └── HttpClientConfig.kt
│   ├── git/                           # Git 相关
│   │   ├── GitDiffExtractor.kt
│   │   └── GitRepositoryUtils.kt
│   ├── ui/                            # UI 相关
│   │   └── GenerateCommitAction.kt
│   └── notifications/                 # 通知
│       └── NotificationHelper.kt
└── src/main/resources/
    ├── META-INF/
    │   ├── plugin.xml                 # 插件配置
    │   └── pluginIcon.svg             # 插件图标
    └── icons/
        └── gitsage.svg                # 操作图标
```

## 贡献

欢迎提交 Issue 和 Pull Request！

## 许可证

MIT License

## 致谢

- [IntelliJ Platform SDK](https://plugins.jetbrains.com/docs/intellij/welcome.html)
- [OkHttp](https://square.github.io/okhttp/)
- [OpenAI API](https://platform.openai.com/docs/introduction)

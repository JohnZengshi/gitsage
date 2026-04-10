# GitSage 插件构建完成

## 插件文件

**路径**: `/Users/littleflower/Work/gitsage/build/distributions/gitsage-1.0.0.zip`
**大小**: 4.4 MB

## 安装方法

### 1. 在 Android Studio / IntelliJ IDEA 中安装

1. 打开 IDE
2. 进入 `Settings/Preferences` → `Plugins` → `⚙️` → `Install from Disk...`
3. 选择文件：`/Users/littleflower/Work/gitsage/build/distributions/gitsage-1.0.0.zip`
4. 点击 `OK`，然后重启 IDE

### 2. 配置插件

1. 打开 `Settings/Preferences` → `Tools` → `GitSage`
2. 配置 AI 提供商：
   - **Base URL**: OpenAI API 地址（默认：https://api.openai.com/v1）
   - **API Key**: 你的 API 密钥
   - **Model**: 模型名称（如 gpt-3.5-turbo）
   - 点击 `Fetch Models` 可获取可用模型列表
3. 配置生成选项：
   - **Commit Convention**: 选择提交规范格式
   - **Language**: 选择生成语言

### 3. 使用插件

1. 打开 Git 工具窗口（View → Tool Windows → Commit）
2. 勾选要提交的文件
3. 点击 Commit 消息框上方的 **AI 图标按钮**
4. 等待 AI 生成 commit 消息

## 支持的 API 提供商

- **OpenAI**: https://api.openai.com/v1
- **Azure OpenAI**: https://{your-resource}.openai.azure.com/openai/deployments/{deployment-id}
- **LocalAI**: http://localhost:8080/v1
- **Ollama**: http://localhost:11434/v1
- 任何 OpenAI 兼容 API

## 项目源码位置

`/Users/littleflower/Work/gitsage/`

## 重新构建

```bash
cd /Users/littleflower/Work/gitsage
export SDKMAN_DIR="$HOME/.sdkman"
[[ -s "$SDKMAN_DIR/bin/sdkman-init.sh" ]] && source "$SDKMAN_DIR/bin/sdkman-init.sh"
sdk use java 17.0.18-tem
export JAVA_HOME="$HOME/.sdkman/candidates/java/17.0.18-tem"
/tmp/gradle-8.5/bin/gradle clean buildPlugin
```

构建完成后，插件包会更新在 `build/distributions/` 目录。

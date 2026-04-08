# CodeReview 代码自动评审

这是一个代码自动评审组件，通过自动化流程提高代码评审的效率和质量，从而提升软件开发项目的整体交付质量。

**核心流程**：获取最近一次提交的 diff 代码 -> 调用 AI 进行代码评审 -> 将评审结果写入日志仓库 -> 通过微信模板消息推送通知

## 架构概览

```
┌─────────────────────────────────────────────────────┐
│                  OpenAICodeReview                    │
│                   (程序入口)                          │
└──────────────────────┬──────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────┐
│           OpenAICodeReviewService                    │
│            (领域服务 - 模板方法模式)                   │
│                                                      │
│  1. getDiffCode()      → GitCommand.diff()           │
│  2. codeReview()       → IOpenAI.completions()       │
│  3. recordCodeReview() → GitCommand.commitAndPush()  │
│  4. pushMessage()      → WeiXin.sendTemplateMessage()│
└─────────────────────────────────────────────────────┘
         │                │                │
         ▼                ▼                ▼
   ┌──────────┐    ┌──────────┐    ┌──────────┐
   │GitCommand│    │ IOpenAI  │    │  WeiXin  │
   │ (Git操作) │    │(AI评审)  │    │(微信通知) │
   └──────────┘    └──────────┘    └──────────┘
```

## 项目结构

```
openai-codereview-zqiusu
├── openai-codereview-zqiusu-sdk        # SDK 核心模块（可独立打包为 jar）
│   └── site.zqiusu.sdk
│       ├── OpenAiCodeReview            # 程序入口（main 方法）
│       ├── domain
│       │   ├── model/Model             # AI 模型枚举
│       │   └── service
│       │       ├── IOpenAICodeReviewService      # 服务接口
│       │       ├── AbstractOpenAICodeReviewService # 抽象类（模板方法）
│       │       └── impl/OpenAICodeReviewService  # 具体实现
│       ├── infrastructure
│       │   ├── git/GitCommand          # Git 操作（diff、commit、push）
│       │   ├── openai
│       │   │   ├── IOpenAI             # OpenAI 接口
│       │   │   ├── impl/ChatGLM        # 智谱 AI 实现
│       │   │   └── dto/                # 请求/响应 DTO
│       │   └── weixin
│       │       ├── WeiXin              # 微信模板消息推送
│       │       └── dto/                # 消息模板 DTO
│       └── types/utils                 # 工具类
│           ├── BearerTokenUtils        # JWT Token 生成
│           ├── WXAccessTokenUtils      # 微信 AccessToken 获取
│           └── RandomStringUtils       # 随机字符串
├── openai-codereview-zqiusu-test       # 测试模块（SDK 接入示例）
└── .github/workflows                   # GitHub Actions 配置
```

## 接入方式

### 方式一：GitHub Actions 集成（推荐）

这是最简单的接入方式，只需在你的项目中添加一个 workflow 文件即可自动触发代码评审。

#### 1. 准备日志仓库

在 GitHub 上创建一个**公开仓库**用于存放代码评审日志，SDK 会将评审结果以 Markdown 文件形式写入该仓库。

#### 2. 打包 SDK 并上传

```bash
# 克隆本项目
git clone https://github.com/ZQIUSU/openai-codereview.git

# 进入 SDK 模块打包
cd openai-codereview/openai-codereview-zqiusu-sdk
mvn clean package -DskipTests

# 将生成的 jar 上传到日志仓库的 Release 中
# target/openai-codereview-zqiusu-sdk-1.0.jar
```

#### 3. 配置 GitHub Secrets

在你的项目仓库的 `Settings -> Secrets and variables -> Actions` 中添加以下 Secrets：

| Secret 名称 | 说明 | 获取方式 |
|---|---|---|
| `CODE_REVIEW_LOG_URI` | 日志仓库地址 | `https://github.com/你的用户名/日志仓库名` |
| `CODE_TOKEN` | GitHub Personal Access Token | GitHub Settings -> Developer settings -> Personal access tokens，需要 `repo` 权限 |
| `CHATGLM_APIHOST` | 智谱 AI API 地址 | `https://open.bigmodel.cn/api/paas/v4/chat/completions` |
| `CHATGLM_APIKEYSECRET` | 智谱 AI API Key | [智谱开放平台](https://open.bigmodel.cn/usercenter/apikeys) 创建 API Key |
| `WEIXIN_APPID` | 微信公众号 AppID | 微信公众平台 -> 开发 -> 基本配置 |
| `WEIXIN_SECRET` | 微信公众号 AppSecret | 微信公众平台 -> 开发 -> 基本配置 |
| `WEIXIN_TOUSER` | 接收通知的用户 OpenID | 微信公众平台 -> 用户管理 |
| `WEIXIN_TEMPLATE_ID` | 微信模板消息 ID | 微信公众平台 -> 模板消息，模板需包含：`repo_name`、`branch_name`、`commit_author`、`commit_message` 字段 |

#### 4. 添加 Workflow 文件

在你的项目中创建 `.github/workflows/code-review.yml`：

```yaml
name: Build and Run OpenAiCodeReview By Github Action

on:
  push:
    branches:
      - '*'

jobs:
  build:
    runs-on: ubuntu-latest

    steps:
      - name: Checkout repository
        uses: actions/checkout@v2
        with:
          fetch-depth: 2

      - name: Set up JDK 11
        uses: actions/setup-java@v2
        with:
          distribution: 'adopt'
          java-version: '11'

      - name: Create new folder
        run: mkdir -p ./libs

      - name: Download openai-code-review-sdk JAR
        run: wget -O ./libs/openai-codereview-sdk-1.0.jar https://github.com/你的用户名/日志仓库名/releases/download/v1.0/openai-codereview-zqiusu-sdk-1.0.jar

      - name: Get repository name
        run: echo "REPO_NAME=${GITHUB_REPOSITORY##*/}" >> $GITHUB_ENV

      - name: Get branch name
        run: echo "BRANCH_NAME=${GITHUB_REF#refs/heads/}" >> $GITHUB_ENV

      - name: Get commit author
        run: echo "COMMIT_AUTHOR=$(git log -1 --pretty=format:'%an <%ae>')" >> $GITHUB_ENV

      - name: Get commit message
        run: echo "COMMIT_MESSAGE=$(git log -1 --pretty=format:'%s')" >> $GITHUB_ENV

      - name: Run code Review
        run: java -jar ./libs/openai-codereview-sdk-1.0.jar
        env:
          GITHUB_REVIEW_LOG_URI: ${{ secrets.CODE_REVIEW_LOG_URI }}
          GITHUB_TOKEN: ${{ secrets.CODE_TOKEN }}
          COMMIT_PROJECT: ${{ env.REPO_NAME }}
          COMMIT_BRANCH: ${{ env.BRANCH_NAME }}
          COMMIT_AUTHOR: ${{ env.COMMIT_AUTHOR }}
          COMMIT_MESSAGE: ${{ env.COMMIT_MESSAGE }}
          WEIXIN_APPID: ${{ secrets.WEIXIN_APPID }}
          WEIXIN_SECRET: ${{ secrets.WEIXIN_SECRET }}
          WEIXIN_TOUSER: ${{ secrets.WEIXIN_TOUSER }}
          WEIXIN_TEMPLATE_ID: ${{ secrets.WEIXIN_TEMPLATE_ID }}
          CHATGLM_APIHOST: ${{ secrets.CHATGLM_APIHOST }}
          CHATGLM_APIKEYSECRET: ${{ secrets.CHATGLM_APIKEYSECRET }}
```

完成后，每次 push 代码就会自动触发代码评审，评审结果会推送到微信，点击通知中的链接可查看详细评审报告。

---

### 方式二：SDK 依赖接入

在 Java 项目中通过 Maven 依赖引入 SDK，适用于需要自定义集成的场景。

#### 1. 引入 Maven 依赖

```xml
<dependency>
    <groupId>site.zqiusu</groupId>
    <artifactId>openai-codereview-zqiusu-sdk</artifactId>
    <version>1.0</version>
</dependency>
```

> 需要先 `mvn install` 将 SDK 安装到本地仓库，或部署到私有 Maven 仓库。

#### 2. 编写调用代码

SDK 的核心入口是 `OpenAiCodeReview.main()`，通过环境变量读取配置。你也可以参照其模板方法模式自行组装各组件：

```java
import site.zqiusu.sdk.domain.service.impl.OpenAICodeReviewService;
import site.zqiusu.sdk.infrastructure.git.GitCommand;
import site.zqiusu.sdk.infrastructure.openai.IOpenAI;
import site.zqiusu.sdk.infrastructure.openai.impl.ChatGLM;
import site.zqiusu.sdk.infrastructure.weixin.WeiXin;

public class MyCodeReview {

    public static void main(String[] args) throws Exception {
        // 1. 初始化 GitCommand（配置 Git 仓库信息）
        GitCommand gitCommand = new GitCommand(
                "提交信息",          // commit message
                "作者",             // commit author
                "master",           // branch
                "my-project",       // project name
                "github_token",     // GitHub Token
                "https://github.com/xxx/log-repo"  // 日志仓库地址
        );

        // 2. 初始化微信通知（可选）
        WeiXin weiXin = new WeiXin(
                "wx_appid",
                "wx_secret",
                "wx_touser",
                "wx_template_id"
        );

        // 3. 初始化 AI 评审服务（使用智谱 AI）
        IOpenAI openAI = new ChatGLM(
                "https://open.bigmodel.cn/api/paas/v4/chat/completions",
                "your_apikey.your_apisecret"
        );

        // 4. 执行代码评审
        OpenAICodeReviewService service = new OpenAICodeReviewService(gitCommand, openAI, weiXin);
        service.exec();
    }
}
```

---

### 方式三：WebHook（待扩展）

通过 WebHook 方式接收代码提交事件并触发评审，适用于更多代码托管平台的集成场景。

## 环境变量说明

| 变量名 | 必填 | 说明 |
|---|---|---|
| `COMMIT_MESSAGE` | 是 | 最近一次提交信息 |
| `COMMIT_AUTHOR` | 是 | 提交作者 |
| `COMMIT_BRANCH` | 是 | 提交分支 |
| `COMMIT_PROJECT` | 是 | 项目名称 |
| `GITHUB_TOKEN` | 是 | GitHub Personal Access Token（需 `repo` 权限） |
| `GITHUB_REVIEW_LOG_URI` | 是 | 评审日志仓库地址 |
| `CHATGLM_APIHOST` | 是 | 智谱 AI API 地址 |
| `CHATGLM_APIKEYSECRET` | 是 | 智谱 AI API Key（格式：`apiKey.apiSecret`） |
| `WEIXIN_APPID` | 否 | 微信公众号 AppID（不配置则跳过通知） |
| `WEIXIN_SECRET` | 否 | 微信公众号 AppSecret |
| `WEIXIN_TOUSER` | 否 | 接收通知的用户 OpenID |
| `WEIXIN_TEMPLATE_ID` | 否 | 微信模板消息 ID |

## 支持的 AI 模型

SDK 当前使用智谱 AI（ChatGLM）作为评审引擎，默认使用 `glm-4-flash` 模型（免费、128k 上下文）。可通过修改 [Model.java](openai-codereview-zqiusu-sdk/src/main/java/site/zqiusu/sdk/domain/model/Model.java) 切换模型：

| 模型 | code | 说明 |
|---|---|---|
| GLM_4_FLASH | `glm-4-flash` | 免费模型，简单任务，速度最快（默认） |
| GLM_3_5_TURBO | `glm-3-turbo` | 知识量、推理能力较强 |
| GLM_4 | `glm-4` | 复杂对话交互和深度内容创作 |
| GLM_4V | `glm-4v` | 支持图像输入 |

## 评审流程

```
代码 Push → GitHub Actions 触发
    ↓
获取最近两次提交的 diff 代码
    ↓
调用智谱 AI 进行代码评审
    ↓
评审结果写入日志仓库（按日期归档）
    ↓
微信模板消息推送评审通知（含日志链接）
```

## 技术栈

- Java 8+
- Spring Boot 2.7
- Maven（shade plugin 打 fat jar）
- JGit（Git 操作）
- 智谱 AI / ChatGLM（代码评审）
- 微信公众平台（消息通知）
- GitHub Actions（CI/CD 集成）


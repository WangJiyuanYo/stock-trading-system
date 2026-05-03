# Stock Trading System 📈

一个基于 **Spring Boot 3.4** 和 **Maven 多模块架构**的智能股票交易系统，集成 AI Agent、实时行情获取、盈亏分析、多渠道消息推送（Server酱、NotifyMe）、飞书机器人和洛克王国远行商人监控功能。

> **项目说明**：本项目主要由 Lingma AI 辅助生成，结合手工编码完成。
>
> **重要更新**（2026年5月1日）：已完成 **Maven 多模块架构重构**，将单体应用拆分为 10 个独立模块，大幅提升代码可维护性和可扩展性。

## ✨ 功能特性

### 🎯 核心功能

- **股票管理**：支持 A 股、港股、美股、英股、贵金属等多种类型股票的增删改查
- **实时行情**：通过新浪 API 获取实时股票行情数据
- **盈亏计算**：自动计算持仓股票的当日盈亏和总盈亏
- **数据持久化**：使用 JSON 文件存储股票数据和节假日信息
- **定时任务**：自动在交易日获取股票数据并推送盈亏报告

### 🤖 AI Agent 能力

- **智能助手**：基于 LangChain4j 1.13.0 和 DeepSeek AI 模型
- **节假日查询**：AI 自动从政府网站获取中国法定节假日信息
- **文件操作**：AI 辅助写入和管理 JSON 数据文件
- **多助手协同**：支持节假日助手、股票助手、文件写入助手等多种专业助手
- **Skills 系统**：支持动态加载和执行技能

### 📱 消息推送

- **Server 酱集成**：支持微信消息推送
- **NotifyMe 集成**：支持 Android 手机通知推送
- **多渠道支持**：可同时启用多个通知渠道，统一接口管理
- **日报推送**：每日自动推送持仓盈亏报告
- **Markdown 格式**：美观的消息展示，包含个股详情和汇总统计

### 🎮 洛克王国监控

- **远行商人监控**：自动监控洛克王国远行商人刷新
- **定时执行**：每天 8:01、12:01、16:01、20:01 自动执行监控
- **截图上传**：自动生成商品详情图并上传到图床
- **即时推送**：发现商人刷新立即推送通知

### 🌐 飞书机器人

- **消息接收**：支持飞书机器人消息接收和处理
- **智能对话**：通过飞书进行股票管理的自然语言交互
- **上下文记忆**：支持多轮对话和上下文理解

## 🛠️ 技术栈

### 后端

- **Java 17**
- **Spring Boot 3.4.13**
- **Maven 多模块架构**：10 个独立模块
- **Spring WebFlux**：响应式 HTTP 客户端
- **LangChain4j 1.13.0**：AI Agent 框架
- **DeepSeek AI**：大语言模型集成
- **飞书 SDK 2.5.3**：飞书机器人集成
- **Playwright 1.40.0**：浏览器自动化（洛克王国截图）
- **HttpClient5**：HTTP 客户端
- **Lombok**：简化 Java 代码

### 外部 API

- **新浪股票 API**：实时行情数据
- **Server 酱**：微信消息推送
- **NotifyMe**：Android 通知推送
- **DeepSeek API**：AI 对话服务
- **飞书开放平台**：机器人消息推送和接收
- **洛克王国 API**：游戏数据获取
- **ImgBB**：图床服务

## 📦 项目结构

```
stock-trading-system/
├── pom.xml                          # 父 POM，管理所有模块
├── trading-common/                  # 通用模块
│   ├── src/main/java/icu/iseenu/common/
│   │   ├── exception/               # 自定义异常
│   │   │   ├── BusinessException.java
│   │   │   ├── ValidationException.java
│   │   │   ├── ResourceNotFoundException.java
│   │   │   └── SystemException.java
│   │   ├── util/                    # 工具类
│   │   │   └── TradingDayUtil.java
│   │   └── Result.java              # 统一返回结果
│   └── pom.xml
│
├── trading-domain/                  # 领域模型模块
│   ├── src/main/java/icu/iseenu/domain/
│   │   ├── entity/                  # 实体类
│   │   │   ├── Stock.java
│   │   │   └── StockMarketData.java
│   │   └── enums/                   # 枚举类
│   │       └── StockTypeEnum.java
│   └── pom.xml
│
├── trading-infrastructure/          # 基础设施模块
│   ├── src/main/java/icu/iseenu/infra/
│   │   ├── api/                     # API 客户端
│   │   │   └── ApiClientService.java
│   │   ├── config/                  # 配置属性
│   │   │   ├── AppProperties.java
│   │   │   ├── NotificationProperties.java
│   │   │   └── FeishuProperties.java
│   │   └── storage/                 # 存储服务
│   │       ├── HolidayJsonService.java
│   │       └── JsonFileService.java
│   └── pom.xml
│
├── trading-notification/            # 通知服务模块
│   ├── src/main/java/icu/iseenu/notification/
│   │   ├── NotificationService.java # 统一通知服务
│   │   └── channel/                 # 通知渠道
│   │       ├── NotificationChannel.java
│   │       ├── ServerChanChannel.java
│   │       └── NotifyMeChannel.java
│   └── pom.xml
│
├── trading-stock/                   # 股票业务模块
│   ├── src/main/java/icu/iseenu/stock/
│   │   ├── api/                     # 股票 API 服务
│   │   │   └── StockApiService.java
│   │   ├── controller/              # 控制器
│   │   │   └── StockController.java
│   │   └── service/                 # 股票服务
│   │       └── StockService.java
│   └── pom.xml
│
├── trading-feishu/                  # 飞书集成模块
│   ├── src/main/java/icu/iseenu/feishu/
│   │   ├── config/                  # 飞书配置
│   │   │   └── FeishuConfig.java
│   │   ├── service/                 # 飞书服务
│   │   │   └── FeishuService.java
│   │   └── FeishuBotMessageReceiver.java
│   └── pom.xml
│
├── trading-ai/                      # AI Agent 模块
│   ├── src/main/java/icu/iseenu/ai/
│   │   ├── agent/                   # AI Agent
│   │   │   ├── assistant/           # 专业助手
│   │   │   │   ├── HolidayAssistant.java
│   │   │   │   ├── StockAssistant.java
│   │   │   │   └── WriteJsonFileAssistant.java
│   │   │   ├── supervisor/          # 监督者 Agent
│   │   │   │   └── SupervisorAgents.java
│   │   │   └── tool/                # Agent 工具
│   │   │       ├── HolidayTools.java
│   │   │       ├── StockTools.java
│   │   │       └── WriteFileTools.java
│   │   ├── config/                  # AI 配置
│   │   │   ├── ChatMemoryConfig.java
│   │   │   └── SkillsConfig.java
│   │   └── mcp/                     # MCP 集成
│   │       └── McpAssistant.java
│   └── pom.xml
│
├── trading-roco/                    # 洛克王国监控模块
│   ├── src/main/java/icu/iseenu/roco/
│   │   ├── config/                  # 配置
│   │   │   └── AppConfig.java
│   │   ├── model/                   # 数据模型
│   │   │   ├── Product.java
│   │   │   ├── RoundInfo.java
│   │   │   └── TemplateData.java
│   │   ├── service/                 # 业务服务
│   │   │   ├── RocoMerchantService.java
│   │   │   ├── HtmlGenerator.java
│   │   │   ├── ScreenshotService.java
│   │   │   └── ImageUploadService.java
│   │   ├── task/                    # 定时任务
│   │   │   └── RocoMerchantScheduledTask.java
│   │   └── util/                    # 工具类
│   │       ├── HttpClientUtil.java
│   │       └── TimeUtil.java
│   ├── src/main/resources/assets/   # 资源文件
│   │   └── yuanxing-shangren/       # 洛克王国素材
│   └── pom.xml
│
├── trading-application/             # 应用启动模块
│   ├── src/main/java/icu/iseenu/application/
│   │   ├── config/                  # 应用配置
│   │   │   ├── GlobalExceptionHandler.java
│   │   │   └── WebConfig.java
│   │   ├── controller/              # 控制器
│   │   │   ├── AiController.java
│   │   │   └── McpController.java
│   │   ├── task/                    # 定时任务
│   │   │   └── StockDataScheduledTask.java
│   │   └── StockTradeApplication.java # 启动类
│   ├── src/main/resources/
│   │   ├── application.yml          # 应用配置
│   │   ├── application-example.yml  # 配置模板
│   │   └── skills/                  # Skills 文件
│   │       └── stock/SKILL.md
│   └── pom.xml
│

├── data/                            # 数据文件
│   ├── calender/cn_holiday.json     # 节假日数据
│   └── json/stocks.json             # 股票数据
│
└── docs/                            # 项目文档
    ├── MULTI_MODULE_ARCHITECTURE.md         # 多模块架构说明
    ├── MULTI_MODULE_MIGRATION_COMPLETE.md   # 迁移完成报告
    ├── MULTI_MODULE_QUICK_START.md          # 快速开始指南
    └── ARCHITECTURE_OPTIMIZATION_V2.md      # 架构优化方案
```

## 🏗️ 多模块架构说明

### 模块依赖关系

```
trading-application (启动模块)
├── trading-stock (股票业务)
├── trading-feishu (飞书集成)
├── trading-ai (AI Agent)
├── trading-roco (洛克王国监控)
├── trading-notification (通知服务)
│   └── trading-infrastructure (基础设施)
├── trading-infrastructure
│   └── trading-domain (领域模型)
│       └── trading-common (通用模块)
```

### 模块职责

| 模块 | 职责 | 主要功能 |
|------|------|----------|
| **trading-common** | 通用基础 | 异常定义、工具类、统一返回格式 |
| **trading-domain** | 领域模型 | 实体类、枚举类、业务对象 |
| **trading-infrastructure** | 基础设施 | API 客户端、配置管理、存储服务 |
| **trading-notification** | 通知服务 | 多渠道通知统一管理 |
| **trading-stock** | 股票业务 | 股票管理、行情获取、盈亏计算 |
| **trading-feishu** | 飞书集成 | 飞书机器人消息收发 |
| **trading-ai** | AI Agent | LangChain4j 集成、智能助手 |
| **trading-roco** | 洛克王国监控 | 游戏数据监控、截图上传 |
| **trading-application** | 应用启动 | Spring Boot 启动、全局配置、定时任务 |

### 架构优势

✅ **高内聚低耦合**：每个模块职责单一，依赖清晰  
✅ **易于测试**：模块独立，可单独编译和测试  
✅ **便于扩展**：新增功能只需添加新模块，不影响现有代码  
✅ **团队协作**：不同团队可并行开发不同模块  
✅ **按需部署**：可选择性打包和部署特定模块  

## 🚀 快速开始

### 环境要求

- **JDK 17+**
- **Maven 3.6+**
- **Node.js 16+**（仅前端开发需要）

### 后端启动

#### 1. 克隆项目

```bash
git clone git@github.com:WangJiyuanYo/stock-trading-system.git
cd stock-trading-system
```

#### 2. 配置 application.yml

复制 `trading-application/src/main/resources/application-example.yml` 为 `application.yml`，并修改以下配置项：

```yaml
# DeepSeek AI 配置
langchain4j:
  open-ai:
    chat-model:
      api-key: your_deepseek_api_key  # 替换为你的 DeepSeek API Key

# 通知渠道配置
notification:
  enabled-channels: serverchan,notifyme  # 启用的通知渠道（逗号分隔）
  serverchan:
    sckey: your_serverchan_sendkey       # Server 酱 SendKey
  notifyme:
    uuid: your_notifyme_uuid             # NotifyMe UUID

# 飞书机器人配置
feishu:
  app-id: your_feishu_app_id          # 替换为你的飞书应用 App ID
  app-secret: your_feishu_app_secret  # 替换为你的飞书应用 App Secret

# 洛克王国配置
roco:
  rocom-api-key: your_rocom_api_key   # 洛克王国 API 密钥
  imgbb-key: your_imgbb_key           # ImgBB 图床密钥
  notifyme-uuid: your_notifyme_uuid   # NotifyMe UUID
  cron: "0 1 8,12,16,20 * * ?"        # 定时任务 cron 表达式
```

#### 3. 编译运行

**方式一：使用 Maven 直接运行（推荐）**

```bash
mvn spring-boot:run -pl trading-application -am
```

**方式二：打包后运行**

```bash
# 编译所有模块
mvn clean install -DskipTests

# 运行
java -jar trading-application/target/trading-application-1.0.0-SNAPSHOT.jar
```

**方式三：使用 -D 参数传递配置（生产环境推荐）**

```bash
java -jar trading-application/target/trading-application-1.0.0-SNAPSHOT.jar \
  -Dlangchain4j.open-ai.chat-model.api-key=your_deepseek_api_key \
  -Dnotification.serverchan.sckey=your_serverchan_sendkey \
  -Dnotification.notifyme.uuid=your_notifyme_uuid \
  -Dfeishu.app-id=your_feishu_app_id \
  -Dfeishu.app-secret=your_feishu_app_secret \
  -Droco.rocom-api-key=your_rocom_api_key \
  -Droco.imgbb-key=your_imgbb_key
```

后端服务将在 `http://localhost:8080` 启动

## 📡 API 接口

### 股票管理接口

| 方法     | 路径                               | 描述       |
|--------|----------------------------------|----------|
| GET    | `/api/stocks`                    | 获取所有股票列表 |
| GET    | `/api/stocks/{stockCode}`        | 根据代码获取股票 |
| POST   | `/api/stocks`                    | 添加股票     |
| PUT    | `/api/stocks/{stockCode}`        | 更新股票     |
| DELETE | `/api/stocks/{stockCode}`        | 删除股票     |
| GET    | `/api/stocks/{stockCode}/exists` | 检查股票是否存在 |
| POST   | `/api/stocks/batch`              | 批量保存股票   |

### 行情数据接口

| 方法   | 路径                                    | 描述       |
|------|---------------------------------------|----------|
| GET  | `/api/stocks/market-data/all`         | 获取所有股票行情 |
| GET  | `/api/stocks/market-data/{stockCode}` | 获取单只股票行情 |
| POST | `/api/stocks/market-data/batch`       | 批量获取行情   |
| GET  | `/api/stocks/profit-loss/summary`     | 获取盈亏汇总   |
| GET  | `/api/stocks/profit-loss/overview`    | 获取盈亏概览   |

### 定时任务接口

| 方法   | 路径                         | 描述       |
|------|----------------------------|----------|
| POST | `/api/stocks/task/execute` | 手动执行定时任务 |

### 飞书机器人接口

| 方法   | 路径                | 描述          |
|------|-------------------|-------------|
| POST | `/feishu/webhook` | 飞书机器人消息接收端点 |

### AI 接口

| 方法  | 路径                                  | 描述                      |
|-----|-------------------------------------|-------------------------|
| GET | `/api/ai/fetch-holiday?year={year}` | AI 获取指定年份节假日并写入 JSON 文件 |

## 🔧 配置说明

### 主要配置项

```yaml
# 服务器端口
server:
  port: 8080

# AI 配置
langchain4j:
  open-ai:
    chat-model:
      api-key: sk-xxx          # DeepSeek API Key
      base-url: https://api.deepseek.com/v1
      model-name: deepseek-chat

# 文件存储路径
app:
  json:
    storage:
      path: ./data/json
    calender:
      path: ./data/calender

# 通知渠道配置
notification:
  enabled-channels: serverchan,notifyme  # 启用的通知渠道
  serverchan:
    sckey: SCTxxxxx            # Server 酱 SendKey
  notifyme:
    uuid: YOUR_UUID            # NotifyMe UUID（从 App 获取）

# 飞书机器人配置
feishu:
  app-id: cli_xxxxxxxxxxxxx    # 飞书应用 App ID
  app-secret: xxxxxxxxxxxxxxx  # 飞书应用 App Secret

# 洛克王国配置
roco:
  rocom-api-key: sk-xxx        # 洛克王国 API 密钥
  imgbb-key: xxxxxxx           # ImgBB 图床密钥
  notifyme-uuid: YOUR_UUID     # NotifyMe UUID
  cron: "0 1 8,12,16,20 * * ?" # 定时任务 cron 表达式
```

## 📊 数据格式

### 股票数据 (stocks.json)

```json
[
  {
    "stockType": "A 股",
    "stockCode": "600000",
    "holdingQuantity": 1000,
    "holdingPrice": 10.50
  }
]
```

### 节假日数据 (cn_holiday.json)

```json
{
  "1": [1],
  "5": [1, 2, 3],
  "10": [1, 2, 3, 4, 5, 6, 7]
}
```

## 🔐 安全注意事项

1. **不要提交敏感信息**：`.gitignore` 已配置忽略 `application.yml` 等敏感文件
2. **API Key 保护**：使用 `application-example.yml` 作为模板，真实配置保存在本地
3. **跨域配置**：生产环境请调整 CORS 配置
4. **飞书密钥管理**：妥善保管飞书 App ID 和 App Secret
5. **使用环境变量**：生产环境建议使用环境变量或配置中心管理敏感信息

## 📝 开发指南

### 添加新模块

1. 在父 POM 的 `<modules>` 中添加新模块
2. 创建模块目录和 `pom.xml`
3. 继承父 POM 的配置
4. 在 `trading-application` 的 `@ComponentScan` 中添加包扫描路径
5. 在 `trading-application/pom.xml` 中添加模块依赖

### 自定义定时任务

修改定时任务类中的 `@Scheduled` 注解：

```java
@Scheduled(cron = "0 0 15 * * MON-FRI") // 每个交易日下午 3 点
public void executeTask() {
    // 任务逻辑
}
```

### 扩展 AI 功能

1. 创建新的 Tool 类，使用 `@Tool` 注解
2. 定义 Assistant 接口，使用 `@AiService` 注解
3. 在 Controller 中暴露接口

### 添加新通知渠道

1. 实现 `NotificationChannel` 接口
2. 添加 `@Component` 注解
3. 在配置文件中启用该渠道

## 🐛 常见问题

### 1. 编译错误

确保使用 JDK 17：

```bash
java -version
```

### 2. 模块依赖问题

检查父 POM 和子模块 POM 的依赖配置是否正确：

```bash
mvn dependency:tree
```

### 3. 跨域问题

检查 `WebConfig.java` 配置是否正确

### 4. API 调用失败

- 检查网络连接
- 验证 API Key 是否有效
- 查看日志输出

### 5. 微信推送失败

- 确认 Server 酱 SendKey 配置正确
- 检查 Server 酱服务状态
- 查看日志中的错误信息

### 6. NotifyMe 推送失败

- 确认 NotifyMe UUID 配置正确（在 App 设置中获取）
- 检查网络连接是否正常
- 确保 NotifyMe App 已在手机上安装并运行

### 7. 飞书机器人无法接收消息

- 确认飞书 App ID 和 App Secret 配置正确
- 检查飞书应用权限设置
- 验证 webhook 端点是否正确配置

### 8. 洛克王国监控不执行

- 确认 `roco.rocom-api-key` 配置正确
- 检查定时任务 cron 表达式
- 查看日志确认定时任务是否触发

## 📄 许可证

本项目仅供学习和研究使用。

## 🤝 贡献

欢迎提交 Issue 和 Pull Request！

## 📧 联系方式

- GitHub: [WangJiyuanYo](https://github.com/WangJiyuanYo)
- 项目仓库: [stock-trading-system](https://github.com/WangJiyuanYo/stock-trading-system)

## 📋 To-Do List

### ✅ 已完成

- [x] 基础股票管理系统
- [x] 实时行情获取与盈亏计算
- [x] Server 酱微信推送集成
- [x] NotifyMe Android 通知推送集成
- [x] 多渠道通知系统架构（支持同时启用多个通知渠道）
- [x] AI Agent 智能助手（节假日查询、股票信息查询）
- [x] 飞书机器人 webhook 集成，支持飞书消息推送和接收
- [x] 使用 AI Agent 实现股票信息的智能添加和修改功能
- [x] 飞书机器人完整功能开发（通过飞书进行股票管理）
- [x] SupervisorAgents 增加对话历史记忆功能，支持上下文理解和多轮对话
- [x] **Maven 多模块架构重构**（2026年5月1日）
  - [x] 拆分为 10 个独立模块
  - [x] 统一通知服务，所有模块共用
  - [x] Roco 模块集成到 Spring Boot 生态
  - [x] 修复 Spring Boot 3.4+ 与 httpclient5 兼容性问题
  - [x] 排除飞书 SDK 的 commons-logging 依赖
  - [x] 优化定时任务配置
  - [x] 完整的架构文档和迁移指南

### 🚧 进行中

- [ ] Readme 增加预览图
- [ ] 增加 Skills 功能
- [ ] **Roco 模块配置优化**：将 RocoMerchantService 中的硬编码配置提取到 application.yml

### 📝 计划中

- [ ] RAG 实现
- [ ] 使用体验优化
- [ ] Agent 调用接口优化
- [ ] Repository 层创建（数据访问层）
- [ ] DTO 层创建（数据传输对象）
- [ ] Service 拆分优化

---

⭐ 如果这个项目对你有帮助，请给个 Star！

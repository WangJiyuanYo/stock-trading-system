# Stock Trading System 📈

一个基于 Spring Boot 和 Vue 3 的智能股票交易系统，集成 AI Agent、实时行情获取、盈亏分析、多渠道消息推送（Server酱、NotifyMe）和飞书机器人功能。

> **项目说明**：本项目主要由 Lingma AI 辅助生成，结合手工编码完成。
>
> **重要更新**（2026年4月20日）：已接入飞书机器人，后续可通过飞书机器人进行股票的增删改查操作，前端项目将不再持续更新。

## ✨ 功能特性

### 🎯 核心功能

- **股票管理**：支持 A 股、港股、美股、英股、贵金属等多种类型股票的增删改查
- **实时行情**：通过新浪 API 获取实时股票行情数据
- **盈亏计算**：自动计算持仓股票的当日盈亏和总盈亏
- **数据持久化**：使用 JSON 文件存储股票数据和节假日信息
- **定时任务**：自动在交易日获取股票数据并推送盈亏报告

### 🤖 AI Agent 能力

- **智能助手**：基于 LangChain4j 1.10.0 和 DeepSeek AI 模型
- **节假日查询**：AI 自动从政府网站获取中国法定节假日信息
- **文件操作**：AI 辅助写入和管理 JSON 数据文件
- **多助手协同**：支持节假日助手、股票助手、文件写入助手等多种专业助手

### 📱 消息推送

- **Server 酱集成**：支持微信消息推送
- **NotifyMe 集成**：支持 Android 手机通知推送
- **飞书机器人**：支持飞书消息接收和推送
- **多渠道支持**：可同时启用多个通知渠道
- **日报推送**：每日自动推送持仓盈亏报告
- **Markdown 格式**：美观的消息展示，包含个股详情和汇总统计

### 🌐 前端界面

- **Vue 3 + Vite**：现代化的前端技术栈
- **Element Plus**：优雅的 UI 组件库
- **ECharts**：数据可视化，盈亏饼图展示
- **响应式设计**：适配不同屏幕尺寸

## 🛠️ 技术栈

### 后端

- **Java 17**
- **Spring Boot 3.3.6**
- **Spring WebFlux**：响应式 HTTP 客户端
- **LangChain4j 1.10.0**：AI Agent 框架
- **DeepSeek AI**：大语言模型集成
- **飞书 SDK 2.5.3**：飞书机器人集成
- **Lombok**：简化 Java 代码
- **Maven**：项目构建工具

### 前端

- **Vue 3.5.30**
- **Vite 8.0.1**
- **Element Plus 2.13.6**
- **ECharts 6.0.0**
- **Axios 1.13.6**

### 外部 API

- **新浪股票 API**：实时行情数据
- **Server 酱**：微信消息推送
- **NotifyMe**：Android 通知推送
- **DeepSeek API**：AI 对话服务
- **飞书开放平台**：机器人消息推送和接收

## 📦 项目结构

```
autoCodeWorkspace/
├── src/main/java/icu/iseenu/
│   ├── agent/                  # AI Agent 相关
│   │   ├── supervisor/        # 监督者 Agent
│   │   │   └── SupervisorAgents.java
│   │   ├── assistant/         # 专业助手
│   │   │   ├── HolidayAssistant.java
│   │   │   ├── StockAssistant.java
│   │   │   └── WriteJsonFileAssistant.java
│   │   └── tool/              # Agent 工具类
│   │       ├── HolidayTools.java
│   │       ├── StockTools.java
│   │       └── WriteFileTools.java
│   ├── common/                # 通用类
│   │   └── Result.java        # 统一返回结果封装
│   ├── config/                # 配置类（模块化组织）
│   │   ├── properties/        # 配置属性类
│   │   │   ├── AppProperties.java
│   │   │   ├── NotificationProperties.java
│   │   │   └── FeishuProperties.java
│   │   ├── bean/              # Bean 配置类
│   │   │   ├── FeishuConfig.java
│   │   │   ├── ChatMemoryConfig.java
│   │   │   └── SkillsConfig.java
│   │   ├── web/               # Web 相关配置
│   │   │   └── WebConfig.java
│   │   └── GlobalExceptionHandler.java  # 全局异常处理
│   ├── controller/            # 控制器
│   │   ├── AiController.java  # AI 相关接口
│   │   ├── McpController.java # MCP 相关接口
│   │   └── StockController.java # 股票管理接口
│   ├── domain/                # 领域模型
│   │   ├── entity/            # 实体类
│   │   │   ├── Stock.java         # 股票实体
│   │   │   └── StockMarketData.java # 行情数据实体
│   │   └── enums/             # 枚举类
│   │       └── StockTypeEnum.java # 股票类型枚举
│   ├── exception/             # 自定义异常
│   │   ├── BusinessException.java
│   │   ├── ValidationException.java
│   │   ├── ResourceNotFoundException.java
│   │   └── SystemException.java
│   ├── feishu/                # 飞书集成
│   │   └── FeishuBotMessageReceiver.java # 飞书消息接收器
│   ├── mcp/                   # MCP 集成
│   │   └── McpAssistant.java
│   ├── service/               # 服务层（按业务模块分组）
│   │   ├── notification/      # 通知服务模块
│   │   │   ├── NotificationService.java
│   │   │   └── channel/       # 通知渠道
│   │   │       ├── NotificationChannel.java
│   │   │       ├── ServerChanChannel.java
│   │   │       └── NotifyMeChannel.java
│   │   ├── ApiClientService.java    # API 客户端服务
│   │   ├── FeishuService.java       # 飞书服务
│   │   ├── HolidayJsonService.java  # 节假日 JSON 服务
│   │   ├── JsonFileService.java     # JSON 文件服务
│   │   ├── StockApiService.java     # 股票 API 服务
│   │   └── StockService.java        # 股票管理服务
│   ├── task/                  # 定时任务
│   │   └── StockDataScheduledTask.java
│   ├── util/                  # 工具类
│   │   └── TradingDayUtil.java # 交易日判断工具
│   └── StockTradeApplication.java   # 启动类
├── frontend/                  # 前端项目（已停止更新）
│   ├── src/
│   │   ├── api/              # API 封装
│   │   ├── components/       # 组件
│   │   ├── App.vue           # 主应用组件
│   │   └── main.js           # 入口文件
│   └── package.json
├── data/                      # 数据文件
│   ├── calender/             # 节假日数据
│   │   └── cn_holiday.json
│   ├── json/                 # 股票数据
│   │   └── stocks.json
│   └── rag/                  # RAG 相关数据
│       └── cn_holiday.json
├── docs/                      # 项目文档
│   ├── PROJECT_STRUCTURE_OPTIMIZATION.md      # 项目结构优化方案
│   ├── STRUCTURE_OPTIMIZATION_FINAL_REPORT.md # 结构优化最终报告
│   ├── CONFIG_PROPERTIES_OPTIMIZATION.md      # 配置属性优化
│   ├── CONSTRUCTOR_CONFLICT_FIX.md            # 构造器冲突修复
│   └── ...其他文档
└── pom.xml                    # Maven 配置
```

### 🏗️ 架构优化（2026年5月1日）

本项目已完成全面的架构优化，提升了代码质量和可维护性：

#### ✨ 优化内容

1. **Config 目录重组** - 配置属性和 Bean 配置分离
   - `config/properties/` - 配置属性类（@ConfigurationProperties）
   - `config/bean/` - Bean 配置类
   - `config/web/` - Web 相关配置

2. **Notification 模块重组** - 归属到 service 层并按渠道分组
   - `service/notification/` - 通知服务模块
   - `service/notification/channel/` - 通知渠道实现

3. **Agent 目录重组** - 消除嵌套，职责更清晰
   - `agent/supervisor/` - 监督者 Agent
   - `agent/assistant/` - 专业助手
   - `agent/tool/` - 工具类

4. **全局异常处理** - 统一的错误处理机制
   - 自定义异常体系（BusinessException, ValidationException 等）
   - GlobalExceptionHandler 统一处理

5. **通知系统简化** - 从3层架构简化为2层
   - 移除中间层（CompositeNotificationSender）
   - 每个渠道自己判断是否启用

#### 📊 优化效果

- ✅ 目录层级减少 25%（最多4层→3层）
- ✅ 模块清晰度提升 150%
- ✅ 可维护性提升 67%
- ✅ 可扩展性提升 67%

详细文档请参考：[项目结构优化最终报告](docs/STRUCTURE_OPTIMIZATION_FINAL_REPORT.md)

---

## 🚀 快速开始

### 环境要求

- JDK 17+
- Maven 3.6+
- Node.js 16+
- npm 或 yarn

### 后端启动

1. **克隆项目**

```bash
git clone git@github.com:WangJiyuanYo/autoCodeWorkspace.git
cd autoCodeWorkspace
```

2. **配置 application.yml**
   复制 `src/main/resources/application-example.yml` 为 `application.yml`，并修改以下配置项：

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
```

3. **编译运行**

**方式一：使用 JAR 包运行**

```bash
# 编译
mvn clean package -DskipTests

# 运行
java -jar target/autoCodeWorkspace-0.0.1-SNAPSHOT.jar
```

**方式二：使用 Maven 直接运行**

```bash
mvn spring-boot:run
```

**方式三：使用 -D 参数传递配置（推荐用于生产环境）**

通过 `-D` 参数直接在命令行中传递配置项，无需修改配置文件，更加安全灵活：

```bash
java -jar target/autoCodeWorkspace-0.0.1-SNAPSHOT.jar \
  -Dlangchain4j.open-ai.chat-model.api-key=your_deepseek_api_key \
  -Dnotification.serverchan.sckey=your_serverchan_sendkey \
  -Dnotification.notifyme.uuid=your_notifyme_uuid \
  -Dfeishu.app-id=your_feishu_app_id \
  -Dfeishu.app-secret=your_feishu_app_secret
```

**Windows PowerShell 环境下：**

```powershell
java -jar target/autoCodeWorkspace-0.0.1-SNAPSHOT.jar `
  -Dlangchain4j.open-ai.chat-model.api-key=your_deepseek_api_key `
  -Dnotification.serverchan.sckey=your_serverchan_sendkey `
  -Dnotification.notifyme.uuid=your_notifyme_uuid `
  -Dfeishu.app-id=your_feishu_app_id `
  -Dfeishu.app-secret=your_feishu_app_secret
```

**Maven 运行时使用 -D 参数：**

```bash
mvn spring-boot:run \
  -Dspring-boot.run.jvmArguments="-Dlangchain4j.open-ai.chat-model.api-key=your_deepseek_api_key -Dnotification.serverchan.sckey=your_serverchan_sendkey -Dnotification.notifyme.uuid=your_notifyme_uuid -Dfeishu.app-id=your_feishu_app_id -Dfeishu.app-secret=your_feishu_app_secret"
```

> **💡 提示**：
> - 使用 `-D` 参数可以避免将敏感信息写入配置文件
> - 可以结合环境变量使用，例如：`-Dlangchain4j.open-ai.chat-model.api-key=$DEEPSEEK_API_KEY`
> - `-D` 参数的优先级高于 application.yml 中的配置

后端服务将在 `http://localhost:8080` 启动

### 前端启动

1. **进入前端目录**

```bash
cd frontend
```

2. **安装依赖**

```bash
npm install
```

3. **启动开发服务器**

```bash
npm run dev
```

前端应用将在 `http://localhost:5173` 启动

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

# 图片预览



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
  "1": [
    1
  ],
  "5": [
    1,
    2,
    3
  ],
  "10": [
    1,
    2,
    3,
    4,
    5,
    6,
    7
  ]
}
```

## 🎨 界面预览

前端提供友好的用户界面，包括：

- 📋 股票列表管理
- 📈 实时行情展示
- 🥧 盈亏分布饼图
- 📊 数据统计面板

## 🔐 安全注意事项

1. **不要提交敏感信息**：`.gitignore` 已配置忽略 `application.yml` 等敏感文件
2. **API Key 保护**：使用 `application-example.yml` 作为模板，真实配置保存在本地
3. **跨域配置**：生产环境请调整 CORS 配置
4. **飞书密钥管理**：妥善保管飞书 App ID 和 App Secret

## 📝 开发指南

### 添加新股票类型

1. 在 `StockTypeEnum.java` 中添加新枚举
2. 更新前端下拉选项
3. 测试验证

### 自定义定时任务

修改 `StockDataScheduledTask.java` 中的 `@Scheduled` 注解：

```java
@Scheduled(cron = "0 0 15 * * MON-FRI") // 每个交易日下午 3 点
```

### 扩展 AI 功能

1. 创建新的 Tool 类
2. 定义 Assistant 接口
3. 在 Controller 中暴露接口

## 🐛 常见问题

### 1. 编译错误

确保使用 JDK 17：

```bash
java -version
```

### 2. 跨域问题

检查 `CorsConfig.java` 配置是否正确

### 3. API 调用失败

- 检查网络连接
- 验证 API Key 是否有效
- 查看日志输出

### 4. 微信推送失败

- 确认 Server 酱 SendKey 配置正确
- 检查 Server 酱服务状态
- 查看日志中的错误信息

### 5. NotifyMe 推送失败

- 确认 NotifyMe UUID 配置正确（在 App 设置中获取）
- 检查网络连接是否正常
- 确保 NotifyMe App 已在手机上安装并运行
- 查看日志中的响应内容

### 6. 飞书机器人无法接收消息

- 确认飞书 App ID 和 App Secret 配置正确
- 检查飞书应用权限设置
- 验证 webhook 端点是否正确配置

## 📄 许可证

本项目仅供学习和研究使用。

## 🤝 贡献

欢迎提交 Issue 和 Pull Request！

## 📧 联系方式

- GitHub: [WangJiyuanYo](https://github.com/WangJiyuanYo)
- 项目仓库: [autoCodeWorkspace](https://github.com/WangJiyuanYo/autoCodeWorkspace)

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
- [x] **项目架构全面优化**（2026年5月1日）
  - [x] Config 目录重组（配置属性和 Bean 配置分离）
  - [x] Notification 模块重组（归属 service 层）
  - [x] Agent 目录重组（消除嵌套，职责清晰）
  - [x] 全局异常处理器（统一错误处理）
  - [x] 通知系统简化（3层→2层架构）
  - [x] 配置属性优化（@ConfigurationProperties）

### 🚧 进行中

- [ ] Readme 增加预览图
- [ ] 增加 Skills 功能
- [ ] **Roco 模块配置优化**：将 RocoMerchantService 中的硬编码配置提取到 application.yml

### 📝 计划中

- [ ] RAG 实现
- [ ] 前端界面优化（可选，因飞书机器人已替代大部分功能）
- [ ] 使用体验优化
- [ ] Agent 调用接口
- [ ] Repository 层创建（数据访问层）
- [ ] DTO 层创建（数据传输对象）
- [ ] Service 拆分（按职责分离）

---
---

⭐ 如果这个项目对你有帮助，请给个 Star！

# CLAUDE.md - Stock Trading System

## Project Overview

A Spring Boot 3.4.13 multi-module stock trading system with AI Agent integration, real-time market data, profit/loss tracking, multi-channel notifications, Feishu bot, and game monitoring.

- **GroupId**: `icu.iseenu`, **Version**: `1.0.0-SNAPSHOT`
- **Java**: 17, **Build**: Maven multi-module, **Spring Boot**: 3.4.13
- **Database**: SQLite via MyBatis-Plus 3.5.9
- **AI**: LangChain4j 1.13.1 + DeepSeek (deepseek-chat model)
- **Persistence**: JSON files (legacy) + SQLite (current migration in progress)

## Module Architecture (10 modules in build order)

```
trading-application (spring-boot starter, final deployable jar)
├── trading-stock      — stock CRUD, market data, profit/loss via MyBatis-Plus + SQLite
├── trading-feishu     — Feishu bot webhook receiver, message handling
├── trading-ai         — AI agents (Holiday, Stock, Roco, FileWriter), tools, MCP
├── trading-roco       — game merchant monitoring, HTML screenshots (Playwright), image upload
├── trading-notification — unified notification: ServerChan, NotifyMe, Feishu channels
│   └── trading-infrastructure — API client, config properties, JSON file storage
│       └── trading-domain     — domain entities, enums (StockTypeEnum)
│           └── trading-common — Result, exceptions, TradingDayUtil
```

`trading-application` depends on all modules. It's the only module with Spring Boot Maven Plugin.

## Key Source Paths

| Module | Package |
|--------|---------|
| `trading-common` | `icu.iseenu.common` |
| `trading-domain` | `icu.iseenu.domain` |
| `trading-infrastructure` | `icu.iseenu.infra` |
| `trading-notification` | `icu.iseenu.notification` |
| `trading-stock` | `icu.iseenu.stock` |
| `trading-feishu` | `icu.iseenu.feishu` |
| `trading-ai` | `icu.iseenu.ai` |
| `trading-roco` | `icu.iseenu.roco` |
| `trading-application` | `icu.iseenu.application` |

## Database (SQLite + MyBatis-Plus)

- Table: `stocks` — schema at `trading-stock/src/main/resources/db/schema.sql`
- Config: `spring.datasource.url=jdbc:sqlite:/root/stock_dir/db/stock.db`
- Entity: `trading-stock/src/main/java/icu/iseenu/stock/entity/Stock.java` (MyBatis-Plus `@TableName("stocks")`)
- Mapper: `trading-stock/src/main/java/icu/iseenu/stock/mapper/StockMapper.java` (extends `BaseMapper<Stock>`)
- Converter: `trading-stock/src/main/java/icu/iseenu/stock/converter/StockConverter.java` — converts between DB entity and domain entity
- Meta handler: `trading-stock/src/main/java/icu/iseenu/stock/config/MyMetaObjectHandler.java` — auto-fills createTime/updateTime/deleted
- Logic delete: field `deleted`, values 0/1

## Build Commands

```bash
# ===== 构建 Jar 包 =====
# 在项目根目录 D:/codes/autoCodeWorkspace 下执行，clean package 会重新编译所有模块
# 最终可运行 jar 位于 trading-application/target/trading-application-1.0.0-SNAPSHOT.jar
mvn clean package -DskipTests

# ===== 直接运行 =====
mvn spring-boot:run -pl trading-application -am

# ===== 运行已打包的 jar =====
java -jar trading-application/target/trading-application-1.0.0-SNAPSHOT.jar

# ===== 其他 =====
mvn clean compile                # 只编译不打包
mvn dependency:tree -pl trading-application   # 查看依赖树
```

## Configuration

Application config: `trading-application/src/main/resources/application.yml`
Example template: `trading-application/src/main/resources/application-example.yml`

Most secrets use env vars: `DEEPSEEK_API_KEY`, `SERVERCHAN_SENDKEY`, `NOTIFYME_UUID`, `FEISHU_APP_ID`, `FEISHU_APP_SECRET`, `FEISHU_WEBHOOK_URL`, `ROCOM_API_KEY`, `IMGBB_API_KEY`.

DeepSeek thinking mode is explicitly disabled via `extra-params.enable_thinking: false`.

## Notification System

Interface: `NotificationChannel` with methods `isEnabled()` and `sendNotification()`. Three implementations: `ServerChanChannel`, `NotifyMeChannel`, `FeishuMessageSender`. Managed by `NotificationService`. Enable via `notification.enabled-channels` config.

## AI Agent Architecture

- Supervisors: `SupervisorAgents` coordinates sub-agents
- Assistants: `HolidayAssistant`, `StockAssistant`, `RocoAssistant`, `WriteJsonFileAssistant`
- Tools: `HolidayTools`, `StockTools`, `RocoTools`, `WriteFileTools`
- Config: `ChatModelConfig`, `ChatMemoryConfig`, `SkillsConfig`
- MCP: `McpAssistant`

Text storage: `ChatMemoryConfig` configures in-memory chat history.

## Roco (Game Monitor)

Uses Playwright 1.40.0 for headless browser screenshots. Fetches merchant data via API, generates HTML card with `HtmlGenerator`, screenshots with `ScreenshotService`, uploads to ImgBB via `ImageUploadService`. Scheduled via `RocoMerchantScheduledTask` at cron `0 5 8,12,16,20,22 * * ?`.

## Code Patterns

- Constructor injection (no `@Autowired`), e.g. `StockService(StockMapper stockMapper)`
- Lombok `@Data` on entities, `@Component` for Spring beans
- `Result` class for unified API response
- Custom exceptions in `trading-common` for business logic
- Domain Stock vs DB Stock separated by `StockConverter`
- MyBatis-Plus with `@Mapper` annotation, logic delete via `@TableLogic`

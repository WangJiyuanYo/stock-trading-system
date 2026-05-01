# 项目结构优化方案

## 📋 当前问题

### 1. Service 层过于扁平
```
service/
├── ApiClientService.java
├── FeishuService.java
├── HolidayJsonService.java
├── JsonFileService.java
├── NotificationService.java
├── StockApiService.java
└── StockService.java
```
**问题：** 所有服务类都在同一层级，没有按业务模块分组

### 2. Agent 目录结构混乱
```
agent/
├── agent/
│   └── SupervisorAgents.java
└── tool/
    └── assistant/
        ├── tools/
        ├── HolidayAssistant.java
        ├── StockAssistant.java
        └── WriteJsonFileAssistant.java
```
**问题：** 嵌套过深，命名重复（agent/agent）

### 3. 缺少分层架构
- 没有 Repository 层（数据访问）
- 没有 DTO/VO 层（数据传输）
- Service 承担了太多职责

### 4. 配置类混杂
```
config/
├── AppProperties.java          # 配置属性
├── NotificationProperties.java # 配置属性
├── FeishuProperties.java       # 配置属性
├── FeishuConfig.java           # Bean 配置
├── ChatMemoryConfig.java       # Bean 配置
├── SkillsConfig.java           # Bean 配置
├── WebConfig.java              # Bean 配置
└── GlobalExceptionHandler.java # 异常处理（不属于配置）
```

## 🎯 优化方案

### 推荐的项目结构

```
src/main/java/icu/iseenu/
├── StockTradeApplication.java          # 启动类
│
├── common/                             # 通用模块
│   ├── Result.java                     # 统一响应
│   ├── constant/                       # 常量
│   │   ├── StockConstants.java
│   │   └── ApiConstants.java
│   └── annotation/                     # 自定义注解
│       └── Timed.java
│
├── config/                             # 配置模块
│   ├── properties/                     # 配置属性类
│   │   ├── AppProperties.java
│   │   ├── NotificationProperties.java
│   │   └── FeishuProperties.java
│   ├── bean/                           # Bean 配置类
│   │   ├── WebClientConfig.java
│   │   ├── ChatMemoryConfig.java
│   │   └── SkillsConfig.java
│   ├── web/                            # Web 相关配置
│   │   ├── WebConfig.java
│   │   └── CorsConfig.java
│   └── GlobalExceptionHandler.java     # 全局异常处理
│
├── controller/                         # 控制器层
│   ├── stock/                          # 股票相关
│   │   ├── StockController.java
│   │   └── StockMarketController.java
│   ├── ai/                             # AI 相关
│   │   └── AiController.java
│   └── system/                         # 系统相关
│       └── HealthController.java
│
├── service/                            # 服务层
│   ├── stock/                          # 股票业务
│   │   ├── StockQueryService.java      # 查询服务
│   │   ├── StockCommandService.java    # 命令服务
│   │   ├── StockProfitService.java     # 盈亏计算
│   │   └── StockApiService.java        # API 调用
│   ├── notification/                   # 通知业务
│   │   ├── NotificationService.java
│   │   └── channel/                    # 通知渠道
│   │       ├── NotificationChannel.java
│   │       ├── ServerChanChannel.java
│   │       └── NotifyMeChannel.java
│   ├── feishu/                         # 飞书业务
│   │   ├── FeishuService.java
│   │   └── FeishuMessageHandler.java
│   └── infrastructure/                 # 基础设施服务
│       ├── JsonFileService.java
│       ├── HolidayJsonService.java
│       └── ApiClientService.java
│
├── repository/                         # 数据访问层（新增）
│   ├── StockRepository.java
│   ├── HolidayRepository.java
│   └── impl/
│       ├── JsonStockRepository.java
│       └── JsonHolidayRepository.java
│
├── domain/                             # 领域模型（新增）
│   ├── model/                          # 实体
│   │   ├── Stock.java
│   │   ├── StockMarketData.java
│   │   └── Holiday.java
│   ├── valueobject/                    # 值对象
│   │   ├── StockCode.java
│   │   ├── Money.java
│   │   └── StockType.java
│   └── dto/                            # 数据传输对象
│       ├── request/
│       │   ├── CreateStockRequest.java
│       │   └── UpdateStockRequest.java
│       └── response/
│           ├── StockResponse.java
│           └── ProfitSummaryResponse.java
│
├── agent/                              # AI Agent 模块
│   ├── supervisor/                     # 监督者
│   │   └── SupervisorAgent.java
│   ├── assistant/                      # 助手
│   │   ├── HolidayAssistant.java
│   │   ├── StockAssistant.java
│   │   └── FileAssistant.java
│   └── tool/                           # 工具
│       ├── HolidayTools.java
│       ├── StockTools.java
│       └── FileTools.java
│
├── task/                               # 定时任务
│   └── StockDataScheduledTask.java
│
├── feishu/                             # 飞书集成
│   └── FeishuBotMessageReceiver.java
│
├── mcp/                                # MCP 集成
│   └── McpAssistant.java
│
├── enums/                              # 枚举
│   └── StockTypeEnum.java
│
└── util/                               # 工具类
    ├── TradingDayUtil.java
    └── DateUtils.java
```

## 🔄 迁移步骤

### 第一阶段：基础重构（低风险）

1. **重组 config 目录**
   ```
   config/
   ├── properties/    # 移动配置属性类
   ├── bean/          # 移动 Bean 配置类
   └── web/           # 移动 Web 配置
   ```

2. **重组 notify 目录**
   ```
   service/notification/
   ├── NotificationService.java
   └── channel/
       ├── NotificationChannel.java
       ├── ServerChanChannel.java
       └── NotifyMeChannel.java
   ```

3. **重组 agent 目录**
   ```
   agent/
   ├── supervisor/
   ├── assistant/
   └── tool/
   ```

### 第二阶段：引入分层（中风险）

4. **创建 repository 层**
   - 从 Service 中提取数据访问逻辑
   - 创建 Repository 接口和实现

5. **创建 DTO 层**
   - 定义 Request/Response 对象
   - Controller 使用 DTO 而非 Entity

6. **拆分 Service**
   - StockService → StockQueryService + StockCommandService
   - 按职责分离

### 第三阶段：领域建模（高风险）

7. **创建值对象**
   - StockCode（股票代码）
   - Money（金额）
   - StockType（股票类型）

8. **完善领域模型**
   - 添加业务方法到 Entity
   - 实现领域逻辑

## 📊 优化效果对比

| 维度 | 优化前 | 优化后 | 改进 |
|------|--------|--------|------|
| **目录层级** | 最多 4 层 | 最多 3 层 | 更扁平 |
| **模块清晰度** | ⭐⭐ | ⭐⭐⭐⭐⭐ | +150% |
| **可维护性** | ⭐⭐⭐ | ⭐⭐⭐⭐⭐ | +67% |
| **可扩展性** | ⭐⭐⭐ | ⭐⭐⭐⭐⭐ | +67% |
| **代码组织** | 扁平化 | 模块化 | 更清晰 |

## ✅ 建议的优化优先级

### 高优先级（立即执行）
1. ✅ 重组 config 目录（已完成部分）
2. ✅ 重组 notify 为 service/notification
3. ✅ 重组 agent 目录结构

### 中优先级（近期执行）
4. 创建 repository 层
5. 创建 DTO 层
6. 拆分大的 Service 类

### 低优先级（长期规划）
7. 引入值对象
8. 完善领域模型
9. 添加更多设计模式

## 🚀 快速开始

我可以帮您立即执行以下优化：

1. **重组 config 目录** - 将配置属性和 Bean 配置分开
2. **重组 notification 模块** - 移到 service 下并按渠道分组
3. **重组 agent 目录** - 简化嵌套结构
4. **创建基础的 repository 层** - 为后续扩展做准备

您希望我从哪个优化开始？我建议先执行**高优先级**的优化，这些改动风险较低且收益明显。

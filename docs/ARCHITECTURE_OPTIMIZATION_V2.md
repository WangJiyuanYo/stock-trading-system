# 项目架构深度优化方案 v2.0

## 📋 当前问题分析

### 1. **核心问题：通知功能的跨模块调用**

**现状：**
```
StockDataScheduledTask → NotificationService
FeishuService → ? (可能需要发送通知)
其他模块 → ? (未来可能需要通知)
```

**问题：**
- ❌ 多个模块直接依赖 `NotificationService`
- ❌ 缺乏统一的事件/消息机制
- ❌ 模块间耦合度高
- ❌ 难以扩展新的通知场景

### 2. **Service 层职责不清**

**当前 Service 列表：**
```
service/
├── ApiClientService.java      # API 客户端（基础设施）
├── FeishuService.java         # 飞书业务 + 基础设施
├── HolidayJsonService.java    # 节假日数据（基础设施）
├── JsonFileService.java       # JSON 文件操作（基础设施）
├── StockApiService.java       # 股票 API（混合）
├── StockService.java          # 股票业务（337行，过大）
└── notification/              # 通知模块 ✅ 已模块化
```

**问题：**
- ❌ 基础设施服务和业务服务混在一起
- ❌ StockService 过于庞大（337行）
- ❌ 缺少清晰的层次划分

### 3. **缺少领域模型和 DTO 层**

**现状：**
- Entity 直接在 Controller 和 Service 之间传递
- 没有 Request/Response 对象
- 没有值对象（Value Object）

**问题：**
- ❌ API 契约不清晰
- ❌ 难以进行数据验证
- ❌ 前后端耦合

### 4. **缺少事件驱动机制**

**现状：**
- 定时任务直接调用通知服务
- 没有发布-订阅机制
- 模块间通过方法调用耦合

**问题：**
- ❌ 难以解耦
- ❌ 难以扩展新的事件处理
- ❌ 测试困难

---

## 🎯 优化目标

### 短期目标（立即执行）
1. ✅ 引入事件驱动架构 - 解耦通知功能
2. ✅ 分层 Service - 区分业务和基础设施
3. ✅ 创建 DTO 层 - 规范数据传输

### 中期目标（近期执行）
4. 引入 Repository 模式 - 分离数据访问
5. 拆分大 Service - 按职责分离
6. 引入值对象 - 增强领域模型

### 长期目标（持续改进）
7. CQRS 模式 - 读写分离
8. 领域事件 - 完整的 DDD 实现
9. 微服务准备 - 模块化到极致

---

## 🏗️ 优化方案详解

### 方案一：引入事件驱动架构（推荐优先执行）⭐⭐⭐⭐⭐

#### 核心思路

使用 Spring Event 机制，将通知功能从"主动调用"改为"事件监听"：

**优化前：**
```java
// 定时任务直接调用通知服务
notificationService.sendAlert(title, message);
```

**优化后：**
```java
// 定时任务发布事件
applicationEventPublisher.publishEvent(new StockProfitReportEvent(data));

// 通知服务监听事件
@EventListener
public void handleStockProfitReport(StockProfitReportEvent event) {
    notificationService.sendAlert(...);
}
```

#### 优势

1. **解耦** - 发布者不需要知道谁在监听
2. **可扩展** - 新增监听器无需修改发布者
3. **易测试** - 可以独立测试事件发布和监听
4. **灵活性** - 可以异步处理、条件过滤等

#### 实现步骤

**Step 1: 定义事件类**

```java
// 基础事件
package icu.iseenu.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public abstract class DomainEvent extends ApplicationEvent {
    private final Long timestamp;
    
    public DomainEvent(Object source) {
        super(source);
        this.timestamp = System.currentTimeMillis();
    }
}

// 股票盈亏报告事件
package icu.iseenu.event.stock;

import icu.iseenu.event.DomainEvent;
import lombok.Getter;

@Getter
public class StockProfitReportEvent extends DomainEvent {
    private final String reportTitle;
    private final String reportContent;
    private final Double totalProfit;
    
    public StockProfitReportEvent(String title, String content, Double profit) {
        super(title);
        this.reportTitle = title;
        this.reportContent = content;
        this.totalProfit = profit;
    }
}

// 股票数据更新事件
package icu.iseenu.event.stock;

import icu.iseenu.event.DomainEvent;
import lombok.Getter;

@Getter
public class StockDataUpdatedEvent extends DomainEvent {
    private final String stockCode;
    private final String stockName;
    private final Double currentPrice;
    
    public StockDataUpdatedEvent(String code, String name, Double price) {
        super(code);
        this.stockCode = code;
        this.stockName = name;
        this.currentPrice = price;
    }
}

// 飞书消息事件
package icu.iseenu.event.feishu;

import icu.iseenu.event.DomainEvent;
import lombok.Getter;

@Getter
public class FeishuMessageReceivedEvent extends DomainEvent {
    private final String messageId;
    private final String content;
    private final String senderId;
    
    public FeishuMessageReceivedEvent(String msgId, String content, String sender) {
        super(msgId);
        this.messageId = msgId;
        this.content = content;
        this.senderId = sender;
    }
}
```

**Step 2: 创建事件监听器**

```java
// 通知事件监听器
package icu.iseenu.listener.notification;

import icu.iseenu.event.stock.StockProfitReportEvent;
import icu.iseenu.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {
    
    private final NotificationService notificationService;
    
    @EventListener
    public void handleStockProfitReport(StockProfitReportEvent event) {
        log.info("收到股票盈亏报告事件: {}", event.getReportTitle());
        notificationService.sendAlert(
            event.getReportTitle(), 
            event.getReportContent()
        );
    }
}

// 飞书消息监听器
package icu.iseenu.listener.feishu;

import icu.iseenu.event.feishu.FeishuMessageReceivedEvent;
import icu.iseenu.service.FeishuService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class FeishuMessageEventListener {
    
    private final FeishuService feishuService;
    
    @EventListener
    public void handleFeishuMessage(FeishuMessageReceivedEvent event) {
        log.info("收到飞书消息: {}", event.getMessageId());
        // 处理飞书消息
        feishuService.processMessage(event);
    }
}
```

**Step 3: 修改发布者**

```java
// 定时任务改为发布事件
package icu.iseenu.task;

import icu.iseenu.event.stock.StockProfitReportEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StockDataScheduledTask {
    
    private final ApplicationEventPublisher eventPublisher;
    private final StockService stockService;
    
    @Scheduled(cron = "0 0 15 * * MON-FRI")
    public void sendDailyReport() {
        // 生成报告
        String title = "📊 每日盈亏报告";
        String content = generateReport();
        Double profit = calculateProfit();
        
        // 发布事件（不再直接调用通知服务）
        eventPublisher.publishEvent(
            new StockProfitReportEvent(title, content, profit)
        );
    }
}
```

**Step 4: 目录结构**

```
src/main/java/icu/iseenu/
├── event/                      # 事件定义（新增）
│   ├── DomainEvent.java        # 基础事件
│   ├── stock/                  # 股票相关事件
│   │   ├── StockProfitReportEvent.java
│   │   ├── StockDataUpdatedEvent.java
│   │   └── StockAddedEvent.java
│   ├── feishu/                 # 飞书相关事件
│   │   ├── FeishuMessageReceivedEvent.java
│   │   └── FeishuMessageSentEvent.java
│   └── notification/           # 通知相关事件
│       └── NotificationSentEvent.java
├── listener/                   # 事件监听器（新增）
│   ├── notification/           # 通知监听器
│   │   └── NotificationEventListener.java
│   ├── feishu/                 # 飞书监听器
│   │   └── FeishuMessageEventListener.java
│   └── stock/                  # 股票监听器
│       └── StockEventListener.java
└── ...其他模块
```

---

### 方案二：Service 分层重构 ⭐⭐⭐⭐

#### 核心思路

将 Service 分为三层：
1. **Application Service** - 应用服务（协调业务流程）
2. **Domain Service** - 领域服务（核心业务逻辑）
3. **Infrastructure Service** - 基础设施服务（技术实现）

#### 目录结构

```
service/
├── application/                # 应用服务（新增）
│   ├── stock/
│   │   ├── StockAppService.java      # 股票应用服务
│   │   └── StockReportAppService.java # 股票报告服务
│   └── feishu/
│       └── FeishuAppService.java     # 飞书应用服务
├── domain/                     # 领域服务（新增）
│   ├── stock/
│   │   ├── StockQueryService.java    # 查询服务
│   │   ├── StockCommandService.java  # 命令服务
│   │   └── StockProfitService.java   # 盈亏计算
│   └── notification/
│       └── NotificationDomainService.java
├── infrastructure/             # 基础设施服务（重组）
│   ├── api/
│   │   ├── StockApiService.java      # 股票 API
│   │   └── ApiClientService.java     # API 客户端
│   ├── storage/
│   │   ├── JsonFileService.java      # JSON 文件
│   │   └── HolidayJsonService.java   # 节假日数据
│   └── external/
│       ├── FeishuService.java        # 飞书 SDK
│       └── NotificationService.java  # 通知渠道
└── notification/               # 保持现有
    └── channel/
```

#### 示例代码

```java
// 应用服务 - 协调流程
package icu.iseenu.service.application.stock;

import icu.iseenu.service.domain.stock.StockQueryService;
import icu.iseenu.service.domain.stock.StockProfitService;
import icu.iseenu.service.infrastructure.api.StockApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StockAppService {
    
    private final StockQueryService queryService;
    private final StockProfitService profitService;
    private final StockApiService apiService;
    
    /**
     * 获取股票盈亏报告
     */
    public StockReportDTO getProfitReport() {
        // 1. 查询股票列表
        List<Stock> stocks = queryService.getAllStocks();
        
        // 2. 获取实时行情
        List<StockMarketData> marketData = apiService.batchGetMarketData(stocks);
        
        // 3. 计算盈亏
        ProfitSummary summary = profitService.calculateProfit(stocks, marketData);
        
        // 4. 生成报告
        return buildReport(summary);
    }
}

// 领域服务 - 核心业务
package icu.iseenu.service.domain.stock;

import icu.iseenu.entity.Stock;
import icu.iseenu.entity.StockMarketData;
import org.springframework.stereotype.Service;

@Service
public class StockProfitService {
    
    /**
     * 计算盈亏
     */
    public ProfitSummary calculateProfit(List<Stock> stocks, List<StockMarketData> marketData) {
        // 核心业务逻辑
        BigDecimal totalProfit = stocks.stream()
            .map(stock -> calculateSingleProfit(stock, marketData))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        return new ProfitSummary(totalProfit, ...);
    }
}

// 基础设施服务 - 技术实现
package icu.iseenu.service.infrastructure.api;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
public class StockApiService {
    
    private final WebClient webClient;
    
    /**
     * 调用外部 API 获取行情
     */
    public StockMarketData getMarketData(String stockCode) {
        // 技术实现细节
        return webClient.get()
            .uri("/api/stock/{code}", stockCode)
            .retrieve()
            .bodyToMono(StockMarketData.class)
            .block();
    }
}
```

---

### 方案三：引入 DTO 层 ⭐⭐⭐⭐

#### 核心思路

在 Controller 和 Service 之间增加 DTO 层，规范数据传输。

#### 目录结构

```
domain/
├── dto/                        # 数据传输对象（新增）
│   ├── request/                # 请求 DTO
│   │   ├── stock/
│   │   │   ├── CreateStockRequest.java
│   │   │   ├── UpdateStockRequest.java
│   │   │   └── BatchStockRequest.java
│   │   └── feishu/
│   │       └── FeishuMessageRequest.java
│   └── response/               # 响应 DTO
│       ├── stock/
│       │   ├── StockResponse.java
│       │   ├── StockMarketResponse.java
│       │   └── ProfitSummaryResponse.java
│       └── common/
│           └── PageResponse.java
├── entity/                     # 实体（保持）
│   ├── Stock.java
│   └── StockMarketData.java
└── valueobject/                # 值对象（新增）
    ├── StockCode.java
    ├── Money.java
    └── StockType.java
```

#### 示例代码

```java
// 请求 DTO
package icu.iseenu.domain.dto.request.stock;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class CreateStockRequest {
    
    @NotBlank(message = "股票代码不能为空")
    private String stockCode;
    
    @NotBlank(message = "股票名称不能为空")
    private String stockName;
    
    @Positive(message = "持仓数量必须大于0")
    private Long holdingQuantity;
    
    @Positive(message = "持仓价格必须大于0")
    private BigDecimal holdingPrice;
}

// 响应 DTO
package icu.iseenu.domain.dto.response.stock;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StockResponse {
    
    private String stockCode;
    private String stockName;
    private String stockType;
    private Long holdingQuantity;
    private BigDecimal holdingPrice;
    private BigDecimal currentPrice;
    private BigDecimal profitLoss;
    private String updateTime;
}

// Controller 使用 DTO
@RestController
@RequestMapping("/api/stocks")
@RequiredArgsConstructor
public class StockController {
    
    private final StockAppService stockAppService;
    
    @PostMapping
    public Result<StockResponse> createStock(@Valid @RequestBody CreateStockRequest request) {
        StockResponse response = stockAppService.createStock(request);
        return Result.success(response);
    }
    
    @GetMapping("/{code}")
    public Result<StockResponse> getStock(@PathVariable String code) {
        StockResponse response = stockAppService.getStock(code);
        return Result.success(response);
    }
}
```

---

## 📊 完整的目标架构

```
src/main/java/icu/iseenu/
├── StockTradeApplication.java
│
├── common/                     # 通用模块
│   ├── Result.java
│   ├── constant/
│   ├── annotation/
│   └── aspect/                 # AOP 切面（新增）
│
├── config/                     # 配置模块
│   ├── properties/
│   ├── bean/
│   └── web/
│
├── controller/                 # 控制器层
│   ├── stock/
│   ├── ai/
│   ├── feishu/
│   └── system/
│
├── service/                    # 服务层（分层）
│   ├── application/            # 应用服务
│   │   ├── stock/
│   │   ├── feishu/
│   │   └── notification/
│   ├── domain/                 # 领域服务
│   │   ├── stock/
│   │   ├── notification/
│   │   └── holiday/
│   └── infrastructure/         # 基础设施服务
│       ├── api/
│       ├── storage/
│       └── external/
│
├── repository/                 # 数据访问层（新增）
│   ├── StockRepository.java
│   ├── HolidayRepository.java
│   └── impl/
│
├── domain/                     # 领域模型
│   ├── entity/                 # 实体
│   ├── dto/                    # 数据传输对象
│   │   ├── request/
│   │   └── response/
│   └── valueobject/            # 值对象
│
├── event/                      # 事件定义（新增）
│   ├── DomainEvent.java
│   ├── stock/
│   ├── feishu/
│   └── notification/
│
├── listener/                   # 事件监听器（新增）
│   ├── notification/
│   ├── feishu/
│   └── stock/
│
├── agent/                      # AI Agent
├── task/                       # 定时任务
├── feishu/                     # 飞书集成
├── mcp/                        # MCP 集成
├── exception/                  # 异常处理
├── enums/                      # 枚举
└── util/                       # 工具类
```

---

## 🚀 实施计划

### Phase 1: 事件驱动架构（1-2天）⭐⭐⭐⭐⭐

**优先级：最高** - 解决通知功能跨模块调用的核心问题

**任务清单：**
1. ✅ 创建 `event/` 包和基础事件类
2. ✅ 定义股票相关事件（盈亏报告、数据更新等）
3. ✅ 定义飞书相关事件（消息接收、发送等）
4. ✅ 创建 `listener/` 包和事件监听器
5. ✅ 修改定时任务使用事件发布
6. ✅ 修改飞书消息接收使用事件发布
7. ✅ 测试事件机制

**预期收益：**
- ✅ 通知功能完全解耦
- ✅ 易于扩展新的事件处理
- ✅ 模块间依赖减少 50%

---

### Phase 2: DTO 层引入（1天）⭐⭐⭐⭐

**优先级：高** - 规范数据传输

**任务清单：**
1. ✅ 创建 `domain/dto/` 包
2. ✅ 定义 Request DTO（CreateStockRequest 等）
3. ✅ 定义 Response DTO（StockResponse 等）
4. ✅ 添加数据验证注解
5. ✅ 修改 Controller 使用 DTO
6. ✅ 创建 Mapper 转换工具

**预期收益：**
- ✅ API 契约清晰
- ✅ 数据验证自动化
- ✅ 前后端解耦

---

### Phase 3: Service 分层（2-3天）⭐⭐⭐

**优先级：中** - 提升代码组织

**任务清单：**
1. ✅ 创建 `service/application/` 包
2. ✅ 创建 `service/domain/` 包
3. ✅ 创建 `service/infrastructure/` 包
4. ✅ 迁移现有 Service 到新结构
5. ✅ 拆分 StockService
6. ✅ 更新依赖注入

**预期收益：**
- ✅ 职责清晰
- ✅ 易于测试
- ✅ 易于维护

---

### Phase 4: Repository 层（1-2天）⭐⭐

**优先级：中低** - 为未来扩展做准备

**任务清单：**
1. ✅ 创建 `repository/` 包
2. ✅ 定义 Repository 接口
3. ✅ 实现 JSON 文件 Repository
4. ✅ 迁移数据访问逻辑

**预期收益：**
- ✅ 数据访问抽象
- ✅ 易于切换存储方式
- ✅ 符合 DDD 原则

---

## 💡 关键设计原则

### 1. 依赖倒置原则（DIP）

**坏例子：**
```java
// 高层模块直接依赖低层模块
public class StockService {
    private final JsonFileService jsonFileService; // 直接依赖实现
}
```

**好例子：**
```java
// 高层模块依赖抽象
public class StockService {
    private final StockRepository stockRepository; // 依赖接口
}
```

### 2. 单一职责原则（SRP）

**坏例子：**
```java
// StockService 承担太多职责
public class StockService {
    public void addStock() { }      // 增
    public void deleteStock() { }   // 删
    public void updateStock() { }   // 改
    public void queryStock() { }    // 查
    public void calculateProfit() { } // 计算
    public void sendNotification() { } // 通知
}
```

**好例子：**
```java
// 按职责拆分
public class StockCommandService {
    public void addStock() { }
    public void deleteStock() { }
    public void updateStock() { }
}

public class StockQueryService {
    public Stock queryStock() { }
}

public class StockProfitService {
    public Profit calculateProfit() { }
}
```

### 3. 开闭原则（OCP）

**坏例子：**
```java
// 添加新通知渠道需要修改代码
public class NotificationService {
    public void send(String type, String msg) {
        if ("serverchan".equals(type)) {
            // ...
        } else if ("notifyme".equals(type)) {
            // ...
        }
    }
}
```

**好例子：**
```java
// 添加新渠道只需实现接口
public interface NotificationChannel {
    void send(String msg);
}

// 新增渠道
@Component
public class EmailChannel implements NotificationChannel {
    public void send(String msg) {
        // 发送邮件
    }
}
```

---

## 📈 优化效果预测

| 维度 | 当前 | 优化后 | 改进 |
|------|------|--------|------|
| **模块耦合度** | ⭐⭐ | ⭐⭐⭐⭐⭐ | -60% |
| **代码可测试性** | ⭐⭐⭐ | ⭐⭐⭐⭐⭐ | +67% |
| **可扩展性** | ⭐⭐⭐ | ⭐⭐⭐⭐⭐ | +67% |
| **可维护性** | ⭐⭐⭐ | ⭐⭐⭐⭐⭐ | +67% |
| **代码清晰度** | ⭐⭐⭐ | ⭐⭐⭐⭐⭐ | +67% |

---

## 🎯 建议的执行顺序

### 立即执行（本周）
1. ✅ **事件驱动架构** - 解决通知跨模块调用问题
2. ✅ **DTO 层引入** - 规范数据传输

### 近期执行（本月）
3. ✅ **Service 分层** - 提升代码组织
4. ✅ **Repository 层** - 数据访问抽象

### 长期规划（季度）
5. 值对象引入
6. CQRS 模式
7. 微服务拆分准备

---

## 🔧 快速开始指南

我可以帮您立即开始执行 **Phase 1: 事件驱动架构**，这是解决您担忧的核心方案。

**您希望我：**
1. ✅ 立即开始实现事件驱动架构？
2. 📋 先创建详细的实施文档？
3. 🤔 先看一个具体的示例代码？

请告诉我您的选择！

# Stock Trading System 📈

一个基于 Spring Boot 和 Vue 3 的智能股票交易系统，集成 AI Agent、实时行情获取、盈亏分析和微信推送功能。

## ✨ 功能特性

### 🎯 核心功能
- **股票管理**：支持 A 股、港股、美股、英股、贵金属等多种类型股票的增删改查
- **实时行情**：通过新浪 API 获取实时股票行情数据
- **盈亏计算**：自动计算持仓股票的当日盈亏和总盈亏
- **数据持久化**：使用 JSON 文件存储股票数据和节假日信息
- **定时任务**：自动在交易日获取股票数据并推送盈亏报告

### 🤖 AI Agent 能力
- **智能助手**：基于 LangChain4j 和 DeepSeek AI 模型
- **节假日查询**：AI 自动从政府网站获取中国法定节假日信息
- **文件操作**：AI 辅助写入和管理 JSON 数据文件

### 📱 消息推送
- **Server 酱集成**：支持微信消息推送
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
- **timor.tech**：中国节假日判断
- **Server 酱**：微信消息推送
- **DeepSeek API**：AI 对话服务

## 📦 项目结构

```
stock-trading-system/
├── src/main/java/icu/iseenu/
│   ├── agent/                  # AI Agent 相关
│   │   └── tool/              # Agent 工具类
│   ├── common/                # 通用类
│   │   └── Result.java        # 统一返回结果封装
│   ├── config/                # 配置类
│   │   ├── AppConfig.java
│   │   ├── CorsConfig.java    # 跨域配置
│   │   └── WebConfig.java
│   ├── controller/            # 控制器
│   │   ├── AiController.java  # AI 相关接口
│   │   └── StockController.java # 股票管理接口
│   ├── entity/                # 实体类
│   │   ├── Stock.java         # 股票实体
│   │   └── StockMarketData.java # 行情数据实体
│   ├── enums/                 # 枚举类
│   │   └── StockTypeEnum.java # 股票类型枚举
│   ├── service/               # 服务层
│   │   ├── ApiClientService.java
│   │   ├── HolidayJsonService.java
│   │   ├── JsonFileService.java
│   │   ├── ServerChanService.java # 微信推送服务
│   │   └── StockApiService.java   # 股票 API 服务
│   ├── task/                  # 定时任务
│   │   └── StockDataScheduledTask.java
│   ├── util/                  # 工具类
│   │   └── TradingDayUtil.java # 交易日判断工具
│   └── DemoApplication.java   # 启动类
├── frontend/                  # 前端项目
│   ├── src/
│   │   ├── api/              # API 封装
│   │   ├── components/       # 组件
│   │   ├── App.vue           # 主应用组件
│   │   └── main.js           # 入口文件
│   └── package.json
├── data/                      # 数据文件
│   ├── calender/             # 节假日数据
│   │   └── cn_holiday.json
│   └── json/                 # 股票数据
│       └── stocks.json
└── pom.xml                    # Maven 配置
```

## 🚀 快速开始

### 环境要求
- JDK 17+
- Maven 3.6+
- Node.js 16+
- npm 或 yarn

### 后端启动

1. **克隆项目**
```bash
git clone git@github.com:WangJiyuanYo/stock-trading-system.git
cd stock-trading-system
```

2. **配置 application.yml**
```yaml
# 修改以下配置项
langchain4j:
  open-ai:
    chat-model:
      api-key: your_deepseek_api_key  # 替换为你的 DeepSeek API Key
      
serverchan:
  sendkey: your_serverchan_sendkey    # 替换为你的 Server 酱 SendKey
```

3. **编译运行**
```bash
# 编译
mvn clean package -DskipTests

# 运行
java -jar target/autoCodeWorkspace-0.0.1-SNAPSHOT.jar

# 或使用 Maven 直接运行
mvn spring-boot:run
```

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

| 方法 | 路径 | 描述 |
|------|------|------|
| GET | `/api/stocks` | 获取所有股票列表 |
| GET | `/api/stocks/{stockCode}` | 根据代码获取股票 |
| POST | `/api/stocks` | 添加股票 |
| PUT | `/api/stocks/{stockCode}` | 更新股票 |
| DELETE | `/api/stocks/{stockCode}` | 删除股票 |
| GET | `/api/stocks/{stockCode}/exists` | 检查股票是否存在 |
| POST | `/api/stocks/batch` | 批量保存股票 |

### 行情数据接口

| 方法 | 路径 | 描述 |
|------|------|------|
| GET | `/api/stocks/market-data/all` | 获取所有股票行情 |
| GET | `/api/stocks/market-data/{stockCode}` | 获取单只股票行情 |
| POST | `/api/stocks/market-data/batch` | 批量获取行情 |
| GET | `/api/stocks/profit-loss/summary` | 获取盈亏汇总 |
| GET | `/api/stocks/profit-loss/overview` | 获取盈亏概览 |

### 定时任务接口

| 方法 | 路径 | 描述 |
|------|------|------|
| POST | `/api/stocks/task/execute` | 手动执行定时任务 |

### AI 接口

| 方法 | 路径 | 描述 |
|------|------|------|
| POST | `/api/ai/holiday` | AI 查询节假日 |
| POST | `/api/ai/write-file` | AI 写入文件 |

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

# Server 酱配置
serverchan:
  sendkey: SCTxxxxx            # Server 酱 SendKey
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

## 🎨 界面预览

前端提供友好的用户界面，包括：
- 📋 股票列表管理
- 📈 实时行情展示
- 🥧 盈亏分布饼图
- 📊 数据统计面板

## 🔐 安全注意事项

1. **不要提交敏感信息**：`.gitignore` 已配置忽略敏感文件
2. **API Key 保护**：将 API Key 保存在环境变量或本地配置中
3. **跨域配置**：生产环境请调整 CORS 配置

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

## 📄 许可证

本项目仅供学习和研究使用。

## 🤝 贡献

欢迎提交 Issue 和 Pull Request！

## 📧 联系方式

- GitHub: [WangJiyuanYo](https://github.com/WangJiyuanYo)
- 项目仓库: [stock-trading-system](https://github.com/WangJiyuanYo/stock-trading-system)

---

⭐ 如果这个项目对你有帮助，请给个 Star！

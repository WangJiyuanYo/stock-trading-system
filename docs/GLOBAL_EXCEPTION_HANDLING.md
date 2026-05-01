# 全局异常处理使用指南

## 📋 概述

本项目已实现统一的全局异常处理机制，通过 `@RestControllerAdvice` 自动捕获和处理所有 Controller 层抛出的异常。

## 🏗️ 架构设计

### 异常类层次结构

```
RuntimeException
└── BusinessException (code: 400)
    ├── ValidationException (code: 400) - 参数校验异常
    ├── ResourceNotFoundException (code: 404) - 资源未找到
    └── SystemException (code: 500) - 系统内部异常
```

### 文件位置

- **异常类**: `src/main/java/icu/iseenu/exception/`
  - `BusinessException.java` - 业务异常基类
  - `ValidationException.java` - 参数校验异常
  - `ResourceNotFoundException.java` - 资源未找到异常
  - `SystemException.java` - 系统内部异常

- **全局处理器**: `src/main/java/icu/iseenu/config/GlobalExceptionHandler.java`

## 🎯 使用方法

### 1. 在 Service 层抛出异常

```java
@Service
public class StockService {
    
    public void validateStock(Stock stock) {
        if (stock == null) {
            throw new ValidationException("股票信息不能为空");
        }
        
        if (stock.getStockCode() == null) {
            throw new ValidationException("股票代码不能为空");
        }
    }
    
    public Stock findByStockCode(String stockCode) throws IOException {
        Stock stock = repository.findByCode(stockCode);
        if (stock == null) {
            throw new ResourceNotFoundException("股票不存在：" + stockCode);
        }
        return stock;
    }
    
    public void processStockData(String data) {
        try {
            // 处理数据
        } catch (IOException e) {
            throw new SystemException("数据处理失败", e);
        }
    }
}
```

### 2. Controller 层无需手动处理异常

```java
@RestController
@RequestMapping("/api/stocks")
public class StockController {
    
    @PostMapping
    public Result<Stock> addStock(@RequestBody Stock stock) {
        // 如果 service 抛出 ValidationException，会被全局处理器自动捕获
        String message = stockService.addStock(stock);
        return Result.success(message, stock);
    }
    
    @GetMapping("/{stockCode}")
    public Result<Stock> getStock(@PathVariable String stockCode) {
        // 如果 service 抛出 ResourceNotFoundException，会被全局处理器自动捕获
        Stock stock = stockService.findByStockCode(stockCode);
        return Result.success("查询成功", stock);
    }
}
```

### 3. 对于受检异常（如 IOException），包装为 RuntimeException

```java
@GetMapping("/list")
public Result<List<Stock>> getAllStocks() {
    try {
        List<Stock> stocks = stockService.getAllStocks();
        return Result.success("查询成功", stocks);
    } catch (IOException e) {
        // 包装为 RuntimeException，由全局处理器捕获
        throw new RuntimeException("查询失败", e);
    }
}
```

## 📊 异常处理流程

```
Controller → Service → 抛出异常
                      ↓
         GlobalExceptionHandler 捕获
                      ↓
         记录日志 + 转换为 Result
                      ↓
         返回统一的 JSON 响应
```

## 🔍 异常类型映射

| 异常类型 | HTTP 状态码 | 使用场景 |
|---------|-----------|---------|
| `ValidationException` | 400 | 参数验证失败、业务规则违反 |
| `ResourceNotFoundException` | 404 | 资源不存在 |
| `BusinessException` | 400 | 一般业务异常 |
| `SystemException` | 500 | 系统内部错误 |
| `IllegalArgumentException` | 400 | 非法参数（Java 标准异常） |
| `IOException` | 500 | IO 操作失败 |
| `RuntimeException` | 500 | 未预期的运行时异常 |
| `Exception` | 500 | 其他所有异常 |

## 💡 最佳实践

### ✅ 推荐做法

1. **使用具体的异常类型**
   ```java
   // 好
   throw new ValidationException("股票代码不能为空");
   throw new ResourceNotFoundException("股票不存在");
   
   // 不好
   throw new RuntimeException("错误");
   ```

2. **提供清晰的错误消息**
   ```java
   // 好
   throw new ValidationException("股票代码格式错误，应为6位数字");
   
   // 不好
   throw new ValidationException("参数错误");
   ```

3. **保留异常链**
   ```java
   try {
       // 可能抛出 IOException 的操作
   } catch (IOException e) {
       throw new SystemException("数据处理失败", e); // 保留原始异常
   }
   ```

4. **Controller 层保持简洁**
   ```java
   // 好 - 让全局处理器处理异常
   @PostMapping
   public Result<Stock> addStock(@RequestBody Stock stock) {
       stockService.addStock(stock);
       return Result.success("添加成功", stock);
   }
   
   // 不好 - 手动处理每种异常
   @PostMapping
   public Result<Stock> addStock(@RequestBody Stock stock) {
       try {
           stockService.addStock(stock);
           return Result.success("添加成功", stock);
       } catch (ValidationException e) {
           return Result.badRequest(e.getMessage());
       } catch (IOException e) {
           return Result.internalError(e.getMessage());
       }
   }
   ```

### ❌ 避免的做法

1. **不要在 Controller 中捕获并返回 Result**
   ```java
   // 避免这样做
   @PostMapping
   public Result<Stock> addStock(@RequestBody Stock stock) {
       try {
           stockService.addStock(stock);
           return Result.success("添加成功", stock);
       } catch (Exception e) {
           return Result.error(e.getMessage());
       }
   }
   ```

2. **不要吞掉异常**
   ```java
   // 避免这样做
   try {
       stockService.process(data);
   } catch (Exception e) {
       // 什么都不做 - 这会隐藏问题
   }
   ```

3. **不要使用过于宽泛的异常**
   ```java
   // 避免这样做
   throw new RuntimeException("未知错误");
   
   // 应该这样做
   throw new SystemException("数据处理失败", e);
   ```

## 🧪 测试示例

### 单元测试

```java
@SpringBootTest
class StockControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    void testAddStockWithInvalidCode() throws Exception {
        mockMvc.perform(post("/api/stocks")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"stockCode\": \"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").exists());
    }
    
    @Test
    void testGetNonExistentStock() throws Exception {
        mockMvc.perform(get("/api/stocks/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404));
    }
}
```

## 📝 响应格式

所有异常都会返回统一的 JSON 格式：

```json
{
  "code": 400,
  "message": "股票代码不能为空",
  "data": null
}
```

对于参数验证异常（`MethodArgumentNotValidException`），还会返回详细的字段错误：

```json
{
  "code": 400,
  "message": "参数验证失败: 股票代码不能为空, 持仓数量必须大于0",
  "data": {
    "stockCode": "股票代码不能为空",
    "holdingQuantity": "持仓数量必须大于0"
  }
}
```

## 🔧 扩展自定义异常

如果需要新的异常类型，可以继承 `BusinessException`：

```java
@Getter
public class AuthorizationException extends BusinessException {
    
    public AuthorizationException(String message) {
        super(401, message);
    }
}
```

然后在全局异常处理器中添加对应的处理方法：

```java
@ExceptionHandler(AuthorizationException.class)
public ResponseEntity<Result<Void>> handleAuthorizationException(AuthorizationException e) {
    log.warn("授权异常: {}", e.getMessage());
    Result<Void> result = Result.error(e.getCode(), e.getMessage());
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);
}
```

## 📌 注意事项

1. **全局异常处理器会自动生效**，无需额外配置
2. **异常消息会记录到日志**，方便排查问题
3. **生产环境不会暴露堆栈信息**，只返回友好的错误消息
4. **所有异常都会被转换为 HTTP 状态码**，便于前端处理
5. **建议使用异步日志**，避免异常处理影响性能

## 🚀 下一步优化

可以考虑的进一步优化：

1. 添加异常监控和告警
2. 实现异常统计和分析
3. 添加多语言错误消息支持
4. 集成 Sentry 等错误追踪服务
5. 添加请求上下文信息到错误响应

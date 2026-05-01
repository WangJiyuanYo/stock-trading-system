# 全局异常处理 - 快速参考

## 🎯 异常类型选择指南

| 场景 | 使用异常 | HTTP 状态码 |
|------|---------|-----------|
| 参数为空、格式错误 | `ValidationException` | 400 |
| 业务规则违反 | `ValidationException` | 400 |
| 资源不存在 | `ResourceNotFoundException` | 404 |
| 系统内部错误 | `SystemException` | 500 |
| IO 操作失败 | 包装为 `RuntimeException` | 500 |

## 💡 常用代码模板

### Service 层抛出异常

```java
// 参数校验
if (param == null || param.isEmpty()) {
    throw new ValidationException("参数不能为空");
}

// 资源查找
Entity entity = repository.findById(id);
if (entity == null) {
    throw new ResourceNotFoundException("资源不存在: " + id);
}

// 业务规则
if (!condition) {
    throw new ValidationException("不满足业务规则: xxx");
}

// IO 异常
try {
    // IO 操作
} catch (IOException e) {
    throw new SystemException("操作失败", e);
}
```

### Controller 层处理

```java
// ✅ 推荐：让全局处理器处理
@PostMapping
public Result<Data> create(@RequestBody Data data) {
    service.create(data);
    return Result.success("创建成功", data);
}

// ❌ 避免：手动处理异常
@PostMapping
public Result<Data> create(@RequestBody Data data) {
    try {
        service.create(data);
        return Result.success("创建成功", data);
    } catch (Exception e) {
        return Result.error(e.getMessage());
    }
}
```

## 📦 导入语句

```java
import icu.iseenu.exception.ValidationException;
import icu.iseenu.exception.ResourceNotFoundException;
import icu.iseenu.exception.SystemException;
```

## 🔧 受检异常处理

对于 `IOException` 等受检异常：

```java
@GetMapping("/list")
public Result<List<Item>> getList() {
    try {
        List<Item> items = service.getAll();
        return Result.success("查询成功", items);
    } catch (IOException e) {
        throw new RuntimeException("查询失败", e);
    }
}
```

## 📊 响应格式

所有异常都会返回：

```json
{
  "code": 400,      // 或 404, 500
  "message": "错误描述",
  "data": null
}
```

## ⚡ 快速决策树

```
发生错误
  ├─ 用户输入问题？ → ValidationException
  ├─ 资源不存在？ → ResourceNotFoundException  
  ├─ 系统故障？ → SystemException
  └─ IO 错误？ → RuntimeException(包装)
```

## 🚫 常见错误

❌ **不要这样做：**
```java
throw new RuntimeException("未知错误");
throw new Exception("错误");
```

✅ **应该这样做：**
```java
throw new ValidationException("股票代码格式错误");
throw new ResourceNotFoundException("股票不存在: 600000");
throw new SystemException("数据处理失败", e);
```

## 📝 日志级别

- **WARN**: ValidationException, ResourceNotFoundException
- **ERROR**: SystemException, IOException, RuntimeException

## 🔗 相关文件

- 异常类: `src/main/java/icu/iseenu/exception/`
- 处理器: `src/main/java/icu/iseenu/config/GlobalExceptionHandler.java`
- 详细文档: `docs/GLOBAL_EXCEPTION_HANDLING.md`

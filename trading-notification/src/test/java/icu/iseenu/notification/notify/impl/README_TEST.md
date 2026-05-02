# NotifyMe 通知测试说明

## 📁 测试文件列表

### 1. NotifyMeNotifierTest.java
**用途**: 单元测试 - 测试 URL 构建和参数编码逻辑  
**运行方式**: 直接运行 main 方法，无需配置 UUID  
**测试内容**:
- ✅ 基本功能测试
- ✅ 特殊字符编码测试
- ✅ 空值处理测试
- ✅ 长文本测试（检查 URL 长度限制）

**适用场景**: 
- 验证 URL 构建逻辑是否正确
- 测试特殊字符的编码处理
- 检查消息长度是否超出限制

---

### 2. NotifyMeRealTest.java
**用途**: 集成测试 - 发送真实的通知到手机  
**运行方式**: 需要先配置 UUID，然后运行 main 方法  
**测试内容**:
- ✅ 发送简单测试消息
- ✅ 发送模拟股票日报（真实场景）

**配置步骤**:
1. 打开 NotifyMe App
2. 在设置中找到你的 UUID
3. 修改 `NotifyMeRealTest.java` 中的 `TEST_UUID` 常量
4. 运行 main 方法

**示例配置**:
```java
private static final String TEST_UUID = "CWYMVYWQHoPGXEkh9yP5Nd"; // 替换为你的真实 UUID
```

---

## 🚀 快速开始

### 方法1: 运行单元测试（推荐先运行）

```bash
# 在项目根目录执行
mvn test-compile exec:java -Dexec.mainClass="icu.iseenu.notify.impl.NotifyMeNotifierTest"
```

或在 IDE 中：
1. 打开 `NotifyMeNotifierTest.java`
2. 右键点击文件
3. 选择 "Run 'NotifyMeNotifierTest.main()'"

**预期输出**:
```
========== NotifyMe 通知测试 ==========

【测试1】基本功能测试
生成的 URL:
https://notifyme-server.wzn556.top/?uuid=CWYMVYWQHoPGXEkh9yP5Nd&title=%F0%9F%93%88+...
URL 长度: 245
✓ 测试通过

【测试2】特殊字符编码测试
...
✓ 特殊字符编码正确

【测试3】空值处理测试
...
✓ 空值处理正常

【测试4】长文本测试
...
✓ URL 长度在合理范围内

========== 所有测试完成 ==========
```

---

### 方法2: 运行真实推送测试

```bash
# 先配置 UUID，然后执行
mvn test-compile exec:java -Dexec.mainClass="icu.iseenu.notify.impl.NotifyMeRealTest"
```

或在 IDE 中：
1. 打开 `NotifyMeRealTest.java`
2. 修改 `TEST_UUID` 为你的真实 UUID
3. 右键点击文件
4. 选择 "Run 'NotifyMeRealTest.main()'"

**预期输出**:
```
========== NotifyMe 真实推送测试 ==========

【测试1】发送简单消息
请求 URL: https://notifyme-server.wzn556.top/?uuid=xxx&title=...
URL 长度: 150
响应码: 200
响应内容: {"isSuccess":true,"result":{"name":"..."}}
✓ 推送成功！

【测试2】发送股票日报
...
✓ 推送成功！

========== 测试完成 ==========
```

**同时你的手机会收到两条通知** 📱

---

## 📋 测试清单

运行测试前，请确认：

- [ ] Java 环境已安装（JDK 17+）
- [ ] Maven 已安装（如果使用命令行）
- [ ] NotifyMe App 已在手机上安装（仅真实测试需要）
- [ ] 已获取 UUID（仅真实测试需要）
- [ ] 网络连接正常

---

## 🔍 常见问题

### Q1: URL 长度过长怎么办？
**A**: GET 请求有 URL 长度限制（通常 2000-8000 字符）。如果消息过长：
- 使用 `bigText=true` 参数
- 缩短消息内容
- 考虑改用 POST 请求（如果 API 支持）

### Q2: 特殊字符显示乱码？
**A**: 确保：
- URL 参数正确进行了 UTF-8 编码
- NotifyMe App 支持 Unicode 字符

### Q3: 推送失败怎么办？
**A**: 检查：
- UUID 是否正确
- 网络连接是否正常
- API 服务器是否可访问
- 查看响应内容中的错误信息

### Q4: 如何测试其他通知渠道？
**A**: 可以参考这两个测试类的实现，为 ServerChan 创建类似的测试。

---

## 💡 提示

1. **先运行单元测试**，确保 URL 构建逻辑正确
2. **再运行真实测试**，验证实际推送效果
3. **观察日志输出**，了解详细的请求和响应信息
4. **检查手机通知**，确认真实收到消息

---

## 📞 获取帮助

- NotifyMe 官方文档: https://notifyme.wzn556.top/
- Telegram 群组: https://t.me/+A_95tYx7rGJlZWE1
- QQ 群: 1046424472

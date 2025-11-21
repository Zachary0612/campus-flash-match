# 🔧 CORS 跨域问题修复说明

## 📋 问题描述

### 症状
- ✅ 前端服务正常启动（Vite 运行在 localhost:3000）
- ✅ 后端服务正常启动（Spring Boot 运行在 localhost:8080）
- ❌ 前端请求后端 API 时返回 **403 Forbidden**
- ❌ 浏览器控制台显示 CORS 错误

### 错误示例
```
POST http://localhost:8080/login → 403 Forbidden
POST http://localhost:3000/api/user/register → 309 Found
```

## 🔍 问题根源

### 1. OPTIONS 预检请求被拦截
浏览器在发送跨域请求前，会先发送一个 OPTIONS 预检请求。但是：
- ❌ `TokenInterceptor` 拦截了 OPTIONS 请求
- ❌ `CampusIpInterceptor` 拦截了 OPTIONS 请求
- ❌ 导致 CORS 预检失败，实际请求无法发送

### 2. CORS 配置不完整
虽然在 `WebConfig` 中配置了 CORS，但：
- ❌ 没有独立的 CORS 过滤器
- ❌ 拦截器优先级高于 CORS 配置
- ❌ OPTIONS 请求在 CORS 处理前就被拦截

## ✅ 解决方案

### 修改 1：TokenInterceptor.java
在 Token 拦截器中添加 OPTIONS 请求放行逻辑：

```java
@Override
public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
    // 放行 OPTIONS 请求（CORS 预检请求）
    if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
        return true;
    }
    
    // ... 原有的 Token 验证逻辑
}
```

### 修改 2：CampusIpInterceptor.java
在 IP 拦截器中添加 OPTIONS 请求放行逻辑：

```java
@Override
public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
    // 放行 OPTIONS 请求（CORS 预检请求）
    if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
        return true;
    }
    
    // ... 原有的 IP 验证逻辑
}
```

### 修改 3：新增 CorsConfig.java
创建独立的 CORS 过滤器配置：

```java
@Configuration
public class CorsConfig {
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOriginPattern("*");
        config.setAllowCredentials(true);
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        config.addExposedHeader("*");
        config.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        
        return new CorsFilter(source);
    }
}
```

## 🚀 应用修复

### 步骤 1：重启后端服务

```bash
# 停止当前运行的后端（Ctrl + C）

# 重新启动
cd "d:\java project\campus-flash-match"
.\mvnw spring-boot:run
```

### 步骤 2：清除浏览器缓存

1. 打开浏览器开发者工具（F12）
2. 右键点击刷新按钮
3. 选择"清空缓存并硬性重新加载"

或者使用无痕模式测试。

### 步骤 3：测试注册/登录

1. 访问 http://localhost:3000/register
2. 填写注册信息
3. 点击注册
4. ✅ 应该成功注册

## 🧪 验证方法

### 1. 检查 OPTIONS 请求
打开浏览器开发者工具 → Network 标签：

**修复前：**
```
OPTIONS /api/user/register → 403 Forbidden
```

**修复后：**
```
OPTIONS /api/user/register → 200 OK
POST /api/user/register → 200 OK
```

### 2. 检查响应头
查看 OPTIONS 请求的响应头，应该包含：

```
Access-Control-Allow-Origin: http://localhost:3000
Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS
Access-Control-Allow-Headers: *
Access-Control-Allow-Credentials: true
```

### 3. 检查后端日志
后端控制台应该输出：

```
客户端IP: 127.0.0.1, 要求前缀: 10
本地开发环境，允许访问
```

## 📊 修复前后对比

### 修复前
```
浏览器 → OPTIONS /api/user/register
        ↓
    拦截器拦截（没有 Token）
        ↓
    返回 403 Forbidden
        ↓
    CORS 预检失败
        ↓
    实际请求无法发送
```

### 修复后
```
浏览器 → OPTIONS /api/user/register
        ↓
    拦截器放行（OPTIONS 请求）
        ↓
    CORS 过滤器处理
        ↓
    返回 200 OK + CORS 响应头
        ↓
    浏览器发送实际请求
        ↓
    POST /api/user/register
        ↓
    成功注册
```

## 🎯 关键知识点

### 什么是 CORS 预检请求？
- 浏览器在发送跨域请求前，会先发送一个 OPTIONS 请求
- 用于检查服务器是否允许跨域访问
- 只有预检通过，才会发送实际请求

### 为什么要放行 OPTIONS 请求？
- OPTIONS 请求不携带 Token
- OPTIONS 请求不需要验证用户身份
- OPTIONS 请求只是用于 CORS 检查

### 拦截器和过滤器的执行顺序
```
请求 → Filter（过滤器）→ Interceptor（拦截器）→ Controller
```

但是 CORS 过滤器需要在拦截器之前处理，所以：
1. 使用 `CorsFilter` Bean
2. 在拦截器中放行 OPTIONS 请求

## 🔗 相关文档

- [Spring CORS 官方文档](https://docs.spring.io/spring-framework/docs/current/reference/html/web.html#mvc-cors)
- [MDN CORS 文档](https://developer.mozilla.org/zh-CN/docs/Web/HTTP/CORS)
- [OPTIONS 请求说明](https://developer.mozilla.org/zh-CN/docs/Web/HTTP/Methods/OPTIONS)

## ⚠️ 注意事项

### 生产环境配置
在生产环境中，应该：
1. 限制允许的域名（不使用 `*`）
2. 限制允许的请求方法
3. 限制允许的请求头

```java
// 生产环境示例
config.setAllowedOrigins(Arrays.asList("https://your-domain.com"));
config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE"));
```

### 安全建议
- ✅ 开发环境可以使用 `allowedOriginPattern("*")`
- ❌ 生产环境不要使用通配符
- ✅ 始终验证 Token（除了 OPTIONS 请求）
- ✅ 记录所有跨域请求日志

## 📞 故障排查

### 问题 1：修改后仍然 403
**解决方案：**
1. 确认后端已重启
2. 清除浏览器缓存
3. 使用无痕模式测试

### 问题 2：OPTIONS 请求返回 404
**解决方案：**
1. 检查 Controller 路径是否正确
2. 检查 `@RequestMapping` 注解

### 问题 3：POST 请求成功但 OPTIONS 失败
**解决方案：**
1. 检查拦截器配置
2. 确认 OPTIONS 请求被放行

---

**修复完成！现在可以正常使用注册和登录功能了。** ✅

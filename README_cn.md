# dyenums - Java 动态枚举库

[![Java](https://img.shields.io/badge/Java-8+-blue.svg)](https://www.oracle.com/java/)
[![Maven](https://img.shields.io/badge/Maven-3.6+-green.svg)](https://maven.apache.org/)
[![License](https://img.shields.io/badge/License-Apache%202.0-lightgrey.svg)](https://opensource.org/licenses/Apache-2.0)

**dyenums** 是一个支持 Java 8+ 的动态枚举库。与编译期固定的传统 Java 枚举不同，dyenums 允许你在运行时注册、修改和加载枚举值。

## 核心特性

- **运行时注册**：在运行时动态注册枚举值
- **配置加载**：从属性文件或数据库加载枚举（可选）
- **自定义加载器**：实现 `DyEnumsLoader` 接口支持任意数据源
- **类型安全**：使用泛型确保编译期类型检查
- **线程安全**：基于 `ConcurrentHashMap` 和同步机制实现
- **最小核心**：核心模块仅依赖 SLF4J
- **Spring 集成**：可选的 Spring/Boot 支持

## 模块结构

```
dyenums
├── dyenums-core        # 核心模块（必需）- 最小依赖
├── dyenums-loader-file # 文件加载器（可选）- 属性文件支持
├── dyenums-loader-db   # 数据库加载器（可选）- JDBC 支持
└── dyenums-spring      # Spring 集成（可选）- 自动配置
```

| 使用场景 | 需要的模块 |
|----------|-----------|
| 手动注册 | `dyenums-core` |
| 自定义加载器 | `dyenums-core` |
| 从属性文件加载 | `dyenums-core` + `dyenums-loader-file` |
| 从数据库加载 | `dyenums-core` + `dyenums-loader-db` |
| Spring/Boot 集成 | `dyenums-core` + `dyenums-spring` |

## 安装

### Maven

**仅核心模块（最小依赖）**：

```xml
<dependency>
    <groupId>cn.itcraft</groupId>
    <artifactId>dyenums-core</artifactId>
    <version>1.0.0</version>
</dependency>
```

**添加文件加载器**：

```xml
<dependency>
    <groupId>cn.itcraft</groupId>
    <artifactId>dyenums-loader-file</artifactId>
    <version>1.0.0</version>
</dependency>
```

**添加数据库加载器**：

```xml
<dependency>
    <groupId>cn.itcraft</groupId>
    <artifactId>dyenums-loader-db</artifactId>
    <version>1.0.0</version>
</dependency>
```

**添加 Spring 集成**：

```xml
<dependency>
    <groupId>cn.itcraft</groupId>
    <artifactId>dyenums-spring</artifactId>
    <version>1.0.0</version>
</dependency>
```

### 从源码构建

```bash
git clone https://github.com/itcraft-cn/dyenums.git
cd dyenums
mvn clean install
```

## 快速开始

### 1. 定义枚举类

继承 `BaseDyEnum`：

```java
import cn.itcraft.dyenums.core.BaseDyEnum;

public class UserStatus extends BaseDyEnum {
    
    public static final UserStatus ACTIVE = new UserStatus("ACTIVE", "激活", "用户已激活", 1);
    public static final UserStatus INACTIVE = new UserStatus("INACTIVE", "未激活", "用户未激活", 2);
    public static final UserStatus LOCKED = new UserStatus("LOCKED", "锁定", "用户被锁定", 3);
    
    private UserStatus(String code, String name, String description, int order) {
        super(code, name, description, order);
    }
}
```

### 2. 注册和使用

```java
import cn.itcraft.dyenums.core.EnumRegistry;

public class Application {
    public static void main(String[] args) {
        // 注册预定义值
        EnumRegistry.register(UserStatus.class, UserStatus.ACTIVE);
        EnumRegistry.register(UserStatus.class, UserStatus.INACTIVE);
        EnumRegistry.register(UserStatus.class, UserStatus.LOCKED);
        
        // 按 code 查询
        UserStatus status = EnumRegistry.valueOf(UserStatus.class, "ACTIVE")
            .orElseThrow(() -> new IllegalArgumentException("状态不存在"));
        
        System.out.println(status.getName()); // 输出: 激活
    }
}
```

## 高级用法

### 动态注册

```java
// 运行时创建并注册
UserStatus customStatus = new UserStatus("CUSTOM", "自定义状态", "自定义状态", 99);
EnumRegistry.register(UserStatus.class, customStatus);

// 或使用反射通过 EnumRegistry.addEnum
EnumRegistry.addEnum(UserStatus.class, "VIP", "VIP用户", "VIP状态", 100);
```

### 自定义加载器

实现 `DyEnumsLoader` 接口从任意数据源加载：

```java
import cn.itcraft.dyenums.loader.DyEnumsLoader;
import cn.itcraft.dyenums.core.DyEnum;
import java.util.function.BiFunction;

public class MyCustomLoader<T extends DyEnum> implements DyEnumsLoader<T> {
    
    @Override
    public int load(Class<T> enumClass, BiFunction<String, String, T> factory) throws Exception {
        // 从你的数据源加载（REST API、Redis 等）
        List<MyData> dataList = fetchFromMySource();
        
        int count = 0;
        for (MyData data : dataList) {
            String code = data.getCode();
            String valueString = data.getName() + "|" + data.getDesc() + "|" + data.getOrder();
            T enumValue = factory.apply(code, valueString);
            EnumRegistry.register(enumClass, enumValue);
            count++;
        }
        return count;
    }
    
    @Override
    public boolean validateSource() {
        return isMySourceAccessible();
    }
}
```

### 多语言支持

扩展支持多语言消息：

```java
import cn.itcraft.dyenums.core.BaseDyEnum;
import java.util.Locale;
import java.util.Map;
import java.util.HashMap;

public class ErrorCode extends BaseDyEnum {
    
    private final Map<String, String> messages;
    
    public ErrorCode(String code, String name, int order, Map<String, String> messages) {
        super(code, name, null, order);
        this.messages = messages;
    }
    
    public String getMessage(String lang) {
        return messages.getOrDefault(lang, getCode());
    }
    
    public String getMessage(Locale locale) {
        return getMessage(locale.getLanguage());
    }
    
    // 便捷方法
    public String getMessageZh() { return getMessage("zh"); }
    public String getMessageEn() { return getMessage("en"); }
}

// 使用示例
Map<String, String> messages = new HashMap<>();
messages.put("zh", "系统错误");
messages.put("en", "System error");
messages.put("pt", "Erro do sistema");
messages.put("ru", "Системная ошибка");

ErrorCode error = new ErrorCode("SYS_001", "系统错误", 1, messages);
EnumRegistry.register(ErrorCode.class, error);

System.out.println(error.getMessageZh()); // 系统错误
System.out.println(error.getMessageEn()); // System error
```

### 从属性文件加载

使用 `dyenums-loader-file` 模块：

```properties
# enums.properties
UserStatus.ACTIVE=ACTIVE|激活|用户已激活|1
UserStatus.INACTIVE=INACTIVE|未激活|用户未激活|2
```

```java
import cn.itcraft.dyenums.loader.file.FileBasedDyEnumsLoader;

FileBasedDyEnumsLoader<UserStatus> loader = new FileBasedDyEnumsLoader<>("enums.properties");
loader.load(UserStatus.class, UserStatus::fromValueString);
```

### 从数据库加载

使用 `dyenums-loader-db` 模块：

```java
import cn.itcraft.dyenums.loader.db.DatabaseDyEnumsLoader;
import javax.sql.DataSource;

DatabaseDyEnumsLoader<UserStatus> loader = new DatabaseDyEnumsLoader<>(dataSource);
loader.load(UserStatus.class, UserStatus::fromValueString);
```

### Spring 集成

使用 `dyenums-spring` 模块：

```java
import cn.itcraft.dyenums.spring.EnumService;
import org.springframework.beans.factory.annotation.Autowired;

@Service
public class UserService {
    
    @Autowired
    private EnumService enumService;
    
    public List<UserStatus> getAllStatuses() {
        return enumService.getValues(UserStatus.class);
    }
    
    public UserStatus getStatus(String code) {
        return enumService.getByCode(UserStatus.class, code);
    }
}
```

## 架构设计

```
┌─────────────────────────────────────────────────────────────┐
│                        应用层                                 │
├─────────────────────────────────────────────────────────────┤
│  EnumRegistry (Core) - 注册、查询、线程安全                   │
├──────────────┬──────────────┬──────────────┬────────────────┤
│  BaseDyEnum  │ DyEnumsLoader│ @EnumDef     │ Spring Config  │
│  (Core)      │ (Core 接口)   │              │ (可选)          │
├──────────────┴──────────────┴──────────────┴────────────────┤
│                    可选加载器实现                              │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────────────────┐│
│  │ 文件加载器   │ │ 数据库加载器 │ │  自定义实现              ││
│  │ (可选)       │ │ (可选)       │ │  (自行实现)              ││
│  └─────────────┘ └─────────────┘ └─────────────────────────┘│
└─────────────────────────────────────────────────────────────┘
```

### 核心组件

| 组件 | 模块 | 说明 |
|------|------|------|
| `DyEnum` | core | 枚举接口定义 |
| `BaseDyEnum` | core | 抽象基类实现 |
| `EnumRegistry` | core | 枚举实例中央注册表 |
| `DyEnumsLoader` | core | 自定义加载器接口 |
| `FileBasedDyEnumsLoader` | loader-file | 属性文件加载器 |
| `PropDyEnumsLoader` | loader-file | 内存 Properties 加载器 |
| `DatabaseDyEnumsLoader` | loader-db | JDBC 数据库加载器 |
| `EnumService` | spring | Spring 枚举访问服务 |
| `EnumConverter` | spring | Spring MVC 类型转换器 |

## 测试

```bash
# 运行所有测试
mvn test

# 运行特定模块测试
mvn test -pl dyenums-core
mvn test -pl dyenums-loader-file

# 运行特定测试类
mvn test -Dtest=EnumRegistryTest
```

## 线程安全

所有注册表操作都是线程安全的：

```java
// 支持并发访问
ExecutorService executor = Executors.newFixedThreadPool(10);
for (int i = 0; i < 100; i++) {
    final int id = i;
    executor.submit(() -> {
        EnumRegistry.addEnum(UserStatus.class, "STATUS_" + id, "状态 " + id, null, id);
    });
}
executor.shutdown();
executor.awaitTermination(1, TimeUnit.MINUTES);

assertEquals(100, EnumRegistry.getCount(UserStatus.class));
```

## 性能特性

- **O(1) 查询**：基于 HashMap 的 code 查询
- **并发访问**：`ConcurrentHashMap` 保证线程安全
- **惰性初始化**：枚举仅在注册时创建
- **内存高效**：共享单一注册表实例

## 最佳实践

1. 在应用启动时注册枚举
2. 枚举实例创建后视为不可变
3. 确保 code 在同一枚举类型内唯一
4. 为自定义数据源实现 `DyEnumsLoader`
5. Spring 应用中使用 `EnumService`

## 贡献

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'feat: add AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 创建 Pull Request

## 许可证

本项目基于 Apache License 2.0 许可 - 详见 [LICENSE](LICENSE) 文件。

## 支持

- 在 GitHub 创建 Issue
- 查看 `doc` 目录中的文档
- 参考 `dyenums-test` 模块中的示例实现
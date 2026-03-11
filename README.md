# dyenums - Dynamic Enum Library for Java

[![Java](https://img.shields.io/badge/Java-8+-blue.svg)](https://www.oracle.com/java/)
[![Maven](https://img.shields.io/badge/Maven-3.6+-green.svg)](https://maven.apache.org/)
[![License](https://img.shields.io/badge/License-Apache%202.0-lightgrey.svg)](https://opensource.org/licenses/Apache-2.0)

**dyenums** is a dynamic enum library for Java 8+ that provides an alternative to Java's static enums. Unlike
traditional enums that are fixed at compile time, dyenums allows runtime registration, dynamic loading from
configuration files or databases, and extensibility that traditional Java enums cannot provide.

## 🎯 Key Features

- **Runtime Registration**: Register enum values at runtime, not just at compile time
- **Configuration Loading**: Load enum definitions from properties files or databases
- **Dynamic Creation**: Create new enum instances dynamically using reflection
- **Type-Safe Access**: Full type safety with generic methods and compile-time checks
- **Spring Integration**: Seamless integration with Spring Framework
- **Thread-Safe**: Built with thread-safe collections and synchronization
- **Lightweight**: Minimal dependencies (only SLF4J required)

## 📦 Installation

### Maven

Add the following dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>cn.itcraft</groupId>
    <artifactId>dyenums</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Building from Source

```bash
git clone https://github.com/itcraft-cn/dyenums.git
cd dyenums
mvn clean install
```

## 🚀 Quick Start

### 1. Define Your Enum Class

Create a class that extends `BaseDyEnum`:

```java
import cn.itcraft.dyenums.core.BaseDyEnum;

public class UserStatus extends BaseDyEnum {
    
    // Predefined values
    public static final UserStatus ACTIVE = new UserStatus("ACTIVE", "激活", "用户已激活", 1);
    public static final UserStatus INACTIVE = new UserStatus("INACTIVE", "未激活", "用户未激活", 2);
    public static final UserStatus LOCKED = new UserStatus("LOCKED", "锁定", "用户被锁定", 3);
    
    // Private constructor
    private UserStatus(String code, String name, String description, int order) {
        super(code, name, description, order);
    }
}
```

### 2. Register Your Enums

```java
import cn.itcraft.dyenums.core.EnumRegistry;

public class Application {
    public static void main(String[] args) {
        // Register predefined values
        EnumRegistry.register(UserStatus.class, UserStatus.ACTIVE);
        EnumRegistry.register(UserStatus.class, UserStatus.INACTIVE);
        EnumRegistry.register(UserStatus.class, UserStatus.LOCKED);
        
        // Look up enum by code
        UserStatus status = EnumRegistry.valueOf(UserStatus.class, "ACTIVE")
            .orElseThrow(() -> new IllegalArgumentException("Status not found"));
        
        System.out.println(status.getName()); // Output: 激活
    }
}
```

### 3. Use in Your Application

```java
public class UserService {
    public void activateUser(String userId) {
        UserStatus activeStatus = EnumRegistry.valueOf(UserStatus.class, "ACTIVE")
            .orElseThrow(() -> new IllegalArgumentException("ACTIVE status not found"));
        
        // Update user status in database
        userRepository.updateStatus(userId, activeStatus.getCode());
    }
}
```

## 📚 Advanced Usage

### Loading Enums from Configuration

#### Properties File (enums.properties)

```properties
UserStatus.ACTIVE=ACTIVE|激活|用户已激活|1
UserStatus.INACTIVE=INACTIVE|未激活|用户未激活|2
UserStatus.LOCKED=LOCKED|锁定|用户被锁定|3
OrderStatus.PENDING=PENDING|待处理|订单等待处理|1
OrderStatus.PROCESSING=PROCESSING|处理中|订单正在处理|2
```

#### Load Configuration

```java
import cn.itcraft.dyenums.loader.FileBasedEnumConfig;
import java.util.Properties;

// Load from file
Properties props = new Properties();
try (InputStream is = Files.newInputStream(Paths.get("enums.properties"))) {
    props.load(is);
}

// Register from configuration
EnumRegistry.registerFromConfig(
    UserStatus.class, 
    props,
    UserStatus::fromValueString  // Factory method
);

// Or use the utility method
FileBasedEnumConfig.loadFromFile(
    "enums.properties",
    UserStatus.class,
    UserStatus::fromValueString
);
```

### Dynamic Enum Creation

```java
// Create a new enum value at runtime
UserStatus customStatus = EnumRegistry.addEnum(
    UserStatus.class,
    "CUSTOM_STATUS",
    "自定义状态",
    "通过代码动态创建的状态",
    999
);

// It's immediately available for use
Optional<UserStatus> found = EnumRegistry.valueOf(UserStatus.class, "CUSTOM_STATUS");
assertTrue(found.isPresent());
```

### Spring Integration

#### Configuration

```java
import cn.itcraft.dyenums.spring.DynamicEnumConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(DynamicEnumConfig.class)
public class ApplicationConfig {
    // Your other configuration
}
```

#### Using EnumService

```java
import cn.itcraft.dyenums.spring.EnumService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    
    @Autowired
    private EnumService enumService;
    
    public List<UserStatus> getAllUserStatuses() {
        return enumService.getValues(UserStatus.class);
    }
    
    public UserStatus getUserStatus(String code) {
        return enumService.getByCode(UserStatus.class, code);
    }
    
    public Map<String, String> getStatusOptions() {
        return enumService.asCodeNameMap(UserStatus.class);
    }
}
```

#### Spring MVC Converter

```java
import cn.itcraft.dyenums.spring.EnumConverter;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    @Override
    public void addFormatters(FormatterRegistry registry) {
        // Register converters for your enum types
        registry.addConverter(new EnumConverter(UserStatus.class));
        registry.addConverter(new EnumConverter(OrderStatus.class));
    }
}
```

Now you can use enums directly in your controllers:

```java
@RestController
@RequestMapping("/users")
public class UserController {
    
    @GetMapping("/status/{status}")
    public List<User> getUsersByStatus(@PathVariable UserStatus status) {
        // Spring automatically converts the path variable to UserStatus
        return userRepository.findByStatus(status.getCode());
    }
}
```

## 🏗️ Architecture

### Core Components

- **DyEnum**: Interface defining the contract for all dynamic enums
- **BaseDyEnum**: Abstract base class providing default implementations
- **EnumRegistry**: Central registry managing all enum instances
- **EnumService**: Spring service providing type-safe access to enums
- **EnumConverter**: Spring converter for automatic type conversion

### Design Pattern: Map+Factory

The library uses a registry pattern where:

1. Each enum type has its own `Map<String, DyEnum>` in a central registry
2. Enum instances are created via factory methods or reflection
3. Thread safety is ensured through `ConcurrentHashMap` and synchronization
4. Configuration can be loaded from multiple sources

## 📖 Example Enums

The library includes example implementations:

### UserStatus

Represents user account states:

- `ACTIVE`: User account is active
- `INACTIVE`: User account is inactive
- `LOCKED`: User account is locked
- `SUSPENDED`: User account is suspended
- `PENDING`: User pending verification

```java
UserStatus status = EnumRegistry.valueOf(UserStatus.class, "ACTIVE")
    .orElseThrow();

if (status.isActive()) {
    // Allow user to login
}

if (status.isBlocked()) {
    // Show account blocked message
}
```

### OrderStatus

Represents order processing states:

- `PENDING`: Order created, waiting for processing
- `PROCESSING`: Order is being processed
- `CONFIRMED`: Order confirmed, payment verified
- `SHIPPED`: Order has been shipped
- `DELIVERED`: Order delivered to customer
- `CANCELLED`: Order cancelled
- `REFUNDED`: Order refunded

```java
OrderStatus status = EnumRegistry.valueOf(OrderStatus.class, "PROCESSING")
    .orElseThrow();

if (status.isInProgress()) {
    // Order can still be modified
}

if (status.canBeCancelled()) {
    // Show cancel order button
}
```

## 🧪 Testing

The library includes comprehensive tests:

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=EnumRegistryTest

# Run with coverage
mvn clean test jacoco:report
```

### Test Categories

- **Unit Tests**: Test individual components (BaseDyEnumTest, EnumRegistryTest)
- **Integration Tests**: Test complete workflows (EnumIntegrationTest)
- **Thread Safety Tests**: Verify concurrent access safety

## 🔧 Configuration

### Maven Dependencies

```xml
<dependencies>
    <!-- Required -->
    <dependency>
        <groupId>org.slf4j</groupId>
        <artifactId>slf4j-api</artifactId>
        <version>1.7.36</version>
    </dependency>
    
    <!-- Optional: Spring Integration -->
    <dependency>
        <groupId>org.springframework</groupId>
        <artifactId>spring-context</artifactId>
        <version>5.3.21</version>
        <optional>true</optional>
    </dependency>
    
    <!-- Test Dependencies -->
    <dependency>
        <groupId>junit</groupId>
        <artifactId>junit</artifactId>
        <version>4.13.2</version>
        <scope>test</scope>
    </dependency>
</dependencies>
```

### System Properties

```properties
# Enable auto-loading from config file (Spring only)
dyenums.config.path=enums.properties
dyenums.config.auto-load=true
```

## 📊 Performance

The library is designed for high performance:

- **O(1) Lookup**: Enum lookup by code uses HashMap
- **Thread-Safe**: ConcurrentHashMap for parallel access
- **Lazy Loading**: Enums are only created when needed
- **Memory Efficient**: Single registry instance, shared storage

## 🔒 Thread Safety

All registry operations are thread-safe:

```java
// Multiple threads can safely register enums
ExecutorService executor = Executors.newFixedThreadPool(10);
for (int i = 0; i < 100; i++) {
    final int id = i;
    executor.submit(() -> {
        EnumRegistry.addEnum(
            UserStatus.class,
            "STATUS_" + id,
            "Status " + id,
            "Dynamically created",
            id
        );
    });
}
executor.shutdown();
executor.awaitTermination(1, TimeUnit.MINUTES);

// All 100 enums are safely registered
assertEquals(100, EnumRegistry.getCount(UserStatus.class));
```

## 📋 Best Practices

1. **Registration**: Register enums during application startup
2. **Immutability**: Treat enum instances as immutable after creation
3. **Code Uniqueness**: Ensure codes are unique within each enum type
4. **Thread Safety**: Use EnumService in Spring applications for convenience
5. **Configuration**: Store dynamic enum definitions in configuration files or database
6. **Documentation**: Document the purpose and usage of each enum value
7. **Testing**: Always test enum registration and lookup in your application

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- Inspired by the limitations of Java's static enum system
- Built with modern Java concurrency patterns
- Designed for enterprise application needs

## 📞 Support

For questions and support:

- Create an issue on GitHub
- Check the documentation in the `docs` directory
- Review the example implementations in the `src/main/java/com/helly/dyenums/model` package

---

**dyenums** - Empowering dynamic enum definitions in Java applications.

# CODEBUDDY.md

This file provides guidance to CodeBuddy Code when working with code in this repository.

## Project Overview

**dyenums** is a dynamic enum library for Java 8+ that provides an alternative to Java's static enums. The library uses a Map+Factory pattern to enable runtime registration, dynamic loading from configuration files or databases, and extensibility that traditional Java enums cannot provide.

This is a **multi-module Maven project** with the following structure:
- **dyenums-core**: Core library with no external dependencies (except SLF4J)
- **dyenums-config-file**: File-based configuration loader (properties files)
- **dyenums-config-db**: Database-based configuration loader (JDBC)
- **dyenums-spring**: Spring Framework integration module
- **dyenums-test**: Test module containing unit tests and example enum implementations

## Technology Stack

- **Java**: Java 8 (must be compatible with Java 8 features)
- **Build Tool**: Maven (multi-module project)
- **Key Dependencies**:
  - SLF4J (logging)
  - Spring Framework 5.3.21 (optional, for dyenums-spring)
  - JUnit 4.13.2 (testing)

## Build Commands

```bash
# Build all modules
mvn clean install

# Compile all modules
mvn compile

# Run tests in all modules
mvn test

# Run tests for a specific module
mvn test -pl dyenums-core

# Run a single test class
mvn test -Dtest=ClassName

# Run a single test method
mvn test -Dtest=ClassName#methodName

# Package all modules
mvn package

# Clean build artifacts
mvn clean

# Skip tests during build
mvn clean install -DskipTests
```

## Project Structure

This is a Maven multi-module project:

```
dyenums/
├── pom.xml                          # Parent POM
├── CODEBUDDY.md                     # This file
├── README.md                        # User documentation
├── design.md                        # Design documentation
├── dyenums-core/                    # Core module (minimal dependencies)
│   ├── pom.xml
│   └── src/
│       └── main/java/cn/itcraft/dyenums/
│           ├── annotation/          # @EnumDefinition annotation
│           ├── config/              # EnumConfigLoader interface
│           └── core/                # Core interfaces and classes
│               ├── DyEnum.java      # Main interface
│               ├── BaseDyEnum.java  # Abstract base implementation
│               └── EnumRegistry.java # Central registry
├── dyenums-config-file/             # File configuration module
│   ├── pom.xml
│   └── src/main/java/cn/itcraft/dyenums/config/file/
│       └── FileBasedEnumConfig.java # Properties file loader
├── dyenums-config-db/               # Database configuration module
│   ├── pom.xml
│   └── src/main/java/cn/itcraft/dyenums/config/db/
│       └── DatabaseEnumConfig.java  # JDBC database loader
├── dyenums-spring/                  # Spring integration module
│   ├── pom.xml
│   └── src/main/java/cn/itcraft/dyenums/spring/
│       ├── DynamicEnumConfig.java   # Spring configuration
│       ├── EnumService.java         # Service layer
│       └── EnumConverter.java       # Spring Converter
└── dyenums-test/                    # Test module (separates examples from core)
    └── src/test/java/cn/itcraft/dyenums/
        ├── core/                    # Unit tests
        ├── integration/             # Integration tests
        └── model/                   # Example enum implementations
```

## Core Architecture

### Module: dyenums-core

**Key Components:**

1. **DyEnum Interface** (`cn.itcraft.dyenums.core.DyEnum`)
   - Defines the contract for all dynamic enums
   - Methods: `getCode()`, `getName()`, `getDescription()`, `getOrder()`

2. **BaseDyEnum Class** (`cn.itcraft.dyenums.core.BaseDyEnum`)
   - Abstract base implementation of DyEnum
   - Handles equals, hashCode, toString, and validation
   - Must be extended by concrete enum classes

3. **EnumRegistry Class** (`cn.itcraft.dyenums.core.EnumRegistry`)
   - Central registry storing all enum instances
   - Uses `ConcurrentHashMap` for thread-safe storage
   - Key methods:
     - `register(Class<T>, T)` - Register an enum instance
     - `valueOf(Class<T>, String)` - Look up by code (returns Optional)
     - `values(Class<T>)` - Get all values for a type (sorted by order)
     - `addEnum(Class<T>, ...)` - Dynamic enum creation via reflection
     - `registerFromConfig(...)` - Load from configuration properties
     - `remove(Class<T>, String)` - Remove an enum value

4. **EnumConfigLoader Interface** (`cn.itcraft.dyenums.config.EnumConfigLoader`)
   - Interface for loading enum definitions from external sources
   - Implementations in separate modules for modularity
   - Methods: `load()`, `validateSource()`

5. **EnumDefinition Annotation** (`cn.itcraft.dyenums.annotation.EnumDefinition`)
   - Metadata annotation for enum classes
   - Properties: category, dynamic, configSource, configPath, etc.

### Module: dyenums-config-file

**File Configuration Loader:**

**FileBasedEnumConfig** (`cn.itcraft.dyenums.config.file.FileBasedEnumConfig`)
- Loads enums from properties files
- Format: `EnumClass.CODE=name|description|order`
- Supports classpath and filesystem loading
- Implements `EnumConfigLoader` interface
- Static utility methods for backward compatibility

### Module: dyenums-config-db

**Database Configuration Loader:**

**DatabaseEnumConfig** (`cn.itcraft.dyenums.config.db.DatabaseEnumConfig`)
- Loads enums from database using JDBC
- Requires DataSource, customizable SQL queries
- Implements `EnumConfigLoader` interface
- Static utility methods for backward compatibility
- Provides table creation DDL

### Module: dyenums-spring

**Spring Integration Components:**

1. **EnumService** (`cn.itcraft.dyenums.spring.EnumService`)
   - Spring service for type-safe enum access
   - Methods: `getValues()`, `getByCode()`, `findByCode()`, `createEnum()`, etc.
   - Converts enums to maps, select options, etc.

2. **EnumConverter** (`cn.itcraft.dyenums.spring.EnumConverter<T>`)
   - Spring Converter for automatic String-to-enum conversion
   - Used in Spring MVC for request parameters and path variables
   - Constructor: `new EnumConverter<>(YourEnum.class)`

3. **DynamicEnumConfig** (`cn.itcraft.dyenums.spring.DynamicEnumConfig`)
   - Spring configuration class
   - Defines EnumService bean
   - Can auto-load configurations on startup

### Module: dyenums-test

**Test and Examples:**

1. **Unit Tests** (`cn.itcraft.dyenums.core.*Test`)
   - BaseDyEnumTest: Tests base functionality
   - EnumRegistryTest: Tests registry operations

2. **Integration Tests** (`cn.itcraft.dyenums.integration.EnumIntegrationTest`)
   - Tests complete workflows
   - Tests Spring integration
   - Tests configuration loading

3. **Example Enums** (`cn.itcraft.dyenums.model`)
   - UserStatus: User account states (ACTIVE, INACTIVE, LOCKED, etc.)
   - OrderStatus: Order processing states (PENDING, PROCESSING, SHIPPED, etc.)

### Design Pattern: Map+Factory

The library uses a registry pattern where:
- Each enum type has its own `Map<String, DyEnum>` in a central registry
- Enum instances are created via constructors or factory methods
- Thread safety is ensured through `ConcurrentHashMap` and synchronized blocks
- Configuration can be loaded from multiple sources (file, database)

## Creating a New Dynamic Enum

### 1. Define Your Enum Class

Create a class in dyenums-test module (for examples) or your own application:

```java
package cn.itcraft.dyenums.model;  // or your application package

import cn.itcraft.dyenums.core.BaseDyEnum;
import cn.itcraft.dyenums.annotation.EnumDefinition;

@EnumDefinition(category = "business", dynamic = true, configSource = "file")
public class OrderStatus extends BaseDyEnum {
    
    // Predefined values
    public static final OrderStatus PENDING = new OrderStatus("PENDING", "待处理", "订单等待处理", 1);
    public static final OrderStatus PROCESSING = new OrderStatus("PROCESSING", "处理中", "订单正在处理", 2);
    
    // Private constructor - also used by reflection for dynamic creation
    private OrderStatus(String code, String name, String description, int order) {
        super(code, name, description, order);
    }
    
    // Factory method for configuration loading
    public static OrderStatus fromValueString(String code, String valueString) {
        String[] parts = valueString.split("\\|", 3);
        return new OrderStatus(code, parts[0], parts[1], Integer.parseInt(parts[2]));
    }
    
    // Business logic methods
    public boolean isInProgress() {
        return this == PENDING || this == PROCESSING;
    }
}
```

### 2. Register Your Enums (Application Startup)

```java
import cn.itcraft.dyenums.core.EnumRegistry;

public class Application {
    public static void main(String[] args) {
        // Register predefined values
        EnumRegistry.register(OrderStatus.class, OrderStatus.PENDING);
        EnumRegistry.register(OrderStatus.class, OrderStatus.PROCESSING);
        
        // Load from configuration
        Properties props = new Properties();
        props.setProperty("OrderStatus.SHIPPED", "已发货|订单已发货|3");
        props.setProperty("OrderStatus.DELIVERED", "已送达|订单已送达|4");
        
        EnumRegistry.registerFromConfig(
            OrderStatus.class, 
            props,
            OrderStatus::fromValueString
        );
    }
}
```

### 3. Use in Your Application

```java
// Look up by code
OrderStatus status = EnumRegistry.valueOf(OrderStatus.class, "PENDING")
    .orElseThrow(() -> new IllegalArgumentException("Status not found"));

// Get all values
List<OrderStatus> allStatuses = EnumRegistry.values(OrderStatus.class);

// Dynamic creation
OrderStatus customStatus = EnumRegistry.addEnum(
    OrderStatus.class,
    "CUSTOM_STATUS",
    "自定义状态",
    "通过代码动态创建的状态",
    999
);
```

### 4. Spring Integration (Optional)

```java
import cn.itcraft.dyenums.spring.EnumService;
import org.springframework.beans.factory.annotation.Autowired;

@Service
public class OrderService {
    
    @Autowired
    private EnumService enumService;
    
    public List<Map<String, String>> getStatusOptions() {
        return enumService.asSelectOptions(OrderStatus.class);
    }
}

// In Spring MVC Controller
@RestController
@RequestMapping("/orders")
public class OrderController {
    
    @GetMapping("/status/{status}")
    public List<Order> getOrdersByStatus(@PathVariable OrderStatus status) {
        // Spring automatically converts path variable using EnumConverter
        return orderRepository.findByStatus(status.getCode());
    }
}
```

## Testing

### Running Tests

```bash
# All tests
mvn test

# Core module only
mvn test -pl dyenums-core

# Specific test class
mvn test -Dtest=EnumRegistryTest

# Specific test method
mvn test -Dtest=EnumRegistryTest#testRegister_SingleValue
```

### Test Coverage

The project includes:
- **Unit Tests**: Test individual components in isolation
- **Integration Tests**: Test complete workflows and Spring integration
- **Thread Safety Tests**: Verify concurrent access safety
- **Example Enums**: Real-world usage examples

## Important Notes

### Module Dependencies

- **dyenums-core**: No dependencies on Spring (can be used standalone)
- **dyenums-spring**: Depends on dyenums-core and Spring Framework
- **dyenums-test**: Depends on dyenums-core and dyenums-spring (test scope)

### Thread Safety

- EnumRegistry uses ConcurrentHashMap for storage
- Synchronization on per-class registries for registration
- Safe for concurrent read and write operations

### Java 8 Compatibility

- Must not use Java 9+ features (e.g., no `List.of()`, no var)
- Use Java 8 APIs only
- Tested with Java 8, 11, and 17

### Package Structure

- `cn.itcraft.dyenums.core`: Core interfaces and registry
- `cn.itcraft.dyenums.config`: Configuration loading classes
- `cn.itcraft.dyenums.annotation`: Annotations
- `cn.itcraft.dyenums.spring`: Spring integration
- `cn.itcraft.dyenums.model`: Example implementations (test module only)

### Best Practices

1. **Registration**: Register enums during application startup
2. **Factory Methods**: Always provide a factory method for configuration loading
3. **Immutability**: Treat enum instances as immutable after creation
4. **Code Uniqueness**: Ensure codes are unique within each enum type
5. **Documentation**: Document the purpose of each enum value
6. **Testing**: Test enum registration, lookup, and business logic

## Configuration Examples

### Properties File (enums.properties)

```properties
UserStatus.ACTIVE=激活|用户已激活|1
UserStatus.INACTIVE=未激活|用户未激活|2
UserStatus.LOCKED=锁定|用户被锁定|3

OrderStatus.PENDING=待处理|订单等待处理|1
OrderStatus.PROCESSING=处理中|订单正在处理|2
OrderStatus.SHIPPED=已发货|订单已发货|3
```

### Database Schema

```sql
CREATE TABLE sys_enum (
    enum_class VARCHAR(100) NOT NULL,
    code VARCHAR(50) NOT NULL,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    sort_order INT DEFAULT 999,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (enum_class, code),
    INDEX idx_enum_class (enum_class),
    INDEX idx_sort_order (sort_order)
);
```

## Troubleshooting

### Common Issues

1. **ClassNotFoundException**: Check package names (should be `cn.itcraft.dyenums`)
2. **No enum constant found**: Ensure enums are registered before use
3. **Thread safety issues**: Always use EnumRegistry methods, not direct map access
4. **Spring conversion errors**: Register EnumConverter in WebMvcConfigurer

### Debug Logging

Enable debug logging for `cn.itcraft.dyenums` package to see:
- Enum registration operations
- Configuration loading details
- Dynamic enum creation

## Resources

- **README.md**: User guide and API examples
- **design.md**: Detailed design decisions and patterns
- **todo.md**: Development tasks and roadmap
- **Module POMs**: See dyenums-core/pom.xml, dyenums-spring/pom.xml

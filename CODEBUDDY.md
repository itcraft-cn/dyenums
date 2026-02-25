# CODEBUDDY.md

This file provides guidance to CodeBuddy Code when working with code in this repository.

## Project Overview

**dyenums** is a dynamic enum library for Java 8 that provides an alternative to Java's static enums. The library uses a
Map+Factory pattern to enable runtime registration, dynamic loading from configuration files or databases, and
extensibility that traditional Java enums cannot provide.

## Technology Stack

- **Java**: Java 8 (must be compatible with Java 8 features)
- **Build Tool**: Maven
- **Key Dependencies**:
    - Spring Framework (for integration features)
    - SLF4J (for logging)
    - JUnit (for testing)

## Build Commands

```bash
# Compile the project
mvn compile

# Run tests
mvn test

# Run a single test class
mvn test -Dtest=ClassName

# Run a single test method
mvn test -Dtest=ClassName#methodName

# Package the library
mvn package

# Install to local repository
mvn install

# Clean build artifacts
mvn clean

# Full clean build with tests
mvn clean install
```

## Project Structure

The project follows standard Maven directory structure:

```
dyenums/
├── src/
│   ├── main/
│   │   └── java/
│   │       └── com/
│   │           └── helly/
│   │               └── dyenums/
│   │                   ├── annotation/          # Custom annotations (@EnumDefinition, etc.)
│   │                   ├── core/                # Core interfaces and base classes
│   │                   │   ├── DyEnum.java    # Main interface
│   │                   │   ├── BaseDyEnum.java
│   │                   │   └── EnumRegistry.java
│   │                   ├── config/              # Configuration loading
│   │                   │   ├── FileBasedEnumConfig.java
│   │                   │   └── DatabaseEnumConfig.java
│   │                   ├── spring/              # Spring integration
│   │                   │   ├── DynamicEnumConfig.java
│   │                   │   ├── EnumService.java
│   │                   │   └── EnumConverter.java
│   │                   └── model/               # Example enum implementations
│   │                       ├── UserStatus.java
│   │                       └── OrderStatus.java
│   └── test/
│       └── java/
│           └── com/
│               └── helly/
│                   └── dyenums/
│                       ├── core/                # Unit tests for core classes
│                       └── integration/         # Integration tests
├── pom.xml
├── design.md               # Detailed design documentation
├── todo.md                 # Development requirements
└── CODEBUDDY.md            # This file
```

## Core Architecture

### Key Components

1. **DyEnum Interface** (core/DyEnum.java)
    - Defines the contract for all dynamic enums
    - Methods: `getCode()`, `getName()`, `getDescription()`, `getOrder()`

2. **BaseDyEnum Class** (core/BaseDyEnum.java)
    - Abstract base implementation of DyEnum
    - Handles common functionality
    - Must be extended by concrete enum classes

3. **EnumRegistry Class** (core/EnumRegistry.java)
    - Central registry storing all enum instances
    - Uses `ConcurrentHashMap` for thread-safe storage
    - Key methods:
        - `register(Class<T>, T)` - Register an enum instance
        - `valueOf(Class<T>, String)` - Look up by code
        - `values(Class<T>)` - Get all values for a type
        - `addEnum(Class<T>, ...)` - Dynamic enum creation
        - `registerFromConfig(...)` - Load from configuration

4. **Configuration Loading** (config/)
    - **FileBasedEnumConfig**: Load enums from .properties files
    - **DatabaseEnumConfig**: Load enums from database

5. **Spring Integration** (spring/)
    - **DynamicEnumConfig**: Spring configuration class
    - **EnumService**: Service layer for accessing enums
    - **EnumConverter**: Spring Converter for HTTP request binding

### Design Pattern: Map+Factory

The library uses a registry pattern where:

- Each enum type has its own Map in the central registry
- Enum instances are created via reflection (Factory pattern)
- Thread safety is ensured through `ConcurrentHashMap` and synchronized methods
- Configuration can be loaded from multiple sources (file, database, remote)

## Development Guidelines

### Creating a New Dynamic Enum

1. Create a class extending `BaseDyEnum`
2. Define static final instances for predefined values
3. Ensure a constructor matching: `(String code, String name, String description, int order)`
4. Register instances in `EnumRegistry` during application startup

Example:

```java
public class OrderStatus extends BaseDyEnum {
    public static final OrderStatus PENDING = new OrderStatus("PENDING", "待处理", "订单等待处理", 1);
    public static final OrderStatus PROCESSING = new OrderStatus("PROCESSING", "处理中", "订单正在处理", 2);
    
    private OrderStatus(String code, String name, String description, int order) {
        super(code, name, description, order);
    }
}
```

### Registering Enums

```java
// Register single instance
EnumRegistry.register(OrderStatus.class, OrderStatus.PENDING);

// Register multiple
EnumRegistry.registerAll(OrderStatus.class, Arrays.asList(
    OrderStatus.PENDING,
    OrderStatus.PROCESSING
));

// Load from configuration
Properties props = new Properties();
// ... load properties file
EnumRegistry.registerFromConfig(OrderStatus.class, props, 
    code -> new OrderStatus(code, "动态", "动态描述", 999));
```

### Using Enums

```java
// Look up by code
OrderStatus status = EnumRegistry.valueOf(OrderStatus.class, "PENDING")
    .orElseThrow(() -> new IllegalArgumentException("Invalid status"));

// Get all values
List<OrderStatus> allStatuses = EnumRegistry.values(OrderStatus.class);

// Check if exists
boolean isValid = EnumRegistry.contains(OrderStatus.class, "PENDING");
```

### Spring Integration

When using with Spring Framework:

1. Configure beans in `DynamicEnumConfig`
2. Use `EnumService` for type-safe access
3. Register `EnumConverter` for automatic request parameter conversion
4. Use `@EnumDefinition` annotation for declarative configuration

## Testing Strategy

- **Unit Tests**: Test individual components (EnumRegistry, BaseDyEnum, etc.)
- **Integration Tests**: Test Spring integration and configuration loading
- **Thread Safety Tests**: Test concurrent registration and access
- **Example Test Pattern**:
  ```java
  @Test
  public void testEnumRegistration() {
      EnumRegistry.register(UserStatus.class, UserStatus.ACTIVE);
      Optional<UserStatus> result = EnumRegistry.valueOf(UserStatus.class, "ACTIVE");
      assertTrue(result.isPresent());
      assertEquals("ACTIVE", result.get().getCode());
  }
  ```

## Important Notes

- This is a **Java 8** project - must not use features from Java 9+
- Thread safety is critical - always use `ConcurrentHashMap` and proper synchronization
- The library should work without Spring (core module has no Spring dependencies)
- Spring integration should be in a separate module or package
- All public APIs must be well-documented with JavaDoc
- Logging should use SLF4J
- Follow existing code style from design.md examples

## Configuration Files

The library supports loading enum definitions from configuration files:

**Properties file format:**

```properties
OrderStatus.PENDING.code=PENDING
OrderStatus.PENDING.name=待处理
OrderStatus.PENDING.description=订单等待处理
OrderStatus.PENDING.order=1

OrderStatus.PROCESSING.code=PROCESSING
OrderStatus.PROCESSING.name=处理中
OrderStatus.PROCESSING.description=订单正在处理
OrderStatus.PROCESSING.order=2
```

Or from database table (example schema):

```sql
CREATE TABLE sys_enum (
    enum_class VARCHAR(100),
    code VARCHAR(50),
    name VARCHAR(100),
    description VARCHAR(500),
    sort_order INT
);
```

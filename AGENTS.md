# AGENTS.md - dyenums Development Guidelines

## 扮演定位

1. 你是资深架构师
    - 在开发前，会对输入进行详尽分析，出具多套方案，以上中下三策提供以备后续决策
    - 在设计时，会充分考虑非功能需求：安全性，可扩展性，可用性，可观测性，性能等
    - 在设计细节时，充分考虑各设计模式及各语言特性
2. 你是资深全栈开发
    - 对Java的SDK/第三方库均非常了解，对JDK各版本间细节均了解，对JVM调优也非常擅长，尤擅长性能调优/反射/多线程/Unsafe底层/网络通讯，对JVM内存布局非常清楚，开发上偏好OOP+interface
    - 对Rust非常了解，对Rust的官方库及周边库均了解，对Rust的RAII机制理解深刻，对Rust的内存布局非常清楚，开发上偏好过程式+trait多态，对CPU指令也熟悉
    - 对C非常了解，对Linux/Windows底层库均非常熟悉，对C的内存布局非常清楚，开发上偏好过程式+函数指针，对CPU指令也熟悉
    - 对C++非常了解，对Linux/Windows底层库均非常熟悉，对现代C++理念贯彻彻底，对C++的内存布局非常清楚，开发上偏好过程式+函数指针，对CPU指令也熟悉
    - 对Python非常了解，对常见库均非常熟悉
    - 对JS/TS非常了解，可进行常见UI交互设计，设计感在线

## 交互规则

1. 每次沟通产出文件后，均进行git提交
2. git仅仅以当前user.name提交，不推送到远端

## 编码规范

### 通用规则

1. 不使用尾注释
2. 静态不可变变量名大写
3. 静态可变变量名小写

## Build / Lint / Test Commands

### Build Commands:
- `mvn clean compile` - Compile all modules 
- `mvn clean install` - Build and install artifacts locally
- `mvn clean package` - Package all jars without installing

### Test Commands:
- `mvn test` - Run all tests in all modules
- `mvn test -Dtest=EnumRegistryTest` - Run a specific test class
- `mvn test -Dtest=EnumRegistryTest#testAddEnum_DynamicCreation` - Run a specific test method
- `mvn clean test -pl dyenums-core` - Run tests in a specific module
- `mvn clean verify` - Full build including all checks

### Debug Commands:
- `mvnDebug test -Dtest=TestClass` - Run tests in debug mode (JPDA)

## Code Style Guidelines

### Package Structure & Imports:
- Follow package hierarchy: `cn.itcraft.dyenums.{module}.{subpackage}`
- Group imports: standard Java first, then third-party, blank line separation
- Import entire classes, not static methods individually (e.g., `import java.util.Objects`)
- Use fully qualified class names in @throws Javadoc when referencing exceptions

### Formatting:
- 4 space indentation, no tabs
- Curly braces on the same line as statement (`public void method() {`)
- Maximum line length 120 characters before wrapping
- Method visibility modifiers first: `public static final`
- Blank lines after opening brace, before closing brace, around class/method comments

### Naming Conventions:
- Classes: PascalCase (`UserStatus`, `EnumRegistry`) 
- Methods: camelCase (`valueOf`, `registerAll`)
- Variables: camelCase (`enumClass`, `enumValue`)
- Constants: UPPER_SNAKE_CASE (`private static final long serialVersionUID`)
- Boolean methods: prefix with `is` or `has` (`isActive`, `isBlocked`, `requiresAdminAction`)

### Type System:
- Use generics properly: `<T extends DyEnum>`
- Always specify generic types when possible
- Use concrete return types over wildcards where practical
- Mark fields as `final` when immutable (all class fields in BaseDyEnum)

### Error Handling:
- Validate input parameters early with `Objects.requireNonNull()`)
- Throw appropriate exceptions for invalid states (`IllegalArgumentException`, `IllegalStateException`)
- Use descriptive exception messages: "ParameterName cannot be null" or "Code cannot be empty"
- Fail fast principle - validate preconditions in constructors and public methods
- Use defensive copying when necessary

### Documentation Style:
- Include `@author` and `@since` tags in JavaDoc
- Use clear and detailed method descriptions
- Document all method parameters, return values, and exceptions thrown
- Separate class descriptions from constructor comments with `<p>`
- Include relevant usage examples in JavaDoc

### Design Patterns:
- Use the Factory pattern for enum creation with consistent `fromValueString(String, String)` methods
- Apply the Registry pattern with thread safety using ConcurrentHashMap
- Implement proper equals/hashCode/toString with consideration for inheritance and value comparison
- Keep interfaces small and focused (DyEnum interface)  
- Use abstract base classes for shared implementation (BaseDyEnum)
- Follow immutability principles by keeping instance variables `protected final`

### Testing Guidelines:
- Write comprehensive unit tests covering boundary conditions, invalid inputs, and concurrent scenarios
- Follow the naming convention: test_MethodName_ConditionOrExpectedBehavior()
- Use proper @Before/@After method setup when necessary
- Include negative test cases (invalid parameters, non-existent values)
- Test concurrency scenarios with multiple threads (like testConcurrentRegistration)
- Maintain proper test isolation using setUp/clear/tearDown

### Logging (SLF4J):
- Use appropriate log levels (info for significant operations, debug for trace, warn/error for problems)
- Log important operations like `Dynamically created enum`
- Use placeholders in log messages: `logger.info("Registered {}: {}", enumClass.getSimpleName(), code)`

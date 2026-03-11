# AGENTS.md - dyenums Development Guidelines

## Role Definition

1. **Senior Architect**: Analyze requirements thoroughly, provide multiple solutions (upper/middle/lower strategies),
   consider non-functional requirements (security, scalability, availability, observability, performance)
2. **Senior Java Developer**: Expert in Java SDK/third-party libraries, JVM tuning, performance optimization,
   reflection, multithreading, Unsafe internals, networking. Preference: OOP + interfaces

## Interaction Rules

1. Commit files after each interaction
2. Use current `user.name` for git commits, do not push to remote
3. Follow Conventional Commits specification

## Project Architecture

dyenums is a Java 8+ dynamic enum library using Map+Factory pattern for runtime enum extension.

**Modules:**

- **dyenums-core**: Core interfaces (DyEnum, BaseDyEnum, EnumRegistry, EnumPerformanceMonitor)
- **dyenums-spring**: Spring integration (EnumService, EnumConverter, DynamicEnumConfig)
- **dyenums-config-file**: File config loader (FileBasedEnumConfig, PropEnumConfig, EnumLoader)
- **dyenums-config-db**: Database config loader (DatabaseEnumConfig, DbSqlExecutor, DbEnumConsts, ResultSetHandler)
- **dyenums-test**: Test module and sample enums

## Build / Lint / Test Commands

### Build:

```bash
mvn clean compile          # Compile all modules
mvn clean install          # Build and install locally
mvn clean package          # Package jars without installing
```

### Test:

```bash
mvn test                                          # Run all tests
mvn test -Dtest=EnumRegistryTest                  # Run specific test class
mvn test -Dtest=EnumRegistryTest#testAddEnum      # Run specific test method
mvn test -pl dyenums-core                         # Run tests in specific module
mvn test -Dtest=EnumRegistryTest -pl dyenums-test # Run specific class in module
```

### Debug:

```bash
mvnDebug test -Dtest=TestClass    # Run tests in debug mode (JPDA port 8000)
```

## Code Style Guidelines

### General Rules:

1. No end-of-line comments
2. Static immutable variables: UPPER_SNAKE_CASE (`SQL_DDL`)
3. Static mutable variables: lowercase
4. Single Responsibility Principle - split large classes into focused small classes
5. Prefer composition over inheritance

### Class Design Patterns:

```java
// Utility class: final + private constructor
final class DbSqlExecutor {
    private DbSqlExecutor() { }
    static void execSql(...) { }
}

// Constants class: final + private constructor + static final fields
final class DbEnumConsts {
    static final String SQL_DDL = "...";
    static final String[] FORBIDDEN_SQL_OP = {"DROP", "DELETE", ...};
    private DbEnumConsts() { }
}

// Functional interface
@FunctionalInterface
interface ResultSetHandler<T> {
    void process(ResultSet rs) throws SQLException;
}
```

### Access Control:

- **Public API**: `public` modifier for external use
- **Internal utilities**: package-private (no modifier) for internal use only
- **Constants**: package-private, access via `static import`

### Naming Conventions:

| Type            | Convention       | Example                      |
|-----------------|------------------|------------------------------|
| Classes         | PascalCase       | `UserStatus`, `EnumRegistry` |
| Methods         | camelCase        | `valueOf`, `registerAll`     |
| Variables       | camelCase        | `enumClass`, `enumValue`     |
| Constants       | UPPER_SNAKE_CASE | `SQL_DML_QUERY`              |
| Boolean methods | is/has prefix    | `isActive`, `hasValue`       |
| Handlers        | Handler suffix   | `ResultSetHandler`           |
| Executors       | Executor suffix  | `DbSqlExecutor`              |
| Constants       | Consts suffix    | `DbEnumConsts`               |

### Formatting:

- 4 spaces indentation, no tabs
- Braces on same line: `public void method() {`
- Max line length: 120 characters
- Visibility modifiers first: `public static final`

### Imports:

- Package hierarchy: `cn.itcraft.dyenums.{module}.{subpackage}`
- Group imports: Java standard → third-party, separated by blank line
- Import whole classes, not individual static methods
- Use `static import` for constants from constant classes

### Type System:

- Use generics properly: `<T extends DyEnum>`
- Specify generic types when possible
- Use concrete return types over wildcards
- Mark immutable fields as `final`
- Use `LongAdder` instead of `AtomicLong` for high-concurrency counting

### Error Handling:

- Validate early with `Objects.requireNonNull()`
- Throw appropriate exceptions: `IllegalArgumentException`, `IllegalStateException`
- Descriptive messages: "ParameterName cannot be null"
- Fail fast - validate preconditions in constructors

### Logging (SLF4J):

- Levels: info (significant), debug (trace), warn (recoverable issues), error (problems)
- Use placeholders: `logger.info("Registered {}: {}", name, value)`
- For recoverable batch errors, use `warn` not `error`

## Thread Safety

- Use `ConcurrentHashMap` for thread-safe storage
- `computeIfAbsent()` for atomic map initialization
- Synchronize when creating new registry entries
- Thread-safe operations: `register()`, `valueOf()`, `values()`, `addEnum()`

## SQL Security

- All queries must be validated
- Forbidden keywords: `DROP`, `DELETE`, `TRUNCATE`, `ALTER`, `INSERT`, `UPDATE`, `EXEC`
- Only `SELECT` statements allowed

## Testing Guidelines

- Test naming: `test_MethodName_ConditionOrExpectedBehavior()`
- Cover: boundary conditions, invalid inputs, concurrent scenarios
- Include negative test cases
- Use `@Before`/`@After` for setup/teardown
- Test concurrency with multiple threads

package cn.itcraft.dyenums.core;

import cn.itcraft.dyenums.annotation.EnumDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

/**
 * Central registry for dynamic enum values.
 * This class implements the Map+Factory pattern to provide runtime registration,
 * lookup, and management of enum instances.
 * <p>
 * The registry uses thread-safe data structures (ConcurrentHashMap) and
 * synchronized methods to ensure thread safety during registration and access.
 *
 * @author Helly
 * @since 1.0.0
 */
public class EnumRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(EnumRegistry.class);

    /**
     * Main registry storage: maps enum class to a map of code -> enum instance.
     * Uses ConcurrentHashMap for thread-safe access.
     */
    private static final Map<Class<?>, Map<String, DyEnum>> REGISTRIES = new ConcurrentHashMap<>();

    /**
     * Cache for Constructor objects to avoid repeated reflection overhead.
     * Key: enum class, Value: cached constructor
     */
    private static final ConcurrentHashMap<Class<?>, Constructor<?>> CONSTRUCTOR_CACHE =
            new ConcurrentHashMap<>();

    /**
     * Cache for sorted values lists to improve performance of frequent queries.
     * Key: enum class, Value: cached sorted list
     */
    private static final ConcurrentHashMap<Class<?>, List<? extends DyEnum>> VALUES_CACHE =
            new ConcurrentHashMap<>();

    /**
     * Static initializer to register default enum values.
     * This method is called when the class is loaded.
     */
    static {
        registerDefaults();
    }

    /**
     * Private constructor to prevent instantiation.
     * This is a utility class with static methods only.
     */
    private EnumRegistry() {
        throw new UnsupportedOperationException("Utility class should not be instantiated");
    }

    /**
     * Security validation: ensures only annotated classes can be dynamically created.
     *
     * @param enumClass the class to validate
     * @throws SecurityException if the class is not allowed for dynamic creation
     */
    private static <T extends DyEnum> void validateEnumClassForDynamicCreation(Class<T> enumClass) {
        if (!enumClass.isAnnotationPresent(EnumDefinition.class)) {
            throw new SecurityException(
                    "Only classes annotated with @EnumDefinition can be dynamically created: "
                            + enumClass.getName());
        }
    }

    /**
     * Invalidates cache for a specific enum class.
     *
     * @param enumClass the enum class whose cache should be invalidated
     */
    private static void invalidateCache(Class<?> enumClass) {
        VALUES_CACHE.remove(enumClass);
    }

    /**
     * Registers default enum values.
     * This method is called during static initialization.
     * Currently a placeholder - subclasses should call register() in their own static blocks.
     */
    private static void registerDefaults() {
        LOGGER.debug("Initializing EnumRegistry with defaults");
        // Default registration is handled by enum classes themselves in their static blocks
    }

    /**
     * Registers a single enum instance.
     *
     * @param enumClass the enum class type
     * @param enumValue the enum instance to register
     * @param <T>       the enum type
     * @throws NullPointerException if enumClass or enumValue is null
     */
    public static <T extends DyEnum> void register(Class<T> enumClass, T enumValue) {
        Objects.requireNonNull(enumClass, "Enum class cannot be null");
        Objects.requireNonNull(enumValue, "Enum value cannot be null");

        long startTime = System.nanoTime();

        // Use computeIfAbsent for atomic registry creation
        Map<String, DyEnum> classRegistry = REGISTRIES.computeIfAbsent(
                enumClass, k -> new ConcurrentHashMap<>()
                                                                      );

        // ConcurrentHashMap.put() is thread-safe, no synchronization needed
        String code = enumValue.getCode();
        DyEnum previous = classRegistry.put(code, enumValue);
        if (previous != null) {
            LOGGER.warn("Overwriting existing enum value for {}: {}",
                        enumClass.getSimpleName(), code);
        }
        LOGGER.debug("Registered {}: {}", enumClass.getSimpleName(), code);

        // Invalidate cache
        invalidateCache(enumClass);

        // Record performance metric
        EnumPerformanceMonitor.recordRegister(startTime, enumClass);
    }

    /**
     * Registers multiple enum instances efficiently.
     *
     * @param enumClass the enum class type
     * @param values    collection of enum instances to register
     * @param <T>       the enum type
     * @throws NullPointerException if enumClass or values is null
     */
    public static <T extends DyEnum> void registerAll(Class<T> enumClass, Collection<T> values) {
        Objects.requireNonNull(enumClass, "Enum class cannot be null");
        Objects.requireNonNull(values, "Values cannot be null");

        if (values.isEmpty()) {
            return;
        }

        // Use computeIfAbsent for atomic registry creation
        Map<String, DyEnum> classRegistry = REGISTRIES.computeIfAbsent(
                enumClass, k -> new ConcurrentHashMap<>()
                                                                      );

        // Batch registration without synchronization
        int count = 0;
        for (T value : values) {
            Objects.requireNonNull(value, "Enum value in collection cannot be null");
            String code = value.getCode();
            DyEnum previous = classRegistry.put(code, value);
            if (previous != null) {
                LOGGER.warn("Overwriting existing enum value for {}: {}",
                            enumClass.getSimpleName(), code);
            }
            count++;
        }
        LOGGER.debug("Registered {} values for {}", count, enumClass.getSimpleName());

        // Invalidate cache
        invalidateCache(enumClass);
    }

    /**
     * Registers enum instances from configuration properties.
     * The configuration format should be: className.code=code|name|description|order
     *
     * @param enumClass the enum class type
     * @param config    the configuration properties
     * @param factory   function to create enum instances from codes
     * @param <T>       the enum type
     * @throws NullPointerException if enumClass, config, or factory is null
     */
    public static <T extends DyEnum> void registerFromConfig(
            Class<T> enumClass,
            Properties config,
            BiFunction<String, String, T> factory) {
        Objects.requireNonNull(enumClass, "Enum class cannot be null");
        Objects.requireNonNull(config, "Config cannot be null");
        Objects.requireNonNull(factory, "Factory cannot be null");

        for (Map.Entry<Object, Object> entry : config.entrySet()) {
            Object keyObj = entry.getKey();
            Object valueObj = entry.getValue();

            if (keyObj == null || valueObj == null) {
                LOGGER.warn("Skipping null key or value in config for enum: {}", enumClass.getSimpleName());
                continue;
            }

            String fullKey = keyObj.toString();
            String valueStr = valueObj.toString();

            if (fullKey.trim().isEmpty() || valueStr.trim().isEmpty()) {
                LOGGER.warn("Skipping empty key or value in config: {}", fullKey);
                continue;
            }

            String[] parts = fullKey.split("\\.", 2); // Limit to 2 splits for efficiency

            // Check if this property belongs to our enum class
            if (parts.length == 2 && parts[0].equals(enumClass.getSimpleName())) {
                String code = parts[1];
                if (code.trim().isEmpty()) {
                    LOGGER.warn("Invalid configuration key format: '{}' for enum '{}'", fullKey,
                                enumClass.getSimpleName());
                    continue;
                }

                // Parse value format: name|description|order
                String[] valueParts = valueStr.split("\\|", 3);
                if (valueParts.length >= 3) {
                    try {
                        // Call factory with the code and the value string
                        T enumValue = factory.apply(code, valueStr);
                        register(enumClass, enumValue);
                        LOGGER.info("Successfully loaded enum from config: {}.{}",
                                    enumClass.getSimpleName(), code);
                    } catch (Exception e) {
                        // Don't log full valueStr to avoid potential sensitive info leakage
                        LOGGER.warn("Failed to create enum from config: {}.{}, error: {}",
                                    enumClass.getSimpleName(), code, e.getMessage());
                    }
                } else {
                    LOGGER.warn("Invalid config format for {}.{}, expected: 'name|description|order'",
                                enumClass.getSimpleName(), code);
                }
            }
        }
    }

    /**
     * Looks up an enum value by code (equivalent to Java enum's valueOf).
     *
     * @param enumClass the enum class type
     * @param code      the code to look up
     * @param <T>       the enum type
     * @return Optional containing the enum value, or empty if not found
     * @throws NullPointerException if enumClass is null
     */
    public static <T extends DyEnum> Optional<T> valueOf(Class<T> enumClass, String code) {
        Objects.requireNonNull(enumClass, "Enum class cannot be null");

        long startTime = System.nanoTime();

        if (code == null) {
            EnumPerformanceMonitor.recordValueOf(startTime, enumClass);
            return Optional.empty();
        }

        Map<String, DyEnum> classRegistry = REGISTRIES.get(enumClass);
        if (classRegistry == null) {
            EnumPerformanceMonitor.recordValueOf(startTime, enumClass);
            return Optional.empty();
        }

        @SuppressWarnings("unchecked")
        T result = (T) classRegistry.get(code);

        // Record performance metric
        EnumPerformanceMonitor.recordValueOf(startTime, enumClass);
        return Optional.ofNullable(result);
    }

    /**
     * Gets all enum values for a class, sorted by order.
     * Results are cached for performance optimization.
     *
     * @param enumClass the enum class type
     * @param <T>       the enum type
     * @return list of all enum values, sorted by order
     * @throws NullPointerException if enumClass is null
     */
    public static <T extends DyEnum> List<T> values(Class<T> enumClass) {
        Objects.requireNonNull(enumClass, "Enum class cannot be null");

        // Check cache first
        @SuppressWarnings("unchecked")
        List<T> cached = (List<T>) VALUES_CACHE.get(enumClass);
        if (cached != null) {
            return cached;
        }

        long startTime = System.nanoTime();

        Map<String, DyEnum> classRegistry = REGISTRIES.get(enumClass);
        if (classRegistry == null || classRegistry.isEmpty()) {
            EnumPerformanceMonitor.recordValues(startTime, enumClass);
            return Collections.emptyList();
        }

        // Pre-calculate size to avoid resizing and use manual iteration to convert types
        List<T> result = new ArrayList<>(classRegistry.size());
        for (DyEnum value : classRegistry.values()) {
            @SuppressWarnings("unchecked")
            T typedValue = (T) value;
            result.add(typedValue);
        }

        // Sort by order once all items collected
        result.sort(Comparator.comparingInt(DyEnum::getOrder));

        // Create unmodifiable list and cache it
        List<T> unmodifiableResult = Collections.unmodifiableList(result);
        VALUES_CACHE.put(enumClass, unmodifiableResult);

        // Record performance metric
        EnumPerformanceMonitor.recordValues(startTime, enumClass);
        return unmodifiableResult;
    }

    /**
     * Checks if the registry contains a specific enum value.
     *
     * @param enumClass the enum class type
     * @param code      the code to check
     * @param <T>       the enum type
     * @return true if the enum value exists, false otherwise
     * @throws NullPointerException if enumClass is null
     */
    public static <T extends DyEnum> boolean contains(Class<T> enumClass, String code) {
        Objects.requireNonNull(enumClass, "Enum class cannot be null");

        if (code == null) {
            return false;
        }

        Map<String, DyEnum> classRegistry = REGISTRIES.get(enumClass);
        return classRegistry != null && classRegistry.containsKey(code);
    }

    /**
     * Dynamically creates and registers a new enum instance using reflection.
     * The enum class must have a constructor accepting (String code, String name, String description, int order)
     * and must be annotated with @EnumDefinition for security purposes.
     *
     * @param enumClass   the enum class type
     * @param code        the unique code for the new enum
     * @param name        the display name for the new enum
     * @param description the description for the new enum
     * @param order       the order/sort index for the new enum
     * @param <T>         the enum type
     * @return the newly created enum instance
     * @throws SecurityException        if the class is not annotated with @EnumDefinition
     * @throws IllegalStateException    if enum creation fails
     * @throws NullPointerException     if enumClass, code, or name is null
     * @throws IllegalArgumentException if code or name is empty
     */
    public static <T extends DyEnum> T addEnum(
            Class<T> enumClass,
            String code,
            String name,
            String description,
            int order) {
        Objects.requireNonNull(enumClass, "Enum class cannot be null");
        Objects.requireNonNull(code, "Code cannot be null");
        Objects.requireNonNull(name, "Name cannot be null");

        // Security validation: only allow annotated classes
        validateEnumClassForDynamicCreation(enumClass);

        long startTime = System.nanoTime();

        if (code.trim().isEmpty()) {
            throw new IllegalArgumentException("Code cannot be empty");
        }
        if (name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be empty");
        }

        try {
            // Use cached constructor for better performance
            @SuppressWarnings("unchecked")
            Constructor<T> constructor = (Constructor<T>) CONSTRUCTOR_CACHE.get(enumClass);

            if (constructor == null) {
                constructor = enumClass.getDeclaredConstructor(
                        String.class, String.class, String.class, int.class
                                                              );
                constructor.setAccessible(true);
                CONSTRUCTOR_CACHE.put(enumClass, constructor);
            }

            T newEnum = constructor.newInstance(code.trim(), name.trim(),
                                                description != null ? description.trim() : "", order);
            register(enumClass, newEnum);

            LOGGER.info("Dynamically created enum: {}.{}", enumClass.getSimpleName(), code);

            // Record performance metric
            EnumPerformanceMonitor.recordAddEnum(startTime, enumClass);
            return newEnum;

        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(
                    "Enum class " + enumClass.getSimpleName() +
                            " must have a constructor (String, String, String, int)", e);
        } catch (InstantiationException e) {
            throw new IllegalStateException(
                    "Enum class " + enumClass.getSimpleName() + " cannot be instantiated via reflection", e);
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Failed to create enum instance of class " + enumClass.getSimpleName(), e);
        }
    }

    /**
     * Removes an enum value from the registry.
     *
     * @param enumClass the enum class type
     * @param code      the code of the enum to remove
     * @param <T>       the enum type
     * @return true if the enum was removed, false if not found
     * @throws NullPointerException if enumClass is null
     */
    public static <T extends DyEnum> boolean remove(Class<T> enumClass, String code) {
        Objects.requireNonNull(enumClass, "Enum class cannot be null");

        long startTime = System.nanoTime();

        if (code == null) {
            EnumPerformanceMonitor.recordRemove(startTime, enumClass);
            return false;
        }

        Map<String, DyEnum> classRegistry = REGISTRIES.get(enumClass);
        if (classRegistry == null) {
            EnumPerformanceMonitor.recordRemove(startTime, enumClass);
            return false;
        }

        // ConcurrentHashMap.remove() is thread-safe
        DyEnum removed = classRegistry.remove(code);
        if (removed != null) {
            LOGGER.info("Removed enum: {}.{}", enumClass.getSimpleName(), code);
            invalidateCache(enumClass);
        }

        // Record performance metric
        EnumPerformanceMonitor.recordRemove(startTime, enumClass);
        return removed != null;
    }

    /**
     * Clears all enum values from the registry and caches.
     * Use with caution - this removes all registered enums.
     */
    public static void clear() {
        REGISTRIES.clear();
        VALUES_CACHE.clear();
        CONSTRUCTOR_CACHE.clear();
        LOGGER.info("Cleared entire enum registry and caches");
    }

    /**
     * Gets all registered enum classes.
     *
     * @return set of all registered enum classes
     */
    public static Set<Class<?>> getRegisteredClasses() {
        return new HashSet<>(REGISTRIES.keySet());
    }

    /**
     * Gets the count of registered enum values for a class.
     *
     * @param enumClass the enum class type
     * @param <T>       the enum type
     * @return count of registered values, or 0 if class not registered
     * @throws NullPointerException if enumClass is null
     */
    public static <T extends DyEnum> int getCount(Class<T> enumClass) {
        Objects.requireNonNull(enumClass, "Enum class cannot be null");

        Map<String, DyEnum> classRegistry = REGISTRIES.get(enumClass);
        return classRegistry != null ? classRegistry.size() : 0;
    }

    /**
     * Gets all enum codes for a class, sorted by order.
     * Provides better performance when only codes are needed.
     * Leverages values() cache for improved performance.
     *
     * @param enumClass the enum class type
     * @param <T>       the enum type
     * @return list of all enum codes, sorted by order
     * @throws NullPointerException if enumClass is null
     */
    public static <T extends DyEnum> List<String> codes(Class<T> enumClass) {
        Objects.requireNonNull(enumClass, "Enum class cannot be null");

        // Reuse values() cache for better performance
        List<T> valuesList = values(enumClass);
        if (valuesList.isEmpty()) {
            return Collections.emptyList();
        }

        // Extract codes from cached values
        List<String> result = new ArrayList<>(valuesList.size());
        for (T value : valuesList) {
            result.add(value.getCode());
        }

        return Collections.unmodifiableList(result);
    }
}

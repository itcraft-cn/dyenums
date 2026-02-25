package com.helly.dyenums.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * Central registry for dynamic enum values.
 * This class implements the Map+Factory pattern to provide runtime registration,
 * lookup, and management of enum instances.
 *
 * The registry uses thread-safe data structures (ConcurrentHashMap) and
 * synchronized methods to ensure thread safety during registration and access.
 *
 * @author Helly
 * @since 1.0.0
 */
public class EnumRegistry {
    
    private static final Logger logger = LoggerFactory.getLogger(EnumRegistry.class);
    
    /**
     * Main registry storage: maps enum class to a map of code -> enum instance.
     * Uses ConcurrentHashMap for thread-safe access.
     */
    private static final Map<Class<?>, Map<String, CodeEnum>> registries = new ConcurrentHashMap<>();
    
    /**
     * Private constructor to prevent instantiation.
     * This is a utility class with static methods only.
     */
    private EnumRegistry() {
        throw new UnsupportedOperationException("Utility class should not be instantiated");
    }
    
    /**
     * Static initializer to register default enum values.
     * This method is called when the class is loaded.
     */
    static {
        registerDefaults();
    }
    
    /**
     * Registers default enum values.
     * This method is called during static initialization.
     * Currently a placeholder - subclasses should call register() in their own static blocks.
     */
    private static void registerDefaults() {
        logger.debug("Initializing EnumRegistry with defaults");
        // Default registration is handled by enum classes themselves in their static blocks
    }
    
    /**
     * Registers a single enum instance.
     *
     * @param enumClass the enum class type
     * @param enumValue the enum instance to register
     * @param <T> the enum type
     * @throws NullPointerException if enumClass or enumValue is null
     */
    public static <T extends CodeEnum> void register(Class<T> enumClass, T enumValue) {
        Objects.requireNonNull(enumClass, "Enum class cannot be null");
        Objects.requireNonNull(enumValue, "Enum value cannot be null");
        
        // Synchronize on the specific class registry to allow concurrent registration
        // of different enum types while preventing race conditions for the same type
        Map<String, CodeEnum> classRegistry = registries.computeIfAbsent(
            enumClass, k -> new ConcurrentHashMap<>()
        );
        
        synchronized (classRegistry) {
            String code = enumValue.getCode();
            if (classRegistry.containsKey(code)) {
                logger.warn("Overwriting existing enum value for {}: {}", 
                    enumClass.getSimpleName(), code);
            }
            classRegistry.put(code, enumValue);
            logger.debug("Registered {}: {}", enumClass.getSimpleName(), code);
        }
    }
    
    /**
     * Registers multiple enum instances.
     *
     * @param enumClass the enum class type
     * @param values collection of enum instances to register
     * @param <T> the enum type
     * @throws NullPointerException if enumClass or values is null
     */
    public static <T extends CodeEnum> void registerAll(Class<T> enumClass, Collection<T> values) {
        Objects.requireNonNull(enumClass, "Enum class cannot be null");
        Objects.requireNonNull(values, "Values cannot be null");
        
        values.forEach(value -> register(enumClass, value));
    }
    
    /**
     * Registers enum instances from configuration properties.
     * The configuration format should be: className.code=code|name|description|order
     *
     * @param enumClass the enum class type
     * @param config the configuration properties
     * @param factory function to create enum instances from codes
     * @param <T> the enum type
     * @throws NullPointerException if enumClass, config, or factory is null
     */
    public static <T extends CodeEnum> void registerFromConfig(
            Class<T> enumClass,
            Properties config,
            BiFunction<String, String, T> factory) {
        Objects.requireNonNull(enumClass, "Enum class cannot be null");
        Objects.requireNonNull(config, "Config cannot be null");
        Objects.requireNonNull(factory, "Factory cannot be null");
        
        config.forEach((key, value) -> {
            String fullKey = key.toString();
            String[] parts = fullKey.split("\\.");
            
            // Check if this property belongs to our enum class
            if (parts.length >= 2 && parts[0].equals(enumClass.getSimpleName())) {
                String code = parts[1];
                String valueStr = value.toString();
                
                // Parse value format: name|description|order
                String[] valueParts = valueStr.split("\\|", 3);
                if (valueParts.length >= 3) {
                    try {
                        T enumValue = factory.apply(code, valueStr);
                        register(enumClass, enumValue);
                        logger.info("Loaded enum from config: {}.{}", 
                            enumClass.getSimpleName(), code);
                    } catch (Exception e) {
                        logger.error("Failed to create enum from config: {}.{} = {}", 
                            enumClass.getSimpleName(), code, valueStr, e);
                    }
                } else {
                    logger.warn("Invalid config format for {}.{}: {}", 
                        enumClass.getSimpleName(), code, valueStr);
                }
            }
        });
    }
    
    /**
     * Looks up an enum value by code (equivalent to Java enum's valueOf).
     *
     * @param enumClass the enum class type
     * @param code the code to look up
     * @param <T> the enum type
     * @return Optional containing the enum value, or empty if not found
     * @throws NullPointerException if enumClass is null
     */
    public static <T extends CodeEnum> Optional<T> valueOf(Class<T> enumClass, String code) {
        Objects.requireNonNull(enumClass, "Enum class cannot be null");
        
        if (code == null) {
            return Optional.empty();
        }
        
        Map<String, CodeEnum> classRegistry = registries.get(enumClass);
        if (classRegistry == null) {
            return Optional.empty();
        }
        
        @SuppressWarnings("unchecked")
        T result = (T) classRegistry.get(code);
        return Optional.ofNullable(result);
    }
    
    /**
     * Gets all enum values for a class, sorted by order.
     *
     * @param enumClass the enum class type
     * @param <T> the enum type
     * @return list of all enum values, sorted by order
     * @throws NullPointerException if enumClass is null
     */
    public static <T extends CodeEnum> List<T> values(Class<T> enumClass) {
        Objects.requireNonNull(enumClass, "Enum class cannot be null");
        
        Map<String, CodeEnum> classRegistry = registries.get(enumClass);
        if (classRegistry == null || classRegistry.isEmpty()) {
            return Collections.emptyList();
        }
        
        List<T> result = classRegistry.values().stream()
                .map(v -> {
                    @SuppressWarnings("unchecked")
                    T typedValue = (T) v;
                    return typedValue;
                })
                .sorted(Comparator.comparingInt(CodeEnum::getOrder))
                .collect(Collectors.toList());
        
        return Collections.unmodifiableList(result);
    }
    
    /**
     * Checks if the registry contains a specific enum value.
     *
     * @param enumClass the enum class type
     * @param code the code to check
     * @param <T> the enum type
     * @return true if the enum value exists, false otherwise
     * @throws NullPointerException if enumClass is null
     */
    public static <T extends CodeEnum> boolean contains(Class<T> enumClass, String code) {
        Objects.requireNonNull(enumClass, "Enum class cannot be null");
        
        if (code == null) {
            return false;
        }
        
        Map<String, CodeEnum> classRegistry = registries.get(enumClass);
        return classRegistry != null && classRegistry.containsKey(code);
    }
    
    /**
     * Dynamically creates and registers a new enum instance using reflection.
     * The enum class must have a constructor accepting (String code, String name, String description, int order).
     *
     * @param enumClass the enum class type
     * @param code the unique code for the new enum
     * @param name the display name for the new enum
     * @param description the description for the new enum
     * @param order the order/sort index for the new enum
     * @param <T> the enum type
     * @return the newly created enum instance
     * @throws IllegalStateException if enum creation fails
     * @throws NullPointerException if enumClass, code, or name is null
     * @throws IllegalArgumentException if code or name is empty
     */
    public static <T extends CodeEnum> T addEnum(
            Class<T> enumClass,
            String code,
            String name,
            String description,
            int order) {
        Objects.requireNonNull(enumClass, "Enum class cannot be null");
        Objects.requireNonNull(code, "Code cannot be null");
        Objects.requireNonNull(name, "Name cannot be null");
        
        if (code.trim().isEmpty()) {
            throw new IllegalArgumentException("Code cannot be empty");
        }
        if (name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be empty");
        }
        
        try {
            // Look for constructor: (String, String, String, int)
            Constructor<T> constructor = enumClass.getDeclaredConstructor(
                    String.class, String.class, String.class, int.class
            );
            constructor.setAccessible(true);
            
            T newEnum = constructor.newInstance(code, name, description, order);
            register(enumClass, newEnum);
            
            logger.info("Dynamically created enum: {}.{}", enumClass.getSimpleName(), code);
            return newEnum;
            
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(
                    "Enum class " + enumClass.getSimpleName() + 
                    " must have a constructor (String, String, String, int)", e);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to create enum instance", e);
        }
    }
    
    /**
     * Removes an enum value from the registry.
     *
     * @param enumClass the enum class type
     * @param code the code of the enum to remove
     * @param <T> the enum type
     * @return true if the enum was removed, false if not found
     * @throws NullPointerException if enumClass is null
     */
    public static <T extends CodeEnum> boolean remove(Class<T> enumClass, String code) {
        Objects.requireNonNull(enumClass, "Enum class cannot be null");
        
        if (code == null) {
            return false;
        }
        
        Map<String, CodeEnum> classRegistry = registries.get(enumClass);
        if (classRegistry == null) {
            return false;
        }
        
        synchronized (classRegistry) {
            boolean removed = classRegistry.remove(code) != null;
            if (removed) {
                logger.info("Removed enum: {}.{}", enumClass.getSimpleName(), code);
            }
            return removed;
        }
    }
    
    /**
     * Clears all enum values from the registry.
     * Use with caution - this removes all registered enums.
     */
    public static void clear() {
        registries.clear();
        logger.warn("Cleared entire enum registry");
    }
    
    /**
     * Gets all registered enum classes.
     *
     * @return set of all registered enum classes
     */
    public static Set<Class<?>> getRegisteredClasses() {
        return new HashSet<>(registries.keySet());
    }
    
    /**
     * Gets the count of registered enum values for a class.
     *
     * @param enumClass the enum class type
     * @param <T> the enum type
     * @return count of registered values, or 0 if class not registered
     * @throws NullPointerException if enumClass is null
     */
    public static <T extends CodeEnum> int getCount(Class<T> enumClass) {
        Objects.requireNonNull(enumClass, "Enum class cannot be null");
        
        Map<String, CodeEnum> classRegistry = registries.get(enumClass);
        return classRegistry != null ? classRegistry.size() : 0;
    }
}

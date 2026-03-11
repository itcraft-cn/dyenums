package cn.itcraft.dyenums.loader.file;

import cn.itcraft.dyenums.core.DyEnum;
import cn.itcraft.dyenums.core.EnumRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.function.BiFunction;

/**
 * Utility class for loading enum definitions from configuration files.
 * Supports loading from properties files with a flexible format.
 * <p>
 * Configuration format:
 * <pre>
 * # Simple format: code=name|description|order
 * UserStatus.ACTIVE=ACTIVE|激活|用户已激活|1
 * UserStatus.INACTIVE=INACTIVE|未激活|用户未激活|2
 *
 * # or more explicit format
 * UserStatus.ACTIVE.code=ACTIVE
 * UserStatus.ACTIVE.name=激活
 * UserStatus.ACTIVE.description=用户已激活
 * UserStatus.ACTIVE.order=1
 * </pre>
 *
 * @author Helly
 * @since 1.0.0
 */
final class SingleLineEnumDefineParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(SingleLineEnumDefineParser.class);

    /**
     * Parses order value from string, returning default if parsing fails.
     *
     * @param orderStr     the order string
     * @param defaultOrder default value if parsing fails
     * @return the parsed order or default
     */
    public static int parseOrder(String orderStr, int defaultOrder) {
        if (orderStr == null || orderStr.trim().isEmpty()) {
            return defaultOrder;
        }

        try {
            return Integer.parseInt(orderStr.trim());
        } catch (NumberFormatException e) {
            LOGGER.warn("Invalid order value: {}, using default: {}", orderStr, defaultOrder);
            return defaultOrder;
        }
    }

    public static <T extends DyEnum> int loadFromPropertiesInternal(
            Properties properties,
            Class<T> enumClass,
            BiFunction<String, String, T> factory) {

        int count = 0;
        String className = enumClass.getSimpleName();

        // Group properties by enum code
        Map<String, Properties> enumConfigs = new HashMap<>();

        for (String key : properties.stringPropertyNames()) {
            if (key.startsWith(className + ".")) {
                String[] parts = key.split("\\.", 3);
                if (parts.length >= 2) {
                    String code = parts[1];
                    enumConfigs.computeIfAbsent(code, k -> new Properties())
                               .setProperty(parts.length > 2 ? parts[2] : "value",
                                            properties.getProperty(key));
                }
            }
        }

        // Create enum instances from grouped properties
        for (Map.Entry<String, Properties> entry : enumConfigs.entrySet()) {
            String code = entry.getKey();
            Properties config = entry.getValue();

            try {
                // Check if it's a simple format: UserStatus.ACTIVE=value
                String simpleValue = config.getProperty("value");
                if (simpleValue == null) {
                    // Complex format: UserStatus.ACTIVE.name=..., UserStatus.ACTIVE.order=...
                    // Try to construct the required format for the factory method
                    String name = config.getProperty("name");
                    String description = config.getProperty("description", "");
                    String orderStr = config.getProperty("order", "999");
                    int order = parseOrder(orderStr, 999);

                    // If name exists, use the formatted string as needed by default factories
                    if (name == null) {
                        LOGGER.warn("Incomplete complex format for {}.{} - missing required 'name' property", className,
                                    code);
                    } else {
                        // Use direct concatenation for better performance
                        String valueString = name + "|" + description + "|" + order;
                        T enumValue = factory.apply(code, valueString);
                        EnumRegistry.register(enumClass, enumValue);
                        count++;
                    }
                } else {
                    T enumValue = factory.apply(code, simpleValue);
                    EnumRegistry.register(enumClass, enumValue);
                    count++;
                }
            } catch (Exception e) {
                LOGGER.warn("Failed to create enum from config: {}.{}", className, code, e);
            }
        }

        LOGGER.info("Loaded {} enum values for {} from properties", count, className);
        return count;
    }
}

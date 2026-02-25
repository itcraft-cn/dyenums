package cn.itcraft.dyenums.config;

import cn.itcraft.dyenums.core.DyEnum;
import cn.itcraft.dyenums.core.EnumRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
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
public class FileBasedEnumConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileBasedEnumConfig.class);

    private FileBasedEnumConfig() {
        throw new UnsupportedOperationException("Utility class should not be instantiated");
    }

    /**
     * Loads enum definitions from a properties file.
     * The file can be located in the classpath or filesystem.
     *
     * @param filePath  the path to the properties file
     * @param enumClass the enum class to load
     * @param factory   function to create enum instances (code, name, description, order -> enum)
     * @param <T>       the enum type
     * @return the number of enums loaded
     * @throws IOException          if the file cannot be read
     * @throws NullPointerException if any parameter is null
     */
    public static <T extends DyEnum> int loadFromFile(
            String filePath,
            Class<T> enumClass,
            BiFunction<String, String, T> factory) throws IOException {

        Objects.requireNonNull(filePath, "File path cannot be null");
        Objects.requireNonNull(enumClass, "Enum class cannot be null");
        Objects.requireNonNull(factory, "Factory cannot be null");

        Properties props = new Properties();

        // Try to load from classpath first
        try (InputStream is = FileBasedEnumConfig.class.getClassLoader().getResourceAsStream(filePath)) {
            if (is == null) {
                // Try to load from filesystem
                Path path = Paths.get(filePath);
                if (Files.exists(path)) {
                    try (InputStream fis = Files.newInputStream(path)) {
                        props.load(fis);
                        LOGGER.info("Loaded enum config from filesystem: {}", filePath);
                    }
                } else {
                    throw new IOException("Configuration file not found: " + filePath);
                }
            } else {
                props.load(is);
                LOGGER.info("Loaded enum config from classpath: {}", filePath);
            }
        }

        return loadFromProperties(props, enumClass, factory);
    }

    /**
     * Loads enum definitions from a Properties object.
     *
     * @param properties the properties containing enum definitions
     * @param enumClass  the enum class to load
     * @param factory    function to create enum instances (fullValue -> enum)
     * @param <T>        the enum type
     * @return the number of enums loaded
     * @throws NullPointerException if any parameter is null
     */
    public static <T extends DyEnum> int loadFromProperties(
            Properties properties,
            Class<T> enumClass,
            BiFunction<String, String, T> factory) {

        Objects.requireNonNull(properties, "Properties cannot be null");
        Objects.requireNonNull(enumClass, "Enum class cannot be null");
        Objects.requireNonNull(factory, "Factory cannot be null");

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
                    LOGGER.warn("Complex format not yet supported for {}.{}", className, code);
                } else {
                    T enumValue = factory.apply(code, simpleValue);
                    EnumRegistry.register(enumClass, enumValue);
                    count++;
                }
            } catch (Exception e) {
                LOGGER.error("Failed to create enum from config: {}.{}", className, code, e);
            }
        }

        LOGGER.info("Loaded {} enum values for {} from properties", count, className);
        return count;
    }

    /**
     * Parses a value string in the format: code|name|description|order
     *
     * @param valueString the value string to parse
     * @return array of [code, name, description, order] or null if parsing fails
     */
    public static String[] parseValueString(String valueString) {
        if (valueString == null || valueString.trim().isEmpty()) {
            return null;
        }

        String[] parts = valueString.split("\\|", 4);
        if (parts.length < 4) {
            LOGGER.warn("Invalid value format. Expected: code|name|description|order, got: {}", valueString);
            return null;
        }

        return parts;
    }

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
}

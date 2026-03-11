package cn.itcraft.dyenums.loader.file;

import cn.itcraft.dyenums.core.DyEnum;
import cn.itcraft.dyenums.loader.DyEnumsLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Properties;
import java.util.function.BiFunction;

import static cn.itcraft.dyenums.loader.file.SingleLineEnumDefineParser.loadFromPropertiesInternal;

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
public class FileBasedDyEnumsLoader<T extends DyEnum> implements DyEnumsLoader<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileBasedDyEnumsLoader.class);

    private final String filePath;

    /**
     * Creates a file-based config loader with a file path.
     *
     * @param filePath the path to the properties file
     * @throws IllegalArgumentException if the path contains invalid characters
     */
    public FileBasedDyEnumsLoader(String filePath) {
        Objects.requireNonNull(filePath, "File path cannot be null");
        this.filePath = validateFilePath(filePath);
    }

    /**
     * Validates the file path for security.
     *
     * @param filePath the path to validate
     * @return the validated path
     * @throws IllegalArgumentException if the path is invalid
     */
    private String validateFilePath(String filePath) {
        String trimmed = filePath.trim();

        // Prevent path traversal attacks
        if (trimmed.contains("..")) {
            throw new IllegalArgumentException(
                    "File path cannot contain parent directory references (..)");
        }

        // Normalize the path
        try {
            Path normalized = Paths.get(trimmed).normalize();
            String normalizedPath = normalized.toString();

            // Double-check after normalization
            if (normalizedPath.contains("..")) {
                throw new IllegalArgumentException(
                        "File path cannot contain parent directory references (..)");
            }

            return trimmed;
        } catch (Exception e) {
            // If path is invalid for the filesystem, just return trimmed version
            // (it might be a classpath resource)
            return trimmed;
        }
    }

    // Static utility methods for backward compatibility

    @Override
    public int load(Class<T> enumClass, BiFunction<String, String, T> factory) throws IOException {
        Objects.requireNonNull(enumClass, "Enum class cannot be null");
        Objects.requireNonNull(factory, "Factory cannot be null");

        Properties props = loadPropertiesFromFile();
        return loadFromPropertiesInternal(props, enumClass, factory);
    }

    @Override
    public boolean validateSource() {
        try {
            Path path = Paths.get(filePath);
            return Files.exists(path) && Files.isReadable(path);
        } catch (Exception e) {
            // Try classpath
            try (InputStream is = getClass().getClassLoader().getResourceAsStream(filePath)) {
                return is != null;
            } catch (Exception ex) {
                return false;
            }
        }
    }

    private Properties loadPropertiesFromFile() throws IOException {
        Properties props = new Properties();

        // Try to load from classpath first
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(filePath)) {
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

        return props;
    }
}

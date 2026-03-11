package cn.itcraft.dyenums.config.file;

import cn.itcraft.dyenums.config.EnumConfigLoader;
import cn.itcraft.dyenums.core.DyEnum;

import java.io.IOException;
import java.util.Objects;
import java.util.Properties;
import java.util.function.BiFunction;

import static cn.itcraft.dyenums.config.file.EnumLoader.loadFromPropertiesInternal;

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
public class PropEnumConfig<T extends DyEnum> implements EnumConfigLoader<T> {

    private final Properties properties;

    /**
     * Creates a file-based config loader with a Properties object.
     *
     * @param properties the properties containing enum definitions
     */
    public PropEnumConfig(Properties properties) {
        this.properties = Objects.requireNonNull(properties, "Properties cannot be null");
    }

    @Override
    public int load(Class<T> enumClass, BiFunction<String, String, T> factory) throws IOException {
        Objects.requireNonNull(enumClass, "Enum class cannot be null");
        Objects.requireNonNull(factory, "Factory cannot be null");

        return loadFromPropertiesInternal(properties, enumClass, factory);
    }

    @Override
    public boolean validateSource() {
        return !properties.isEmpty();
    }
}

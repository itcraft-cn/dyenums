package cn.itcraft.dyenums.loader;

import cn.itcraft.dyenums.core.DyEnum;

import java.util.function.BiFunction;

/**
 * Interface for loading enum definitions from external sources.
 * Implementations can load enums from files, databases, remote services, etc.
 *
 * @param <T> the enum type
 * @author Helly
 * @since 1.0.0
 */
public interface DyEnumsLoader<T extends DyEnum> {

    /**
     * Loads enum definitions from the configured source.
     *
     * @param enumClass the enum class to load
     * @param factory   function to create enum instances {@code (code, valueString -> enum)}
     * @return the number of enums loaded
     * @throws Exception if loading fails
     */
    int load(Class<T> enumClass, BiFunction<String, String, T> factory) throws Exception;

    /**
     * Validates that the configuration source is accessible.
     *
     * @return true if the source is accessible, false otherwise
     */
    boolean validateSource();
}

package com.helly.dyenums.spring;

import com.helly.dyenums.core.CodeEnum;
import com.helly.dyenums.core.EnumRegistry;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service layer for accessing dynamic enums in Spring applications.
 * Provides type-safe access to enum values and additional utility methods.
 *
 * This service can be injected into other Spring components to provide
 * convenient access to enum functionality.
 *
 * @author Helly
 * @since 1.0.0
 */
@Service
public class EnumService {
    
    /**
     * Gets all enum values for a class, sorted by order.
     *
     * @param enumClass the enum class type
     * @param <T> the enum type
     * @return list of all enum values
     * @throws NullPointerException if enumClass is null
     */
    public <T extends CodeEnum> List<T> getValues(Class<T> enumClass) {
        return EnumRegistry.values(enumClass);
    }
    
    /**
     * Gets an enum value by code.
     *
     * @param enumClass the enum class type
     * @param code the code to look up
     * @param <T> the enum type
     * @return the enum value
     * @throws IllegalArgumentException if the code is not found
     * @throws NullPointerException if enumClass is null
     */
    public <T extends CodeEnum> T getByCode(Class<T> enumClass, String code) {
        return EnumRegistry.valueOf(enumClass, code)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No enum constant " + enumClass.getCanonicalName() + "." + code));
    }
    
    /**
     * Gets an enum value by code, returning a default value if not found.
     *
     * @param enumClass the enum class type
     * @param code the code to look up
     * @param defaultValue the default value to return if not found
     * @param <T> the enum type
     * @return the enum value or default
     * @throws NullPointerException if enumClass is null
     */
    public <T extends CodeEnum> T getByCodeOrDefault(
            Class<T> enumClass, String code, T defaultValue) {
        return EnumRegistry.valueOf(enumClass, code).orElse(defaultValue);
    }
    
    /**
     * Gets an enum value by code as an Optional.
     *
     * @param enumClass the enum class type
     * @param code the code to look up
     * @param <T> the enum type
     * @return Optional containing the enum value, or empty if not found
     * @throws NullPointerException if enumClass is null
     */
    public <T extends CodeEnum> Optional<T> findByCode(Class<T> enumClass, String code) {
        return EnumRegistry.valueOf(enumClass, code);
    }
    
    /**
     * Gets enum values matching any of the provided codes.
     *
     * @param enumClass the enum class type
     * @param codes collection of codes to look up
     * @param <T> the enum type
     * @return list of matching enum values
     * @throws NullPointerException if enumClass or codes is null
     */
    public <T extends CodeEnum> List<T> getByCodes(Class<T> enumClass, Collection<String> codes) {
        Objects.requireNonNull(codes, "Codes cannot be null");
        
        return codes.stream()
                .map(code -> EnumRegistry.valueOf(enumClass, code))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }
    
    /**
     * Registers a new enum value.
     *
     * @param enumClass the enum class type
     * @param enumValue the enum value to register
     * @param <T> the enum type
     * @throws NullPointerException if enumClass or enumValue is null
     */
    public <T extends CodeEnum> void addEnum(Class<T> enumClass, T enumValue) {
        EnumRegistry.register(enumClass, enumValue);
    }
    
    /**
     * Dynamically creates and registers a new enum instance.
     *
     * @param enumClass the enum class type
     * @param code the unique code
     * @param name the display name
     * @param description the description
     * @param order the sort order
     * @param <T> the enum type
     * @return the newly created enum instance
     */
    public <T extends CodeEnum> T createEnum(
            Class<T> enumClass,
            String code,
            String name,
            String description,
            int order) {
        return EnumRegistry.addEnum(enumClass, code, name, description, order);
    }
    
    /**
     * Removes an enum value.
     *
     * @param enumClass the enum class type
     * @param code the code to remove
     * @param <T> the enum type
     * @return true if removed, false if not found
     * @throws NullPointerException if enumClass is null
     */
    public <T extends CodeEnum> boolean removeEnum(Class<T> enumClass, String code) {
        return EnumRegistry.remove(enumClass, code);
    }
    
    /**
     * Checks if an enum value exists.
     *
     * @param enumClass the enum class type
     * @param code the code to check
     * @param <T> the enum type
     * @return true if the enum exists, false otherwise
     * @throws NullPointerException if enumClass is null
     */
    public <T extends CodeEnum> boolean exists(Class<T> enumClass, String code) {
        return EnumRegistry.contains(enumClass, code);
    }
    
    /**
     * Gets all enum codes for a class.
     *
     * @param enumClass the enum class type
     * @param <T> the enum type
     * @return set of all codes
     * @throws NullPointerException if enumClass is null
     */
    public <T extends CodeEnum> Set<String> getAllCodes(Class<T> enumClass) {
        return EnumRegistry.values(enumClass).stream()
                .map(CodeEnum::getCode)
                .collect(Collectors.toSet());
    }
    
    /**
     * Gets all enum names for a class.
     *
     * @param enumClass the enum class type
     * @param <T> the enum type
     * @return set of all names
     * @throws NullPointerException if enumClass is null
     */
    public <T extends CodeEnum> Set<String> getAllNames(Class<T> enumClass) {
        return EnumRegistry.values(enumClass).stream()
                .map(CodeEnum::getName)
                .collect(Collectors.toSet());
    }
    
    /**
     * Converts enum values to a map keyed by code.
     *
     * @param enumClass the enum class type
     * @param <T> the enum type
     * @return map of code -> enum value
     * @throws NullPointerException if enumClass is null
     */
    public <T extends CodeEnum> Map<String, T> asMap(Class<T> enumClass) {
        return EnumRegistry.values(enumClass).stream()
                .collect(Collectors.toMap(CodeEnum::getCode, e -> e));
    }
    
    /**
     * Converts enum values to a map of code -> name.
     *
     * @param enumClass the enum class type
     * @param <T> the enum type
     * @return map of code -> name
     * @throws NullPointerException if enumClass is null
     */
    public <T extends CodeEnum> Map<String, String> asCodeNameMap(Class<T> enumClass) {
        return EnumRegistry.values(enumClass).stream()
                .collect(Collectors.toMap(CodeEnum::getCode, CodeEnum::getName));
    }
    
    /**
     * Gets a dropdown/list representation of enum values.
     * Useful for populating UI select boxes.
     *
     * @param enumClass the enum class type
     * @param <T> the enum type
     * @return list of maps containing value and label
     * @throws NullPointerException if enumClass is null
     */
    public <T extends CodeEnum> List<Map<String, String>> asSelectOptions(Class<T> enumClass) {
        return EnumRegistry.values(enumClass).stream()
                .map(enumValue -> {
                    Map<String, String> option = new LinkedHashMap<>();
                    option.put("value", enumValue.getCode());
                    option.put("label", enumValue.getName());
                    return option;
                })
                .collect(Collectors.toList());
    }
    
    /**
     * Gets all registered enum classes.
     *
     * @return set of registered enum classes
     */
    public Set<Class<?>> getAllRegisteredClasses() {
        return EnumRegistry.getRegisteredClasses();
    }
    
    /**
     * Gets the count of registered values for an enum class.
     *
     * @param enumClass the enum class type
     * @param <T> the enum type
     * @return count of registered values
     * @throws NullPointerException if enumClass is null
     */
    public <T extends CodeEnum> int getCount(Class<T> enumClass) {
        return EnumRegistry.getCount(enumClass);
    }
    
    /**
     * Clears all enum values from the registry.
     * Use with caution - this removes all registered enums.
     */
    public void clearAll() {
        EnumRegistry.clear();
    }
}

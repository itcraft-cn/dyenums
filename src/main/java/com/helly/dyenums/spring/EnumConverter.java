package com.helly.dyenums.spring;

import com.helly.dyenums.core.CodeEnum;
import com.helly.dyenums.core.EnumRegistry;
import org.springframework.core.convert.converter.Converter;

/**
 * Spring Converter for converting String values to dynamic enum instances.
 * This converter enables automatic conversion of request parameters, path variables,
 * and other String inputs to enum types in Spring MVC applications.
 *
 * To use this converter, register it with Spring's ConversionService:
 * <pre>
 * &#64;Configuration
 * public class WebConfig implements WebMvcConfigurer {
 *     &#64;Override
 *     public void addFormatters(FormatterRegistry registry) {
 *         registry.addConverter(new EnumConverter(UserStatus.class));
 *         registry.addConverter(new EnumConverter(OrderStatus.class));
 *     }
 * }
 * </pre>
 *
 * @author Helly
 * @since 1.0.0
 * @param <T> the enum type
 */
public class EnumConverter<T extends CodeEnum> implements Converter<String, T> {
    
    private final Class<T> enumClass;
    
    /**
     * Constructs a new EnumConverter for the specified enum class.
     *
     * @param enumClass the enum class type
     * @throws NullPointerException if enumClass is null
     */
    public EnumConverter(Class<T> enumClass) {
        if (enumClass == null) {
            throw new NullPointerException("Enum class cannot be null");
        }
        this.enumClass = enumClass;
    }
    
    /**
     * Converts a String to the corresponding enum value.
     *
     * @param code the string code to convert
     * @return the enum value
     * @throws IllegalArgumentException if the code is not found
     */
    @Override
    public T convert(String code) {
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("Enum code cannot be null or empty");
        }
        
        return EnumRegistry.valueOf(enumClass, code)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No enum constant " + enumClass.getCanonicalName() + "." + code));
    }
    
    /**
     * Gets the enum class that this converter handles.
     *
     * @return the enum class
     */
    public Class<T> getEnumClass() {
        return enumClass;
    }
}

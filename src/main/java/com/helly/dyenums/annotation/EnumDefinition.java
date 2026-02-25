package com.helly.dyenums.annotation;

import java.lang.annotation.*;

/**
 * Annotation to mark classes as dynamic enum definitions.
 * This annotation can be used to provide metadata about the enum
 * and control how it's loaded and managed.
 *
 * Example usage:
 * <pre>
 * &#64;EnumDefinition(
 *     category = "system",
 *     dynamic = true,
 *     configSource = "database"
 * )
 * public class UserStatus extends BaseCodeEnum {
 *     // implementation
 * }
 * </pre>
 *
 * @author Helly
 * @since 1.0.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface EnumDefinition {
    
    /**
     * The category or domain this enum belongs to.
     * Useful for grouping related enums together.
     *
     * @return the category name
     */
    String category() default "";
    
    /**
     * Whether this enum supports dynamic values loaded from configuration.
     * If true, additional values can be loaded from files or database.
     *
     * @return true if dynamic values are supported, false otherwise
     */
    boolean dynamic() default true;
    
    /**
     * The source from which this enum should load dynamic values.
     * Options: "file", "database", "remote", "none"
     *
     * @return the configuration source
     */
    String configSource() default "none";
    
    /**
     * The configuration file path if configSource is "file".
     * 
     * @return the configuration file path
     */
    String configPath() default "";
    
    /**
     * Description of the enum's purpose and usage.
     *
     * @return the description
     */
    String description() default "";
    
    /**
     * The order/priority of this enum definition.
     * Used for sorting when multiple enums are processed.
     *
     * @return the order value
     */
    int order() default 0;
}

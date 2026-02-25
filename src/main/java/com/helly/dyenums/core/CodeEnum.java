package com.helly.dyenums.core;

/**
 * Core interface for dynamic enums.
 * This interface defines the contract that all dynamic enum implementations must follow.
 * It provides methods to access the code, name, description, and order of enum values.
 *
 * @author Helly
 * @since 1.0.0
 */
public interface CodeEnum {
    
    /**
     * Gets the unique code for this enum value.
     * This is typically used for storage and identification purposes.
     *
     * @return the unique code
     */
    String getCode();
    
    /**
     * Gets the display name for this enum value.
     * This is typically used for UI display and user-facing text.
     *
     * @return the display name
     */
    String getName();
    
    /**
     * Gets the description for this enum value.
     * This provides additional context or documentation about the enum value.
     *
     * @return the description
     */
    String getDescription();
    
    /**
     * Gets the order/sort index for this enum value.
     * This is used when ordering enum values in lists or dropdowns.
     *
     * @return the order index
     */
    int getOrder();
}

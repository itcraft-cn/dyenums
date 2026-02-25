package com.helly.dyenums.model;

import com.helly.dyenums.annotation.EnumDefinition;
import com.helly.dyenums.core.BaseCodeEnum;

/**
 * User status enum representing different states a user can be in.
 * This is an example implementation of a dynamic enum.
 *
 * Predefined values:
 * - ACTIVE: User account is active and can login
 * - INACTIVE: User account is inactive
 * - LOCKED: User account is locked due to security reasons
 * - SUSPENDED: User account is temporarily suspended
 *
 * @author Helly
 * @since 1.0.0
 */
@EnumDefinition(
    category = "system",
    dynamic = true,
    configSource = "database",
    description = "User account status enum"
)
public class UserStatus extends BaseCodeEnum {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * User account is active and can login
     */
    public static final UserStatus ACTIVE = new UserStatus(
        "ACTIVE", "激活", "用户已激活", 1
    );
    
    /**
     * User account is inactive
     */
    public static final UserStatus INACTIVE = new UserStatus(
        "INACTIVE", "未激活", "用户未激活", 2
    );
    
    /**
     * User account is locked due to security reasons
     */
    public static final UserStatus LOCKED = new UserStatus(
        "LOCKED", "锁定", "用户被锁定", 3
    );
    
    /**
     * User account is temporarily suspended
     */
    public static final UserStatus SUSPENDED = new UserStatus(
        "SUSPENDED", "暂停", "用户被暂停", 4
    );
    
    /**
     * User account is pending verification
     */
    public static final UserStatus PENDING = new UserStatus(
        "PENDING", "待验证", "用户待验证", 5
    );
    
    /**
     * Private constructor for predefined values.
     * This constructor is also used by reflection for dynamic creation.
     *
     * @param code the unique code
     * @param name the display name
     * @param description the description
     * @param order the sort order
     */
    private UserStatus(String code, String name, String description, int order) {
        super(code, name, description, order);
    }
    
    /**
     * Factory method for creating UserStatus instances.
     * Used by configuration loading mechanisms.
     *
     * @param code the unique code
     * @param valueString value in format: code|name|description|order
     * @return new UserStatus instance
     * @throws IllegalArgumentException if valueString format is invalid
     */
    public static UserStatus fromValueString(String code, String valueString) {
        String[] parts = valueString.split("\\|", 3);
        if (parts.length < 3) {
            throw new IllegalArgumentException(
                "Invalid value format. Expected: name|description|order, got: " + valueString
            );
        }
        
        String name = parts[0];
        String description = parts[1];
        int order = Integer.parseInt(parts[2]);
        
        return new UserStatus(code, name, description, order);
    }
    
    /**
     * Checks if this status represents an active user.
     *
     * @return true if the user can login and use the system
     */
    public boolean isActive() {
        return this == ACTIVE;
    }
    
    /**
     * Checks if this status represents a user that cannot access the system.
     *
     * @return true if the user cannot login
     */
    public boolean isBlocked() {
        return this == LOCKED || this == SUSPENDED;
    }
    
    /**
     * Checks if this status requires admin attention.
     *
     * @return true if admin action is needed
     */
    public boolean requiresAdminAction() {
        return this == LOCKED || this == SUSPENDED || this == PENDING;
    }
}

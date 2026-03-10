package cn.itcraft.dyenums.model;

import cn.itcraft.dyenums.annotation.EnumDefinition;
import cn.itcraft.dyenums.core.BaseDyEnum;

import java.util.Objects;

/**
 * User status enum representing different states a user can be in.
 * This is an example implementation of a dynamic enum.
 * <p>
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
public class UserStatus extends BaseDyEnum {

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
    private static final long serialVersionUID = 3145336379884807160L;

    /**
     * Private constructor for predefined values.
     * This constructor is also used by reflection for dynamic creation.
     *
     * @param code        the unique code
     * @param name        the display name
     * @param description the description
     * @param order       the sort order
     */
    private UserStatus(String code, String name, String description, int order) {
        super(code, name, description, order);
    }

    /**
     * Factory method for creating UserStatus instances.
     * Used by configuration loading mechanisms.
     *
     * @param code        the unique code
     * @param valueString value in format: name|description|order
     * @return new UserStatus instance
     * @throws NullPointerException     if code or valueString is null
     * @throws IllegalArgumentException if valueString format is invalid
     */
    public static UserStatus fromValueString(String code, String valueString) {
        Objects.requireNonNull(code, "Code cannot be null");
        Objects.requireNonNull(valueString, "Value string cannot be null");

        String[] parts = valueString.split("\\|", 3);
        if (parts.length < 3) {
            throw new IllegalArgumentException(
                    "Invalid value format. Expected: name|description|order, got: " + valueString
            );
        }

        String name = parts[0].trim();
        String description = parts[1].trim();

        if (name.isEmpty()) {
            throw new IllegalArgumentException("Name cannot be empty");
        }

        int order;
        try {
            order = Integer.parseInt(parts[2].trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Invalid order value: " + parts[2] + ", expected integer", e);
        }

        return new UserStatus(code.trim(), name, description, order);
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

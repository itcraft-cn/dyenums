package com.helly.dyenums.core;

import java.io.Serializable;
import java.util.Objects;

/**
 * Abstract base class for dynamic enum implementations.
 * This class provides a default implementation of the CodeEnum interface
 * and handles common functionality like equals, hashCode, and toString.
 *
 * Subclasses must provide a constructor that accepts code, name, description, and order.
 *
 * @author Helly
 * @since 1.0.0
 */
public abstract class BaseCodeEnum implements CodeEnum, Serializable {
    
    private static final long serialVersionUID = 1L;
    
    protected final String code;
    protected final String name;
    protected final String description;
    protected final int order;
    
    /**
     * Constructs a new BaseCodeEnum instance.
     *
     * @param code the unique code for this enum value
     * @param name the display name for this enum value
     * @param description the description for this enum value
     * @param order the order/sort index for this enum value
     * @throws NullPointerException if code or name is null
     * @throws IllegalArgumentException if code or name is empty
     */
    protected BaseCodeEnum(String code, String name, String description, int order) {
        Objects.requireNonNull(code, "Code cannot be null");
        Objects.requireNonNull(name, "Name cannot be null");
        
        String trimmedCode = code.trim();
        String trimmedName = name.trim();
        
        if (trimmedCode.isEmpty()) {
            throw new IllegalArgumentException("Code cannot be empty");
        }
        if (trimmedName.isEmpty()) {
            throw new IllegalArgumentException("Name cannot be empty");
        }
        
        this.code = trimmedCode;
        this.name = trimmedName;
        this.description = description != null ? description.trim() : "";
        this.order = order;
    }
    
    @Override
    public String getCode() {
        return code;
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public String getDescription() {
        return description;
    }
    
    @Override
    public int getOrder() {
        return order;
    }
    
    /**
     * Compares this enum with another object for equality.
     * Two BaseCodeEnum instances are equal if they have the same class and code.
     *
     * @param obj the object to compare
     * @return true if the objects are equal, false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        BaseCodeEnum that = (BaseCodeEnum) obj;
        return Objects.equals(code, that.code);
    }
    
    /**
     * Returns the hash code for this enum value.
     * The hash code is based on the code field.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(code);
    }
    
    /**
     * Returns a string representation of this enum value.
     * The format is: ClassName{code='CODE', name='Name', order=N}
     *
     * @return the string representation
     */
    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", order=" + order +
                '}';
    }
}

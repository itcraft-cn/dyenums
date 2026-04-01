package cn.itcraft.dyenums.core;

import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.Objects;

/**
 * Abstract base class for dynamic enum implementations.
 * This class provides a default implementation of the DyEnum interface
 * and handles common functionality like equals, hashCode, and toString.
 * <p>
 * Subclasses must provide a constructor that accepts code, name, description, and order.
 * <p>
 * Security features:
 * <ul>
 *   <li>Input validation with length limits</li>
 *   <li>Deserialization protection to prevent forged instances</li>
 * </ul>
 *
 * @author Helly
 * @since 1.0.0
 */
public abstract class BaseDyEnum implements DyEnum, Serializable {

    private static final long serialVersionUID = -533038945084607168L;

    /**
     * Maximum allowed length for code field
     */
    private static final int MAX_CODE_LENGTH = 50;

    /**
     * Maximum allowed length for name field
     */
    private static final int MAX_NAME_LENGTH = 100;

    /**
     * Maximum allowed length for description field
     */
    private static final int MAX_DESCRIPTION_LENGTH = 500;

    /**
     * Maximum allowed order value
     */
    private static final int MAX_ORDER = 999999;

    protected final String code;
    protected final String name;
    protected final String description;
    protected final int order;

    /**
     * Constructs a new BaseDyEnum instance.
     *
     * @param code        the unique code for this enum value
     * @param name        the display name for this enum value
     * @param description the description for this enum value
     * @param order       the order/sort index for this enum value
     * @throws NullPointerException     if code or name is null
     * @throws IllegalArgumentException if code or name is empty, or exceeds length limits
     */
    protected BaseDyEnum(String code, String name, String description, int order) {
        Objects.requireNonNull(code, "Code cannot be null");
        Objects.requireNonNull(name, "Name cannot be null");

        String trimmedCode = code.trim();
        String trimmedName = name.trim();

        if (trimmedCode.isEmpty()) {
            throw new IllegalArgumentException("Code cannot be empty");
        }
        if (trimmedCode.length() > MAX_CODE_LENGTH) {
            throw new IllegalArgumentException(
                    "Code cannot exceed " + MAX_CODE_LENGTH + " characters, got: " + trimmedCode.length());
        }
        if (trimmedName.isEmpty()) {
            throw new IllegalArgumentException("Name cannot be empty");
        }
        if (trimmedName.length() > MAX_NAME_LENGTH) {
            throw new IllegalArgumentException(
                    "Name cannot exceed " + MAX_NAME_LENGTH + " characters, got: " + trimmedName.length());
        }
        if (description != null && description.length() > MAX_DESCRIPTION_LENGTH) {
            throw new IllegalArgumentException(
                    "Description cannot exceed " + MAX_DESCRIPTION_LENGTH + " characters, got: " + description.length());
        }
        if (order < 0 || order > MAX_ORDER) {
            throw new IllegalArgumentException(
                    "Order must be between 0 and " + MAX_ORDER + ", got: " + order);
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
     * Two BaseDyEnum instances are equal if they have the same class and code.
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
        BaseDyEnum that = (BaseDyEnum) obj;

        // Since all field values are set by constructor and guaranteed non-null via validation,
        // we only need to compare the code field for semantic equality
        // Code is the business identifier that makes each enum value unique within a class
        return Objects.equals(this.code, that.code);
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

    /**
     * Validates the object during deserialization.
     * This prevents creation of invalid enum instances through deserialization attacks.
     *
     * @param in the object input stream
     * @throws java.io.IOException if an I/O error occurs
     * @throws ClassNotFoundException if the class of a serialized object could not be found
     * @throws InvalidObjectException if the deserialized object is invalid
     */
    private void readObject(ObjectInputStream in)
            throws java.io.IOException, ClassNotFoundException {
        in.defaultReadObject();
        // Basic validation - fields should have been validated by constructor
        if (code == null || code.isEmpty()) {
            throw new InvalidObjectException("Invalid enum: code is null or empty");
        }
        if (name == null || name.isEmpty()) {
            throw new InvalidObjectException("Invalid enum: name is null or empty");
        }
    }

    /**
     * Ensures deserialization returns the registered singleton instance.
     * This prevents forged enum instances from being created via deserialization.
     *
     * @return the registered enum instance
     * @throws ObjectStreamException if the enum is not registered
     */
    @SuppressWarnings("unchecked")
    protected Object readResolve() throws ObjectStreamException {
        // Look up the registered instance by code
        Class<? extends DyEnum> enumClass = (Class<? extends DyEnum>) this.getClass();
        java.util.Optional<? extends DyEnum> existing = EnumRegistry.valueOf(enumClass, this.code);

        if (existing.isPresent()) {
            return existing.get();
        }

        // Reject unregistered enum instances
        throw new InvalidObjectException(
                "Cannot deserialize unregistered enum: " + this.getClass().getSimpleName() + "." + this.code);
    }
}

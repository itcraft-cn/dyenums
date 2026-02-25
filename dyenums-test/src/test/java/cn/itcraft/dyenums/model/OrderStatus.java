package cn.itcraft.dyenums.model;

import cn.itcraft.dyenums.annotation.EnumDefinition;
import cn.itcraft.dyenums.core.BaseDyEnum;

/**
 * Order status enum representing different states an order can be in.
 * This is an example implementation of a dynamic enum for e-commerce systems.
 * <p>
 * Predefined values:
 * - PENDING: Order created but not yet processed
 * - PROCESSING: Order is being processed
 * - CONFIRMED: Order confirmed and payment verified
 * - SHIPPED: Order has been shipped
 * - DELIVERED: Order delivered to customer
 * - CANCELLED: Order cancelled
 * - REFUNDED: Order refunded
 *
 * @author Helly
 * @since 1.0.0
 */
@EnumDefinition(
        category = "order",
        dynamic = true,
        configSource = "file",
        configPath = "order-status.properties",
        description = "Order status enum for e-commerce"
)
public class OrderStatus extends BaseDyEnum {

    /**
     * Order created but not yet processed
     */
    public static final OrderStatus PENDING = new OrderStatus(
            "PENDING", "待处理", "订单已创建，等待处理", 1
    );
    /**
     * Order is being processed
     */
    public static final OrderStatus PROCESSING = new OrderStatus(
            "PROCESSING", "处理中", "订单正在处理中", 2
    );
    /**
     * Order confirmed and payment verified
     */
    public static final OrderStatus CONFIRMED = new OrderStatus(
            "CONFIRMED", "已确认", "订单已确认，支付已验证", 3
    );
    /**
     * Order has been shipped
     */
    public static final OrderStatus SHIPPED = new OrderStatus(
            "SHIPPED", "已发货", "订单已发货", 4
    );
    /**
     * Order delivered to customer
     */
    public static final OrderStatus DELIVERED = new OrderStatus(
            "DELIVERED", "已送达", "订单已送达客户", 5
    );
    /**
     * Order cancelled
     */
    public static final OrderStatus CANCELLED = new OrderStatus(
            "CANCELLED", "已取消", "订单已取消", 6
    );
    /**
     * Order refunded
     */
    public static final OrderStatus REFUNDED = new OrderStatus(
            "REFUNDED", "已退款", "订单已退款", 7
    );
    /**
     * Order failed during processing
     */
    public static final OrderStatus FAILED = new OrderStatus(
            "FAILED", "处理失败", "订单处理失败", 8
    );
    private static final long serialVersionUID = 1L;

    /**
     * Private constructor for predefined values.
     * This constructor is also used by reflection for dynamic creation.
     *
     * @param code        the unique code
     * @param name        the display name
     * @param description the description
     * @param order       the sort order
     */
    private OrderStatus(String code, String name, String description, int order) {
        super(code, name, description, order);
    }

    /**
     * Factory method for creating OrderStatus instances.
     * Used by configuration loading mechanisms.
     *
     * @param code        the unique code
     * @param valueString value in format: name|description|order
     * @return new OrderStatus instance
     * @throws IllegalArgumentException if valueString format is invalid
     */
    public static OrderStatus fromValueString(String code, String valueString) {
        String[] parts = valueString.split("\\|", 3);
        if (parts.length < 3) {
            throw new IllegalArgumentException(
                    "Invalid value format. Expected: name|description|order, got: " + valueString
            );
        }

        String name = parts[0];
        String description = parts[1];
        int order = Integer.parseInt(parts[2]);

        return new OrderStatus(code, name, description, order);
    }

    /**
     * Checks if this status represents an order that is still being processed
     * and can be modified.
     *
     * @return true if the order is in progress
     */
    public boolean isInProgress() {
        return this == PENDING || this == PROCESSING || this == CONFIRMED;
    }

    /**
     * Checks if this status represents a successfully completed order.
     *
     * @return true if the order was successfully delivered
     */
    public boolean isSuccessful() {
        return this == DELIVERED;
    }

    /**
     * Checks if this status represents a failed or cancelled order.
     *
     * @return true if the order failed or was cancelled
     */
    public boolean isFailed() {
        return this == CANCELLED || this == FAILED || this == REFUNDED;
    }

    /**
     * Checks if this status allows the order to be cancelled.
     *
     * @return true if the order can be cancelled
     */
    public boolean canBeCancelled() {
        return this == PENDING || this == PROCESSING || this == CONFIRMED;
    }

    /**
     * Checks if this status allows the order to be modified.
     *
     * @return true if the order details can be modified
     */
    public boolean canBeModified() {
        return this == PENDING || this == PROCESSING;
    }

    /**
     * Checks if this status represents a terminal state (no further transitions).
     *
     * @return true if this is a final state
     */
    public boolean isTerminalState() {
        return this == DELIVERED || this == CANCELLED || this == REFUNDED || this == FAILED;
    }
}

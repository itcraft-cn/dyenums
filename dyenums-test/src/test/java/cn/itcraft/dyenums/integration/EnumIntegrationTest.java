package cn.itcraft.dyenums.integration;

import cn.itcraft.dyenums.core.EnumRegistry;
import cn.itcraft.dyenums.model.OrderStatus;
import cn.itcraft.dyenums.model.UserStatus;
import cn.itcraft.dyenums.spring.EnumService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class EnumIntegrationTest {

    private EnumService enumService;

    @BeforeEach
    public void setUp() {
        EnumRegistry.clear();
        enumService = new EnumService();
    }

    @AfterEach
    public void tearDown() {
        EnumRegistry.clear();
    }

    @Test
    public void testUserStatus_RegistrationAndUsage() {
        EnumRegistry.register(UserStatus.class, UserStatus.ACTIVE);
        EnumRegistry.register(UserStatus.class, UserStatus.INACTIVE);
        EnumRegistry.register(UserStatus.class, UserStatus.LOCKED);
        EnumRegistry.register(UserStatus.class, UserStatus.SUSPENDED);
        EnumRegistry.register(UserStatus.class, UserStatus.PENDING);

        UserStatus active = enumService.getByCode(UserStatus.class, "ACTIVE");
        assertEquals(UserStatus.ACTIVE, active);
        assertTrue(active.isActive());
        assertFalse(active.isBlocked());

        UserStatus locked = enumService.getByCode(UserStatus.class, "LOCKED");
        assertFalse(locked.isActive());
        assertTrue(locked.isBlocked());
        assertTrue(locked.requiresAdminAction());

        List<UserStatus> allStatuses = enumService.getValues(UserStatus.class);
        assertEquals(5, allStatuses.size());
        assertEquals(UserStatus.ACTIVE, allStatuses.get(0));
        assertEquals(UserStatus.PENDING, allStatuses.get(4));

        List<Map<String, String>> options = enumService.asSelectOptions(UserStatus.class);
        assertEquals(5, options.size());
        assertEquals("ACTIVE", options.get(0).get("value"));
        assertEquals("激活", options.get(0).get("label"));
    }

    @Test
    public void testOrderStatus_BusinessWorkflow() {
        EnumRegistry.register(OrderStatus.class, OrderStatus.PENDING);
        EnumRegistry.register(OrderStatus.class, OrderStatus.PROCESSING);
        EnumRegistry.register(OrderStatus.class, OrderStatus.CONFIRMED);
        EnumRegistry.register(OrderStatus.class, OrderStatus.SHIPPED);
        EnumRegistry.register(OrderStatus.class, OrderStatus.DELIVERED);
        EnumRegistry.register(OrderStatus.class, OrderStatus.CANCELLED);

        OrderStatus status = enumService.getByCode(OrderStatus.class, "PENDING");
        assertTrue(status.isInProgress());
        assertTrue(status.canBeCancelled());
        assertTrue(status.canBeModified());

        status = enumService.getByCode(OrderStatus.class, "PROCESSING");
        assertTrue(status.isInProgress());
        assertTrue(status.canBeCancelled());
        assertTrue(status.canBeModified());

        status = enumService.getByCode(OrderStatus.class, "SHIPPED");
        assertFalse(status.isInProgress());
        assertFalse(status.canBeCancelled());
        assertFalse(status.canBeModified());

        status = enumService.getByCode(OrderStatus.class, "DELIVERED");
        assertFalse(status.isInProgress());
        assertTrue(status.isSuccessful());
        assertTrue(status.isTerminalState());

        status = enumService.getByCode(OrderStatus.class, "CANCELLED");
        assertTrue(status.isFailed());
        assertTrue(status.isTerminalState());
    }

    @Test
    public void testDynamicEnumCreation() {
        EnumRegistry.register(OrderStatus.class, OrderStatus.PENDING);
        EnumRegistry.register(OrderStatus.class, OrderStatus.PROCESSING);

        OrderStatus customStatus = enumService.createEnum(
                OrderStatus.class,
                "CUSTOM_REVIEW",
                "待审核",
                "订单需要人工审核",
                15
                                                         );

        assertNotNull(customStatus);
        assertEquals("CUSTOM_REVIEW", customStatus.getCode());
        assertEquals("待审核", customStatus.getName());

        Optional<OrderStatus> found = enumService.findByCode(OrderStatus.class, "CUSTOM_REVIEW");
        assertTrue(found.isPresent());
        assertEquals(customStatus, found.get());

        List<OrderStatus> allStatuses = enumService.getValues(OrderStatus.class);
        assertEquals(3, allStatuses.size());
        assertTrue(allStatuses.contains(customStatus));
    }

    @Test
    public void testConfigurationLoading() {
        Properties orderConfig = new Properties();
        orderConfig.setProperty("OrderStatus.CUSTOM1", "Custom Status 1|Custom description 1|100");
        orderConfig.setProperty("OrderStatus.CUSTOM2", "Custom Status 2|Custom description 2|200");

        EnumRegistry.register(OrderStatus.class, OrderStatus.PENDING);
        EnumRegistry.register(OrderStatus.class, OrderStatus.PROCESSING);

        EnumRegistry.registerFromConfig(
                OrderStatus.class,
                orderConfig,
                OrderStatus::fromValueString
                                       );

        List<OrderStatus> allStatuses = enumService.getValues(OrderStatus.class);
        assertEquals(4, allStatuses.size());

        OrderStatus custom1 = enumService.getByCode(OrderStatus.class, "CUSTOM1");
        assertEquals("Custom Status 1", custom1.getName());
        assertEquals("Custom description 1", custom1.getDescription());
        assertEquals(100, custom1.getOrder());
    }

    @Test
    public void testMapConversion() {
        EnumRegistry.register(UserStatus.class, UserStatus.ACTIVE);
        EnumRegistry.register(UserStatus.class, UserStatus.INACTIVE);
        EnumRegistry.register(UserStatus.class, UserStatus.LOCKED);

        Map<String, UserStatus> statusMap = enumService.asMap(UserStatus.class);
        assertEquals(3, statusMap.size());
        assertEquals(UserStatus.ACTIVE, statusMap.get("ACTIVE"));
        assertEquals(UserStatus.INACTIVE, statusMap.get("INACTIVE"));
        assertEquals(UserStatus.LOCKED, statusMap.get("LOCKED"));

        Map<String, String> codeNameMap = enumService.asCodeNameMap(UserStatus.class);
        assertEquals(3, codeNameMap.size());
        assertEquals("激活", codeNameMap.get("ACTIVE"));
        assertEquals("未激活", codeNameMap.get("INACTIVE"));
    }

    @Test
    public void testEnumRemoval() {
        EnumRegistry.register(UserStatus.class, UserStatus.ACTIVE);
        EnumRegistry.register(UserStatus.class, UserStatus.INACTIVE);
        EnumRegistry.register(UserStatus.class, UserStatus.LOCKED);

        boolean removed = enumService.removeEnum(UserStatus.class, "INACTIVE");
        assertTrue(removed);

        assertFalse(enumService.exists(UserStatus.class, "INACTIVE"));
        assertEquals(2, enumService.getCount(UserStatus.class));

        assertTrue(enumService.exists(UserStatus.class, "ACTIVE"));
        assertTrue(enumService.exists(UserStatus.class, "LOCKED"));
    }

    @Test
    public void testMultipleEnumTypes() {
        EnumRegistry.register(UserStatus.class, UserStatus.ACTIVE);
        EnumRegistry.register(UserStatus.class, UserStatus.INACTIVE);

        EnumRegistry.register(OrderStatus.class, OrderStatus.PENDING);
        EnumRegistry.register(OrderStatus.class, OrderStatus.PROCESSING);

        assertEquals(2, enumService.getCount(UserStatus.class));
        assertEquals(2, enumService.getCount(OrderStatus.class));

        Set<Class<?>> registeredClasses = enumService.getAllRegisteredClasses();
        assertEquals(2, registeredClasses.size());
        assertTrue(registeredClasses.contains(UserStatus.class));
        assertTrue(registeredClasses.contains(OrderStatus.class));

        Optional<UserStatus> userStatus = EnumRegistry.valueOf(UserStatus.class, "PENDING");
        assertFalse(userStatus.isPresent());

        Optional<OrderStatus> orderStatus = EnumRegistry.valueOf(OrderStatus.class, "PENDING");
        assertTrue(orderStatus.isPresent());
    }

    @Test
    public void testGetByCodes() {
        EnumRegistry.register(UserStatus.class, UserStatus.ACTIVE);
        EnumRegistry.register(UserStatus.class, UserStatus.INACTIVE);
        EnumRegistry.register(UserStatus.class, UserStatus.LOCKED);
        EnumRegistry.register(UserStatus.class, UserStatus.SUSPENDED);

        List<String> codes = Arrays.asList("ACTIVE", "LOCKED", "NON_EXISTENT");
        List<UserStatus> statuses = enumService.getByCodes(UserStatus.class, codes);

        assertEquals(2, statuses.size());
        assertTrue(statuses.stream().anyMatch(s -> s.getCode().equals("ACTIVE")));
        assertTrue(statuses.stream().anyMatch(s -> s.getCode().equals("LOCKED")));
    }

    @Test
    public void testEnumServiceAsBean() {
        EnumRegistry.register(UserStatus.class, UserStatus.ACTIVE);
        EnumRegistry.register(UserStatus.class, UserStatus.INACTIVE);

        UserStatus status = enumService.getByCodeOrDefault(
                UserStatus.class,
                "NON_EXISTENT",
                UserStatus.INACTIVE
                                                           );

        assertEquals(UserStatus.INACTIVE, status);
    }
}
package com.helly.dyenums.integration;

import com.helly.dyenums.core.EnumRegistry;
import com.helly.dyenums.model.OrderStatus;
import com.helly.dyenums.model.UserStatus;
import com.helly.dyenums.spring.EnumService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.*;

/**
 * Integration tests demonstrating real-world usage of the dynamic enum library.
 * Tests the complete flow from registration to usage with example enums.
 *
 * @author Helly
 * @since 1.0.0
 */
public class EnumIntegrationTest {
    
    private EnumService enumService;
    
    @Before
    public void setUp() {
        // Clear registry before each test
        EnumRegistry.clear();
        enumService = new EnumService();
    }
    
    @After
    public void tearDown() {
        // Clean up after each test
        EnumRegistry.clear();
    }
    
    @Test
    public void testUserStatus_RegistrationAndUsage() {
        // Register predefined UserStatus values
        EnumRegistry.register(UserStatus.class, UserStatus.ACTIVE);
        EnumRegistry.register(UserStatus.class, UserStatus.INACTIVE);
        EnumRegistry.register(UserStatus.class, UserStatus.LOCKED);
        EnumRegistry.register(UserStatus.class, UserStatus.SUSPENDED);
        EnumRegistry.register(UserStatus.class, UserStatus.PENDING);
        
        // Test lookup
        UserStatus active = enumService.getByCode(UserStatus.class, "ACTIVE");
        assertEquals(UserStatus.ACTIVE, active);
        assertTrue(active.isActive());
        assertFalse(active.isBlocked());
        
        // Test business logic methods
        UserStatus locked = enumService.getByCode(UserStatus.class, "LOCKED");
        assertFalse(locked.isActive());
        assertTrue(locked.isBlocked());
        assertTrue(locked.requiresAdminAction());
        
        // Test getting all values
        List<UserStatus> allStatuses = enumService.getValues(UserStatus.class);
        assertEquals(5, allStatuses.size());
        assertEquals(UserStatus.ACTIVE, allStatuses.get(0)); // Sorted by order
        assertEquals(UserStatus.PENDING, allStatuses.get(4));
        
        // Test conversion to select options
        List<Map<String, String>> options = enumService.asSelectOptions(UserStatus.class);
        assertEquals(5, options.size());
        assertEquals("ACTIVE", options.get(0).get("value"));
        assertEquals("激活", options.get(0).get("label"));
    }
    
    @Test
    public void testOrderStatus_BusinessWorkflow() {
        // Register OrderStatus values
        EnumRegistry.register(OrderStatus.class, OrderStatus.PENDING);
        EnumRegistry.register(OrderStatus.class, OrderStatus.PROCESSING);
        EnumRegistry.register(OrderStatus.class, OrderStatus.CONFIRMED);
        EnumRegistry.register(OrderStatus.class, OrderStatus.SHIPPED);
        EnumRegistry.register(OrderStatus.class, OrderStatus.DELIVERED);
        EnumRegistry.register(OrderStatus.class, OrderStatus.CANCELLED);
        
        // Simulate order workflow
        OrderStatus status = enumService.getByCode(OrderStatus.class, "PENDING");
        assertTrue(status.isInProgress());
        assertTrue(status.canBeCancelled());
        assertTrue(status.canBeModified());
        
        // Transition to processing
        status = enumService.getByCode(OrderStatus.class, "PROCESSING");
        assertTrue(status.isInProgress());
        assertTrue(status.canBeCancelled());
        assertTrue(status.canBeModified());
        
        // Transition to shipped
        status = enumService.getByCode(OrderStatus.class, "SHIPPED");
        assertFalse(status.isInProgress());
        assertFalse(status.canBeCancelled());
        assertFalse(status.canBeModified());
        
        // Successful delivery
        status = enumService.getByCode(OrderStatus.class, "DELIVERED");
        assertFalse(status.isInProgress());
        assertTrue(status.isSuccessful());
        assertTrue(status.isTerminalState());
        
        // Failed order
        status = enumService.getByCode(OrderStatus.class, "CANCELLED");
        assertTrue(status.isFailed());
        assertTrue(status.isTerminalState());
    }
    
    @Test
    public void testDynamicEnumCreation() {
        // Start with predefined values
        EnumRegistry.register(OrderStatus.class, OrderStatus.PENDING);
        EnumRegistry.register(OrderStatus.class, OrderStatus.PROCESSING);
        
        // Dynamically add a new status for a custom workflow
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
        
        // Verify it's available for lookup
        Optional<OrderStatus> found = enumService.findByCode(OrderStatus.class, "CUSTOM_REVIEW");
        assertTrue(found.isPresent());
        assertEquals(customStatus, found.get());
        
        // Verify it's included in values list
        List<OrderStatus> allStatuses = enumService.getValues(OrderStatus.class);
        assertEquals(3, allStatuses.size());
        assertTrue(allStatuses.contains(customStatus));
    }
    
    @Test
    public void testConfigurationLoading() {
        // Simulate loading from configuration
        Properties orderConfig = new Properties();
        orderConfig.setProperty("OrderStatus.CUSTOM1", "Custom Status 1|Custom description 1|100");
        orderConfig.setProperty("OrderStatus.CUSTOM2", "Custom Status 2|Custom description 2|200");
        
        // Register predefined values
        EnumRegistry.register(OrderStatus.class, OrderStatus.PENDING);
        EnumRegistry.register(OrderStatus.class, OrderStatus.PROCESSING);
        
        // Load from config
        EnumRegistry.registerFromConfig(
            OrderStatus.class,
            orderConfig,
            OrderStatus::fromValueString
        );
        
        // Verify predefined + custom values
        List<OrderStatus> allStatuses = enumService.getValues(OrderStatus.class);
        assertEquals(4, allStatuses.size());
        
        // Verify custom status
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
        
        // Convert to map
        Map<String, UserStatus> statusMap = enumService.asMap(UserStatus.class);
        assertEquals(3, statusMap.size());
        assertEquals(UserStatus.ACTIVE, statusMap.get("ACTIVE"));
        assertEquals(UserStatus.INACTIVE, statusMap.get("INACTIVE"));
        assertEquals(UserStatus.LOCKED, statusMap.get("LOCKED"));
        
        // Convert to code-name map
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
        
        // Remove one status
        boolean removed = enumService.removeEnum(UserStatus.class, "INACTIVE");
        assertTrue(removed);
        
        // Verify it's gone
        assertFalse(enumService.exists(UserStatus.class, "INACTIVE"));
        assertEquals(2, enumService.getCount(UserStatus.class));
        
        // Verify others still exist
        assertTrue(enumService.exists(UserStatus.class, "ACTIVE"));
        assertTrue(enumService.exists(UserStatus.class, "LOCKED"));
    }
    
    @Test
    public void testMultipleEnumTypes() {
        // Register both enum types
        EnumRegistry.register(UserStatus.class, UserStatus.ACTIVE);
        EnumRegistry.register(UserStatus.class, UserStatus.INACTIVE);
        
        EnumRegistry.register(OrderStatus.class, OrderStatus.PENDING);
        EnumRegistry.register(OrderStatus.class, OrderStatus.PROCESSING);
        
        // Verify they don't interfere with each other
        assertEquals(2, enumService.getCount(UserStatus.class));
        assertEquals(2, enumService.getCount(OrderStatus.class));
        
        Set<Class<?>> registeredClasses = enumService.getAllRegisteredClasses();
        assertEquals(2, registeredClasses.size());
        assertTrue(registeredClasses.contains(UserStatus.class));
        assertTrue(registeredClasses.contains(OrderStatus.class));
        
        // Verify lookup doesn't cross-contaminate
        Optional<UserStatus> userStatus = EnumRegistry.valueOf(UserStatus.class, "PENDING");
        assertFalse(userStatus.isPresent()); // PENDING is an OrderStatus, not UserStatus
        
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
        
        assertEquals(2, statuses.size()); // NON_EXISTENT should be filtered out
        assertTrue(statuses.stream().anyMatch(s -> s.getCode().equals("ACTIVE")));
        assertTrue(statuses.stream().anyMatch(s -> s.getCode().equals("LOCKED")));
    }
    
    @Test
    public void testEnumServiceAsBean() {
        EnumRegistry.register(UserStatus.class, UserStatus.ACTIVE);
        EnumRegistry.register(UserStatus.class, UserStatus.INACTIVE);
        
        // Simulate using EnumService as a Spring bean
        UserStatus status = enumService.getByCodeOrDefault(
            UserStatus.class, 
            "NON_EXISTENT", 
            UserStatus.INACTIVE
        );
        
        assertEquals(UserStatus.INACTIVE, status);
    }
}

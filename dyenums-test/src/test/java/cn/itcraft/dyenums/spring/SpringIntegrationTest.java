package cn.itcraft.dyenums.spring;

import cn.itcraft.dyenums.core.EnumRegistry;
import cn.itcraft.dyenums.model.OrderStatus;
import cn.itcraft.dyenums.model.UserStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = DyEnumSpringTestConfig.class)
public class SpringIntegrationTest {

    @Autowired
    private EnumService enumService;

    @Autowired
    private EnumConverter<UserStatus> userStatusConverter;

    @Autowired
    private EnumConverter<OrderStatus> orderStatusConverter;

    @BeforeEach
    public void setUp() {
        EnumRegistry.clear();
        EnumRegistry.register(UserStatus.class, UserStatus.ACTIVE);
        EnumRegistry.register(UserStatus.class, UserStatus.INACTIVE);
        EnumRegistry.register(UserStatus.class, UserStatus.LOCKED);
        EnumRegistry.register(OrderStatus.class, OrderStatus.PENDING);
        EnumRegistry.register(OrderStatus.class, OrderStatus.PROCESSING);
        EnumRegistry.register(OrderStatus.class, OrderStatus.DELIVERED);
    }

    @AfterEach
    public void tearDown() {
        EnumRegistry.clear();
    }

    @Test
    public void testEnumServiceInjection() {
        assertNotNull(enumService, "EnumService should be injected by Spring");
    }

    @Test
    public void testEnumConverter_UserStatus() {
        UserStatus status = userStatusConverter.convert("ACTIVE");
        
        assertNotNull(status);
        assertEquals("ACTIVE", status.getCode());
        assertEquals("激活", status.getName());
        assertTrue(status.isActive());
    }

    @Test
    public void testEnumConverter_OrderStatus() {
        OrderStatus status = orderStatusConverter.convert("PENDING");
        
        assertNotNull(status);
        assertEquals("PENDING", status.getCode());
        assertEquals("待处理", status.getName());
        assertTrue(status.isInProgress());
    }

    @Test
    public void testEnumConverter_InvalidCode() {
        assertThrows(IllegalArgumentException.class, () -> {
            userStatusConverter.convert("INVALID_CODE");
        });
    }

    @Test
    public void testEnumConverter_EmptyCode() {
        assertThrows(IllegalArgumentException.class, () -> {
            userStatusConverter.convert("");
        });
    }

    @Test
    public void testEnumConverter_NullCode() {
        assertThrows(IllegalArgumentException.class, () -> {
            userStatusConverter.convert(null);
        });
    }

    @Test
    public void testMultipleEnumTypesInSpring() {
        assertEquals(3, enumService.getCount(UserStatus.class));
        assertEquals(3, enumService.getCount(OrderStatus.class));
        
        Set<Class<?>> registeredClasses = enumService.getAllRegisteredClasses();
        assertEquals(2, registeredClasses.size());
        assertTrue(registeredClasses.contains(UserStatus.class));
        assertTrue(registeredClasses.contains(OrderStatus.class));
    }

    @Test
    public void testEnumServiceAsSpringBean() {
        List<UserStatus> userStatuses = enumService.getValues(UserStatus.class);
        assertEquals(3, userStatuses.size());
        
        List<OrderStatus> orderStatuses = enumService.getValues(OrderStatus.class);
        assertEquals(3, orderStatuses.size());
    }

    @Test
    public void testEnumServiceOperations() {
        UserStatus active = enumService.getByCode(UserStatus.class, "ACTIVE");
        assertNotNull(active);
        assertEquals("ACTIVE", active.getCode());
        
        UserStatus notFound = enumService.getByCodeOrDefault(UserStatus.class, "NOT_FOUND", UserStatus.LOCKED);
        assertEquals(UserStatus.LOCKED, notFound);
        
        assertTrue(enumService.exists(UserStatus.class, "ACTIVE"));
        assertFalse(enumService.exists(UserStatus.class, "NOT_FOUND"));
    }

    @Test
    public void testEnumServiceDynamicCreation() {
        int initialCount = enumService.getCount(UserStatus.class);
        
        UserStatus newStatus = enumService.createEnum(
                UserStatus.class,
                "SPRING_DYNAMIC",
                "Spring动态状态",
                "通过Spring Bean动态创建",
                999
        );
        
        assertNotNull(newStatus);
        assertEquals("SPRING_DYNAMIC", newStatus.getCode());
        assertEquals(initialCount + 1, enumService.getCount(UserStatus.class));
        assertTrue(enumService.exists(UserStatus.class, "SPRING_DYNAMIC"));
    }

    @Test
    public void testEnumServiceMapConversion() {
        Map<String, UserStatus> statusMap = enumService.asMap(UserStatus.class);
        assertEquals(3, statusMap.size());
        assertTrue(statusMap.containsKey("ACTIVE"));
        assertTrue(statusMap.containsKey("INACTIVE"));
        assertTrue(statusMap.containsKey("LOCKED"));
        
        Map<String, String> codeNameMap = enumService.asCodeNameMap(UserStatus.class);
        assertEquals(3, codeNameMap.size());
        assertEquals("激活", codeNameMap.get("ACTIVE"));
    }

    @Test
    public void testEnumServiceSelectOptions() {
        List<Map<String, String>> options = enumService.asSelectOptions(UserStatus.class);
        
        assertEquals(3, options.size());
        
        Map<String, String> firstOption = options.get(0);
        assertEquals("ACTIVE", firstOption.get("value"));
        assertEquals("激活", firstOption.get("label"));
    }

    @Test
    public void testEnumServiceRemoval() {
        int initialCount = enumService.getCount(UserStatus.class);
        
        boolean removed = enumService.removeEnum(UserStatus.class, "INACTIVE");
        assertTrue(removed);
        assertEquals(initialCount - 1, enumService.getCount(UserStatus.class));
        assertFalse(enumService.exists(UserStatus.class, "INACTIVE"));
    }

    @Test
    public void testConverterAndGetEnumClass() {
        assertEquals(UserStatus.class, userStatusConverter.getEnumClass());
        assertEquals(OrderStatus.class, orderStatusConverter.getEnumClass());
    }
}
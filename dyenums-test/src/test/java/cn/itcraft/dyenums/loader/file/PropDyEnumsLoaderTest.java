package cn.itcraft.dyenums.loader.file;

import cn.itcraft.dyenums.core.EnumRegistry;
import cn.itcraft.dyenums.model.OrderStatus;
import cn.itcraft.dyenums.model.UserStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PropDyEnumsLoaderTest {

    @BeforeEach
    public void setUp() {
        EnumRegistry.clear();
    }

    @AfterEach
    public void tearDown() {
        EnumRegistry.clear();
    }

    @Test
    public void testLoad_ValidProperties() throws IOException {
        Properties props = new Properties();
        props.setProperty("UserStatus.PROP1", "Property Status 1|Property description 1|100");
        props.setProperty("UserStatus.PROP2", "Property Status 2|Property description 2|200");
        
        PropDyEnumsLoader<UserStatus> loader = new PropDyEnumsLoader<>(props);
        
        int count = loader.load(UserStatus.class, UserStatus::fromValueString);
        
        assertEquals(2, count);
        
        UserStatus prop1 = EnumRegistry.valueOf(UserStatus.class, "PROP1").orElse(null);
        assertNotNull(prop1);
        assertEquals("Property Status 1", prop1.getName());
        assertEquals(100, prop1.getOrder());
    }

    @Test
    public void testLoad_EmptyProperties() throws IOException {
        Properties props = new Properties();
        
        PropDyEnumsLoader<UserStatus> loader = new PropDyEnumsLoader<>(props);
        
        int count = loader.load(UserStatus.class, UserStatus::fromValueString);
        
        assertEquals(0, count);
    }

    @Test
    public void testLoad_SimpleFormat() throws IOException {
        Properties props = new Properties();
        props.setProperty("UserStatus.SIMPLE1", "Simple Name|Simple description|50");
        
        PropDyEnumsLoader<UserStatus> loader = new PropDyEnumsLoader<>(props);
        
        int count = loader.load(UserStatus.class, UserStatus::fromValueString);
        
        assertEquals(1, count);
        
        UserStatus simple1 = EnumRegistry.valueOf(UserStatus.class, "SIMPLE1").orElse(null);
        assertNotNull(simple1);
        assertEquals("Simple Name", simple1.getName());
        assertEquals("Simple description", simple1.getDescription());
        assertEquals(50, simple1.getOrder());
    }

    @Test
    public void testLoad_ComplexFormat() throws IOException {
        Properties props = new Properties();
        props.setProperty("UserStatus.COMPLEX_PROP.name", "Complex Prop Name");
        props.setProperty("UserStatus.COMPLEX_PROP.description", "Complex prop description");
        props.setProperty("UserStatus.COMPLEX_PROP.order", "75");
        
        PropDyEnumsLoader<UserStatus> loader = new PropDyEnumsLoader<>(props);
        
        int count = loader.load(UserStatus.class, UserStatus::fromValueString);
        
        assertEquals(1, count);
        
        UserStatus complexProp = EnumRegistry.valueOf(UserStatus.class, "COMPLEX_PROP").orElse(null);
        assertNotNull(complexProp);
        assertEquals("Complex Prop Name", complexProp.getName());
        assertEquals("Complex prop description", complexProp.getDescription());
        assertEquals(75, complexProp.getOrder());
    }

    @Test
    public void testValidateSource_NonEmpty() {
        Properties props = new Properties();
        props.setProperty("UserStatus.TEST", "Test|Test desc|1");
        
        PropDyEnumsLoader<UserStatus> loader = new PropDyEnumsLoader<>(props);
        
        assertTrue(loader.validateSource());
    }

    @Test
    public void testValidateSource_Empty() {
        Properties props = new Properties();
        
        PropDyEnumsLoader<UserStatus> loader = new PropDyEnumsLoader<>(props);
        
        assertFalse(loader.validateSource());
    }

    @Test
    public void testConstructor_NullProperties() {
        assertThrows(NullPointerException.class, () -> {
            new PropDyEnumsLoader<UserStatus>(null);
        });
    }

    @Test
    public void testIntegration_WithEnumRegistry() throws IOException {
        EnumRegistry.register(UserStatus.class, UserStatus.ACTIVE);
        EnumRegistry.register(UserStatus.class, UserStatus.INACTIVE);
        
        Properties props = new Properties();
        props.setProperty("UserStatus.INTEGRATION1", "Integration Status 1|Integration desc 1|100");
        props.setProperty("UserStatus.INTEGRATION2", "Integration Status 2|Integration desc 2|200");
        
        PropDyEnumsLoader<UserStatus> loader = new PropDyEnumsLoader<>(props);
        int loaded = loader.load(UserStatus.class, UserStatus::fromValueString);
        
        assertEquals(2, loaded);
        
        List<UserStatus> allValues = EnumRegistry.values(UserStatus.class);
        assertEquals(4, allValues.size());
        
        assertTrue(EnumRegistry.contains(UserStatus.class, "ACTIVE"));
        assertTrue(EnumRegistry.contains(UserStatus.class, "INTEGRATION1"));
    }

    @Test
    public void testLoad_MultipleEnumTypes() throws IOException {
        Properties props = new Properties();
        props.setProperty("UserStatus.MULTI_USER", "Multi User|Multi user desc|1");
        props.setProperty("OrderStatus.MULTI_ORDER", "Multi Order|Multi order desc|2");
        
        PropDyEnumsLoader<UserStatus> userLoader = new PropDyEnumsLoader<>(props);
        PropDyEnumsLoader<OrderStatus> orderLoader = new PropDyEnumsLoader<>(props);
        
        int userCount = userLoader.load(UserStatus.class, UserStatus::fromValueString);
        int orderCount = orderLoader.load(OrderStatus.class, OrderStatus::fromValueString);
        
        assertEquals(1, userCount);
        assertEquals(1, orderCount);
        
        assertTrue(EnumRegistry.contains(UserStatus.class, "MULTI_USER"));
        assertTrue(EnumRegistry.contains(OrderStatus.class, "MULTI_ORDER"));
    }
}
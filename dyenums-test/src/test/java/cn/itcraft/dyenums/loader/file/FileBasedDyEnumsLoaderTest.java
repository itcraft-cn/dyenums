package cn.itcraft.dyenums.loader.file;

import cn.itcraft.dyenums.core.EnumRegistry;
import cn.itcraft.dyenums.model.OrderStatus;
import cn.itcraft.dyenums.model.UserStatus;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for FileBasedDyEnumsLoader.
 *
 * @author Helly
 * @since 1.0.0
 */
public class FileBasedDyEnumsLoaderTest {

    @Before
    public void setUp() {
        EnumRegistry.clear();
    }

    @After
    public void tearDown() {
        EnumRegistry.clear();
    }

    @Test
    public void testLoad_ValidFile() throws IOException {
        FileBasedDyEnumsLoader<UserStatus> loader = new FileBasedDyEnumsLoader<>("test-enums.properties");
        
        int count = loader.load(UserStatus.class, UserStatus::fromValueString);
        
        assertEquals(3, count);
        
        UserStatus custom1 = EnumRegistry.valueOf(UserStatus.class, "CUSTOM1").orElse(null);
        assertNotNull(custom1);
        assertEquals("Custom Status 1", custom1.getName());
        assertEquals("Custom description 1", custom1.getDescription());
        assertEquals(100, custom1.getOrder());
    }

    @Test
    public void testLoad_ComplexFormat() throws IOException {
        FileBasedDyEnumsLoader<UserStatus> loader = new FileBasedDyEnumsLoader<>("test-enums-complex.properties");
        
        int count = loader.load(UserStatus.class, UserStatus::fromValueString);
        
        assertEquals(2, count);
        
        UserStatus complex1 = EnumRegistry.valueOf(UserStatus.class, "COMPLEX1").orElse(null);
        assertNotNull(complex1);
        assertEquals("Complex Status 1", complex1.getName());
        assertEquals("Complex description 1", complex1.getDescription());
        assertEquals(400, complex1.getOrder());
    }

    @Test(expected = IOException.class)
    public void testLoad_FileNotFound() throws IOException {
        FileBasedDyEnumsLoader<UserStatus> loader = new FileBasedDyEnumsLoader<>("non-existent-file.properties");
        
        loader.load(UserStatus.class, UserStatus::fromValueString);
    }

    @Test
    public void testValidateSource_ExistingFile() {
        FileBasedDyEnumsLoader<UserStatus> loader = new FileBasedDyEnumsLoader<>("test-enums.properties");
        
        assertTrue(loader.validateSource());
    }

    @Test
    public void testValidateSource_NonExistingFile() {
        FileBasedDyEnumsLoader<UserStatus> loader = new FileBasedDyEnumsLoader<>("non-existent-file.properties");
        
        assertFalse(loader.validateSource());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_PathTraversal() {
        new FileBasedDyEnumsLoader<>("../secret/config.properties");
    }

    @Test(expected = NullPointerException.class)
    public void testConstructor_NullFilePath() {
        new FileBasedDyEnumsLoader<UserStatus>(null);
    }

    @Test
    public void testIntegration_WithEnumRegistry() throws IOException {
        EnumRegistry.register(UserStatus.class, UserStatus.ACTIVE);
        EnumRegistry.register(UserStatus.class, UserStatus.INACTIVE);
        
        FileBasedDyEnumsLoader<UserStatus> loader = new FileBasedDyEnumsLoader<>("test-enums.properties");
        int loaded = loader.load(UserStatus.class, UserStatus::fromValueString);
        
        assertEquals(3, loaded);
        
        List<UserStatus> allValues = EnumRegistry.values(UserStatus.class);
        assertEquals(5, allValues.size());
        
        assertTrue(EnumRegistry.contains(UserStatus.class, "ACTIVE"));
        assertTrue(EnumRegistry.contains(UserStatus.class, "CUSTOM1"));
    }

    @Test
    public void testLoad_OrderStatus() throws IOException {
        FileBasedDyEnumsLoader<OrderStatus> loader = new FileBasedDyEnumsLoader<>("test-enums.properties");
        
        int count = loader.load(OrderStatus.class, OrderStatus::fromValueString);
        
        assertEquals(2, count);
        
        OrderStatus customOrder1 = EnumRegistry.valueOf(OrderStatus.class, "CUSTOM_ORDER1").orElse(null);
        assertNotNull(customOrder1);
        assertEquals("Custom Order 1", customOrder1.getName());
    }
}
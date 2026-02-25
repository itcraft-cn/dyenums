package com.helly.dyenums.core;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import java.util.*;
import java.util.stream.Collectors;
import static org.junit.Assert.*;

/**
 * Unit tests for EnumRegistry.
 * Tests the core registry functionality including registration, lookup, and management.
 *
 * @author Helly
 * @since 1.0.0
 */
public class EnumRegistryTest {
    
    /**
     * Test enum for testing purposes.
     */
    public static class TestEnum extends BaseCodeEnum {
        private static final long serialVersionUID = 1L;
        
        public static final TestEnum VALUE1 = new TestEnum("VALUE1", "Value One", "First value", 1);
        public static final TestEnum VALUE2 = new TestEnum("VALUE2", "Value Two", "Second value", 2);
        public static final TestEnum VALUE3 = new TestEnum("VALUE3", "Value Three", "Third value", 3);
        
        public TestEnum(String code, String name, String description, int order) {
            super(code, name, description, order);
        }
        
        public static TestEnum fromValueString(String code, String valueString) {
            String[] parts = valueString.split("\\|", 4);
            if (parts.length < 3) {
                throw new IllegalArgumentException("Invalid format");
            }
            return new TestEnum(code, parts[0], parts[1], Integer.parseInt(parts[2]));
        }
    }
    
    /**
     * Another test enum to verify class isolation.
     */
    public static class AnotherEnum extends BaseCodeEnum {
        private static final long serialVersionUID = 1L;
        
        public AnotherEnum(String code, String name, String description, int order) {
            super(code, name, description, order);
        }
    }
    
    @Before
    public void setUp() {
        // Clear the registry before each test
        EnumRegistry.clear();
    }
    
    @After
    public void tearDown() {
        // Clean up after each test
        EnumRegistry.clear();
    }
    
    @Test
    public void testRegister_SingleValue() {
        EnumRegistry.register(TestEnum.class, TestEnum.VALUE1);
        
        Optional<TestEnum> result = EnumRegistry.valueOf(TestEnum.class, "VALUE1");
        assertTrue(result.isPresent());
        assertEquals(TestEnum.VALUE1, result.get());
    }
    
    @Test
    public void testRegister_MultipleValues() {
        EnumRegistry.register(TestEnum.class, TestEnum.VALUE1);
        EnumRegistry.register(TestEnum.class, TestEnum.VALUE2);
        EnumRegistry.register(TestEnum.class, TestEnum.VALUE3);
        
        List<TestEnum> values = EnumRegistry.values(TestEnum.class);
        assertEquals(3, values.size());
        assertTrue(values.contains(TestEnum.VALUE1));
        assertTrue(values.contains(TestEnum.VALUE2));
        assertTrue(values.contains(TestEnum.VALUE3));
    }
    
    @Test
    public void testRegisterAll() {
        List<TestEnum> values = Arrays.asList(
            TestEnum.VALUE1, TestEnum.VALUE2, TestEnum.VALUE3
        );
        
        EnumRegistry.registerAll(TestEnum.class, values);
        
        List<TestEnum> result = EnumRegistry.values(TestEnum.class);
        assertEquals(3, result.size());
    }
    
    @Test
    public void testValueOf_ExistingValue() {
        EnumRegistry.register(TestEnum.class, TestEnum.VALUE1);
        
        Optional<TestEnum> result = EnumRegistry.valueOf(TestEnum.class, "VALUE1");
        assertTrue(result.isPresent());
        assertEquals(TestEnum.VALUE1, result.get());
    }
    
    @Test
    public void testValueOf_NonExistingValue() {
        Optional<TestEnum> result = EnumRegistry.valueOf(TestEnum.class, "NON_EXISTENT");
        assertFalse(result.isPresent());
    }
    
    @Test
    public void testValueOf_NullCode() {
        EnumRegistry.register(TestEnum.class, TestEnum.VALUE1);
        
        Optional<TestEnum> result = EnumRegistry.valueOf(TestEnum.class, null);
        assertFalse(result.isPresent());
    }
    
    @Test
    public void testValues_Empty() {
        List<TestEnum> values = EnumRegistry.values(TestEnum.class);
        assertTrue(values.isEmpty());
    }
    
    @Test
    public void testValues_SortedByOrder() {
        // Register in random order
        EnumRegistry.register(TestEnum.class, TestEnum.VALUE3);
        EnumRegistry.register(TestEnum.class, TestEnum.VALUE1);
        EnumRegistry.register(TestEnum.class, TestEnum.VALUE2);
        
        List<TestEnum> values = EnumRegistry.values(TestEnum.class);
        assertEquals(3, values.size());
        assertEquals(TestEnum.VALUE1, values.get(0));
        assertEquals(TestEnum.VALUE2, values.get(1));
        assertEquals(TestEnum.VALUE3, values.get(2));
    }
    
    @Test
    public void testContains_True() {
        EnumRegistry.register(TestEnum.class, TestEnum.VALUE1);
        
        assertTrue(EnumRegistry.contains(TestEnum.class, "VALUE1"));
    }
    
    @Test
    public void testContains_False() {
        assertFalse(EnumRegistry.contains(TestEnum.class, "VALUE1"));
    }
    
    @Test
    public void testContains_NullCode() {
        assertFalse(EnumRegistry.contains(TestEnum.class, null));
    }
    
    @Test
    public void testAddEnum_DynamicCreation() {
        TestEnum newEnum = EnumRegistry.addEnum(
            TestEnum.class, "DYNAMIC", "Dynamic Value", "Created dynamically", 10
        );
        
        assertNotNull(newEnum);
        assertEquals("DYNAMIC", newEnum.getCode());
        assertEquals("Dynamic Value", newEnum.getName());
        assertEquals("Created dynamically", newEnum.getDescription());
        assertEquals(10, newEnum.getOrder());
        
        // Verify it's registered
        Optional<TestEnum> result = EnumRegistry.valueOf(TestEnum.class, "DYNAMIC");
        assertTrue(result.isPresent());
        assertEquals(newEnum, result.get());
    }
    
    @Test
    public void testAddEnum_NullCode() {
        try {
            EnumRegistry.addEnum(TestEnum.class, null, "Name", "Desc", 1);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            assertTrue(e.getMessage().contains("Code cannot be null"));
        }
    }
    
    @Test
    public void testAddEnum_EmptyCode() {
        try {
            EnumRegistry.addEnum(TestEnum.class, "", "Name", "Desc", 1);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Code cannot be empty"));
        }
    }
    
    @Test
    public void testRemove_ExistingValue() {
        EnumRegistry.register(TestEnum.class, TestEnum.VALUE1);
        
        boolean removed = EnumRegistry.remove(TestEnum.class, "VALUE1");
        assertTrue(removed);
        
        Optional<TestEnum> result = EnumRegistry.valueOf(TestEnum.class, "VALUE1");
        assertFalse(result.isPresent());
    }
    
    @Test
    public void testRemove_NonExistingValue() {
        boolean removed = EnumRegistry.remove(TestEnum.class, "NON_EXISTENT");
        assertFalse(removed);
    }
    
    @Test
    public void testClear() {
        EnumRegistry.register(TestEnum.class, TestEnum.VALUE1);
        EnumRegistry.register(AnotherEnum.class, new AnotherEnum("TEST", "Test", "Desc", 1));
        
        EnumRegistry.clear();
        
        assertTrue(EnumRegistry.values(TestEnum.class).isEmpty());
        assertTrue(EnumRegistry.values(AnotherEnum.class).isEmpty());
    }
    
    @Test
    public void testClassIsolation() {
        EnumRegistry.register(TestEnum.class, TestEnum.VALUE1);
        EnumRegistry.register(AnotherEnum.class, new AnotherEnum("TEST", "Test", "Desc", 1));
        
        // Should not find TestEnum in AnotherEnum registry
        Optional<AnotherEnum> result = EnumRegistry.valueOf(AnotherEnum.class, "VALUE1");
        assertFalse(result.isPresent());
        
        // Should find TEST in AnotherEnum registry
        result = EnumRegistry.valueOf(AnotherEnum.class, "TEST");
        assertTrue(result.isPresent());
    }
    
    @Test
    public void testGetRegisteredClasses() {
        EnumRegistry.register(TestEnum.class, TestEnum.VALUE1);
        EnumRegistry.register(AnotherEnum.class, new AnotherEnum("TEST", "Test", "Desc", 1));
        
        Set<Class<?>> classes = EnumRegistry.getRegisteredClasses();
        assertEquals(2, classes.size());
        assertTrue(classes.contains(TestEnum.class));
        assertTrue(classes.contains(AnotherEnum.class));
    }
    
    @Test
    public void testGetCount() {
        EnumRegistry.register(TestEnum.class, TestEnum.VALUE1);
        EnumRegistry.register(TestEnum.class, TestEnum.VALUE2);
        
        assertEquals(2, EnumRegistry.getCount(TestEnum.class));
        assertEquals(0, EnumRegistry.getCount(AnotherEnum.class));
    }
    
    @Test
    public void testRegisterFromConfig() {
        Properties props = new Properties();
        props.setProperty("TestEnum.CONFIG1", "Config One|From config file|10");
        props.setProperty("TestEnum.CONFIG2", "Config Two|Also from config|20");
        
        EnumRegistry.registerFromConfig(
            TestEnum.class, 
            props,
            TestEnum::fromValueString
        );
        
        Optional<TestEnum> result1 = EnumRegistry.valueOf(TestEnum.class, "CONFIG1");
        assertTrue(result1.isPresent());
        assertEquals("Config One", result1.get().getName());
        
        Optional<TestEnum> result2 = EnumRegistry.valueOf(TestEnum.class, "CONFIG2");
        assertTrue(result2.isPresent());
        assertEquals("Config Two", result2.get().getName());
    }
    
    @Test
    public void testValues_IsImmutable() {
        EnumRegistry.register(TestEnum.class, TestEnum.VALUE1);
        
        List<TestEnum> values = EnumRegistry.values(TestEnum.class);
        int originalSize = values.size();
        
        // Try to modify the returned list
        try {
            values.add(new TestEnum("NEW", "New", "New value", 99));
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // Expected
        }
        
        // Verify the original list is unchanged
        assertEquals(originalSize, EnumRegistry.values(TestEnum.class).size());
    }
    
    @Test
    public void testRegisterAll_NullCollection() {
        try {
            EnumRegistry.registerAll(TestEnum.class, null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            assertTrue(e.getMessage().contains("Values cannot be null"));
        }
    }
    
    @Test
    public void testConcurrentRegistration() throws InterruptedException {
        final int threadCount = 10;
        final int enumsPerThread = 5;
        Thread[] threads = new Thread[threadCount];
        
        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            threads[i] = new Thread(() -> {
                for (int j = 0; j < enumsPerThread; j++) {
                    String code = String.format("THREAD%d_ENUM%d", threadId, j);
                    EnumRegistry.addEnum(
                        TestEnum.class, 
                        code, 
                        "Name " + code, 
                        "Desc " + code, 
                        threadId * enumsPerThread + j
                    );
                }
            });
        }
        
        // Start all threads
        for (Thread thread : threads) {
            thread.start();
        }
        
        // Wait for all threads to complete
        for (Thread thread : threads) {
            thread.join();
        }
        
        // Verify all enums were registered
        assertEquals(threadCount * enumsPerThread, EnumRegistry.getCount(TestEnum.class));
    }
}

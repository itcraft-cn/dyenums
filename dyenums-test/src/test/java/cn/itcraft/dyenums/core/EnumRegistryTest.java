package cn.itcraft.dyenums.core;

import cn.itcraft.dyenums.annotation.EnumDefinition;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class EnumRegistryTest {

    @BeforeEach
    public void setUp() {
        EnumRegistry.clear();
    }

    @AfterEach
    public void tearDown() {
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

        Optional<AnotherEnum> result = EnumRegistry.valueOf(AnotherEnum.class, "VALUE1");
        assertFalse(result.isPresent());

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

        try {
            values.add(new TestEnum("NEW", "New", "New value", 99));
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
        }

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

        for (Thread thread : threads) {
            thread.start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        assertEquals(threadCount * enumsPerThread, EnumRegistry.getCount(TestEnum.class));
    }

    @EnumDefinition(category = "test", dynamic = true, description = "Test enum")
    public static class TestEnum extends BaseDyEnum {
        public static final TestEnum VALUE1 = new TestEnum("VALUE1", "Value One", "First value", 1);
        public static final TestEnum VALUE2 = new TestEnum("VALUE2", "Value Two", "Second value", 2);
        public static final TestEnum VALUE3 = new TestEnum("VALUE3", "Value Three", "Third value", 3);
        private static final long serialVersionUID = 1L;

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

    @EnumDefinition(category = "test", dynamic = true, description = "Another test enum")
    public static class AnotherEnum extends BaseDyEnum {
        private static final long serialVersionUID = 1L;

        public AnotherEnum(String code, String name, String description, int order) {
            super(code, name, description, order);
        }
    }
}
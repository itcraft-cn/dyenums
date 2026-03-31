package cn.itcraft.dyenums.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class BaseDyEnumTest {

    @Test
    public void testConstructor_ValidValues() {
        TestEnum enumValue = new TestEnum("TEST", "Test Name", "Test Description", 1);

        assertEquals("TEST", enumValue.getCode());
        assertEquals("Test Name", enumValue.getName());
        assertEquals("Test Description", enumValue.getDescription());
        assertEquals(1, enumValue.getOrder());
    }

    @Test
    public void testConstructor_NullCode() {
        try {
            new TestEnum(null, "Test", "Description", 1);
            fail("Expected NullPointerException for null code");
        } catch (NullPointerException e) {
            assertTrue(e.getMessage().contains("Code cannot be null"));
        }
    }

    @Test
    public void testConstructor_NullName() {
        try {
            new TestEnum("TEST", null, "Description", 1);
            fail("Expected NullPointerException for null name");
        } catch (NullPointerException e) {
            assertTrue(e.getMessage().contains("Name cannot be null"));
        }
    }

    @Test
    public void testConstructor_EmptyCode() {
        try {
            new TestEnum("", "Test", "Description", 1);
            fail("Expected IllegalArgumentException for empty code");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Code cannot be empty"));
        }
    }

    @Test
    public void testConstructor_EmptyName() {
        try {
            new TestEnum("TEST", "", "Description", 1);
            fail("Expected IllegalArgumentException for empty name");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Name cannot be empty"));
        }
    }

    @Test
    public void testConstructor_NullDescription() {
        TestEnum enumValue = new TestEnum("TEST", "Test", null, 1);
        assertEquals("", enumValue.getDescription());
    }

    @Test
    public void testEquals_SameInstance() {
        TestEnum enumValue = new TestEnum("TEST", "Test", "Desc", 1);
        assertEquals(enumValue, enumValue);
    }

    @Test
    public void testEquals_SameCode() {
        TestEnum enumValue1 = new TestEnum("TEST", "Test1", "Desc1", 1);
        TestEnum enumValue2 = new TestEnum("TEST", "Test2", "Desc2", 2);

        assertEquals(enumValue1, enumValue2);
    }

    @Test
    public void testEquals_DifferentCode() {
        TestEnum enumValue1 = new TestEnum("TEST1", "Test", "Desc", 1);
        TestEnum enumValue2 = new TestEnum("TEST2", "Test", "Desc", 1);

        assertNotEquals(enumValue1, enumValue2);
    }

    @Test
    public void testEquals_DifferentClass() {
        TestEnum enumValue = new TestEnum("TEST", "Test", "Desc", 1);
        String other = "TEST";

        assertNotEquals(enumValue, other);
    }

    @Test
    public void testEquals_Null() {
        TestEnum enumValue = new TestEnum("TEST", "Test", "Desc", 1);
        assertNotEquals(enumValue, null);
    }

    @Test
    public void testHashCode_SameCode() {
        TestEnum enumValue1 = new TestEnum("TEST", "Test1", "Desc1", 1);
        TestEnum enumValue2 = new TestEnum("TEST", "Test2", "Desc2", 2);

        assertEquals(enumValue1.hashCode(), enumValue2.hashCode());
    }

    @Test
    public void testHashCode_DifferentCode() {
        TestEnum enumValue1 = new TestEnum("TEST1", "Test", "Desc", 1);
        TestEnum enumValue2 = new TestEnum("TEST2", "Test", "Desc", 1);

        assertNotEquals(enumValue1.hashCode(), enumValue2.hashCode());
    }

    @Test
    public void testToString() {
        TestEnum enumValue = new TestEnum("TEST_CODE", "Test Name", "Test Description", 5);
        String toString = enumValue.toString();

        assertTrue(toString.contains("TestEnum"));
        assertTrue(toString.contains("code='TEST_CODE'"));
        assertTrue(toString.contains("name='Test Name'"));
        assertTrue(toString.contains("order=5"));
    }

    @Test
    public void testImplementsDyEnum() {
        TestEnum enumValue = new TestEnum("TEST", "Test", "Desc", 1);
        assertTrue(enumValue instanceof DyEnum);
    }

    @Test
    public void testImplementsSerializable() {
        TestEnum enumValue = new TestEnum("TEST", "Test", "Desc", 1);
        assertTrue(enumValue instanceof java.io.Serializable);
    }

    @Test
    public void testConstructor_TrimsWhitespace() {
        TestEnum enumValue = new TestEnum("  TEST  ", "  Test  ", "  Desc  ", 1);

        assertEquals("TEST", enumValue.getCode());
        assertEquals("Test", enumValue.getName());
        assertEquals("Desc", enumValue.getDescription());
    }

    private static class TestEnum extends BaseDyEnum {
        private static final long serialVersionUID = 1L;

        public TestEnum(String code, String name, String description, int order) {
            super(code, name, description, order);
        }
    }
}
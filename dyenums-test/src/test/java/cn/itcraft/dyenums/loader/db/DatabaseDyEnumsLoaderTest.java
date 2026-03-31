package cn.itcraft.dyenums.loader.db;

import cn.itcraft.dyenums.core.EnumRegistry;
import cn.itcraft.dyenums.model.UserStatus;
import org.h2.jdbcx.JdbcConnectionPool;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DatabaseDyEnumsLoaderTest {

    private DataSource dataSource;

    @BeforeEach
    public void setUp() throws Exception {
        EnumRegistry.clear();
        
        dataSource = JdbcConnectionPool.create("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1", "sa", "");
        
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            
            stmt.execute("CREATE TABLE SYS_ENUM (" +
                    "ENUM_CLASS VARCHAR(100) NOT NULL," +
                    "CODE VARCHAR(50) NOT NULL," +
                    "NAME VARCHAR(100) NOT NULL," +
                    "DESCRIPTION VARCHAR(500)," +
                    "SORT_ORDER INT DEFAULT 999," +
                    "PRIMARY KEY (ENUM_CLASS, CODE)" +
                    ")");
            
            stmt.execute("INSERT INTO SYS_ENUM VALUES ('UserStatus', 'DB_STATUS1', 'DB Status 1', 'DB description 1', 100)");
            stmt.execute("INSERT INTO SYS_ENUM VALUES ('UserStatus', 'DB_STATUS2', 'DB Status 2', 'DB description 2', 200)");
            stmt.execute("INSERT INTO SYS_ENUM VALUES ('UserStatus', 'DB_STATUS3', 'DB Status 3', 'DB description 3', 300)");
        }
    }

    @AfterEach
    public void tearDown() throws Exception {
        EnumRegistry.clear();
        
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("DROP TABLE IF EXISTS SYS_ENUM");
        }
        
        if (dataSource instanceof JdbcConnectionPool) {
            ((JdbcConnectionPool) dataSource).dispose();
        }
    }

    @Test
    public void testLoad_ValidDataSource() throws Exception {
        DatabaseDyEnumsLoader<UserStatus> loader = new DatabaseDyEnumsLoader<>(dataSource);
        
        int count = loader.load(UserStatus.class, UserStatus::fromValueString);
        
        assertEquals(3, count);
        
        UserStatus status1 = EnumRegistry.valueOf(UserStatus.class, "DB_STATUS1").orElse(null);
        assertNotNull(status1);
        assertEquals("DB Status 1", status1.getName());
        assertEquals("DB description 1", status1.getDescription());
        assertEquals(100, status1.getOrder());
    }

    @Test
    public void testLoad_CustomQuery() throws Exception {
        String customQuery = "SELECT ENUM_CLASS, CODE, NAME, DESCRIPTION, SORT_ORDER FROM SYS_ENUM WHERE CODE = 'DB_STATUS1'";
        
        DatabaseDyEnumsLoader<UserStatus> loader = new DatabaseDyEnumsLoader<>(dataSource, customQuery);
        
        int count = loader.load(UserStatus.class, UserStatus::fromValueString);
        
        assertEquals(1, count);
        assertTrue(EnumRegistry.contains(UserStatus.class, "DB_STATUS1"));
        assertFalse(EnumRegistry.contains(UserStatus.class, "DB_STATUS2"));
    }

    @Test
    public void testLoad_InvalidQuery_NotSelect() {
        String invalidQuery = "DELETE FROM SYS_ENUM";
        
        assertThrows(IllegalArgumentException.class, () -> {
            new DatabaseDyEnumsLoader<UserStatus>(dataSource, invalidQuery);
        });
    }

    @Test
    public void testLoad_InvalidQuery_ForbiddenKeyword() {
        String invalidQuery = "SELECT * FROM SYS_ENUM; DROP TABLE SYS_ENUM";
        
        assertThrows(IllegalArgumentException.class, () -> {
            new DatabaseDyEnumsLoader<UserStatus>(dataSource, invalidQuery);
        });
    }

    @Test
    public void testValidateSource_ValidTable() {
        DatabaseDyEnumsLoader<UserStatus> loader = new DatabaseDyEnumsLoader<>(dataSource);
        
        assertTrue(loader.validateSource());
    }

    @Test
    public void testValidateSource_InvalidTable() throws Exception {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("DROP TABLE SYS_ENUM");
        }
        
        DatabaseDyEnumsLoader<UserStatus> loader = new DatabaseDyEnumsLoader<>(dataSource);
        
        assertFalse(loader.validateSource());
    }

    @Test
    public void testConstructor_InvalidColumnMappings() {
        String[] invalidMappings = {"code", "name"};
        
        assertThrows(IllegalArgumentException.class, () -> {
            new DatabaseDyEnumsLoader<UserStatus>(dataSource, "SELECT 1", "SELECT 1", invalidMappings);
        });
    }

    @Test
    public void testConstructor_NullDataSource() {
        assertThrows(NullPointerException.class, () -> {
            new DatabaseDyEnumsLoader<UserStatus>(null);
        });
    }

    @Test
    public void testConstructor_NullQuery() {
        assertThrows(NullPointerException.class, () -> {
            new DatabaseDyEnumsLoader<UserStatus>(dataSource, null);
        });
    }

    @Test
    public void testIntegration_WithEnumRegistry() throws Exception {
        EnumRegistry.register(UserStatus.class, UserStatus.ACTIVE);
        EnumRegistry.register(UserStatus.class, UserStatus.INACTIVE);
        
        DatabaseDyEnumsLoader<UserStatus> loader = new DatabaseDyEnumsLoader<>(dataSource);
        int loaded = loader.load(UserStatus.class, UserStatus::fromValueString);
        
        assertEquals(3, loaded);
        
        List<UserStatus> allValues = EnumRegistry.values(UserStatus.class);
        assertEquals(5, allValues.size());
        
        assertTrue(EnumRegistry.contains(UserStatus.class, "ACTIVE"));
        assertTrue(EnumRegistry.contains(UserStatus.class, "DB_STATUS1"));
    }

    @Test
    public void testLoad_WithNullDescription() throws Exception {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("INSERT INTO SYS_ENUM VALUES ('UserStatus', 'NULL_DESC', 'Null Desc', NULL, 400)");
        }
        
        DatabaseDyEnumsLoader<UserStatus> loader = new DatabaseDyEnumsLoader<>(dataSource);
        
        int count = loader.load(UserStatus.class, UserStatus::fromValueString);
        
        assertEquals(4, count);
        
        UserStatus nullDesc = EnumRegistry.valueOf(UserStatus.class, "NULL_DESC").orElse(null);
        assertNotNull(nullDesc);
        assertEquals("Null Desc", nullDesc.getName());
        assertEquals("", nullDesc.getDescription());
    }

    @Test
    public void testLoad_WithNullOrder() throws Exception {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("INSERT INTO SYS_ENUM VALUES ('UserStatus', 'NULL_ORDER', 'Null Order', 'Desc', NULL)");
        }
        
        DatabaseDyEnumsLoader<UserStatus> loader = new DatabaseDyEnumsLoader<>(dataSource);
        
        int count = loader.load(UserStatus.class, UserStatus::fromValueString);
        
        assertEquals(4, count);
        
        UserStatus nullOrder = EnumRegistry.valueOf(UserStatus.class, "NULL_ORDER").orElse(null);
        assertNotNull(nullOrder);
        assertEquals(999, nullOrder.getOrder());
    }

    @Test
    public void testLoad_OrderBySortOrder() throws Exception {
        DatabaseDyEnumsLoader<UserStatus> loader = new DatabaseDyEnumsLoader<>(dataSource);
        
        loader.load(UserStatus.class, UserStatus::fromValueString);
        
        List<UserStatus> values = EnumRegistry.values(UserStatus.class);
        
        assertEquals("DB_STATUS1", values.get(0).getCode());
        assertEquals("DB_STATUS2", values.get(1).getCode());
        assertEquals("DB_STATUS3", values.get(2).getCode());
    }
}
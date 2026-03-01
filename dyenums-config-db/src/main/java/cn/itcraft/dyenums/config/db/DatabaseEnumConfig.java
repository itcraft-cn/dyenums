package cn.itcraft.dyenums.config.db;

import cn.itcraft.dyenums.config.EnumConfigLoader;
import cn.itcraft.dyenums.core.DyEnum;
import cn.itcraft.dyenums.core.EnumRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Objects;
import java.util.function.BiFunction;

/**
 * Database configuration loader for enum definitions.
 * <p>
 * Expected database table structure:
 * <pre>
 * CREATE TABLE sys_enum (
 *     enum_class VARCHAR(100) NOT NULL,
 *     code VARCHAR(50) NOT NULL,
 *     name VARCHAR(100) NOT NULL,
 *     description VARCHAR(500),
 *     sort_order INT DEFAULT 999,
 *     PRIMARY KEY (enum_class, code)
 * );
 * </pre>
 *
 * @param <T> the enum type
 * @author Helly
 * @since 1.0.0
 */
public class DatabaseEnumConfig<T extends DyEnum> implements EnumConfigLoader<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseEnumConfig.class);

    private final DataSource dataSource;
    private final String query;
    private final String[] columnMappings;

    /**
     * Creates a database config loader with default query and column mappings.
     *
     * @param dataSource the data source to use
     */
    public DatabaseEnumConfig(DataSource dataSource) {
        this(dataSource,
             "SELECT enum_class, code, name, description, sort_order FROM sys_enum WHERE enum_class = ?",
             new String[]{"code", "name", "description", "sort_order"});
    }

    /**
     * Creates a database config loader with custom query.
     *
     * @param dataSource the data source to use
     * @param query the SQL query to execute
     */
    public DatabaseEnumConfig(DataSource dataSource, String query) {
        this(dataSource, query, new String[]{"code", "name", "description", "sort_order"});
    }

    /**
     * Creates a database config loader with custom query and column mappings.
     *
     * @param dataSource the data source to use
     * @param query the SQL query to execute
     * @param columnMappings array of column names in order: [code, name, description, order]
     */
    public DatabaseEnumConfig(DataSource dataSource, String query, String[] columnMappings) {
        this.dataSource = Objects.requireNonNull(dataSource, "DataSource cannot be null");
        this.query = Objects.requireNonNull(query, "Query cannot be null");
        this.columnMappings = Objects.requireNonNull(columnMappings, "Column mappings cannot be null");
        
        if (columnMappings.length < 4) {
            throw new IllegalArgumentException(
                    "Column mappings must have at least 4 elements: [code, name, description, order]");
        }
    }

    @Override
    public int load(Class<T> enumClass, BiFunction<String, String, T> factory) throws Exception {
        Objects.requireNonNull(enumClass, "Enum class cannot be null");
        Objects.requireNonNull(factory, "Factory cannot be null");

        int count = 0;
        String className = enumClass.getSimpleName();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            // Set parameter for enum class if query expects it
            if (query.contains("?")) {
                stmt.setString(1, className);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    try {
                        String code = rs.getString(columnMappings[0]);
                        String name = rs.getString(columnMappings[1]);
                        String description = rs.getString(columnMappings[2]);
                        int order = rs.getInt(columnMappings[3]);

                        // Handle potential nulls
                        if (rs.wasNull()) {
                            order = 999; // default order
                        }

                        // Create value string in format: code|name|description|order
                        String valueString = String.format("%s|%s|%s|%d",
                                                           code,
                                                           name != null ? name : code,
                                                           description != null ? description : "",
                                                           order);

                        T enumValue = factory.apply(code, valueString);
                        EnumRegistry.register(enumClass, enumValue);
                        count++;

                    } catch (Exception e) {
                        LOGGER.error("Failed to create enum from database row", e);
                    }
                }
            }

        } catch (Exception e) {
            LOGGER.error("Failed to load enums from database for class: {}", className, e);
            throw new Exception("Failed to load enums from database", e);
        }

        LOGGER.info("Loaded {} enum values for {} from database", count, className);
        return count;
    }

    @Override
    public boolean validateSource() {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT 1 FROM sys_enum LIMIT 1")) {
            
            try (java.sql.ResultSet rs = stmt.executeQuery()) {
                // The query executed successfully, table exists
                return true;
            }
        } catch (Exception e) {
            LOGGER.warn("Database enum table validation failed: {}", e.getMessage());
            return false;
        }
    }

    // Static utility methods for backward compatibility

    /**
     * Loads enum definitions from the database using the default query.
     *
     * @param dataSource the data source to use
     * @param enumClass  the enum class to load
     * @param factory    function to create enum instances (code, name, description, order -> enum)
     * @param <T>        the enum type
     * @return the number of enums loaded
     * @throws NullPointerException if any parameter is null
     */
    public static <T extends DyEnum> int loadFromDatabase(
            DataSource dataSource,
            Class<T> enumClass,
            BiFunction<String, String, T> factory) {

        DatabaseEnumConfig<T> config = new DatabaseEnumConfig<>(dataSource);
        try {
            return config.load(enumClass, factory);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load enums from database", e);
        }
    }

    /**
     * Loads enum definitions from the database using a custom query.
     *
     * @param dataSource the data source to use
     * @param enumClass  the enum class to load
     * @param factory    function to create enum instances (fullValue -> enum)
     * @param query      the SQL query to execute (must return: code, name, description, sort_order)
     * @param <T>        the enum type
     * @return the number of enums loaded
     * @throws NullPointerException if any parameter is null
     */
    public static <T extends DyEnum> int loadFromDatabase(
            DataSource dataSource,
            Class<T> enumClass,
            BiFunction<String, String, T> factory,
            String query) {

        DatabaseEnumConfig<T> config = new DatabaseEnumConfig<>(dataSource, query);
        try {
            return config.load(enumClass, factory);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load enums from database", e);
        }
    }

    /**
     * Loads enum definitions from the database using a custom query with flexible column mapping.
     *
     * @param dataSource     the data source to use
     * @param enumClass      the enum class to load
     * @param factory        function to create enum instances (code, valueString -> enum)
     * @param query          the SQL query to execute
     * @param columnMappings array of column names in order: [code, name, description, order]
     * @param <T>            the enum type
     * @return the number of enums loaded
     * @throws NullPointerException if any parameter is null
     */
    public static <T extends DyEnum> int loadFromDatabase(
            DataSource dataSource,
            Class<T> enumClass,
            BiFunction<String, String, T> factory,
            String query,
            String[] columnMappings) {

        DatabaseEnumConfig<T> config = new DatabaseEnumConfig<>(dataSource, query, columnMappings);
        try {
            return config.load(enumClass, factory);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load enums from database", e);
        }
    }

    /**
     * Validates that the database table exists and is accessible.
     *
     * @param dataSource the data source to check
     * @return true if the table is accessible, false otherwise
     */
    public static boolean validateTable(DataSource dataSource) {
        DatabaseEnumConfig<?> config = new DatabaseEnumConfig<>(dataSource);
        return config.validateSource();
    }

    /**
     * Gets the default table creation SQL for the enum table.
     *
     * @return the SQL DDL for creating the enum table
     */
    public static String getDefaultTableDDL() {
        return "CREATE TABLE sys_enum (" +
                "    enum_class VARCHAR(100) NOT NULL," +
                "    code VARCHAR(50) NOT NULL," +
                "    name VARCHAR(100) NOT NULL," +
                "    description VARCHAR(500)," +
                "    sort_order INT DEFAULT 999," +
                "    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
                "    PRIMARY KEY (enum_class, code)," +
                "    INDEX idx_enum_class (enum_class)," +
                "    INDEX idx_sort_order (sort_order)" +
                ")";
    }
}

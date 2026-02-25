package com.helly.dyenums.config;

import com.helly.dyenums.core.CodeEnum;
import com.helly.dyenums.core.EnumRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;
import java.util.function.BiFunction;

/**
 * Utility class for loading enum definitions from a database.
 * 
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
 * @author Helly
 * @since 1.0.0
 */
public class DatabaseEnumConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(DatabaseEnumConfig.class);
    
    private DatabaseEnumConfig() {
        throw new UnsupportedOperationException("Utility class should not be instantiated");
    }
    
    /**
     * Loads enum definitions from the database using the default query.
     *
     * @param dataSource the data source to use
     * @param enumClass the enum class to load
     * @param factory function to create enum instances (code, name, description, order -> enum)
     * @param <T> the enum type
     * @return the number of enums loaded
     * @throws NullPointerException if any parameter is null
     */
    public static <T extends CodeEnum> int loadFromDatabase(
            DataSource dataSource,
            Class<T> enumClass,
            BiFunction<String, String, T> factory) {
        
        return loadFromDatabase(dataSource, enumClass, factory, 
            "SELECT enum_class, code, name, description, sort_order FROM sys_enum WHERE enum_class = ?");
    }
    
    /**
     * Loads enum definitions from the database using a custom query.
     *
     * @param dataSource the data source to use
     * @param enumClass the enum class to load
     * @param factory function to create enum instances (fullValue -> enum)
     * @param query the SQL query to execute (must return: code, name, description, sort_order)
     * @param <T> the enum type
     * @return the number of enums loaded
     * @throws NullPointerException if any parameter is null
     */
    public static <T extends CodeEnum> int loadFromDatabase(
            DataSource dataSource,
            Class<T> enumClass,
            BiFunction<String, String, T> factory,
            String query) {
        
        Objects.requireNonNull(dataSource, "DataSource cannot be null");
        Objects.requireNonNull(enumClass, "Enum class cannot be null");
        Objects.requireNonNull(factory, "Factory cannot be null");
        Objects.requireNonNull(query, "Query cannot be null");
        
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
                        String code = rs.getString("code");
                        String name = rs.getString("name");
                        String description = rs.getString("description");
                        int order = rs.getInt("sort_order");
                        
                        // Create value string in format: code|name|description|order
                        String valueString = String.format("%s|%s|%s|%d", 
                            code, name, description != null ? description : "", order);
                        
                        T enumValue = factory.apply(code, valueString);
                        EnumRegistry.register(enumClass, enumValue);
                        count++;
                        
                    } catch (Exception e) {
                        String code = rs.getString("code");
                        logger.error("Failed to create enum from database: {}.{}", 
                            className, code, e);
                    }
                }
            }
            
        } catch (Exception e) {
            logger.error("Failed to load enums from database for class: {}", className, e);
            throw new RuntimeException("Failed to load enums from database", e);
        }
        
        logger.info("Loaded {} enum values for {} from database", count, className);
        return count;
    }
    
    /**
     * Loads enum definitions from the database using a custom query with flexible column mapping.
     * 
     * @param dataSource the data source to use
     * @param enumClass the enum class to load
     * @param factory function to create enum instances (code, valueString -> enum)
     * @param query the SQL query to execute
     * @param columnMappings array of column names in order: [code, name, description, order]
     * @param <T> the enum type
     * @return the number of enums loaded
     * @throws NullPointerException if any parameter is null
     */
    public static <T extends CodeEnum> int loadFromDatabase(
            DataSource dataSource,
            Class<T> enumClass,
            BiFunction<String, String, T> factory,
            String query,
            String[] columnMappings) {
        
        Objects.requireNonNull(dataSource, "DataSource cannot be null");
        Objects.requireNonNull(enumClass, "Enum class cannot be null");
        Objects.requireNonNull(factory, "Factory cannot be null");
        Objects.requireNonNull(query, "Query cannot be null");
        Objects.requireNonNull(columnMappings, "Column mappings cannot be null");
        
        if (columnMappings.length < 4) {
            throw new IllegalArgumentException("Column mappings must have at least 4 elements: [code, name, description, order]");
        }
        
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
                        logger.error("Failed to create enum from database row", e);
                    }
                }
            }
            
        } catch (Exception e) {
            logger.error("Failed to load enums from database for class: {}", className, e);
            throw new RuntimeException("Failed to load enums from database", e);
        }
        
        logger.info("Loaded {} enum values for {} from database", count, className);
        return count;
    }
    
    /**
     * Validates that the database table exists and is accessible.
     *
     * @param dataSource the data source to check
     * @return true if the table is accessible, false otherwise
     */
    public static boolean validateTable(DataSource dataSource) {
        Objects.requireNonNull(dataSource, "DataSource cannot be null");
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT 1 FROM sys_enum WHERE 1 = 0")) {
            
            stmt.executeQuery();
            return true;
            
        } catch (Exception e) {
            logger.warn("Database enum table validation failed: {}", e.getMessage());
            return false;
        }
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

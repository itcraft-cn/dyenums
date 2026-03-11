package cn.itcraft.dyenums.loader.db;

import cn.itcraft.dyenums.core.DyEnum;
import cn.itcraft.dyenums.loader.DyEnumsLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.util.Objects;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.BiFunction;

import static cn.itcraft.dyenums.loader.db.DbEnumConsts.COLUMN_MAPPINGS;
import static cn.itcraft.dyenums.loader.db.DbEnumConsts.SQL_DML_QUERY;
import static cn.itcraft.dyenums.loader.db.DbEnumConsts.SQL_DML_VALID;
import static cn.itcraft.dyenums.loader.db.DbSqlExecutor.validateQuery;

/**
 * Database configuration loader for enum definitions.
 * <p>
 * Expected database table structure: {@link DbEnumConsts#SQL_DDL}
 *
 * @param <T> the enum type
 * @author Helly
 * @since 1.0.0
 */
public class DatabaseDyEnumsLoader<T extends DyEnum> implements DyEnumsLoader<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseDyEnumsLoader.class);

    private static final ResultSetHandler<? extends DyEnum> HANDLE_NONE = rs -> {
    };

    private final DataSource dataSource;
    private final String query;
    private final String validQuery;
    private final String[] columnMappings;

    /**
     * Creates a database config loader with default query and column mappings.
     *
     * @param dataSource the data source to use
     */
    public DatabaseDyEnumsLoader(DataSource dataSource) {
        this(dataSource, SQL_DML_QUERY, SQL_DML_VALID, COLUMN_MAPPINGS);
    }

    /**
     * Creates a database config loader with custom query.
     *
     * @param dataSource the data source to use
     * @param query      the SQL query to execute
     */
    public DatabaseDyEnumsLoader(DataSource dataSource, String query) {
        this(dataSource, query, SQL_DML_VALID, COLUMN_MAPPINGS);
    }

    /**
     * Creates a database config loader with custom query.
     *
     * @param dataSource the data source to use
     * @param query      the SQL query to execute
     * @param validQuery the SQL query to execute
     */
    public DatabaseDyEnumsLoader(DataSource dataSource, String query, String validQuery) {
        this(dataSource, query, SQL_DML_VALID, COLUMN_MAPPINGS);
    }

    /**
     * Creates a database config loader with custom query and column mappings.
     *
     * @param dataSource     the data source to use
     * @param query          the SQL query to execute
     * @param validQuery     the SQL query to execute
     * @param columnMappings array of column names in order: [code, name, description, order]
     * @throws IllegalArgumentException if query contains forbidden SQL keywords or is not a SELECT statement
     */
    public DatabaseDyEnumsLoader(DataSource dataSource, String query, String validQuery, String[] columnMappings) {
        this.dataSource = Objects.requireNonNull(dataSource, "DataSource cannot be null");
        this.query = validateQuery(Objects.requireNonNull(query, "Query cannot be null"));
        this.validQuery = validateQuery(Objects.requireNonNull(validQuery, "Query cannot be null"));
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
        String className = enumClass.getSimpleName();
        LongAdder count = new LongAdder();
        DbSqlExecutor.execSql(dataSource, query, className,
                              new DyEnumQueryHandler<>(enumClass, columnMappings, factory, count));
        LOGGER.info("Loaded {} enum values for {} from database", count, className);
        return count.intValue();
    }

    @Override
    public boolean validateSource() {
        try {
            DbSqlExecutor.execSql(dataSource, validQuery, null, HANDLE_NONE);
            // The query executed successfully, table exists
            return true;
        } catch (Exception e) {
            LOGGER.warn("Database enum table validation failed: {}", e.getMessage());
            return false;
        }
    }

}

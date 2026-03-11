package cn.itcraft.dyenums.config.db;

import cn.itcraft.dyenums.core.DyEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static cn.itcraft.dyenums.config.db.DbEnumConsts.FORBIDDEN_SQL_OP;
import static cn.itcraft.dyenums.config.db.DbEnumConsts.VALID_SQL_OP;

/**
 * Database sql executor
 *
 * @author Helly
 * @since 1.0.0
 */
final class DbSqlExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(DbSqlExecutor.class);

    private DbSqlExecutor() {
    }

    static <T extends DyEnum> void execSql(DataSource dataSource, String query, String className,
                                           ResultSetHandler<T> handler) throws Exception {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            // Set parameter for enum class if query expects it
            if (query.contains("?")) {
                stmt.setString(1, className);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                handler.process(rs);
            }

        } catch (Exception e) {
            LOGGER.warn("Failed to load enums from database for class: {}", className, e);
            throw new Exception("Failed to load enums from database", e);
        }
    }

    /**
     * Validates that the query is safe and is a SELECT statement.
     *
     * @param query the SQL query to validate
     * @return the validated query
     * @throws IllegalArgumentException if the query is invalid
     */
    public static String validateQuery(String query) {
        String upperQuery = query.toUpperCase().trim();

        // Must be a SELECT statement
        if (!upperQuery.startsWith(VALID_SQL_OP)) {
            throw new IllegalArgumentException("Query must be a SELECT statement");
        }

        // Check for forbidden keywords (basic SQL injection prevention)
        for (String keyword : FORBIDDEN_SQL_OP) {
            if (upperQuery.contains(keyword)) {
                throw new IllegalArgumentException(
                        "Query contains forbidden SQL keyword: " + keyword);
            }
        }

        return query;
    }
}

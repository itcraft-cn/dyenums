package cn.itcraft.dyenums.config.db;

/**
 * Database configuration constants
 *
 * @author Helly
 * @since 1.0.0
 */
final class DbEnumConsts {

    static final String SQL_DDL
            = "CREATE TABLE SYS_ENUM (" +
            "    ENUM_CLASS VARCHAR(100) NOT NULL," +
            "    CODE VARCHAR(50) NOT NULL," +
            "    NAME VARCHAR(100) NOT NULL," +
            "    DESCRIPTION VARCHAR(500)," +
            "    SORT_ORDER INT DEFAULT 999," +
            "    CREATED_AT TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "    UPDATED_AT TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
            "    PRIMARY KEY (ENUM_CLASS, CODE)," +
            "    INDEX IDX_ENUM_CLASS (ENUM_CLASS)," +
            "    INDEX IDX_SORT_ORDER (SORT_ORDER)" +
            ")";
    static final String SQL_DML_QUERY =
            "SELECT ENUM_CLASS, CODE, NAME, DESCRIPTION, SORT_ORDER FROM SYS_ENUM WHERE ENUM_CLASS = ?";
    static final String SQL_DML_VALID = "SELECT 1 FROM SYS_ENUM LIMIT 1";
    static final String[] COLUMN_MAPPINGS = {"CODE", "NAME", "DESCRIPTION", "SORT_ORDER"};
    static final String VALID_SQL_OP = "SELECT";
    static final String[] FORBIDDEN_SQL_OP =
            {"DROP", "DELETE", "TRUNCATE", "ALTER", "INSERT", "UPDATE", "EXEC", "EXECUTE"};

    private DbEnumConsts() {
    }

}

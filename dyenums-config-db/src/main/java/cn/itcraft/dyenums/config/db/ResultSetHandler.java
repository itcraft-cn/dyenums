package cn.itcraft.dyenums.config.db;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author Helly Guo
 * <p>
 * Created on 2026-03-11 09:46
 */
@FunctionalInterface
interface ResultSetHandler<T> {
    void process(ResultSet resultSet) throws SQLException;
}

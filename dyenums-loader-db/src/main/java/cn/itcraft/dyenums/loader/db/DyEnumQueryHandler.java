package cn.itcraft.dyenums.loader.db;

import cn.itcraft.dyenums.core.DyEnum;
import cn.itcraft.dyenums.core.EnumRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.BiFunction;

/**
 * @author Helly Guo
 * <p>
 * Created on 2026-03-11 10:00
 */
final class DyEnumQueryHandler<T extends DyEnum> implements ResultSetHandler<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DyEnumQueryHandler.class);

    private final Class<T> enumClass;
    private final String[] columnMappings;
    private final BiFunction<String, String, T> factory;
    private final LongAdder count;

    public DyEnumQueryHandler(
            Class<T> enumClass, String[] columnMappings, BiFunction<String, String, T> factory, LongAdder count) {
        this.enumClass = enumClass;
        this.columnMappings = columnMappings;
        this.factory = factory;
        this.count = count;
    }

    @Override
    public void process(ResultSet rs) throws SQLException {
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

                // Create value string using direct concatenation (faster than String.format)
                String effectiveName = name != null ? name : code;
                String effectiveDesc = description != null ? description : "";
                String valueString = effectiveName + "|" + effectiveDesc + "|" + order;

                T enumValue = factory.apply(code, valueString);
                EnumRegistry.register(enumClass, enumValue);
                count.increment();

            } catch (Exception e) {
                LOGGER.warn("Failed to create enum from database row", e);
            }
        }
    }
}

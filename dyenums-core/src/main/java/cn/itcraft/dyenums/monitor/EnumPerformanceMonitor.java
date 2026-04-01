package cn.itcraft.dyenums.monitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Performance monitor for EnumRegistry operations.
 * Tracks various metrics and statistics about enum operations for performance optimization.
 *
 * @author Helly
 * @since 1.0.1
 */
public class EnumPerformanceMonitor {

    private static final Logger LOGGER = LoggerFactory.getLogger(EnumPerformanceMonitor.class);

    // Operation counters
    private static final AtomicLong REGISTER_COUNT = new AtomicLong(0);
    private static final AtomicLong VALUE_OF_COUNT = new AtomicLong(0);
    private static final AtomicLong VALUES_COUNT = new AtomicLong(0);
    private static final AtomicLong ADD_ENUM_COUNT = new AtomicLong(0);
    private static final AtomicLong REMOVE_COUNT = new AtomicLong(0);

    // Timing statistics (in nanoseconds)
    private static final AtomicLong REGISTER_TIME_ACCUMULATOR = new AtomicLong(0);
    private static final AtomicLong VALUE_OF_TIME_ACCUMULATOR = new AtomicLong(0);
    private static final AtomicLong VALUES_TIME_ACCUMULATOR = new AtomicLong(0);
    private static final AtomicLong ADD_ENUM_TIME_ACCUMULATOR = new AtomicLong(0);
    private static final AtomicLong REMOVE_TIME_ACCUMULATOR = new AtomicLong(0);

    // Operation counts per enum type for analysis
    private static final ConcurrentHashMap<Class<?>, AtomicLong> PER_CLASS_OPERATIONS = new ConcurrentHashMap<>();

    private EnumPerformanceMonitor() {
        throw new UnsupportedOperationException("Utility class should not be instantiated");
    }

    /**
     * Record an operation on an enum class.
     *
     * @param enumClass the enum class
     */
    public static void recordOperation(Class<?> enumClass) {
        AtomicLong counter = PER_CLASS_OPERATIONS.computeIfAbsent(enumClass, k -> new AtomicLong(0));
        counter.incrementAndGet();
    }

    /**
     * Record registry operation timing.
     *
     * @param startTime the start time in nanoseconds
     * @param enumClass the enum class
     */
    public static void recordRegister(long startTime, Class<?> enumClass) {
        long duration = System.nanoTime() - startTime;
        REGISTER_COUNT.incrementAndGet();
        REGISTER_TIME_ACCUMULATOR.addAndGet(duration);
        recordOperation(enumClass);
    }

    /**
     * Record valueOf operation timing.
     *
     * @param startTime the start time in nanoseconds
     * @param enumClass the enum class
     */
    public static void recordValueOf(long startTime, Class<?> enumClass) {
        long duration = System.nanoTime() - startTime;
        VALUE_OF_COUNT.incrementAndGet();
        VALUE_OF_TIME_ACCUMULATOR.addAndGet(duration);
        recordOperation(enumClass);
    }

    /**
     * Record values() operation timing.
     *
     * @param startTime the start time in nanoseconds
     * @param enumClass the enum class
     */
    public static void recordValues(long startTime, Class<?> enumClass) {
        long duration = System.nanoTime() - startTime;
        VALUES_COUNT.incrementAndGet();
        VALUES_TIME_ACCUMULATOR.addAndGet(duration);
        recordOperation(enumClass);
    }

    /**
     * Record addEnum operation timing.
     *
     * @param startTime the start time in nanoseconds
     * @param enumClass the enum class
     */
    public static void recordAddEnum(long startTime, Class<?> enumClass) {
        long duration = System.nanoTime() - startTime;
        ADD_ENUM_COUNT.incrementAndGet();
        ADD_ENUM_TIME_ACCUMULATOR.addAndGet(duration);
        recordOperation(enumClass);
    }

    /**
     * Record remove operation timing.
     *
     * @param startTime the start time in nanoseconds
     * @param enumClass the enum class
     */
    public static void recordRemove(long startTime, Class<?> enumClass) {
        long duration = System.nanoTime() - startTime;
        REMOVE_COUNT.incrementAndGet();
        REMOVE_TIME_ACCUMULATOR.addAndGet(duration);
        recordOperation(enumClass);
    }

    /**
     * Get average time in microseconds for register operations.
     *
     * @return average register time in microseconds
     */
    public static double getAverageRegisterTimeMicros() {
        long totalOps = REGISTER_COUNT.get();
        return totalOps > 0 ? REGISTER_TIME_ACCUMULATOR.get() / (double) totalOps / 1000.0 : 0.0;
    }

    /**
     * Get average time in microseconds for valueOf operations.
     *
     * @return average valueOf time in microseconds
     */
    public static double getAverageValueOfTimeMicros() {
        long totalOps = VALUE_OF_COUNT.get();
        return totalOps > 0 ? VALUE_OF_TIME_ACCUMULATOR.get() / (double) totalOps / 1000.0 : 0.0;
    }

    /**
     * Get average time in microseconds for values() operations.
     *
     * @return average values time in microseconds
     */
    public static double getAverageValuesTimeMicros() {
        long totalOps = VALUES_COUNT.get();
        return totalOps > 0 ? VALUES_TIME_ACCUMULATOR.get() / (double) totalOps / 1000.0 : 0.0;
    }

    /**
     * Generate detailed statistics report.
     *
     * @return formatted performance report string
     */
    public static String generateReport() {
        StringBuilder buf = new StringBuilder();
        buf.append("\n=== Enum Registry Performance Report ===\n");
        buf.append("Total Register Operations: ").append(REGISTER_COUNT.get()).append("\n");
        buf.append("Average Register Time: ").append(String.format("%.2f", getAverageRegisterTimeMicros()))
           .append(" μs\n");
        buf.append("Total valueOf Operations: ").append(VALUE_OF_COUNT.get()).append("\n");
        buf.append("Average valueOf Time: ").append(String.format("%.2f", getAverageValueOfTimeMicros()))
           .append(" μs\n");
        buf.append("Total values() Operations: ").append(VALUES_COUNT.get()).append("\n");
        buf.append("Average values() Time: ").append(String.format("%.2f", getAverageValuesTimeMicros()))
           .append(" μs\n");
        buf.append("Total addEnum Operations: ").append(ADD_ENUM_COUNT.get()).append("\n");
        buf.append("Total remove Operations: ").append(REMOVE_COUNT.get()).append("\n");
        buf.append("Most Active Enum Classes:\n");

        PER_CLASS_OPERATIONS.entrySet().stream()
                            .sorted((e1, e2) -> Long.compare(e2.getValue().get(), e1.getValue().get()))
                            .limit(10)
                            .forEach(entry ->
                                             buf.append("  ").append(entry.getKey().getSimpleName())
                                                .append(": ").append(entry.getValue().get()).append(" operations\n")
                                    );

        buf.append("===========================================");
        return buf.toString();
    }

    /**
     * Reset all statistics
     */
    public static void reset() {
        REGISTER_COUNT.set(0);
        VALUE_OF_COUNT.set(0);
        VALUES_COUNT.set(0);
        ADD_ENUM_COUNT.set(0);
        REMOVE_COUNT.set(0);

        REGISTER_TIME_ACCUMULATOR.set(0);
        VALUE_OF_TIME_ACCUMULATOR.set(0);
        VALUES_TIME_ACCUMULATOR.set(0);
        ADD_ENUM_TIME_ACCUMULATOR.set(0);
        REMOVE_TIME_ACCUMULATOR.set(0);

        PER_CLASS_OPERATIONS.clear();

        LOGGER.info("Enum performance statistics have been reset");
    }
}

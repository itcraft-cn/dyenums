package cn.itcraft.dyenums.core;

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
    private static final AtomicLong registerCount = new AtomicLong(0);
    private static final AtomicLong valueOfCount = new AtomicLong(0);
    private static final AtomicLong valuesCount = new AtomicLong(0);
    private static final AtomicLong addEnumCount = new AtomicLong(0);
    private static final AtomicLong removeCount = new AtomicLong(0);
    
    // Timing statistics (in nanoseconds)
    private static final AtomicLong registerTimeAccumulator = new AtomicLong(0);
    private static final AtomicLong valueOfTimeAccumulator = new AtomicLong(0);
    private static final AtomicLong valuesTimeAccumulator = new AtomicLong(0);
    private static final AtomicLong addEnumTimeAccumulator = new AtomicLong(0);
    private static final AtomicLong removeTimeAccumulator = new AtomicLong(0);
    
    // Operation counts per enum type for analysis
    private static final ConcurrentHashMap<Class<?>, AtomicLong> perClassOperations = new ConcurrentHashMap<>();
    
    private EnumPerformanceMonitor() {
        throw new UnsupportedOperationException("Utility class should not be instantiated");
    }
    
    /**
     * Record an operation on an enum class
     */
    public static void recordOperation(Class<?> enumClass) {
        AtomicLong counter = perClassOperations.computeIfAbsent(enumClass, k -> new AtomicLong(0));
        counter.incrementAndGet();
    }
    
    /**
     * Record registry operation timing
     */
    public static void recordRegister(long startTime, Class<?> enumClass) {
        long duration = System.nanoTime() - startTime;
        registerCount.incrementAndGet();
        registerTimeAccumulator.addAndGet(duration);
        recordOperation(enumClass);
    }
    
    /**
     * Record valueOf operation timing
     */
    public static void recordValueOf(long startTime, Class<?> enumClass) {
        long duration = System.nanoTime() - startTime;
        valueOfCount.incrementAndGet();
        valueOfTimeAccumulator.addAndGet(duration);
        recordOperation(enumClass);
    }
    
    /**
     * Record values() operation timing
     */
    public static void recordValues(long startTime, Class<?> enumClass) {
        long duration = System.nanoTime() - startTime;
        valuesCount.incrementAndGet();
        valuesTimeAccumulator.addAndGet(duration);
        recordOperation(enumClass);
    }
    
    /**
     * Record addEnum operation timing
     */
    public static void recordAddEnum(long startTime, Class<?> enumClass) {
        long duration = System.nanoTime() - startTime;
        addEnumCount.incrementAndGet();
        addEnumTimeAccumulator.addAndGet(duration);
        recordOperation(enumClass);
    }
    
    /**
     * Record remove operation timing
     */
    public static void recordRemove(long startTime, Class<?> enumClass) {
        long duration = System.nanoTime() - startTime;
        removeCount.incrementAndGet();
        removeTimeAccumulator.addAndGet(duration);
        recordOperation(enumClass);
    }
    
    /**
     * Get average time in microseconds for register operations
     */
    public static double getAverageRegisterTimeMicros() {
        long totalOps = registerCount.get();
        return totalOps > 0 ? registerTimeAccumulator.get() / (double) totalOps / 1000.0 : 0.0;
    }
    
    /**
     * Get average time in microseconds for valueOf operations
     */
    public static double getAverageValueOfTimeMicros() {
        long totalOps = valueOfCount.get();
        return totalOps > 0 ? valueOfTimeAccumulator.get() / (double) totalOps / 1000.0 : 0.0;
    }
    
    /**
     * Get average time in microseconds for values() operations
     */
    public static double getAverageValuesTimeMicros() {
        long totalOps = valuesCount.get();
        return totalOps > 0 ? valuesTimeAccumulator.get() / (double) totalOps / 1000.0 : 0.0;
    }
    
    /**
     * Generate detailed statistics report
     */
    public static String generateReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n=== Enum Registry Performance Report ===\n");
        sb.append("Total Register Operations: ").append(registerCount.get()).append("\n");
        sb.append("Average Register Time: ").append(String.format("%.2f", getAverageRegisterTimeMicros())).append(" μs\n");
        sb.append("Total valueOf Operations: ").append(valueOfCount.get()).append("\n");
        sb.append("Average valueOf Time: ").append(String.format("%.2f", getAverageValueOfTimeMicros())).append(" μs\n");
        sb.append("Total values() Operations: ").append(valuesCount.get()).append("\n");
        sb.append("Average values() Time: ").append(String.format("%.2f", getAverageValuesTimeMicros())).append(" μs\n");
        sb.append("Total addEnum Operations: ").append(addEnumCount.get()).append("\n");
        sb.append("Total remove Operations: ").append(removeCount.get()).append("\n");
        sb.append("Most Active Enum Classes:\n");
        
        perClassOperations.entrySet().stream()
            .sorted((e1, e2) -> Long.compare(e2.getValue().get(), e1.getValue().get()))
            .limit(10)
            .forEach(entry -> 
                sb.append("  ").append(entry.getKey().getSimpleName())
                  .append(": ").append(entry.getValue().get()).append(" operations\n")
            );
        
        sb.append("===========================================");
        return sb.toString();
    }
    
    /**
     * Reset all statistics
     */
    public static void reset() {
        registerCount.set(0);
        valueOfCount.set(0);
        valuesCount.set(0);
        addEnumCount.set(0);
        removeCount.set(0);
        
        registerTimeAccumulator.set(0);
        valueOfTimeAccumulator.set(0);
        valuesTimeAccumulator.set(0);
        addEnumTimeAccumulator.set(0);
        removeTimeAccumulator.set(0);
        
        perClassOperations.clear();
        
        LOGGER.info("Enum performance statistics have been reset");
    }
}
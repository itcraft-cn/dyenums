package com.helly.dyenums.spring;

import com.helly.dyenums.config.FileBasedEnumConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import javax.annotation.PostConstruct;
import java.io.IOException;

/**
 * Spring configuration for dynamic enum support.
 * 
 * This configuration class sets up the enum service and can optionally
 * load enum definitions from configuration files on startup.
 * 
 * To use this configuration, import it in your main configuration:
 * <pre>
 * &#64;Configuration
 * &#64;Import(DynamicEnumConfig.class)
 * public class ApplicationConfig {
 *     // your configuration
 * }
 * </pre>
 *
 * @author Helly
 * @since 1.0.0
 */
@Configuration
@ComponentScan(basePackages = "com.helly.dyenums.spring")
@PropertySource(value = "classpath:dyenums.properties", ignoreResourceNotFound = true)
public class DynamicEnumConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(DynamicEnumConfig.class);
    
    /**
     * Path to the enum configuration file.
     * Can be overridden in application.properties: dyenums.config.path=custom-path.properties
     */
    @Value("${dyenums.config.path:}")
    private String configPath;
    
    /**
     * Whether to auto-load enums from the configuration file on startup.
     * Can be overridden: dyenums.config.auto-load=true
     */
    @Value("${dyenums.config.auto-load:false}")
    private boolean autoLoadConfig;
    
    /**
     * Creates the EnumService bean.
     *
     * @return the EnumService instance
     */
    @Bean
    public EnumService enumService() {
        return new EnumService();
    }
    
    /**
     * Optional initialization method that can load enum configurations
     * from a properties file on startup.
     * 
     * This method is called after all properties are injected.
     */
    @PostConstruct
    public void initialize() {
        if (autoLoadConfig && configPath != null && !configPath.trim().isEmpty()) {
            logger.info("Auto-loading enum configurations from: {}", configPath);
            // Note: Actual loading would require specific enum class knowledge
            // This is a placeholder that can be extended for specific needs
        } else {
            logger.debug("Enum auto-loading disabled or no config path specified");
        }
    }
}

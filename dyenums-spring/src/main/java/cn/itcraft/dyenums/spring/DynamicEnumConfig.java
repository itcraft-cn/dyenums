package cn.itcraft.dyenums.spring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import javax.annotation.PostConstruct;

/**
 * Spring configuration for dynamic enum support.
 * <p>
 * This configuration class sets up the enum service and can optionally
 * load enum definitions from configuration files on startup.
 * <p>
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
@ComponentScan(basePackages = "cn.itcraft.dyenums.spring")
@PropertySource(value = "classpath:dyenums.properties", ignoreResourceNotFound = true)
public class DynamicEnumConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(DynamicEnumConfig.class);

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
     * <p>
     * This method is called after all properties are injected.
     */
    @PostConstruct
    public void initialize() {
        if (autoLoadConfig && configPath != null && !configPath.trim().isEmpty()) {
            LOGGER.info("Enum auto-loading is enabled from: {}", configPath);
            LOGGER.info("Actual loading will occur through @EnumDefinition annotation processing or manual loading.");
        } else {
            LOGGER.debug("Enum auto-loading is disabled (auto-load={}, config-path={})", 
                        autoLoadConfig, configPath);
        }
    }
}

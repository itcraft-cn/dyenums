package cn.itcraft.dyenums.spring;

import cn.itcraft.dyenums.model.OrderStatus;
import cn.itcraft.dyenums.model.UserStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Spring test configuration for dyenums integration tests.
 * Configures the necessary beans for testing Spring integration.
 *
 * @author Helly
 * @since 1.0.0
 */
@Configuration
@ComponentScan(basePackages = "cn.itcraft.dyenums.spring")
public class DyEnumSpringTestConfig {

    /**
     * Creates a converter for UserStatus enum.
     *
     * @return the UserStatus converter
     */
    @Bean
    public EnumConverter<UserStatus> userStatusConverter() {
        return new EnumConverter<>(UserStatus.class);
    }

    /**
     * Creates a converter for OrderStatus enum.
     *
     * @return the OrderStatus converter
     */
    @Bean
    public EnumConverter<OrderStatus> orderStatusConverter() {
        return new EnumConverter<>(OrderStatus.class);
    }
}
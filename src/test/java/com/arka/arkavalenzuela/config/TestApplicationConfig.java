package com.arka.arkavalenzuela.config;

import com.arka.arkavalenzuela.infrastructure.config.SecurityConfig;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

/**
 * Test configuration that excludes the main SecurityConfig
 */
@TestConfiguration
@EnableAutoConfiguration
@ComponentScan(
    basePackages = "com.arka.arkavalenzuela", 
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE, 
        classes = SecurityConfig.class
    )
)
public class TestApplicationConfig {
    // This configuration excludes the main SecurityConfig from test context
}

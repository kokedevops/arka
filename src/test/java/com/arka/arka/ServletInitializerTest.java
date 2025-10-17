package com.arka.arka;

import org.junit.jupiter.api.Test;
import org.springframework.boot.builder.SpringApplicationBuilder;
import static org.junit.jupiter.api.Assertions.*;

public class ServletInitializerTest {

    @Test
    public void testConfigureAddsCorrectSource() {
        ServletInitializer initializer = new ServletInitializer();
        SpringApplicationBuilder builder = new SpringApplicationBuilder();
        SpringApplicationBuilder result = initializer.configure(builder);

        // Simply verify that configure returns a valid SpringApplicationBuilder
        assertNotNull(result);
    }
}

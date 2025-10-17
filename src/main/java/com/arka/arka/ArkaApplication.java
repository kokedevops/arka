package com.arka.arka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.r2dbc.R2dbcAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(exclude = {
    R2dbcAutoConfiguration.class
})
@EnableScheduling  // 🔔 Enable scheduling for abandoned cart notifications
public class ArkaApplication {

	public static void main(String[] args) {
		SpringApplication.run(ArkaApplication.class, args);
	}

}

package com.arka.arka;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
	"spring.cloud.config.enabled=false",
	"eureka.client.enabled=false",
	"spring.datasource.url=jdbc:h2:mem:testdb",
	"spring.jpa.hibernate.ddl-auto=create-drop"
})
class ArkajvalenzuelaApplicationTests {

	@Test
	void contextLoads() {
		// Este test verifica que el contexto de Spring se carga correctamente
	}

}

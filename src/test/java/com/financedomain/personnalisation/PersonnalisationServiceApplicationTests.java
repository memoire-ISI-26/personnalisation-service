package com.financedomain.personnalisation;

import com.financedomain.personnalisation.controller.PersonnalisationController;
import com.financedomain.personnalisation.service.PersonnalisationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(properties = {
		"server.port=8401",
		"personnalisation-service.uriport=8401",
		"python.api.url=http://localhost:8000",
		"spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
		"spring.datasource.driver-class-name=org.h2.Driver",
		"spring.datasource.username=sa",
		"spring.datasource.password=",
		"spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
		"eureka.client.enabled=false",
		"spring.cloud.config.enabled=false"
})
class PersonnalisationServiceApplicationTests {

	@Autowired
	private ApplicationContext applicationContext;

	@Autowired
	private PersonnalisationController personnalisationController;

	@Autowired
	private PersonnalisationService personnalisationService;

	@Test
	@DisplayName("Vérifie le chargement du contexte Spring Boot et l'injection des beans du microservice personnalisation")
	void contextLoads() {
		assertNotNull(applicationContext, "Le contexte Spring ne doit pas être nul.");
		assertThat(personnalisationController).isNotNull();
		assertThat(personnalisationService).isNotNull();
	}
}

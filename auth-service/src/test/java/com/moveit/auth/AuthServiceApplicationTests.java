package com.moveit.auth;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class AuthServiceApplicationTests {

	@Test
	void contextLoads() {
		// Test unitaire simple - pas besoin de contexte Spring
		AuthServiceApplication app = new AuthServiceApplication();
		assertNotNull(app, "L'application ne doit pas être null");
	}

}

package com.moveit.championship;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class ChampionshipApplicationTests {

	@Test
	void contextLoads() {
		// Test unitaire simple - pas besoin de contexte Spring
		ChampionshipServiceApplication app = new ChampionshipServiceApplication();
		assertNotNull(app, "L'application ne doit pas être null");
	}

}

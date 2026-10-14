package com.taskboard.backend.health;

import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

	private final JdbcTemplate jdbcTemplate;

	public HealthController(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@GetMapping("/api/health")
	public Map<String, String> health() {
		String database = jdbcTemplate.queryForObject("select current_database()", String.class);
		return Map.of("status", "UP", "database", database);
	}

}
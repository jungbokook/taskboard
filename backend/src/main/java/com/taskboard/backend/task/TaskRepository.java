package com.taskboard.backend.task;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class TaskRepository {

	private static final RowMapper<Task> TASK_ROW_MAPPER = (rs, rowNum) -> new Task(
			rs.getLong("id"),
			rs.getString("title"),
			rs.getString("description"),
			rs.getBoolean("done"),
			rs.getObject("created_at", OffsetDateTime.class));

	private final JdbcTemplate jdbcTemplate;

	public TaskRepository(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public List<Task> findAll() {
		return jdbcTemplate.query(
				"select id, title, description, done, created_at from tasks order by created_at",
				TASK_ROW_MAPPER);
	}

	public Task create(String title, String description) {
		return jdbcTemplate.queryForObject(
				"insert into tasks (title, description) values (?, ?) returning id, title, description, done, created_at",
				TASK_ROW_MAPPER, title, description);
	}

	public Optional<Task> update(long id, String title, String description, Boolean done) {
		return jdbcTemplate.query(
				"""
						update tasks
						   set title = coalesce(?, title),
						       description = coalesce(?, description),
						       done = coalesce(?, done)
						 where id = ?
						returning id, title, description, done, created_at""",
				TASK_ROW_MAPPER, title, description, done, id)
			.stream()
			.findFirst();
	}

}
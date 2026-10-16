package com.taskboard.backend.task;

import java.time.LocalDate;
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
			rs.getObject("created_at", OffsetDateTime.class),
			rs.getObject("due_date", LocalDate.class));

	private final JdbcTemplate jdbcTemplate;

	public TaskRepository(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public List<Task> findAll() {
		return jdbcTemplate.query(
				"select id, title, description, done, created_at, due_date from tasks order by created_at",
				TASK_ROW_MAPPER);
	}

	public Task create(String title, String description, LocalDate dueDate) {
		return jdbcTemplate.queryForObject(
				"""
						insert into tasks (title, description, due_date) values (?, ?, ?)
						returning id, title, description, done, created_at, due_date""",
				TASK_ROW_MAPPER, title, description, dueDate);
	}

	public Optional<Task> update(long id, String title, String description, Boolean done, LocalDate dueDate) {
		return jdbcTemplate.query(
				"""
						update tasks
						   set title = coalesce(?, title),
						       description = coalesce(?, description),
						       done = coalesce(?, done),
						       due_date = coalesce(?, due_date)
						 where id = ?
						returning id, title, description, done, created_at, due_date""",
				TASK_ROW_MAPPER, title, description, done, dueDate, id)
			.stream()
			.findFirst();
	}

	public boolean delete(long id) {
		return jdbcTemplate.update("delete from tasks where id = ?", id) > 0;
	}

}
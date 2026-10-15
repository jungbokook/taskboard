package com.taskboard.backend.task;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.OffsetDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(TaskController.class)
@Import(TaskRepository.class)
class TaskControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private JdbcTemplate jdbcTemplate;

	@Test
	void findAllReturnsTasks() throws Exception {
		List<Task> tasks = List.of(
				new Task(1L, "첫 할 일", "설명", false, OffsetDateTime.parse("2026-10-16T09:00:00+09:00")),
				new Task(2L, "두 번째 할 일", null, true, OffsetDateTime.parse("2026-10-16T10:00:00+09:00")));
		given(jdbcTemplate.query(anyString(), any(RowMapper.class))).willReturn(tasks);

		mockMvc.perform(get("/api/tasks"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.length()").value(2))
			.andExpect(jsonPath("$[0].id").value(1))
			.andExpect(jsonPath("$[0].title").value("첫 할 일"))
			.andExpect(jsonPath("$[0].done").value(false))
			.andExpect(jsonPath("$[1].id").value(2))
			.andExpect(jsonPath("$[1].done").value(true));
	}

	@Test
	void findAllReturnsEmptyListWhenNoTasks() throws Exception {
		given(jdbcTemplate.query(anyString(), any(RowMapper.class))).willReturn(List.of());

		mockMvc.perform(get("/api/tasks"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.length()").value(0));
	}

	@Test
	void createReturnsCreatedTask() throws Exception {
		Task created = new Task(1L, "새 할 일", "설명", false, OffsetDateTime.parse("2026-10-16T09:00:00+09:00"));
		given(jdbcTemplate.queryForObject(anyString(), any(RowMapper.class), eq("새 할 일"), eq("설명")))
			.willReturn(created);

		mockMvc.perform(post("/api/tasks")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"title": "새 할 일", "description": "설명"}
						"""))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.id").value(1))
			.andExpect(jsonPath("$.title").value("새 할 일"))
			.andExpect(jsonPath("$.description").value("설명"))
			.andExpect(jsonPath("$.done").value(false))
			.andExpect(jsonPath("$.createdAt").exists());
	}

	@Test
	void createReturnsBadRequestWhenTitleIsBlank() throws Exception {
		mockMvc.perform(post("/api/tasks")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"title": "   "}
						"""))
			.andExpect(status().isBadRequest());

		verifyNoInteractions(jdbcTemplate);
	}

}
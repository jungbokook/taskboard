package com.taskboard.backend.task;

import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
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
				new Task(1L, "첫 할 일", "설명", false, OffsetDateTime.parse("2026-10-16T09:00:00+09:00"),
						LocalDate.of(2026, 10, 20)),
				new Task(2L, "두 번째 할 일", null, true, OffsetDateTime.parse("2026-10-16T10:00:00+09:00"), null));
		given(jdbcTemplate.query(anyString(), any(RowMapper.class))).willReturn(tasks);

		mockMvc.perform(get("/api/tasks"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.length()").value(2))
			.andExpect(jsonPath("$[0].id").value(1))
			.andExpect(jsonPath("$[0].title").value("첫 할 일"))
			.andExpect(jsonPath("$[0].done").value(false))
			.andExpect(jsonPath("$[0].dueDate").value("2026-10-20"))
			.andExpect(jsonPath("$[1].id").value(2))
			.andExpect(jsonPath("$[1].done").value(true))
			.andExpect(jsonPath("$[1].dueDate").value(nullValue()));
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
		Task created = new Task(1L, "새 할 일", "설명", false, OffsetDateTime.parse("2026-10-16T09:00:00+09:00"), null);
		given(jdbcTemplate.queryForObject(anyString(), any(RowMapper.class), eq("새 할 일"), eq("설명"), isNull()))
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
	void createWithDueDateReturnsDueDate() throws Exception {
		Task created = new Task(1L, "보고서 제출", null, false, OffsetDateTime.parse("2026-10-16T09:00:00+09:00"),
				LocalDate.of(2026, 10, 20));
		given(jdbcTemplate.queryForObject(anyString(), any(RowMapper.class), eq("보고서 제출"), isNull(),
				eq(LocalDate.of(2026, 10, 20))))
			.willReturn(created);

		mockMvc.perform(post("/api/tasks")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"title": "보고서 제출", "dueDate": "2026-10-20"}
						"""))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.id").value(1))
			.andExpect(jsonPath("$.dueDate").value("2026-10-20"));
	}

	@Test
	void createWithoutDueDateReturnsNullDueDate() throws Exception {
		Task created = new Task(1L, "새 할 일", null, false, OffsetDateTime.parse("2026-10-16T09:00:00+09:00"), null);
		given(jdbcTemplate.queryForObject(anyString(), any(RowMapper.class), eq("새 할 일"), isNull(), isNull()))
			.willReturn(created);

		mockMvc.perform(post("/api/tasks")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"title": "새 할 일"}
						"""))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.dueDate").value(nullValue()));
	}

	@Test
	void createReturnsBadRequestWhenDueDateFormatInvalid() throws Exception {
		mockMvc.perform(post("/api/tasks")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"title": "새 할 일", "dueDate": "2026/10/20"}
						"""))
			.andExpect(status().isBadRequest());

		verifyNoInteractions(jdbcTemplate);
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

	@Test
	void updateChangesOnlyGivenFields() throws Exception {
		Task updated = new Task(1L, "수정된 제목", "원래 설명", false, OffsetDateTime.parse("2026-10-16T09:00:00+09:00"),
				null);
		given(jdbcTemplate.query(anyString(), any(RowMapper.class), eq("수정된 제목"), isNull(), isNull(), isNull(),
				eq(1L)))
			.willReturn(List.of(updated));

		mockMvc.perform(patch("/api/tasks/1")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"title": "수정된 제목"}
						"""))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(1))
			.andExpect(jsonPath("$.title").value("수정된 제목"))
			.andExpect(jsonPath("$.description").value("원래 설명"))
			.andExpect(jsonPath("$.done").value(false))
			.andExpect(jsonPath("$.createdAt").exists());
	}

	@Test
	void updateChangesDueDate() throws Exception {
		Task updated = new Task(1L, "할 일", null, false, OffsetDateTime.parse("2026-10-16T09:00:00+09:00"),
				LocalDate.of(2026, 11, 1));
		given(jdbcTemplate.query(anyString(), any(RowMapper.class), isNull(), isNull(), isNull(),
				eq(LocalDate.of(2026, 11, 1)), eq(1L)))
			.willReturn(List.of(updated));

		mockMvc.perform(patch("/api/tasks/1")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"dueDate": "2026-11-01"}
						"""))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(1))
			.andExpect(jsonPath("$.dueDate").value("2026-11-01"));
	}

	@Test
	void updateReturnsNotFoundWhenTaskMissing() throws Exception {
		given(jdbcTemplate.query(anyString(), any(RowMapper.class), isNull(), isNull(), eq(true), isNull(), eq(99L)))
			.willReturn(List.of());

		mockMvc.perform(patch("/api/tasks/99")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"done": true}
						"""))
			.andExpect(status().isNotFound());
	}

	@Test
	void updateReturnsBadRequestWhenTitleIsBlank() throws Exception {
		mockMvc.perform(patch("/api/tasks/1")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"title": "   "}
						"""))
			.andExpect(status().isBadRequest());

		verifyNoInteractions(jdbcTemplate);
	}

	@Test
	void deleteReturnsNoContentWhenTaskDeleted() throws Exception {
		given(jdbcTemplate.update(anyString(), eq(1L))).willReturn(1);

		mockMvc.perform(delete("/api/tasks/1"))
			.andExpect(status().isNoContent())
			.andExpect(content().string(""));
	}

	@Test
	void deleteReturnsNotFoundWhenTaskMissing() throws Exception {
		given(jdbcTemplate.update(anyString(), eq(99L))).willReturn(0);

		mockMvc.perform(delete("/api/tasks/99"))
			.andExpect(status().isNotFound());
	}

}
package com.taskboard.backend.task;

import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
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
import org.springframework.dao.DataAccessResourceFailureException;
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
						LocalDate.of(2026, 10, 20), Priority.HIGH),
				new Task(2L, "두 번째 할 일", null, true, OffsetDateTime.parse("2026-10-16T10:00:00+09:00"), null,
						Priority.MEDIUM));
		given(jdbcTemplate.query(anyString(), any(RowMapper.class), any(Object[].class))).willReturn(tasks);

		mockMvc.perform(get("/api/tasks"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.length()").value(2))
			.andExpect(jsonPath("$[0].id").value(1))
			.andExpect(jsonPath("$[0].title").value("첫 할 일"))
			.andExpect(jsonPath("$[0].done").value(false))
			.andExpect(jsonPath("$[0].dueDate").value("2026-10-20"))
			.andExpect(jsonPath("$[0].priority").value("HIGH"))
			.andExpect(jsonPath("$[1].id").value(2))
			.andExpect(jsonPath("$[1].done").value(true))
			.andExpect(jsonPath("$[1].dueDate").value(nullValue()))
			.andExpect(jsonPath("$[1].priority").value("MEDIUM"));
	}

	@Test
	void findAllReturnsEmptyListWhenNoTasks() throws Exception {
		given(jdbcTemplate.query(anyString(), any(RowMapper.class), any(Object[].class))).willReturn(List.of());

		mockMvc.perform(get("/api/tasks"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.length()").value(0));
	}

	@Test
	void findAllFiltersByDone() throws Exception {
		Task task = new Task(2L, "끝난 일", null, true, OffsetDateTime.parse("2026-10-16T10:00:00+09:00"), null,
				Priority.MEDIUM);
		given(jdbcTemplate.query(contains("where done = ?"), any(RowMapper.class), eq(true)))
			.willReturn(List.of(task));

		mockMvc.perform(get("/api/tasks").param("done", "true"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.length()").value(1))
			.andExpect(jsonPath("$[0].id").value(2))
			.andExpect(jsonPath("$[0].done").value(true));
	}

	@Test
	void findAllFiltersByPriority() throws Exception {
		Task task = new Task(1L, "급한 일", null, false, OffsetDateTime.parse("2026-10-16T09:00:00+09:00"), null,
				Priority.HIGH);
		given(jdbcTemplate.query(contains("where priority = ?"), any(RowMapper.class), eq("HIGH")))
			.willReturn(List.of(task));

		mockMvc.perform(get("/api/tasks").param("priority", "HIGH"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.length()").value(1))
			.andExpect(jsonPath("$[0].id").value(1))
			.andExpect(jsonPath("$[0].priority").value("HIGH"));
	}

	@Test
	void findAllFiltersByDoneAndPriority() throws Exception {
		Task task = new Task(3L, "나중에 할 일", null, false, OffsetDateTime.parse("2026-10-16T11:00:00+09:00"), null,
				Priority.LOW);
		given(jdbcTemplate.query(contains("where done = ? and priority = ?"), any(RowMapper.class), eq(false),
				eq("LOW")))
			.willReturn(List.of(task));

		mockMvc.perform(get("/api/tasks").param("done", "false").param("priority", "LOW"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.length()").value(1))
			.andExpect(jsonPath("$[0].id").value(3))
			.andExpect(jsonPath("$[0].done").value(false))
			.andExpect(jsonPath("$[0].priority").value("LOW"));
	}

	@Test
	void findAllReturnsBadRequestWhenDoneInvalid() throws Exception {
		mockMvc.perform(get("/api/tasks").param("done", "yes"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.message").value("done 값은 true 또는 false여야 합니다"));

		verifyNoInteractions(jdbcTemplate);
	}

	@Test
	void findAllReturnsBadRequestWhenPriorityInvalid() throws Exception {
		mockMvc.perform(get("/api/tasks").param("priority", "URGENT"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.message").value("priority 값이 올바르지 않습니다"));

		verifyNoInteractions(jdbcTemplate);
	}

	@Test
	void findAllReturnsBadRequestWhenPriorityIsLowerCase() throws Exception {
		mockMvc.perform(get("/api/tasks").param("priority", "high"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.message").value("priority 값이 올바르지 않습니다"));

		verifyNoInteractions(jdbcTemplate);
	}

	@Test
	void createReturnsCreatedTask() throws Exception {
		Task created = new Task(1L, "새 할 일", "설명", false, OffsetDateTime.parse("2026-10-16T09:00:00+09:00"), null,
				Priority.MEDIUM);
		given(jdbcTemplate.queryForObject(anyString(), any(RowMapper.class), eq("새 할 일"), eq("설명"), isNull(),
				eq("MEDIUM")))
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
				LocalDate.of(2026, 10, 20), Priority.MEDIUM);
		given(jdbcTemplate.queryForObject(anyString(), any(RowMapper.class), eq("보고서 제출"), isNull(),
				eq(LocalDate.of(2026, 10, 20)), eq("MEDIUM")))
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
		Task created = new Task(1L, "새 할 일", null, false, OffsetDateTime.parse("2026-10-16T09:00:00+09:00"), null,
				Priority.MEDIUM);
		given(jdbcTemplate.queryForObject(anyString(), any(RowMapper.class), eq("새 할 일"), isNull(), isNull(),
				eq("MEDIUM")))
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
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.message").value("요청 본문 형식이 올바르지 않습니다"));

		verifyNoInteractions(jdbcTemplate);
	}

	@Test
	void createWithPriorityReturnsPriority() throws Exception {
		Task created = new Task(1L, "급한 일", null, false, OffsetDateTime.parse("2026-10-16T09:00:00+09:00"), null,
				Priority.HIGH);
		given(jdbcTemplate.queryForObject(anyString(), any(RowMapper.class), eq("급한 일"), isNull(), isNull(),
				eq("HIGH")))
			.willReturn(created);

		mockMvc.perform(post("/api/tasks")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"title": "급한 일", "priority": "HIGH"}
						"""))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.priority").value("HIGH"));
	}

	@Test
	void createWithoutPriorityUsesMedium() throws Exception {
		Task created = new Task(1L, "새 할 일", null, false, OffsetDateTime.parse("2026-10-16T09:00:00+09:00"), null,
				Priority.MEDIUM);
		given(jdbcTemplate.queryForObject(anyString(), any(RowMapper.class), eq("새 할 일"), isNull(), isNull(),
				eq("MEDIUM")))
			.willReturn(created);

		mockMvc.perform(post("/api/tasks")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"title": "새 할 일", "priority": null}
						"""))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.priority").value("MEDIUM"));
	}

	@Test
	void createReturnsBadRequestWhenPriorityInvalid() throws Exception {
		mockMvc.perform(post("/api/tasks")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"title": "새 할 일", "priority": "URGENT"}
						"""))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.message").value("요청 본문 형식이 올바르지 않습니다"));

		verifyNoInteractions(jdbcTemplate);
	}

	@Test
	void createReturnsBadRequestWhenPriorityIsLowerCase() throws Exception {
		mockMvc.perform(post("/api/tasks")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"title": "새 할 일", "priority": "high"}
						"""))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.message").value("요청 본문 형식이 올바르지 않습니다"));

		verifyNoInteractions(jdbcTemplate);
	}

	@Test
	void createReturnsBadRequestWhenTitleIsBlank() throws Exception {
		mockMvc.perform(post("/api/tasks")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"title": "   "}
						"""))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.message").value("제목을 입력하세요"));

		verifyNoInteractions(jdbcTemplate);
	}

	@Test
	void updateChangesOnlyGivenFields() throws Exception {
		Task updated = new Task(1L, "수정된 제목", "원래 설명", false, OffsetDateTime.parse("2026-10-16T09:00:00+09:00"),
				null, Priority.MEDIUM);
		given(jdbcTemplate.query(anyString(), any(RowMapper.class), eq("수정된 제목"), isNull(), isNull(), isNull(),
				isNull(), eq(1L)))
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
				LocalDate.of(2026, 11, 1), Priority.MEDIUM);
		given(jdbcTemplate.query(anyString(), any(RowMapper.class), isNull(), isNull(), isNull(),
				eq(LocalDate.of(2026, 11, 1)), isNull(), eq(1L)))
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
	void updateChangesPriority() throws Exception {
		Task updated = new Task(1L, "할 일", null, false, OffsetDateTime.parse("2026-10-16T09:00:00+09:00"), null,
				Priority.LOW);
		given(jdbcTemplate.query(anyString(), any(RowMapper.class), isNull(), isNull(), isNull(), isNull(), eq("LOW"),
				eq(1L)))
			.willReturn(List.of(updated));

		mockMvc.perform(patch("/api/tasks/1")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"priority": "LOW"}
						"""))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(1))
			.andExpect(jsonPath("$.priority").value("LOW"));
	}

	@Test
	void updateReturnsBadRequestWhenPriorityInvalid() throws Exception {
		mockMvc.perform(patch("/api/tasks/1")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"priority": "URGENT"}
						"""))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.message").value("요청 본문 형식이 올바르지 않습니다"));

		verifyNoInteractions(jdbcTemplate);
	}

	@Test
	void updateReturnsNotFoundWhenTaskMissing() throws Exception {
		given(jdbcTemplate.query(anyString(), any(RowMapper.class), isNull(), isNull(), eq(true), isNull(), isNull(),
				eq(99L)))
			.willReturn(List.of());

		mockMvc.perform(patch("/api/tasks/99")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"done": true}
						"""))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.message").value("할 일을 찾을 수 없습니다"));
	}

	@Test
	void updateReturnsBadRequestWhenTitleIsBlank() throws Exception {
		mockMvc.perform(patch("/api/tasks/1")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"title": "   "}
						"""))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.message").value("제목을 입력하세요"));

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
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.message").value("할 일을 찾을 수 없습니다"));
	}

	@Test
	void findAllReturnsServerErrorWhenDatabaseFails() throws Exception {
		given(jdbcTemplate.query(anyString(), any(RowMapper.class), any(Object[].class)))
			.willThrow(new DataAccessResourceFailureException("connection refused"));

		mockMvc.perform(get("/api/tasks"))
			.andExpect(status().isInternalServerError())
			.andExpect(jsonPath("$.message").value("서버 오류가 발생했습니다"));
	}

}
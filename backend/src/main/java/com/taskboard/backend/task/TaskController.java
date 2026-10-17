package com.taskboard.backend.task;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class TaskController {

	private final TaskRepository taskRepository;

	public TaskController(TaskRepository taskRepository) {
		this.taskRepository = taskRepository;
	}

	@GetMapping("/api/tasks")
	public ResponseEntity<List<Task>> findAll(@RequestParam(required = false) String done,
			@RequestParam(required = false) Priority priority, @RequestParam(required = false) String q) {
		if (done != null && !done.equals("true") && !done.equals("false")) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "done 값은 true 또는 false여야 합니다");
		}
		Boolean doneFilter = done == null ? null : Boolean.valueOf(done);
		String keyword = q == null || q.isBlank() ? null : q.strip();
		return ResponseEntity.ok(taskRepository.findAll(doneFilter, priority, keyword));
	}

	@PostMapping("/api/tasks")
	public ResponseEntity<Task> create(@RequestBody CreateTaskRequest request) {
		if (request.title() == null || request.title().isBlank()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "제목을 입력하세요");
		}
		Priority priority = request.priority() != null ? request.priority() : Priority.MEDIUM;
		Task task = taskRepository.create(request.title(), request.description(), request.dueDate(), priority);
		return ResponseEntity.status(HttpStatus.CREATED).body(task);
	}

	@PatchMapping("/api/tasks/{id}")
	public ResponseEntity<Task> update(@PathVariable long id, @RequestBody UpdateTaskRequest request) {
		if (request.title() != null && request.title().isBlank()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "제목을 입력하세요");
		}
		return taskRepository
			.update(id, request.title(), request.description(), request.done(), request.dueDate(), request.priority())
			.map(ResponseEntity::ok)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "할 일을 찾을 수 없습니다"));
	}

	@DeleteMapping("/api/tasks/{id}")
	public ResponseEntity<Void> delete(@PathVariable long id) {
		if (!taskRepository.delete(id)) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "할 일을 찾을 수 없습니다");
		}
		return ResponseEntity.noContent().build();
	}

}
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

@RestController
public class TaskController {

	private final TaskRepository taskRepository;

	public TaskController(TaskRepository taskRepository) {
		this.taskRepository = taskRepository;
	}

	@GetMapping("/api/tasks")
	public ResponseEntity<List<Task>> findAll(@RequestParam(required = false) String done,
			@RequestParam(required = false) Priority priority) {
		if (done != null && !done.equals("true") && !done.equals("false")) {
			return ResponseEntity.badRequest().build();
		}
		Boolean doneFilter = done == null ? null : Boolean.valueOf(done);
		return ResponseEntity.ok(taskRepository.findAll(doneFilter, priority));
	}

	@PostMapping("/api/tasks")
	public ResponseEntity<Task> create(@RequestBody CreateTaskRequest request) {
		if (request.title() == null || request.title().isBlank()) {
			return ResponseEntity.badRequest().build();
		}
		Priority priority = request.priority() != null ? request.priority() : Priority.MEDIUM;
		Task task = taskRepository.create(request.title(), request.description(), request.dueDate(), priority);
		return ResponseEntity.status(HttpStatus.CREATED).body(task);
	}

	@PatchMapping("/api/tasks/{id}")
	public ResponseEntity<Task> update(@PathVariable long id, @RequestBody UpdateTaskRequest request) {
		if (request.title() != null && request.title().isBlank()) {
			return ResponseEntity.badRequest().build();
		}
		return taskRepository
			.update(id, request.title(), request.description(), request.done(), request.dueDate(), request.priority())
			.map(ResponseEntity::ok)
			.orElseGet(() -> ResponseEntity.notFound().build());
	}

	@DeleteMapping("/api/tasks/{id}")
	public ResponseEntity<Void> delete(@PathVariable long id) {
		if (taskRepository.delete(id)) {
			return ResponseEntity.noContent().build();
		}
		return ResponseEntity.notFound().build();
	}

}
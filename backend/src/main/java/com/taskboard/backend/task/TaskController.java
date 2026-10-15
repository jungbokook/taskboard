package com.taskboard.backend.task;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TaskController {

	private final TaskRepository taskRepository;

	public TaskController(TaskRepository taskRepository) {
		this.taskRepository = taskRepository;
	}

	@GetMapping("/api/tasks")
	public List<Task> findAll() {
		return taskRepository.findAll();
	}

	@PostMapping("/api/tasks")
	public ResponseEntity<Task> create(@RequestBody CreateTaskRequest request) {
		if (request.title() == null || request.title().isBlank()) {
			return ResponseEntity.badRequest().build();
		}
		Task task = taskRepository.create(request.title(), request.description());
		return ResponseEntity.status(HttpStatus.CREATED).body(task);
	}

}
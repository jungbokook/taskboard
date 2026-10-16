package com.taskboard.backend.task;

import java.time.LocalDate;
import java.time.OffsetDateTime;

public record Task(Long id, String title, String description, boolean done, OffsetDateTime createdAt,
		LocalDate dueDate) {
}
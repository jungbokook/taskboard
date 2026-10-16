package com.taskboard.backend.task;

import java.time.LocalDate;

public record CreateTaskRequest(String title, String description, LocalDate dueDate) {
}
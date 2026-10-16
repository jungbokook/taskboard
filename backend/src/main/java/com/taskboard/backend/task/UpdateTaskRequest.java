package com.taskboard.backend.task;

import java.time.LocalDate;

public record UpdateTaskRequest(String title, String description, Boolean done, LocalDate dueDate) {
}
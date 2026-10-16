package com.taskboard.backend.task;

public record UpdateTaskRequest(String title, String description, Boolean done) {
}
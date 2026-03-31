package com.cb.coaless.controller;

import com.cb.coaless.model.Task;
import com.cb.coaless.repo.TaskRepository;

import java.util.List;

public class TaskController {

    private final TaskRepository repo;

    public TaskController(TaskRepository repo) {
        this.repo = repo;
    }

    public List<Task> getAll() {
        return repo.findAll();
    }

    public void delete(Long id) {
        repo.delete(id);
    }

    public Task create(Task task) {
        validate(task);
        return repo.save(task);
    }

    public Task update(Long id, Task updated) {
        Task existing = repo.findById(id);
        if (existing == null) {
            throw new RuntimeException("Task not found");
        }

        validate(updated);

        existing.setTitle(updated.getTitle());
        existing.setContent(updated.getContent());
        existing.setStatus(updated.getStatus());
        existing.setDueDate(updated.getDueDate());

        return repo.save(existing);

    }

    public Task getById(Long id) {
        Task task = repo.findById(id);
        if (task == null) {
            throw new RuntimeException("Task not found");
        }
        return task;
    }

    private void validate(Task task) {
        if (task.getTitle() == null || task.getTitle().isBlank()) {
            throw new IllegalArgumentException("Title is required");
        }
        if (task.getStatus() == null) {
            throw new IllegalArgumentException("Status is required");
        }
    }
}

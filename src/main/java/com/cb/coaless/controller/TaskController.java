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

    public Task create(Task task) {
        repo.save(task);
        return task;
    }

    public Task update(Long id, Task updated) {
        Task existing = repo.findById(id);
        if (existing == null) return null;

        existing.setTitle(updated.getTitle());
        existing.setContent(updated.getContent());
        existing.setStatus(updated.getStatus());
        existing.setDueDate(updated.getDueDate());

        repo.update(existing);
        return existing;
    }

    public void delete(Long id) {
        repo.delete(id);
    }
}

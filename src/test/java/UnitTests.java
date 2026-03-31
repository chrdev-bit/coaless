package com.cb.coaless.tests;

import com.cb.coaless.controller.TaskController;
import com.cb.coaless.model.Task;
import com.cb.coaless.repo.TaskRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UnitTests {

        private TaskRepository repo;
        private TaskController controller;

        @BeforeEach
        void setup() {
            repo = Mockito.mock(TaskRepository.class);
            controller = new TaskController(repo);
        }

        @Test
        void shouldFailWhenTitleMissing() {
            Task task = new Task();
            task.setStatus(Task.Status.TODO);

            Exception ex = assertThrows(IllegalArgumentException.class, () -> {
                controller.create(task);
            });

            assertEquals("Title is required", ex.getMessage());
        }
        @Test
        void shouldFailUpdateWhenTaskNotFound() {
            when(repo.findById(1L)).thenReturn(null);

            Task updated = new Task();
            updated.setTitle("New");
            updated.setStatus(Task.Status.TODO);

            Exception ex = assertThrows(RuntimeException.class, () -> {
                controller.update(1L, updated);
            });

            assertEquals("Task not found", ex.getMessage());
        }

        @Test
        void shouldCreateTask() {
            Task task = new Task();
            task.setTitle("Test");
            task.setStatus(Task.Status.TODO);

            when(repo.save(task)).thenReturn(task);

            Task result = controller.create(task);

            assertEquals("Test", result.getTitle());
            verify(repo).save(task);
        }

        @Test
        void shouldFailWhenStatusMissing() {
            Task task = new Task();
            task.setTitle("Test");

            Exception ex = assertThrows(IllegalArgumentException.class, () -> {
                controller.create(task);
            });

            assertEquals("Status is required", ex.getMessage());
        }

        @Test
        void shouldReturnTaskById() {
            Task task = new Task();
            task.setId(1L);
            task.setTitle("Test");

            Mockito.when(repo.findById(1L)).thenReturn(task);

            Task result = controller.getById(1L);

            assertEquals("Test", result.getTitle());
        }

}

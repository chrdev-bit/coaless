package com.cb.coaless.repo;

import com.cb.coaless.model.Task;
import jakarta.persistence.EntityManager;

import java.util.List;

public class TaskRepository {

    private final EntityManager em;

    public TaskRepository(EntityManager em) {
        this.em = em;
    }

    public List<Task> findAll() {
        return em.createQuery("SELECT a FROM Task a", Task.class)
                .getResultList();
    }

    public Task findById(Long id) {
        return em.find(Task.class, id);
    }

    public Task save(Task task) {
        em.getTransaction().begin();
        em.persist(task);
        em.getTransaction().commit();
        return task;
    }

    public void update(Task task) {
        em.getTransaction().begin();
        em.merge(task);
        em.getTransaction().commit();
    }

    public void delete(Long id) {
        em.getTransaction().begin();
        Task a = em.find(Task.class, id);
        if (a != null) em.remove(a);
        em.getTransaction().commit();
    }
}

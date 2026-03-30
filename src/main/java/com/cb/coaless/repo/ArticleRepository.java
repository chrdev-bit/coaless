package com.cb.coaless.repo;

import com.cb.coaless.model.Article;
import jakarta.persistence.EntityManager;

import java.util.List;

public class ArticleRepository {

    private final EntityManager em;

    public ArticleRepository(EntityManager em) {
        this.em = em;
    }

    public List<Article> findAll() {
        return em.createQuery("SELECT a FROM Article a", Article.class)
                .getResultList();
    }

    public Article findById(Long id) {
        return em.find(Article.class, id);
    }

    public void save(Article article) {
        em.getTransaction().begin();
        em.persist(article);
        em.getTransaction().commit();
    }

    public void update(Article article) {
        em.getTransaction().begin();
        em.merge(article);
        em.getTransaction().commit();
    }

    public void delete(Long id) {
        em.getTransaction().begin();
        Article a = em.find(Article.class, id);
        if (a != null) em.remove(a);
        em.getTransaction().commit();
    }
}

package com.cb.coaless.controller;

import com.cb.coaless.model.Article;
import com.cb.coaless.repo.ArticleRepository;

import java.util.List;

public class ArticleController {

    private final ArticleRepository repo;

    public ArticleController(ArticleRepository repo) {
        this.repo = repo;
    }

    public List<Article> getAll() {
        return repo.findAll();
    }

    public Article create(Article article) {
        repo.save(article);
        return article;
    }

    public Article update(Long id, Article updated) {
        Article existing = repo.findById(id);
        if (existing == null) return null;

        existing.setTitle(updated.getTitle());
        existing.setContent(updated.getContent());

        repo.update(existing);
        return existing;
    }

    public void delete(Long id) {
        repo.delete(id);
    }
}

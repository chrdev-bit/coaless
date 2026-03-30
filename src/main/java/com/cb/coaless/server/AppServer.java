package com.cb.coaless.server;

import com.cb.coaless.controller.ArticleController;
import com.cb.coaless.db.JPAUtil;
import com.cb.coaless.model.Article;
import com.cb.coaless.repo.ArticleRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;

import jakarta.persistence.EntityManager;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AppServer {

    private final ObjectMapper mapper = new ObjectMapper();

    public void start(int port) throws IOException {

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.setExecutor(java.util.concurrent.Executors.newVirtualThreadPerTaskExecutor());

        server.createContext("/api/articles", this::articlesHandler);
        server.createContext("/", this::spaHandler);

        server.start();
    }

    private void articlesHandler(HttpExchange exchange) throws IOException {

        EntityManager em = JPAUtil.createEntityManager();
        try {

            ArticleRepository repo = new ArticleRepository(em);
            ArticleController controller = new ArticleController(repo);

            // Seed if empty
            if (repo.findAll().isEmpty()) {
                Article article = new Article();
                article.setTitle("Test Case");
                article.setContent("Test case description");
                repo.save(article);
            }

            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();
            String response = "";
            int status = 200;

            try {
                if ("GET".equalsIgnoreCase(method) && path.equals("/api/articles")) {
                    List<Article> articles = controller.getAll();
                    response = mapper.writeValueAsString(articles);

                } else if ("POST".equalsIgnoreCase(method) && path.equals("/api/articles")) {
                    InputStream is = exchange.getRequestBody();
                    Article article = mapper.readValue(is, Article.class);

                    if (article.getTitle() == null || article.getTitle().isBlank()) {
                        sendJsonError(exchange, 400, "Title is required");
                        return;
                    }

                    controller.create(article);
                    response = mapper.writeValueAsString(article);
                    status = 201;

                } else if (("PUT".equalsIgnoreCase(method) || "DELETE".equalsIgnoreCase(method))
                        && path.matches("/api/articles/\\d+")) {

                    Long id = Long.parseLong(path.split("/")[3]);

                    if ("PUT".equalsIgnoreCase(method)) {
                        InputStream is = exchange.getRequestBody();
                        Article updated = mapper.readValue(is, Article.class);

                        Article result = controller.update(id, updated);
                        if (result == null) {
                            sendJsonError(exchange, 404, "Article not found");
                            return;
                        } else {
                            response = mapper.writeValueAsString(result);
                        }

                    } else { // DELETE
                        Article exists = controller.getAll().stream()
                                .filter(a -> a.getId().equals(id))
                                .findFirst().orElse(null);

                        if (exists == null) {
                            sendJsonError(exchange, 404, "Article not found");
                            return;
                        }

                        controller.delete(id);
                        response = "";
                        status = 204;
                    }

                } else {
                    sendJsonError(exchange, 405, "Method not allowed");
                    return;
                }

                sendJsonResponse(exchange, response, status);

            } catch (Exception e) {
                e.printStackTrace();
                sendJsonError(exchange, 500, "Internal server error: " + e.getMessage());
            }

        } finally {
            if (em.isOpen()) em.close();
        }
    }

    private void spaHandler(HttpExchange exchange) throws IOException {
        InputStream is = getClass().getClassLoader().getResourceAsStream("templates/index.html");
        byte[] bytes = is.readAllBytes();
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=utf-8");
        exchange.sendResponseHeaders(200, bytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
    }

    private void sendJsonResponse(HttpExchange exchange, String response, int status) throws IOException {
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(status, bytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
    }

    private void sendJsonError(HttpExchange exchange, int status, String message) throws IOException {
        Map<String, String> error = new HashMap<>();
        error.put("error", message);
        String json = mapper.writeValueAsString(error);

        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(status, bytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
    }
}
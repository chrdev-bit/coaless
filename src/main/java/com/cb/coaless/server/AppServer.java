package com.cb.coaless.server;

import com.cb.coaless.controller.TaskController;
import com.cb.coaless.db.JPAUtil;
import com.cb.coaless.model.Task;
import com.cb.coaless.repo.TaskRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import jakarta.persistence.EntityManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AppServer {

    private final ObjectMapper mapper = new ObjectMapper();

    public void start(int port) throws IOException {

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.setExecutor(java.util.concurrent.Executors.newVirtualThreadPerTaskExecutor());

        server.createContext("/api/tasks", this::tasksHandler);
        server.createContext("/", this::spaHandler);
        server.createContext("/swagger-ui", exchange -> {
            String path = exchange.getRequestURI().getPath();
            System.out.println("path:"+path);
            if (path.isEmpty() || path.equals("/")) path = "/swagger-ui/index.html";

            InputStream is = getClass().getClassLoader().getResourceAsStream(path.substring(1));
            if (is == null) {
                exchange.sendResponseHeaders(404, -1);
                return;
            }

            byte[] bytes = is.readAllBytes();
            String contentType = "text/html";
            if (path.endsWith(".js")) contentType = "application/javascript";
            else if (path.endsWith(".css")) contentType = "text/css";
            else if (path.endsWith(".png")) contentType = "image/png";

            exchange.getResponseHeaders().set("Content-Type", contentType);
            exchange.sendResponseHeaders(200, bytes.length);
            exchange.getResponseBody().write(bytes);
            exchange.getResponseBody().close();
        });
        server.createContext("/swagger.json", exchange -> {
            InputStream is = getClass().getClassLoader().getResourceAsStream("swagger.json");
            byte[] bytes = is.readAllBytes();
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, bytes.length);
            exchange.getResponseBody().write(bytes);
            exchange.getResponseBody().close();
        });


        server.start();
    }

    private void tasksHandler(HttpExchange exchange) throws IOException {

        EntityManager em = JPAUtil.createEntityManager();
        try {

            TaskRepository repo = new TaskRepository(em);
            TaskController controller = new TaskController(repo);

            if (repo.findAll().isEmpty()) {
                Task task = new Task();
                task.setTitle("Test Task");
                task.setContent("Test task description");
                task.setStatus(Task.Status.TODO);
                LocalDate localDue = LocalDate.now().plusDays(7);
                task.setDueDate(Date.valueOf(localDue));
                repo.save(task);
            }

            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();
            String response = "";
            int status = 200;

            try {
                if ("GET".equalsIgnoreCase(method) && path.equals("/api/tasks")) {
                    List<Task> tasks = controller.getAll();
                    response = mapper.writeValueAsString(tasks);

                }else if ("GET".equalsIgnoreCase(method) && path.matches("/api/tasks/\\d+")) {

                        Long id = Long.parseLong(path.split("/")[3]);

                        Task task = controller.getById(id);

                        if (task == null) {
                            sendJsonError(exchange, 404, "Task not found");
                            return;
                        }

                        response = mapper.writeValueAsString(task);

                } else if ("POST".equalsIgnoreCase(method) && path.equals("/api/tasks")) {
                    InputStream is = exchange.getRequestBody();
                    Task task = mapper.readValue(is, Task.class);

                    if (task.getTitle() == null || task.getTitle().isBlank()) {
                        sendJsonError(exchange, 400, "Title is required");
                        return;
                    }

                    controller.create(task);
                    response = mapper.writeValueAsString(task);
                    status = 201;

                } else if (("PUT".equalsIgnoreCase(method) || "DELETE".equalsIgnoreCase(method))
                        && path.matches("/api/tasks/\\d+")) {

                    Long id = Long.parseLong(path.split("/")[3]);

                    if ("PUT".equalsIgnoreCase(method)) {
                        InputStream is = exchange.getRequestBody();
                        Task updated = mapper.readValue(is, Task.class);

                        Task result = controller.update(id, updated);
                        if (result == null) {
                            sendJsonError(exchange, 404, "Task not found");
                            return;
                        } else {
                            response = mapper.writeValueAsString(result);
                        }

                    } else { // DELETE
                        Task exists = controller.getAll().stream()
                                .filter(a -> a.getId().equals(id))
                                .findFirst().orElse(null);

                        if (exists == null) {
                            sendJsonError(exchange, 404, "Task not found");
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
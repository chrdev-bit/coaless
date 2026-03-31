package com.cb.coaless.tests;

import com.cb.coaless.model.Task;
import com.cb.coaless.model.Task.Status;
import com.cb.coaless.server.AppServer;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class IntegrationTests {

    private AppServer server;
    private final ObjectMapper mapper = new ObjectMapper();
    private final int PORT = 8085;

    @BeforeAll
    void startServer() throws Exception {
        server = new AppServer();
        server.start(PORT);
    }

    @AfterAll
    void stopServer() {

    }

    private String baseUrl(String path) {
        return "http://localhost:" + PORT + path;
    }


    @Test
    @Order(1)
    void testCreateUpdateTask() throws Exception {
        //create
        Task task = new Task();
        task.setTitle("To Update");
        task.setContent("Old Content");
        task.setStatus(Status.TODO);
        task.setDueDate(Date.valueOf(LocalDate.now()));

        HttpURLConnection conn = (HttpURLConnection) new URL(baseUrl("/api/tasks")).openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json");
        try (OutputStream os = conn.getOutputStream()) {
            mapper.writeValue(os, task);
        }
        assertEquals(201, conn.getResponseCode());

        //get ID from GET
        HttpURLConnection getConn = (HttpURLConnection) new URL(baseUrl("/api/tasks")).openConnection();
        getConn.setRequestMethod("GET");
        Task fetched;
        try (InputStream is = getConn.getInputStream()) {
            fetched = mapper.readValue(is, new TypeReference<List<Task>>() {}).get(0);
        }

        //update
        fetched.setTitle("Updated Title");
        fetched.setStatus(Status.INPROGRESS);

        HttpURLConnection putConn = (HttpURLConnection) new URL(baseUrl("/api/tasks/" + fetched.getId())).openConnection();
        putConn.setRequestMethod("PUT");
        putConn.setDoOutput(true);
        putConn.setRequestProperty("Content-Type", "application/json");
        try (OutputStream os = putConn.getOutputStream()) {
            mapper.writeValue(os, fetched);
        }

        assertEquals(200, putConn.getResponseCode());

        //verify
        HttpURLConnection verifyConn = (HttpURLConnection) new URL(baseUrl("/api/tasks")).openConnection();
        verifyConn.setRequestMethod("GET");
        try (InputStream is = verifyConn.getInputStream()) {
            Task updated = mapper.readValue(is, new TypeReference<List<Task>>() {}).get(0);
            assertEquals("Updated Title", updated.getTitle());
            assertEquals(Status.INPROGRESS, updated.getStatus());
        }
    }

    @Test
    @Order(2)
    void testDeleteTask() throws Exception {
        HttpURLConnection getConn = (HttpURLConnection) new URL(baseUrl("/api/tasks")).openConnection();
        getConn.setRequestMethod("GET");
        Task fetched;
        try (InputStream is = getConn.getInputStream()) {
            fetched = mapper.readValue(is, new TypeReference<List<Task>>() {}).get(0);
        }
        HttpURLConnection delConn = (HttpURLConnection) new URL(baseUrl("/api/tasks/" + fetched.getId())).openConnection();
        delConn.setRequestMethod("DELETE");
        assertEquals(204, delConn.getResponseCode());
    }
}
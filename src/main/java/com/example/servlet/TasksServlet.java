package com.example.servlet;

import com.example.dao.TaskDAO;
import com.example.model.Task;
import com.google.gson.Gson;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * REST API Servlet for Task CRUD operations.
 * Mapped to /api/tasks/*
 */
@WebServlet("/api/tasks/*")
public class TasksServlet extends HttpServlet {
    private final TaskDAO taskDAO = new TaskDAO();
    private final Gson gson = new Gson();

    /**
     * Handle GET requests.
     * GET /api/tasks - returns all tasks
     * GET /api/tasks/{id} - returns a single task by ID
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        setJsonResponse(response);
        PrintWriter out = response.getWriter();

        Integer taskId = extractIdFromPath(request);

        if (taskId == null) {
            // GET all tasks
            List<Task> tasks = taskDAO.getAllTasks();
            out.print(gson.toJson(tasks));
        } else {
            // GET single task by ID
            Task task = taskDAO.getTaskById(taskId);
            if (task != null) {
                out.print(gson.toJson(task));
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print("{\"error\":\"Task not found\"}");
            }
        }
    }

    /**
     * Handle POST requests.
     * POST /api/tasks - creates a new task
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        setJsonResponse(response);
        PrintWriter out = response.getWriter();

        String body = readRequestBody(request);
        Task task = gson.fromJson(body, Task.class);

        if (task == null || task.getTitle() == null || task.getTitle().trim().isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\":\"Title is required\"}");
            return;
        }

        Task createdTask = taskDAO.createTask(task);
        response.setStatus(HttpServletResponse.SC_CREATED);
        out.print(gson.toJson(createdTask));
    }

    /**
     * Handle PUT requests.
     * PUT /api/tasks/{id} - updates an existing task
     */
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
        setJsonResponse(response);
        PrintWriter out = response.getWriter();

        Integer taskId = extractIdFromPath(request);
        if (taskId == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\":\"Task ID is required in path\"}");
            return;
        }

        String body = readRequestBody(request);
        Task task = gson.fromJson(body, Task.class);

        if (task == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\":\"Invalid request body\"}");
            return;
        }

        task.setId(taskId);
        boolean updated = taskDAO.updateTask(task);

        if (updated) {
            Task updatedTask = taskDAO.getTaskById(taskId);
            out.print(gson.toJson(updatedTask));
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            out.print("{\"error\":\"Task not found\"}");
        }
    }

    /**
     * Handle DELETE requests.
     * DELETE /api/tasks/{id} - deletes a task
     */
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        setJsonResponse(response);
        PrintWriter out = response.getWriter();

        Integer taskId = extractIdFromPath(request);
        if (taskId == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\":\"Task ID is required in path\"}");
            return;
        }

        boolean deleted = taskDAO.deleteTask(taskId);

        if (deleted) {
            out.print("{\"message\":\"Task deleted successfully\"}");
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            out.print("{\"error\":\"Task not found\"}");
        }
    }

    /**
     * Extract task ID from the request path.
     *
     * @param request the HTTP request
     * @return the task ID, or null if not present or invalid
     */
    private Integer extractIdFromPath(HttpServletRequest request) {
        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            return null;
        }

        String idString = pathInfo.substring(1); // Remove leading "/"
        try {
            return Integer.parseInt(idString);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Read the request body as a string.
     *
     * @param request the HTTP request
     * @return the request body
     * @throws IOException if reading fails
     */
    private String readRequestBody(HttpServletRequest request) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        return sb.toString();
    }

    /**
     * Set the response content type to JSON.
     *
     * @param response the HTTP response
     */
    private void setJsonResponse(HttpServletResponse response) {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
    }
}

package com.example.dao;

import com.example.model.Task;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Task CRUD operations using SQLite.
 */
public class TaskDAO {
    private static final String DB_URL = "jdbc:sqlite:todo.db";

    static {
        try {
            Class.forName("org.sqlite.JDBC");
            initializeDatabase();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("SQLite JDBC driver not found", e);
        }
    }

    /**
     * Initialize the database and create the tasks table if it doesn't exist.
     */
    private static void initializeDatabase() {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS tasks ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "title TEXT NOT NULL, "
                + "completed INTEGER DEFAULT 0)";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            stmt.execute(createTableSQL);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize database", e);
        }
    }

    /**
     * Get a database connection.
     *
     * @return a new database connection
     * @throws SQLException if connection fails
     */
    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    /**
     * Retrieve all tasks from the database.
     *
     * @return list of all tasks
     */
    public List<Task> getAllTasks() {
        List<Task> tasks = new ArrayList<>();
        String sql = "SELECT id, title, completed FROM tasks";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Task task = new Task();
                task.setId(rs.getInt("id"));
                task.setTitle(rs.getString("title"));
                task.setCompleted(rs.getInt("completed") == 1);
                tasks.add(task);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to retrieve tasks", e);
        }
        return tasks;
    }

    /**
     * Retrieve a single task by its ID.
     *
     * @param id the task ID
     * @return the task, or null if not found
     */
    public Task getTaskById(int id) {
        String sql = "SELECT id, title, completed FROM tasks WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Task task = new Task();
                    task.setId(rs.getInt("id"));
                    task.setTitle(rs.getString("title"));
                    task.setCompleted(rs.getInt("completed") == 1);
                    return task;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to retrieve task by ID", e);
        }
        return null;
    }

    /**
     * Create a new task in the database.
     *
     * @param task the task to create (id will be set after creation)
     * @return the created task with generated ID
     */
    public Task createTask(Task task) {
        String sql = "INSERT INTO tasks (title, completed) VALUES (?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, task.getTitle());
            pstmt.setInt(2, task.isCompleted() ? 1 : 0);
            pstmt.executeUpdate();

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    task.setId(generatedKeys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create task", e);
        }
        return task;
    }

    /**
     * Update an existing task in the database.
     *
     * @param task the task to update
     * @return true if the task was updated, false if not found
     */
    public boolean updateTask(Task task) {
        String sql = "UPDATE tasks SET title = ?, completed = ? WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, task.getTitle());
            pstmt.setInt(2, task.isCompleted() ? 1 : 0);
            pstmt.setInt(3, task.getId());

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update task", e);
        }
    }

    /**
     * Delete a task from the database.
     *
     * @param id the ID of the task to delete
     * @return true if the task was deleted, false if not found
     */
    public boolean deleteTask(int id) {
        String sql = "DELETE FROM tasks WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete task", e);
        }
    }
}

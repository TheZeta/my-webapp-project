package com.example.model;

/**
 * Task model representing a TODO item.
 */
public class Task {
    private int id;
    private String title;
    private boolean completed;

    /**
     * Default no-arg constructor.
     */
    public Task() {
    }

    /**
     * Parameterized constructor.
     *
     * @param id        the task id
     * @param title     the task title
     * @param completed the completion status
     */
    public Task(int id, String title, boolean completed) {
        this.id = id;
        this.title = title;
        this.completed = completed;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
}

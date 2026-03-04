<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>TODO List App</title>
    <!-- Bootstrap CSS -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <!-- Bootstrap Icons -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.1/font/bootstrap-icons.css" rel="stylesheet">
    <style>
        body {
            background-color: #f5f5f5;
            min-height: 100vh;
        }
        .todo-container {
            max-width: 600px;
            margin: 50px auto;
        }
        .task-item {
            display: flex;
            align-items: center;
            padding: 12px 15px;
            background: white;
            border-radius: 8px;
            margin-bottom: 10px;
            box-shadow: 0 2px 4px rgba(0,0,0,0.1);
            transition: all 0.2s;
        }
        .task-item:hover {
            box-shadow: 0 4px 8px rgba(0,0,0,0.15);
        }
        .task-item.completed .task-title {
            text-decoration: line-through;
            color: #888;
        }
        .task-title {
            flex-grow: 1;
            margin: 0 15px;
            font-size: 1.1rem;
        }
        .task-title-input {
            flex-grow: 1;
            margin: 0 15px;
            border: 1px solid #dee2e6;
            border-radius: 4px;
            padding: 5px 10px;
            font-size: 1.1rem;
        }
        .task-checkbox {
            width: 20px;
            height: 20px;
            cursor: pointer;
        }
        .btn-icon {
            border: none;
            background: transparent;
            padding: 5px 8px;
            cursor: pointer;
            font-size: 1.1rem;
            transition: color 0.2s;
        }
        .btn-icon:hover {
            color: #0d6efd;
        }
        .btn-icon.delete:hover {
            color: #dc3545;
        }
        .add-task-form {
            display: flex;
            gap: 10px;
            margin-bottom: 30px;
        }
        .add-task-input {
            flex-grow: 1;
        }
        .empty-state {
            text-align: center;
            padding: 40px;
            color: #888;
        }
        .empty-state i {
            font-size: 3rem;
            margin-bottom: 15px;
        }
    </style>
</head>
<body>
    <div class="container todo-container">
        <h1 class="text-center mb-4">
            <i class="bi bi-check2-square"></i> TODO List
        </h1>

        <!-- Add Task Form -->
        <form class="add-task-form" id="addTaskForm">
            <input type="text" class="form-control add-task-input" id="taskInput" 
                   placeholder="Enter a new task..." required>
            <button type="submit" class="btn btn-primary">
                <i class="bi bi-plus-lg"></i> Add
            </button>
        </form>

        <!-- Tasks List -->
        <div id="tasksList">
            <!-- Tasks will be loaded here -->
        </div>

        <!-- Empty State -->
        <div id="emptyState" class="empty-state" style="display: none;">
            <i class="bi bi-inbox"></i>
            <p>No tasks yet. Add one above!</p>
        </div>
    </div>

    <!-- Bootstrap JS -->
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
    
    <script>
        const API_URL = '${pageContext.request.contextPath}/api/tasks';
        const tasksList = document.getElementById('tasksList');
        const emptyState = document.getElementById('emptyState');
        const addTaskForm = document.getElementById('addTaskForm');
        const taskInput = document.getElementById('taskInput');

        // Load tasks on page load
        document.addEventListener('DOMContentLoaded', loadTasks);

        // Add task form submission
        addTaskForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const title = taskInput.value.trim();
            if (!title) return;

            try {
                const response = await fetch(API_URL, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ title: title, completed: false })
                });
                
                if (response.ok) {
                    taskInput.value = '';
                    loadTasks();
                } else {
                    alert('Failed to add task');
                }
            } catch (error) {
                console.error('Error adding task:', error);
                alert('Error adding task');
            }
        });

        // Load all tasks from API
        async function loadTasks() {
            try {
                const response = await fetch(API_URL);
                const tasks = await response.json();
                renderTasks(tasks);
            } catch (error) {
                console.error('Error loading tasks:', error);
                tasksList.innerHTML = '<p class="text-danger">Error loading tasks</p>';
            }
        }

        // Render tasks to the DOM
        function renderTasks(tasks) {
            if (tasks.length === 0) {
                tasksList.innerHTML = '';
                emptyState.style.display = 'block';
                return;
            }

            emptyState.style.display = 'none';
            tasksList.innerHTML = tasks.map(task => createTaskHTML(task)).join('');
        }

        // Create HTML for a single task
        function createTaskHTML(task) {
            const completedClass = task.completed ? 'completed' : '';
            const checkedAttr = task.completed ? 'checked' : '';
            
            return `
                <div class="task-item \${completedClass}" data-id="\${task.id}">
                    <input type="checkbox" class="task-checkbox" \${checkedAttr} 
                           onchange="toggleComplete(\${task.id}, this.checked)">
                    <span class="task-title">\${escapeHtml(task.title)}</span>
                    <button class="btn-icon edit" onclick="editTask(\${task.id})" title="Edit">
                        <i class="bi bi-pencil"></i>
                    </button>
                    <button class="btn-icon delete" onclick="deleteTask(\${task.id})" title="Delete">
                        <i class="bi bi-trash"></i>
                    </button>
                </div>
            `;
        }

        // Toggle task completion status
        async function toggleComplete(id, completed) {
            const taskItem = document.querySelector(`.task-item[data-id="\${id}"]`);
            const title = taskItem.querySelector('.task-title').textContent;

            try {
                const response = await fetch(`\${API_URL}/\${id}`, {
                    method: 'PUT',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ title: title, completed: completed })
                });

                if (response.ok) {
                    loadTasks();
                } else {
                    alert('Failed to update task');
                    loadTasks();
                }
            } catch (error) {
                console.error('Error updating task:', error);
                alert('Error updating task');
                loadTasks();
            }
        }

        // Edit task - replace title with input field
        function editTask(id) {
            const taskItem = document.querySelector(`.task-item[data-id="\${id}"]`);
            const titleSpan = taskItem.querySelector('.task-title');
            const currentTitle = titleSpan.textContent;
            const isCompleted = taskItem.classList.contains('completed');

            // Replace title span with input
            const input = document.createElement('input');
            input.type = 'text';
            input.className = 'task-title-input';
            input.value = currentTitle;
            
            titleSpan.replaceWith(input);
            input.focus();
            input.select();

            // Change edit button to save button
            const editBtn = taskItem.querySelector('.btn-icon.edit');
            editBtn.innerHTML = '<i class="bi bi-check-lg"></i>';
            editBtn.title = 'Save';
            editBtn.onclick = () => saveTask(id, input.value, isCompleted);

            // Save on Enter, cancel on Escape
            input.addEventListener('keydown', (e) => {
                if (e.key === 'Enter') {
                    saveTask(id, input.value, isCompleted);
                } else if (e.key === 'Escape') {
                    loadTasks();
                }
            });

            // Save on blur
            input.addEventListener('blur', (e) => {
                // Small delay to allow button click to register
                setTimeout(() => {
                    if (document.querySelector(`.task-item[data-id="\${id}"] .task-title-input`)) {
                        saveTask(id, input.value, isCompleted);
                    }
                }, 150);
            });
        }

        // Save edited task
        async function saveTask(id, newTitle, completed) {
            const title = newTitle.trim();
            if (!title) {
                alert('Task title cannot be empty');
                loadTasks();
                return;
            }

            try {
                const response = await fetch(`\${API_URL}/\${id}`, {
                    method: 'PUT',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ title: title, completed: completed })
                });

                if (response.ok) {
                    loadTasks();
                } else {
                    alert('Failed to save task');
                    loadTasks();
                }
            } catch (error) {
                console.error('Error saving task:', error);
                alert('Error saving task');
                loadTasks();
            }
        }

        // Delete task
        async function deleteTask(id) {
            if (!confirm('Are you sure you want to delete this task?')) return;

            try {
                const response = await fetch(`\${API_URL}/\${id}`, {
                    method: 'DELETE'
                });

                if (response.ok) {
                    loadTasks();
                } else {
                    alert('Failed to delete task');
                }
            } catch (error) {
                console.error('Error deleting task:', error);
                alert('Error deleting task');
            }
        }

        // Escape HTML to prevent XSS
        function escapeHtml(text) {
            const div = document.createElement('div');
            div.textContent = text;
            return div.innerHTML;
        }
    </script>
</body>
</html>

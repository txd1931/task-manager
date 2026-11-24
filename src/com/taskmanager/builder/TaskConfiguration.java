package com.taskmanager.builder;

import java.time.LocalDate;

import com.taskmanager.model.TaskExecutionMode;
import com.taskmanager.model.TaskPriority;

public final class TaskConfiguration {

    private final String title;
    private final String description;
    private final TaskPriority priority;
    private final LocalDate dueDate;
    private final TaskExecutionMode executionMode;

    private TaskConfiguration(Builder builder) {
        this.title = builder.title;
        this.description = builder.description;
        this.priority = builder.priority;
        this.dueDate = builder.dueDate;
        this.executionMode = builder.executionMode;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public TaskPriority getPriority() {
        return priority;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public TaskExecutionMode getExecutionMode() {
        return executionMode;
    }

    public static class Builder {

        private final String title;
        private String description = "";
        private TaskPriority priority = TaskPriority.MEDIUM;
        private LocalDate dueDate;
        private TaskExecutionMode executionMode = TaskExecutionMode.DIRECT;

        public Builder(String title) {
            if (title == null || title.isBlank()) {
                throw new IllegalArgumentException(
                        "O título da tarefa não pode ser vazio."
                );
            }

            this.title = title.trim();
        }

        public Builder description(String description) {
            this.description = description == null ? "" : description;
            return this;
        }

        public Builder priority(TaskPriority priority) {
            if (priority == null) {
                throw new IllegalArgumentException(
                        "A prioridade não pode ser nula."
                );
            }

            this.priority = priority;
            return this;
        }

        public Builder dueDate(LocalDate dueDate) {
            this.dueDate = dueDate;
            return this;
        }

        public Builder executionMode(TaskExecutionMode executionMode) {
            if (executionMode == null) {
                throw new IllegalArgumentException(
                        "O modo de execução não pode ser nulo."
                );
            }

            this.executionMode = executionMode;
            return this;
        }

        public TaskConfiguration build() {
            return new TaskConfiguration(this);
        }
    }
}

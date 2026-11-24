package com.taskmanager.observer;

import java.time.LocalDateTime;

public final class TaskEvent {

    private final TaskEventType type;
    private final long taskId;
    private final String description;
    private final LocalDateTime timestamp;

    public TaskEvent(
            TaskEventType type,
            long taskId,
            String description,
            LocalDateTime timestamp) {

        if (type == null || timestamp == null) {
            throw new IllegalArgumentException(
                    "O tipo e a data do evento são obrigatórios."
            );
        }

        if (taskId < 1) {
            throw new IllegalArgumentException(
                    "O identificador da tarefa deve ser positivo."
            );
        }

        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException(
                    "A descrição do evento não pode ser vazia."
            );
        }

        this.type = type;
        this.taskId = taskId;
        this.description = description;
        this.timestamp = timestamp;
    }

    public TaskEventType getType() {
        return type;
    }

    public long getTaskId() {
        return taskId;
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}

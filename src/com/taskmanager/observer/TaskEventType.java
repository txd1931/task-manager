package com.taskmanager.observer;

public enum TaskEventType {
    TASK_CREATED("Tarefa criada"),
    TASK_UPDATED("Tarefa atualizada"),
    TASK_STATE_CHANGED("Estado alterado"),
    TASK_DELETED("Tarefa excluída"),
    RECURRENCE_GENERATED("Ocorrência recorrente gerada"),
    RECURRENCE_ENDED("Recorrência encerrada");

    private final String displayName;

    TaskEventType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

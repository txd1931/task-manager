package com.taskmanager.model;

public enum TaskPriority {
    VERY_LOW(1, "Muito baixa"),
    LOW(2, "Baixa"),
    MEDIUM(3, "Média"),
    HIGH(4, "Alta"),
    VERY_HIGH(5, "Muito alta");

    private final int value;
    private final String displayName;

    TaskPriority(int value, String displayName) {
        this.value = value;
        this.displayName = displayName;
    }

    public int getValue() {
        return value;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static TaskPriority fromValue(int value) {
        for (TaskPriority priority : values()) {
            if (priority.value == value) {
                return priority;
            }
        }

        throw new IllegalArgumentException("Prioridade inválida: " + value);
    }
}
package com.taskmanager.query;

public enum TaskStatus {

    PENDING("Pendente"),
    IN_PROGRESS("Em andamento"),
    COMPLETED("Concluída");

    private final String displayName;

    TaskStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

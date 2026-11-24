package com.taskmanager.query;

public enum TaskSortField {

    CREATED_AT("Data de criação"),
    DUE_DATE("Prazo"),
    PRIORITY("Prioridade"),
    TITLE("Título");

    private final String displayName;

    TaskSortField(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

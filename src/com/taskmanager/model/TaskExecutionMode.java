package com.taskmanager.model;

public enum TaskExecutionMode {
    DIRECT("Conclusão direta"),
    TRACKED("Com acompanhamento");

    private final String displayName;

    TaskExecutionMode(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
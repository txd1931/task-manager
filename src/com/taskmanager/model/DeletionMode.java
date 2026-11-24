package com.taskmanager.model;

public enum DeletionMode {
    MAKE_ROOT("Tornar subtarefas independentes"),
    PROMOTE("Promover subtarefas"),
    CASCADE("Excluir todas as subtarefas");

    private final String displayName;

    DeletionMode(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

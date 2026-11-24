package com.taskmanager.model;

public enum RecurrenceType {
    DAILY("Diária"),
    WEEKLY("Semanal"),
    MONTHLY("Mensal");

    private final String displayName;

    RecurrenceType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

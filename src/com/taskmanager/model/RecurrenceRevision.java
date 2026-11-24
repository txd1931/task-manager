package com.taskmanager.model;

import java.time.LocalDate;

public final class RecurrenceRevision {

    private final LocalDate effectiveDate;
    private final String title;
    private final String description;
    private final TaskPriority priority;
    private final long dueDateOffset;

    public RecurrenceRevision(
            LocalDate effectiveDate,
            String title,
            String description,
            TaskPriority priority,
            long dueDateOffset) {

        if (effectiveDate == null) {
            throw new IllegalArgumentException(
                    "A data de vigência é obrigatória."
            );
        }

        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException(
                    "O título não pode ser vazio."
            );
        }

        if (priority == null) {
            throw new IllegalArgumentException(
                    "A prioridade não pode ser nula."
            );
        }

        this.effectiveDate = effectiveDate;
        this.title = title.trim();
        this.description = description == null ? "" : description;
        this.priority = priority;
        this.dueDateOffset = dueDateOffset;
    }

    public LocalDate getEffectiveDate() {
        return effectiveDate;
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

    public long getDueDateOffset() {
        return dueDateOffset;
    }

    public LocalDate getDueDateFor(LocalDate scheduledDate) {
        return scheduledDate.plusDays(dueDateOffset);
    }
}

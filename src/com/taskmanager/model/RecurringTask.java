
package com.taskmanager.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

public class RecurringTask extends Task {

    private final long recurrenceId;
    private final LocalDate scheduledDate;

    public RecurringTask(
            long id,
            String title,
            String description,
            TaskPriority priority,
            LocalDate dueDate,
            LocalDateTime createdAt,
            TaskExecutionMode executionMode,
            long recurrenceId,
            LocalDate scheduledDate) {

        super(
                id,
                title,
                description,
                priority,
                dueDate,
                createdAt,
                executionMode
        );

        if (recurrenceId < 1) {
            throw new IllegalArgumentException("O ID da recorrência deve ser positivo.");
        }

        if (dueDate == null || scheduledDate == null) {
            throw new IllegalArgumentException(
                    "Uma tarefa recorrente precisa de prazo e data programada."
            );
        }

        this.recurrenceId = recurrenceId;
        this.scheduledDate = scheduledDate;
    }

    public long getRecurrenceId() {
        return recurrenceId;
    }

    public LocalDate getScheduledDate() {
        return scheduledDate;
    }
	
	public void validateRevision(RecurrenceRevision revision) {
	    if (revision == null) {
	        throw new IllegalArgumentException(
	                "A revisão não pode ser nula."
	        );
	    }
	
	    if (!isRoot()) {
	        throw new IllegalStateException(
	                "Uma tarefa recorrente precisa ser raiz."
	        );
	    }
	
	    LocalDate newDueDate = revision.getDueDateFor(scheduledDate);
	
	    long dayOffset = java.time.temporal.ChronoUnit.DAYS.between(
	            getDueDate(),
	            newDueDate
	    );
	
	    prepareDeadlineShift(dayOffset);
	}
	
	public void applyRevision(RecurrenceRevision revision) {
	    validateRevision(revision);
	
	    LocalDate newDueDate = revision.getDueDateFor(scheduledDate);
	
	    long dayOffset = java.time.temporal.ChronoUnit.DAYS.between(
	            getDueDate(),
	            newDueDate
	    );
	
	    Map<Task, LocalDate> deadlines =
	            prepareDeadlineShift(dayOffset);
	
	    applyPreparedDeadlines(deadlines);
	    applyRevisionAttributes(revision);
	}

}

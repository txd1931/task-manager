package com.taskmanager.factory;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.taskmanager.builder.TaskConfiguration;
import com.taskmanager.model.RecurringTask;
import com.taskmanager.model.Task;

public class RecurringTaskCreator extends TaskCreator {

    private final long recurrenceId;
    private final LocalDate scheduledDate;

    public RecurringTaskCreator(
            long recurrenceId,
            LocalDate scheduledDate) {

        if (recurrenceId < 1) {
            throw new IllegalArgumentException(
                    "O identificador da recorrência deve ser positivo."
            );
        }

        if (scheduledDate == null) {
            throw new IllegalArgumentException(
                    "A data programada é obrigatória."
            );
        }

        this.recurrenceId = recurrenceId;
        this.scheduledDate = scheduledDate;
    }

    @Override
    protected Task instantiateTask(
            long id,
            TaskConfiguration configuration,
            LocalDateTime createdAt) {

        if (configuration.getDueDate() == null) {
            throw new IllegalArgumentException(
                    "Uma tarefa recorrente precisa de um prazo."
            );
        }

        return new RecurringTask(
                id,
                configuration.getTitle(),
                configuration.getDescription(),
                configuration.getPriority(),
                configuration.getDueDate(),
                createdAt,
                configuration.getExecutionMode(),
                recurrenceId,
                scheduledDate
        );
    }
}

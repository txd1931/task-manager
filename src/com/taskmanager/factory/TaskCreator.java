package com.taskmanager.factory;

import java.time.LocalDateTime;

import com.taskmanager.builder.TaskConfiguration;
import com.taskmanager.model.Task;

public abstract class TaskCreator {

    public final Task createTask(
            long id,
            TaskConfiguration configuration,
            LocalDateTime createdAt) {

        if (id < 1) {
            throw new IllegalArgumentException(
                    "O identificador da tarefa deve ser positivo."
            );
        }

        if (configuration == null || createdAt == null) {
            throw new IllegalArgumentException(
                    "A configuração e a data de criação são obrigatórias."
            );
        }

        return instantiateTask(id, configuration, createdAt);
    }

    protected abstract Task instantiateTask(
            long id,
            TaskConfiguration configuration,
            LocalDateTime createdAt);
}

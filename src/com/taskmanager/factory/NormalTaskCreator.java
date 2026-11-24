package com.taskmanager.factory;

import java.time.LocalDateTime;

import com.taskmanager.builder.TaskConfiguration;
import com.taskmanager.model.NormalTask;
import com.taskmanager.model.Task;

public class NormalTaskCreator extends TaskCreator {

    @Override
    protected Task instantiateTask(
            long id,
            TaskConfiguration configuration,
            LocalDateTime createdAt) {

        return new NormalTask(
                id,
                configuration.getTitle(),
                configuration.getDescription(),
                configuration.getPriority(),
                configuration.getDueDate(),
                createdAt,
                configuration.getExecutionMode()
        );
    }
}


package com.taskmanager.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class NormalTask extends Task {

    public NormalTask(
            long id,
            String title,
            String description,
            TaskPriority priority,
            LocalDate dueDate,
            LocalDateTime createdAt,
            TaskExecutionMode executionMode) {

        super(
                id,
                title,
                description,
                priority,
                dueDate,
                createdAt,
                executionMode
        );
    }
}

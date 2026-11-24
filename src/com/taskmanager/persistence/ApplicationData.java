package com.taskmanager.persistence;

import com.taskmanager.observer.HistoryObserver;
import com.taskmanager.query.SavedTaskQueryService;
import com.taskmanager.service.RecurrenceService;
import com.taskmanager.service.TaskService;
import com.taskmanager.util.TaskIdGenerator;

public final class ApplicationData {

    private final TaskService taskService;
    private final RecurrenceService recurrenceService;
    private final SavedTaskQueryService savedQueryService;
    private final HistoryObserver historyObserver;
    private final TaskIdGenerator taskIds;
    private final TaskIdGenerator recurrenceIds;

    public ApplicationData(
            TaskService taskService,
            RecurrenceService recurrenceService,
            SavedTaskQueryService savedQueryService,
            HistoryObserver historyObserver,
            TaskIdGenerator taskIds,
            TaskIdGenerator recurrenceIds) {

        if (taskService == null
                || recurrenceService == null
                || savedQueryService == null
                || historyObserver == null
                || taskIds == null
                || recurrenceIds == null) {
            throw new IllegalArgumentException(
                    "Os componentes da aplicação são obrigatórios."
            );
        }

        this.taskService = taskService;
        this.recurrenceService = recurrenceService;
        this.savedQueryService = savedQueryService;
        this.historyObserver = historyObserver;
        this.taskIds = taskIds;
        this.recurrenceIds = recurrenceIds;
    }

    public TaskService getTaskService() {
        return taskService;
    }

    public RecurrenceService getRecurrenceService() {
        return recurrenceService;
    }

    public SavedTaskQueryService getSavedQueryService() {
        return savedQueryService;
    }

    public HistoryObserver getHistoryObserver() {
        return historyObserver;
    }

    public TaskIdGenerator getTaskIds() {
        return taskIds;
    }

    public TaskIdGenerator getRecurrenceIds() {
        return recurrenceIds;
    }
}

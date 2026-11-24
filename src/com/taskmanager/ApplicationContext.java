package com.taskmanager;

import com.taskmanager.observer.ConsoleObserver;
import com.taskmanager.observer.TaskEventPublisher;
import com.taskmanager.persistence.ApplicationData;
import com.taskmanager.service.TaskManagerFacade;
import com.taskmanager.persistence.ApplicationPersistence;
import com.taskmanager.persistence.AutoSaveManager;

public final class ApplicationContext {

    private final ApplicationData data;
    private final TaskEventPublisher publisher;
    private final TaskManagerFacade facade;

    public ApplicationContext(ApplicationData data) {
        if (data == null) {
            throw new IllegalArgumentException(
                    "Os dados da aplicação são obrigatórios."
            );
        }

        this.data = data;
        this.publisher = new TaskEventPublisher();

        publisher.addObserver(new ConsoleObserver());
        publisher.addObserver(data.getHistoryObserver());

        this.facade = new TaskManagerFacade(
                data.getTaskService(),
                data.getRecurrenceService(),
                data.getTaskIds(),
                publisher,
                data.getSavedQueryService()
        );
    }

	public ApplicationContext(
	        ApplicationData data,
	        ApplicationPersistence persistence) {
	
	    this(data);
	
	    AutoSaveManager autoSaveManager =
	            new AutoSaveManager(persistence, data);
	
	    facade.setMutationListener(
	            autoSaveManager::saveChanges
	    );
	}


    public ApplicationData getData() {
        return data;
    }

    public TaskManagerFacade getFacade() {
        return facade;
    }
}

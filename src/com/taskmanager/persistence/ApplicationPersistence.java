package com.taskmanager.persistence;

import java.io.IOException;
import java.nio.file.Path;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import com.taskmanager.model.Recurrence;
import com.taskmanager.model.Task;
import com.taskmanager.observer.TaskEvent;
import com.taskmanager.query.SavedTaskQuery;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gson.JsonElement;

import com.taskmanager.observer.HistoryObserver;
import com.taskmanager.query.SavedTaskQueryService;
import com.taskmanager.service.RecurrenceService;
import com.taskmanager.service.TaskService;
import com.taskmanager.util.TaskIdGenerator;

public class ApplicationPersistence {

    private static final int FORMAT_VERSION = 1;

    private final JsonStorage storage;
    private final TaskJsonMapper taskMapper;
    private final RecurrenceJsonMapper recurrenceMapper;
    private final SavedTaskQueryJsonMapper queryMapper;
    private final TaskEventJsonMapper eventMapper;

    public ApplicationPersistence(Path filePath) {
        this.storage = new JsonStorage(filePath);
        this.taskMapper = new TaskJsonMapper();
        this.recurrenceMapper = new RecurrenceJsonMapper();
        this.queryMapper = new SavedTaskQueryJsonMapper();
        this.eventMapper = new TaskEventJsonMapper();
    }

    public void save(ApplicationData data) throws IOException {
        if (data == null) {
            throw new IllegalArgumentException(
                    "Os dados da aplicação são obrigatórios."
            );
        }

        JsonObject json = new JsonObject();

        json.addProperty("version", FORMAT_VERSION);

        json.addProperty(
                "nextTaskId",
                data.getTaskIds().getNextId()
        );

        json.addProperty(
                "nextRecurrenceId",
                data.getRecurrenceIds().getNextId()
        );

        json.addProperty(
                "nextSavedQueryId",
                data.getSavedQueryService().getNextId()
        );

        JsonArray tasks = new JsonArray();

        for (Task task : data.getTaskService().getRootTasks()) {
            tasks.add(taskMapper.toJson(task));
        }

        json.add("tasks", tasks);

        JsonArray recurrences = new JsonArray();

        for (Recurrence recurrence
                : data.getRecurrenceService().getRecurrences()) {

            recurrences.add(recurrenceMapper.toJson(recurrence));
        }

        json.add("recurrences", recurrences);

        JsonArray savedQueries = new JsonArray();

        for (SavedTaskQuery query
                : data.getSavedQueryService().getAll()) {

            savedQueries.add(queryMapper.toJson(query));
        }

        json.add("savedQueries", savedQueries);

        JsonArray events = new JsonArray();

        for (TaskEvent event
                : data.getHistoryObserver().getHistory()) {

            events.add(eventMapper.toJson(event));
        }

        json.add("events", events);

        storage.save(json);
    }

    public JsonObject readRawData() throws IOException {
        return storage.load();
    }

    public Path getFilePath() {
        return storage.getFilePath();
    }
    
	
	public ApplicationData load() throws IOException {
	    JsonObject json = storage.load();
	
	    if (json == null) {
	        return createEmptyData();
	    }
	
	    try {
	        int version = json.get("version").getAsInt();
	
	        if (version != FORMAT_VERSION) {
	            throw new IllegalArgumentException(
	                    "Versão do arquivo incompatível: " + version
	            );
	        }
	
	        long nextTaskId = json.get("nextTaskId").getAsLong();
	        long nextRecurrenceId =
	                json.get("nextRecurrenceId").getAsLong();
	        long nextSavedQueryId =
	                json.get("nextSavedQueryId").getAsLong();
	
	        validateNextId(nextTaskId, "tarefa");
	        validateNextId(nextRecurrenceId, "recorrência");
	        validateNextId(nextSavedQueryId, "consulta");
	
	        TaskIdGenerator taskIds =
	                new TaskIdGenerator(nextTaskId);
	
	        TaskIdGenerator recurrenceIds =
	                new TaskIdGenerator(nextRecurrenceId);
	
	        TaskService taskService = new TaskService();
	        Set<Long> taskIdsFound = new HashSet<>();
	
	        JsonArray tasks = requireArray(json, "tasks");
	
	        for (JsonElement element : tasks) {
	            Task task = taskMapper.fromJson(
	                    element.getAsJsonObject()
	            );
	
	            validateTaskIds(
	                    task,
	                    taskIdsFound,
	                    nextTaskId
	            );
	
	            taskService.addRootTask(task);
	        }
	
	        List<Recurrence> restoredRecurrences =
	                new ArrayList<>();
	
	        JsonArray recurrences = requireArray(
	                json,
	                "recurrences"
	        );
	
	        Set<Long> recurrenceIdsFound = new HashSet<>();
	
	        for (JsonElement element : recurrences) {
	            Recurrence recurrence = recurrenceMapper.fromJson(
	                    element.getAsJsonObject()
	            );
	
	            if (recurrence.getId() <= 0
	                    || recurrence.getId() >= nextRecurrenceId) {
	                throw new IllegalArgumentException(
	                        "ID de recorrência inválido."
	                );
	            }
	
	            if (!recurrenceIdsFound.add(recurrence.getId())) {
	                throw new IllegalArgumentException(
	                        "ID de recorrência duplicado."
	                );
	            }
	
	            restoredRecurrences.add(recurrence);
	        }
	
	        RecurrenceService recurrenceService =
	                new RecurrenceService(
	                        taskService,
	                        taskIds,
	                        recurrenceIds
	                );
	
	        recurrenceService.restoreRecurrences(
	                restoredRecurrences
	        );
	
	        List<SavedTaskQuery> restoredQueries =
	                new ArrayList<>();
	
	        JsonArray savedQueries = requireArray(
	                json,
	                "savedQueries"
	        );
	
	        for (JsonElement element : savedQueries) {
	            restoredQueries.add(
	                    queryMapper.fromJson(
	                            element.getAsJsonObject()
	                    )
	            );
	        }
	
	        SavedTaskQueryService savedQueryService =
	                new SavedTaskQueryService();
	
	        savedQueryService.restoreSavedQueries(
	                restoredQueries,
	                nextSavedQueryId
	        );
	
	        List<TaskEvent> restoredEvents = new ArrayList<>();
	
	        JsonArray events = requireArray(json, "events");
	
	        for (JsonElement element : events) {
	            restoredEvents.add(
	                    eventMapper.fromJson(
	                            element.getAsJsonObject()
	                    )
	            );
	        }
	
	        HistoryObserver history = new HistoryObserver();
	        history.restoreHistory(restoredEvents);
	
	        return new ApplicationData(
	                taskService,
	                recurrenceService,
	                savedQueryService,
	                history,
	                taskIds,
	                recurrenceIds
	        );
	
	    } catch (RuntimeException exception) {
	        throw new IOException(
	                "O arquivo contém dados inválidos ou inconsistentes.",
	                exception
	        );
	    }
	}
	
	
	private ApplicationData createEmptyData() {
	    TaskIdGenerator taskIds = new TaskIdGenerator(1);
	    TaskIdGenerator recurrenceIds = new TaskIdGenerator(1);
	
	    TaskService taskService = new TaskService();
	
	    RecurrenceService recurrenceService =
	            new RecurrenceService(
	                    taskService,
	                    taskIds,
	                    recurrenceIds
	            );
	
	    return new ApplicationData(
	            taskService,
	            recurrenceService,
	            new SavedTaskQueryService(),
	            new HistoryObserver(),
	            taskIds,
	            recurrenceIds
	    );
	}
	
	private JsonArray requireArray(
	        JsonObject json,
	        String fieldName) {
	
	    if (!json.has(fieldName)
	            || !json.get(fieldName).isJsonArray()) {
	        throw new IllegalArgumentException(
	                "Campo obrigatório ausente ou inválido: "
	                        + fieldName
	        );
	    }
	
	    return json.getAsJsonArray(fieldName);
	}
	
	private void validateNextId(
	        long id,
	        String type) {
	
	    if (id <= 0) {
	        throw new IllegalArgumentException(
	                "O próximo ID de " + type
	                        + " deve ser positivo."
	        );
	    }
	}
	
	private void validateTaskIds(
	        Task task,
	        Set<Long> found,
	        long nextTaskId) {
	
	    if (task.getId() <= 0
	            || task.getId() >= nextTaskId) {
	        throw new IllegalArgumentException(
	                "ID de tarefa inválido: " + task.getId()
	        );
	    }
	
	    if (!found.add(task.getId())) {
	        throw new IllegalArgumentException(
	                "ID de tarefa duplicado: " + task.getId()
	        );
	    }
	
	    for (Task child : task.getSubtasks()) {
	        validateTaskIds(
	                child,
	                found,
	                nextTaskId
	        );
	    }
	}

}

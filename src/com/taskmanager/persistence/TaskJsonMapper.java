package com.taskmanager.persistence;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import com.taskmanager.model.RecurringTask;
import com.taskmanager.model.Task;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.taskmanager.builder.TaskConfiguration;
import com.taskmanager.factory.NormalTaskCreator;
import com.taskmanager.model.TaskExecutionMode;
import com.taskmanager.model.TaskPriority;
import com.taskmanager.factory.RecurringTaskCreator;

public class TaskJsonMapper {

    public JsonObject toJson(Task task) {
        if (task == null) {
            throw new IllegalArgumentException(
                    "A tarefa não pode ser nula."
            );
        }

        JsonObject json = new JsonObject();

        json.addProperty("id", task.getId());
        json.addProperty("type",
                task instanceof RecurringTask ? "RECURRING" : "NORMAL");

        json.addProperty("title", task.getTitle());
        json.addProperty("description", task.getDescription());
        json.addProperty("priority", task.getPriority().name());

        if (task.getDueDate() != null) {
            json.addProperty("dueDate", task.getDueDate().toString());
        } else {
            json.add("dueDate", null);
        }

        json.addProperty("createdAt", task.getCreatedAt().toString());
        json.addProperty("executionMode", task.getExecutionMode().name());
        json.addProperty(
                "state",
                task.getState().getClass().getSimpleName()
        );

        if (task instanceof RecurringTask) {
            RecurringTask recurring = (RecurringTask) task;

            json.addProperty(
                    "recurrenceId",
                    recurring.getRecurrenceId()
            );

            json.addProperty(
                    "scheduledDate",
                    recurring.getScheduledDate().toString()
            );
        }

        JsonArray children = new JsonArray();

        for (Task child : task.getSubtasks()) {
            children.add(toJson(child));
        }

        json.add("subtasks", children);

        return json;
    }
    
	
	
	public Task fromJson(JsonObject json) {
	    if (json == null) {
	        throw new IllegalArgumentException(
	                "Os dados da tarefa não podem ser nulos."
	        );
	    }
	
	    String type = json.get("type").getAsString();
	
	    long id = json.get("id").getAsLong();
	
	    String title = json.get("title").getAsString();
	    String description = json.get("description").getAsString();
	
	    TaskPriority priority = TaskPriority.valueOf(
	            json.get("priority").getAsString()
	    );
	
	    LocalDate dueDate = null;
	
	    if (json.has("dueDate")
	            && !json.get("dueDate").isJsonNull()) {
	        dueDate = LocalDate.parse(
	                json.get("dueDate").getAsString()
	        );
	    }
	
	    LocalDateTime createdAt = LocalDateTime.parse(
	            json.get("createdAt").getAsString()
	    );
	
	    TaskExecutionMode executionMode = TaskExecutionMode.valueOf(
	            json.get("executionMode").getAsString()
	    );
	
	    String stateName = json.get("state").getAsString();
	
	    TaskConfiguration configuration =
	            new TaskConfiguration.Builder(title)
	                    .description(description)
	                    .priority(priority)
	                    .dueDate(dueDate)
	                    .executionMode(executionMode)
	                    .build();
	
	    Task task;
	
	    switch (type) {
	        case "NORMAL":
	            task = new NormalTaskCreator().createTask(
	                    id,
	                    configuration,
	                    createdAt
	            );
	            break;
	
	        case "RECURRING":
	            long recurrenceId = json.get("recurrenceId").getAsLong();
	
	            LocalDate scheduledDate = LocalDate.parse(
	                    json.get("scheduledDate").getAsString()
	            );
	
	            task = new RecurringTaskCreator(
	                    recurrenceId,
	                    scheduledDate
	            ).createTask(
	                    id,
	                    configuration,
	                    createdAt
	            );
	            break;
	
	        default:
	            throw new IllegalArgumentException(
	                    "Tipo de tarefa desconhecido: " + type
	            );
	    }
	
	    JsonArray children = json.getAsJsonArray("subtasks");
	
	    if (children != null) {
	        for (var element : children) {
	            Task child = fromJson(element.getAsJsonObject());
	            task.addSubtask(child);
	        }
	    }
	
	    task.restoreStateFromStorage(stateName);
	
	    return task;
	}

}

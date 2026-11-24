package com.taskmanager.persistence;

import java.time.LocalDateTime;

import com.google.gson.JsonObject;

import com.taskmanager.observer.TaskEvent;
import com.taskmanager.observer.TaskEventType;

public class TaskEventJsonMapper {

    public JsonObject toJson(TaskEvent event) {
        if (event == null) {
            throw new IllegalArgumentException(
                    "O evento não pode ser nulo."
            );
        }

        JsonObject json = new JsonObject();

        json.addProperty("type", event.getType().name());
        json.addProperty("taskId", event.getTaskId());
        json.addProperty("description", event.getDescription());
        json.addProperty("timestamp", event.getTimestamp().toString());

        return json;
    }

    public TaskEvent fromJson(JsonObject json) {
        if (json == null) {
            throw new IllegalArgumentException(
                    "Os dados do evento não podem ser nulos."
            );
        }

        TaskEventType type = TaskEventType.valueOf(
                json.get("type").getAsString()
        );

        long taskId = json.get("taskId").getAsLong();

        String description = json.get("description").getAsString();

        LocalDateTime timestamp = LocalDateTime.parse(
                json.get("timestamp").getAsString()
        );

        return new TaskEvent(
                type,
                taskId,
                description,
                timestamp
        );
    }
}

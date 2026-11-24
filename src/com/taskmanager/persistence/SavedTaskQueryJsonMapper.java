package com.taskmanager.persistence;

import java.time.LocalDate;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import com.taskmanager.model.TaskPriority;
import com.taskmanager.query.SavedTaskQuery;
import com.taskmanager.query.TaskQuery;
import com.taskmanager.query.TaskSortField;
import com.taskmanager.query.TaskStatus;

public class SavedTaskQueryJsonMapper {

    public JsonObject toJson(SavedTaskQuery saved) {
        if (saved == null) {
            throw new IllegalArgumentException(
                    "A consulta salva não pode ser nula."
            );
        }

        TaskQuery query = saved.getQuery();
        JsonObject json = new JsonObject();

        json.addProperty("id", saved.getId());
        json.addProperty("name", saved.getName());

        JsonArray statuses = new JsonArray();

        for (TaskStatus status : query.getStatuses()) {
            statuses.add(status.name());
        }

        json.add("statuses", statuses);

        JsonArray priorities = new JsonArray();

        for (TaskPriority priority : query.getPriorities()) {
            priorities.add(priority.name());
        }

        json.add("priorities", priorities);

        if (query.getDueDateFrom() != null) {
            json.addProperty(
                    "dueDateFrom",
                    query.getDueDateFrom().toString()
            );
        } else {
            json.add("dueDateFrom", null);
        }

        if (query.getDueDateTo() != null) {
            json.addProperty(
                    "dueDateTo",
                    query.getDueDateTo().toString()
            );
        } else {
            json.add("dueDateTo", null);
        }

        json.addProperty(
                "sortField",
                query.getSortField().name()
        );

        json.addProperty("ascending", query.isAscending());

        return json;
    }

    public SavedTaskQuery fromJson(JsonObject json) {
        if (json == null) {
            throw new IllegalArgumentException(
                    "Os dados da consulta não podem ser nulos."
            );
        }

        long id = json.get("id").getAsLong();
        String name = json.get("name").getAsString();

        TaskQuery query = new TaskQuery();

        JsonArray statuses = json.getAsJsonArray("statuses");
        JsonArray priorities = json.getAsJsonArray("priorities");

        if (statuses == null || priorities == null) {
            throw new IllegalArgumentException(
                    "Os filtros da consulta estão incompletos."
            );
        }

        for (JsonElement element : statuses) {
            query.addStatus(
                    TaskStatus.valueOf(element.getAsString())
            );
        }

        for (JsonElement element : priorities) {
            query.addPriority(
                    TaskPriority.valueOf(element.getAsString())
            );
        }

        LocalDate from = null;
        LocalDate to = null;

        if (json.has("dueDateFrom")
                && !json.get("dueDateFrom").isJsonNull()) {
            from = LocalDate.parse(
                    json.get("dueDateFrom").getAsString()
            );
        }

        if (json.has("dueDateTo")
                && !json.get("dueDateTo").isJsonNull()) {
            to = LocalDate.parse(
                    json.get("dueDateTo").getAsString()
            );
        }

        query.setDueDateRange(from, to);

        query.setSorting(
                TaskSortField.valueOf(
                        json.get("sortField").getAsString()
                ),
                json.get("ascending").getAsBoolean()
        );

        return new SavedTaskQuery(id, name, query);
    }
}

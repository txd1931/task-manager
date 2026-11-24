package com.taskmanager.persistence;

import java.time.LocalDate;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import com.taskmanager.model.Recurrence;
import com.taskmanager.model.RecurrenceRevision;
import com.taskmanager.model.RecurrenceType;
import com.taskmanager.model.TaskPriority;

public class RecurrenceJsonMapper {

    public JsonObject toJson(Recurrence recurrence) {
        if (recurrence == null) {
            throw new IllegalArgumentException(
                    "A recorrência não pode ser nula."
            );
        }

        JsonObject json = new JsonObject();

        json.addProperty("id", recurrence.getId());
        json.addProperty("type", recurrence.getType().name());
        json.addProperty("interval", recurrence.getInterval());
        json.addProperty("startDate",
                recurrence.getStartDate().toString());
        json.addProperty("active", recurrence.isActive());

        JsonArray revisions = new JsonArray();

        for (RecurrenceRevision revision : recurrence.getRevisions()) {
            JsonObject item = new JsonObject();

            item.addProperty(
                    "effectiveDate",
                    revision.getEffectiveDate().toString()
            );
            item.addProperty("title", revision.getTitle());
            item.addProperty("description", revision.getDescription());
            item.addProperty("priority", revision.getPriority().name());
            item.addProperty("dueDateOffset",
                    revision.getDueDateOffset());

            revisions.add(item);
        }

        json.add("revisions", revisions);

        JsonArray generatedDates = new JsonArray();

        for (LocalDate date : recurrence.getGeneratedDates()) {
            generatedDates.add(date.toString());
        }

        json.add("generatedDates", generatedDates);

        JsonArray processedIds = new JsonArray();

        for (Long id : recurrence.getProcessedOccurrenceIds()) {
            processedIds.add(id);
        }

        json.add("processedOccurrenceIds", processedIds);

        return json;
    }

    public Recurrence fromJson(JsonObject json) {
        if (json == null) {
            throw new IllegalArgumentException(
                    "Os dados da recorrência não podem ser nulos."
            );
        }

        long id = json.get("id").getAsLong();

        RecurrenceType type = RecurrenceType.valueOf(
                json.get("type").getAsString()
        );

        int interval = json.get("interval").getAsInt();

        LocalDate startDate = LocalDate.parse(
                json.get("startDate").getAsString()
        );

        Recurrence recurrence = new Recurrence(
                id,
                type,
                interval,
                startDate
        );

        JsonArray revisions = json.getAsJsonArray("revisions");

        if (revisions == null || revisions.isEmpty()) {
            throw new IllegalArgumentException(
                    "A recorrência não possui uma configuração inicial."
            );
        }

        for (JsonElement element : revisions) {
            JsonObject item = element.getAsJsonObject();

            RecurrenceRevision revision = new RecurrenceRevision(
                    LocalDate.parse(
                            item.get("effectiveDate").getAsString()
                    ),
                    item.get("title").getAsString(),
                    item.get("description").getAsString(),
                    TaskPriority.valueOf(
                            item.get("priority").getAsString()
                    ),
                    item.get("dueDateOffset").getAsLong()
            );

            recurrence.addRevision(revision);
        }

        recurrence.getRevisionFor(startDate);

        JsonArray generatedDates = json.getAsJsonArray("generatedDates");

        if (generatedDates == null) {
            throw new IllegalArgumentException(
                    "O histórico de datas geradas está ausente."
            );
        }

        for (JsonElement element : generatedDates) {
            recurrence.registerGeneratedDate(
                    LocalDate.parse(element.getAsString())
            );
        }

        JsonArray processedIds =
                json.getAsJsonArray("processedOccurrenceIds");

        if (processedIds == null) {
            throw new IllegalArgumentException(
                    "O histórico de ocorrências processadas está ausente."
            );
        }

        for (JsonElement element : processedIds) {
            recurrence.registerGeneratedSuccessor(
                    element.getAsLong()
            );
        }

        if (!json.get("active").getAsBoolean()) {
            recurrence.end();
        }

        return recurrence;
    }
}

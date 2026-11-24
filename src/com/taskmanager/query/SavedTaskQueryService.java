package com.taskmanager.query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SavedTaskQueryService {

    private final Map<Long, SavedTaskQuery> queries;
    private long nextId;

    public SavedTaskQueryService() {
        this.queries = new LinkedHashMap<>();
        this.nextId = 1;
    }

    public SavedTaskQuery save(String name, TaskQuery query) {
        validateUniqueName(name, null);

        SavedTaskQuery saved = new SavedTaskQuery(
                nextId,
                name,
                query
        );

        queries.put(nextId, saved);
        nextId++;

        return saved;
    }

    public void update(
            long id,
            String name,
            TaskQuery query) {

        SavedTaskQuery saved = requireQuery(id);

        validateUniqueName(name, id);

        saved.update(name, query);
    }

    public void delete(long id) {
        requireQuery(id);
        queries.remove(id);
    }

    public SavedTaskQuery findById(long id) {
        return queries.get(id);
    }

    public List<SavedTaskQuery> getAll() {
        return Collections.unmodifiableList(
                new ArrayList<>(queries.values())
        );
    }

    private SavedTaskQuery requireQuery(long id) {
        SavedTaskQuery saved = queries.get(id);

        if (saved == null) {
            throw new IllegalArgumentException(
                    "Consulta salva não encontrada: " + id
            );
        }

        return saved;
    }

    private void validateUniqueName(
            String name,
            Long ignoredId) {

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "O nome da consulta não pode ser vazio."
            );
        }

        String normalized = name.trim();

        for (SavedTaskQuery saved : queries.values()) {
            if (ignoredId != null
                    && saved.getId() == ignoredId) {
                continue;
            }

            if (saved.getName().equalsIgnoreCase(normalized)) {
                throw new IllegalArgumentException(
                        "Já existe uma consulta salva com esse nome."
                );
            }
        }
    }
	
	public long getNextId() {
	    return nextId;
	}
	
	public void restoreSavedQueries(
	        List<SavedTaskQuery> restored,
	        long restoredNextId) {
	
	    if (restored == null) {
	        throw new IllegalArgumentException(
	                "As consultas recuperadas não podem ser nulas."
	        );
	    }
	
	    if (!queries.isEmpty()) {
	        throw new IllegalStateException(
	                "O serviço já possui consultas cadastradas."
	        );
	    }
	
	    if (restoredNextId <= 0) {
	        throw new IllegalArgumentException(
	                "O próximo identificador é inválido."
	        );
	    }
	
	    Map<Long, SavedTaskQuery> validated = new LinkedHashMap<>();
	
	    for (SavedTaskQuery saved : restored) {
	        if (saved == null) {
	            throw new IllegalArgumentException(
	                    "Uma consulta recuperada é nula."
	            );
	        }
	
	        if (saved.getId() >= restoredNextId) {
	            throw new IllegalArgumentException(
	                    "O próximo ID deve ser maior que os IDs existentes."
	            );
	        }
	
	        if (validated.containsKey(saved.getId())) {
	            throw new IllegalArgumentException(
	                    "Existem consultas com IDs duplicados."
	            );
	        }
	
	        for (SavedTaskQuery existing : validated.values()) {
	            if (existing.getName().equalsIgnoreCase(saved.getName())) {
	                throw new IllegalArgumentException(
	                        "Existem consultas com nomes duplicados."
	                );
	            }
	        }
	
	        SavedTaskQuery copy = new SavedTaskQuery(
	                saved.getId(),
	                saved.getName(),
	                saved.getQuery()
	        );
	
	        validated.put(copy.getId(), copy);
	    }
	
	    queries.putAll(validated);
	    nextId = restoredNextId;
	}

}

package com.taskmanager.query;

public final class SavedTaskQuery {

    private final long id;
    private String name;
    private TaskQuery query;

    public SavedTaskQuery(long id, String name, TaskQuery query) {
        if (id <= 0) {
            throw new IllegalArgumentException(
                    "O identificador deve ser positivo."
            );
        }

        validateName(name);

        if (query == null) {
            throw new IllegalArgumentException(
                    "A consulta é obrigatória."
            );
        }

        this.id = id;
        this.name = name.trim();
        this.query = new TaskQuery(query);
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public TaskQuery getQuery() {
        return new TaskQuery(query);
    }

    public void update(String name, TaskQuery query) {
        validateName(name);

        if (query == null) {
            throw new IllegalArgumentException(
                    "A consulta é obrigatória."
            );
        }

        this.name = name.trim();
        this.query = new TaskQuery(query);
    }

    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "O nome da consulta não pode ser vazio."
            );
        }
    }
}

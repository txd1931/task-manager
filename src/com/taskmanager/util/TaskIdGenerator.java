package com.taskmanager.util;

public class TaskIdGenerator {

    private long nextId;

    public TaskIdGenerator(long nextId) {
        if (nextId < 1) {
            throw new IllegalArgumentException("O próximo ID deve ser positivo.");
        }

        this.nextId = nextId;
    }

    public long generateId() {
        return nextId++;
    }

    public long getNextId() {
        return nextId;
    }
}
package com.taskmanager.model;

public interface TaskState {

    String getDisplayName();

    void start(Task task);

    void complete(Task task);

    void reopen(Task task);

    void reopenForDescendant(Task task);
}

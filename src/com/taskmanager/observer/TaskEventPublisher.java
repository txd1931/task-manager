package com.taskmanager.observer;

import java.util.ArrayList;
import java.util.List;

public class TaskEventPublisher {

    private final List<TaskObserver> observers;

    public TaskEventPublisher() {
        this.observers = new ArrayList<>();
    }

    public void addObserver(TaskObserver observer) {
        if (observer == null) {
            throw new IllegalArgumentException(
                    "O observador não pode ser nulo."
            );
        }

        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    public void removeObserver(TaskObserver observer) {
        observers.remove(observer);
    }

    public void publish(TaskEvent event) {
        if (event == null) {
            throw new IllegalArgumentException(
                    "O evento não pode ser nulo."
            );
        }

        for (TaskObserver observer : new ArrayList<>(observers)) {
            observer.onTaskEvent(event);
        }
    }
}

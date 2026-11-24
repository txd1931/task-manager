package com.taskmanager.observer;

public class ConsoleObserver implements TaskObserver {

    @Override
    public void onTaskEvent(TaskEvent event) {
        System.out.println();
        System.out.println("[NOTIFICAÇÃO] " + event.getDescription());
    }
}

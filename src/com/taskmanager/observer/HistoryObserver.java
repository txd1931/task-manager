package com.taskmanager.observer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HistoryObserver implements TaskObserver {

    private final List<TaskEvent> history;

    public HistoryObserver() {
        this.history = new ArrayList<>();
    }

    @Override
    public void onTaskEvent(TaskEvent event) {
        history.add(event);
    }

    public List<TaskEvent> getHistory() {
        return Collections.unmodifiableList(history);
    }

    public void clearHistory() {
        history.clear();
    }
	    
	public void restoreHistory(List<TaskEvent> restoredEvents) {
	    if (restoredEvents == null) {
	        throw new IllegalArgumentException(
	                "O histórico recuperado não pode ser nulo."
	        );
	    }
	
	    if (!history.isEmpty()) {
	        throw new IllegalStateException(
	                "O histórico já possui eventos registrados."
	        );
	    }
	
	    for (TaskEvent event : restoredEvents) {
	        if (event == null) {
	            throw new IllegalArgumentException(
	                    "O histórico contém um evento nulo."
	            );
	        }
	    }
	
	    history.addAll(restoredEvents);
	}

}

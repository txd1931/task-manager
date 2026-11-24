package com.taskmanager.query;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


import com.taskmanager.model.Task;
import com.taskmanager.service.TaskService;
import com.taskmanager.model.PendingState;
import com.taskmanager.model.InProgressState;
import com.taskmanager.model.CompletedState;

public class TaskQueryService {

    private final TaskService taskService;

    public TaskQueryService(TaskService taskService) {
        if (taskService == null) {
            throw new IllegalArgumentException(
                    "O serviço de tarefas é obrigatório."
            );
        }

        this.taskService = taskService;
    }

    public List<Task> execute(TaskQuery query) {
        if (query == null) {
            throw new IllegalArgumentException(
                    "A consulta não pode ser nula."
            );
        }

        List<Task> results = new ArrayList<>();

        for (Task root : taskService.getRootTasks()) {
            collectMatchingTasks(root, query, results);
        }

        results.sort(createComparator(query));

        return results;
    }

    private void collectMatchingTasks(
            Task task,
            TaskQuery query,
            List<Task> results) {

        if (matches(task, query)) {
            results.add(task);
        }

        for (Task child : task.getSubtasks()) {
            collectMatchingTasks(child, query, results);
        }
    }

    private boolean matches(Task task, TaskQuery query) {
        return matchesStatus(task, query)
                && matchesPriority(task, query)
                && matchesDueDate(task, query);
    }


	private boolean matchesStatus(Task task, TaskQuery query) {
	    if (query.getStatuses().isEmpty()) {
	        return true;
	    }
	
	    TaskStatus status;
	
	    if (task.getState() instanceof PendingState) {
	        status = TaskStatus.PENDING;
	    } else if (task.getState() instanceof InProgressState) {
	        status = TaskStatus.IN_PROGRESS;
	    } else if (task.getState() instanceof CompletedState) {
	        status = TaskStatus.COMPLETED;
	    } else {
	        throw new IllegalStateException(
	                "Estado de tarefa desconhecido."
	        );
	    }
	
	    return query.getStatuses().contains(status);
	}


    private boolean matchesPriority(Task task, TaskQuery query) {
        return query.getPriorities().isEmpty()
                || query.getPriorities().contains(task.getPriority());
    }

    private boolean matchesDueDate(Task task, TaskQuery query) {
        LocalDate from = query.getDueDateFrom();
        LocalDate to = query.getDueDateTo();

        if (from == null && to == null) {
            return true;
        }

        LocalDate dueDate = task.getDueDate();

        if (dueDate == null) {
            return false;
        }

        if (from != null && dueDate.isBefore(from)) {
            return false;
        }

        if (to != null && dueDate.isAfter(to)) {
            return false;
        }

        return true;
    }

	
	private Comparator<Task> createComparator(TaskQuery query) {
	    Comparator<Task> comparator;
	
	    switch (query.getSortField()) {
	        case CREATED_AT:
	            comparator = Comparator.comparing(Task::getCreatedAt);
	            break;
	
	        case DUE_DATE:
	            Comparator<LocalDate> dateComparator =
	                    query.isAscending()
	                            ? Comparator.naturalOrder()
	                            : Comparator.reverseOrder();
	
	            comparator = Comparator.comparing(
	                    Task::getDueDate,
	                    Comparator.nullsLast(dateComparator)
	            );
	
	            return comparator.thenComparingLong(Task::getId);
	
	        case PRIORITY:
	            comparator = Comparator.comparingInt(
	                    task -> task.getPriority().getValue()
	            );
	            break;
	
	        case TITLE:
	            comparator = Comparator.comparing(
	                    Task::getTitle,
	                    String.CASE_INSENSITIVE_ORDER
	            );
	            break;
	
	        default:
	            throw new IllegalArgumentException(
	                    "Critério de ordenação inválido."
	            );
	    }
	
	    if (!query.isAscending()) {
	        comparator = comparator.reversed();
	    }
	
	    return comparator.thenComparingLong(Task::getId);
	}

}

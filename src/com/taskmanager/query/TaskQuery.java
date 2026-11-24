package com.taskmanager.query;

import java.time.LocalDate;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import com.taskmanager.model.TaskPriority;

public class TaskQuery {

    private final Set<TaskStatus> statuses;
    private final Set<TaskPriority> priorities;

    private LocalDate dueDateFrom;
    private LocalDate dueDateTo;

    private TaskSortField sortField;
    private boolean ascending;

    public TaskQuery() {
        this.statuses = EnumSet.noneOf(TaskStatus.class);
        this.priorities = EnumSet.noneOf(TaskPriority.class);
        this.sortField = TaskSortField.CREATED_AT;
        this.ascending = true;
    }
	
	public TaskQuery(TaskQuery other) {
	    if (other == null) {
	        throw new IllegalArgumentException(
	                "A consulta original não pode ser nula."
	        );
	    }
	
	    this.statuses = EnumSet.noneOf(TaskStatus.class);
	    this.statuses.addAll(other.statuses);
	
	    this.priorities = EnumSet.noneOf(TaskPriority.class);
	    this.priorities.addAll(other.priorities);
	
	    this.dueDateFrom = other.dueDateFrom;
	    this.dueDateTo = other.dueDateTo;
	    this.sortField = other.sortField;
	    this.ascending = other.ascending;
	}


    public void addStatus(TaskStatus status) {
        if (status == null) {
            throw new IllegalArgumentException(
                    "O estado não pode ser nulo."
            );
        }

        statuses.add(status);
    }

    public void removeStatus(TaskStatus status) {
        statuses.remove(status);
    }

    public void addPriority(TaskPriority priority) {
        if (priority == null) {
            throw new IllegalArgumentException(
                    "A prioridade não pode ser nula."
            );
        }

        priorities.add(priority);
    }

    public void removePriority(TaskPriority priority) {
        priorities.remove(priority);
    }

    public void setDueDateRange(
            LocalDate from,
            LocalDate to) {

        if (from != null && to != null && from.isAfter(to)) {
            throw new IllegalArgumentException(
                    "A data inicial não pode ser posterior à data final."
            );
        }

        this.dueDateFrom = from;
        this.dueDateTo = to;
    }

    public void setSorting(
            TaskSortField sortField,
            boolean ascending) {

        if (sortField == null) {
            throw new IllegalArgumentException(
                    "O critério de ordenação não pode ser nulo."
            );
        }

        this.sortField = sortField;
        this.ascending = ascending;
    }

    public Set<TaskStatus> getStatuses() {
        return Collections.unmodifiableSet(statuses);
    }

    public Set<TaskPriority> getPriorities() {
        return Collections.unmodifiableSet(priorities);
    }

    public LocalDate getDueDateFrom() {
        return dueDateFrom;
    }

    public LocalDate getDueDateTo() {
        return dueDateTo;
    }

    public TaskSortField getSortField() {
        return sortField;
    }

    public boolean isAscending() {
        return ascending;
    }
}

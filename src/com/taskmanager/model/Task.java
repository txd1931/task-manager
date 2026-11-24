package com.taskmanager.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public abstract class Task {

    private final long id;
    private String title;
    private String description;
    private TaskPriority priority;
    private LocalDate dueDate;
    private final LocalDateTime createdAt;
    private final TaskExecutionMode executionMode;


    private TaskState state;
    
    private Task parent;
    private final List<Task> subtasks;

    protected Task(
            long id,
            String title,
            String description,
            TaskPriority priority,
            LocalDate dueDate,
            LocalDateTime createdAt,
            TaskExecutionMode executionMode) {

        this.id = id;
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.dueDate = dueDate;
        this.createdAt = createdAt;
        this.executionMode = executionMode;

        this.state = PendingState.INSTANCE;

        this.parent = null;
        this.subtasks = new ArrayList<>();
    }

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public TaskPriority getPriority() {
        return priority;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public TaskExecutionMode getExecutionMode() {
        return executionMode;
    }

    public Task getParent() {
        return parent;
    }

    public List<Task> getSubtasks() {
        return Collections.unmodifiableList(subtasks);
    }

    public boolean isRoot() {
        return parent == null;
    }

    public boolean hasSubtasks() {
        return !subtasks.isEmpty();
    }
    
	

	public void addSubtask(Task subtask) {
	    if (subtask == null) {
	        throw new IllegalArgumentException("A subtarefa não pode ser nula.");
	    }
	
	    if (subtask == this) {
	        throw new IllegalArgumentException(
	                "Uma tarefa não pode ser subtarefa de si mesma."
	        );
	    }
	
	    if (subtask.parent != null) {
	        throw new IllegalStateException(
	                "A tarefa já possui uma tarefa superior."
	        );
	    }
	
	    if (subtask instanceof RecurringTask) {
	        throw new IllegalArgumentException(
	                "Tarefas recorrentes não podem ser subtarefas."
	        );
	    }
	
	    for (Task ancestor = this; ancestor != null; ancestor = ancestor.parent) {
	        if (ancestor == subtask) {
	            throw new IllegalArgumentException(
	                    "A operação criaria um ciclo na hierarquia."
	            );
	        }
	    }
	
	    LocalDate deadline = getEffectiveDeadline();
	
	    validateSubtreeDeadlines(subtask, deadline);

		for (Task ancestor = this; ancestor != null; ancestor = ancestor.parent) {
		    if (ancestor.isCompleted() && !subtask.isCompleted()) {
		        throw new IllegalStateException(
		                "Não é possível adicionar uma subtarefa pendente a uma tarefa concluída."
		        );
		    }
		}

	    subtasks.add(subtask);
	    subtask.parent = this;
	}
	

	private LocalDate getEffectiveDeadline() {
	    LocalDate deadline = null;
	
	    for (Task current = this; current != null; current = current.parent) {
	        if (current.dueDate != null) {
	            if (deadline == null || current.dueDate.isBefore(deadline)) {
	                deadline = current.dueDate;
	            }
	        }
	    }
	
	    return deadline;
	}
	
	private void validateSubtreeDeadlines(Task task, LocalDate deadline) {
	    LocalDate currentDeadline = deadline;
	
	    if (task.dueDate != null) {
	        if (deadline != null && task.dueDate.isAfter(deadline)) {
	            throw new IllegalArgumentException(
	                    "O prazo de uma tarefa ultrapassa o prazo de um ancestral."
	            );
	        }
	
	        currentDeadline = task.dueDate;
	    }
	
	    for (Task child : task.subtasks) {
	        validateSubtreeDeadlines(child, currentDeadline);
	    }
	}


	
	public void removeSubtask(Task subtask) {
	    if (!subtasks.remove(subtask)) {
	        throw new IllegalArgumentException("A tarefa informada não é uma subtarefa direta.");
	    }
	
	    subtask.parent = null;
	}

	public List<Task> detachSubtasks() {
	    List<Task> detached = new ArrayList<>(subtasks);
	
	    for (Task subtask : detached) {
	        removeSubtask(subtask);
	    }
	
	    return detached;
	}

	public void promoteSubtasks() {
	    if (parent == null) {
	        throw new IllegalStateException(
	                "Não é possível promover subtarefas de uma tarefa raiz."
	        );
	    }
	
	    List<Task> detached = detachSubtasks();
	
	    for (Task subtask : detached) {
	        parent.addSubtask(subtask);
	    }
	}
	

	public TaskState getState() {
	    return state;
	}
	
	void transitionTo(TaskState state) {
	    if (state == null) {
	        throw new IllegalArgumentException(
	                "O estado não pode ser nulo."
	        );
	    }
	
	    this.state = state;
	}
	
	public boolean isCompleted() {
	    return state == CompletedState.INSTANCE;
	}
	
	public boolean areSubtasksCompleted() {
	    for (Task subtask : subtasks) {
	        if (!subtask.isCompleted()) {
	            return false;
	        }
	    }
	
	    return true;
	}
	
	public void start() {
	    state.start(this);
	}
	
	public void complete() {
	    if (!areSubtasksCompleted()) {
	        throw new IllegalStateException(
	                "Todas as subtarefas precisam estar concluídas."
	        );
	    }
	
	    state.complete(this);
	}
	
	public void reopen() {
	    state.reopen(this);
	
	    for (Task ancestor = parent; ancestor != null; ancestor = ancestor.parent) {
	        ancestor.state.reopenForDescendant(ancestor);
	    }
	}
	
	
	public void updateDetails(
	        String title,
	        String description,
	        TaskPriority priority,
	        LocalDate dueDate) {
	
	    if (title == null || title.isBlank()) {
	        throw new IllegalArgumentException(
	                "O título da tarefa não pode ser vazio."
	        );
	    }
	
	    if (priority == null) {
	        throw new IllegalArgumentException(
	                "A prioridade não pode ser nula."
	        );
	    }
	
	    if (this instanceof RecurringTask && dueDate == null) {
	        throw new IllegalArgumentException(
	                "Uma tarefa recorrente precisa de um prazo."
	        );
	    }
	
	    LocalDate ancestorDeadline = parent == null
	            ? null
	            : parent.getEffectiveDeadline();
	
	    if (dueDate != null
	            && ancestorDeadline != null
	            && dueDate.isAfter(ancestorDeadline)) {
	        throw new IllegalArgumentException(
	                "O prazo ultrapassa o limite de uma tarefa superior."
	        );
	    }
	
	    LocalDate effectiveDeadline = ancestorDeadline;
	
	    if (dueDate != null
	            && (effectiveDeadline == null
	                    || dueDate.isBefore(effectiveDeadline))) {
	        effectiveDeadline = dueDate;
	    }
	
	    for (Task subtask : subtasks) {
	        validateSubtreeDeadlines(subtask, effectiveDeadline);
	    }
	
	    this.title = title.trim();
	    this.description = description == null ? "" : description;
	    this.priority = priority;
	    this.dueDate = dueDate;
	}
	
	
	protected final Map<Task, LocalDate> prepareDeadlineShift(
	        long dayOffset) {
	
	    Map<Task, LocalDate> deadlines = new IdentityHashMap<>();
	
	    collectShiftedDeadlines(this, dayOffset, deadlines);
	
	    validateShiftedDeadlines(this, null, deadlines);
	
	    return deadlines;
	}
	
	private void collectShiftedDeadlines(
	        Task task,
	        long dayOffset,
	        Map<Task, LocalDate> deadlines) {
	
	    LocalDate newDate = task.dueDate == null
	            ? null
	            : task.dueDate.plusDays(dayOffset);
	
	    deadlines.put(task, newDate);
	
	    for (Task child : task.subtasks) {
	        collectShiftedDeadlines(child, dayOffset, deadlines);
	    }
	}
	
	private void validateShiftedDeadlines(
	        Task task,
	        LocalDate ancestorDeadline,
	        Map<Task, LocalDate> deadlines) {
	
	    LocalDate newDate = deadlines.get(task);
	
	    if (newDate != null
	            && ancestorDeadline != null
	            && newDate.isAfter(ancestorDeadline)) {
	        throw new IllegalArgumentException(
	                "O prazo de uma subtarefa ultrapassaria o prazo de um ancestral."
	        );
	    }
	
	    LocalDate effectiveDeadline = ancestorDeadline;
	
	    if (newDate != null
	            && (effectiveDeadline == null
	                    || newDate.isBefore(effectiveDeadline))) {
	        effectiveDeadline = newDate;
	    }
	
	    for (Task child : task.subtasks) {
	        validateShiftedDeadlines(
	                child,
	                effectiveDeadline,
	                deadlines
	        );
	    }
	}
	
	protected final void applyPreparedDeadlines(
	        Map<Task, LocalDate> deadlines) {
	
	    for (Map.Entry<Task, LocalDate> entry : deadlines.entrySet()) {
	        entry.getKey().dueDate = entry.getValue();
	    }
	}
	
	protected final void applyRevisionAttributes(
	        RecurrenceRevision revision) {
	
	    this.title = revision.getTitle();
	    this.description = revision.getDescription();
	    this.priority = revision.getPriority();
	}
	
	
	public final void restoreStateFromStorage(String stateName) {
	    if (stateName == null) {
	        throw new IllegalArgumentException(
	                "O estado armazenado é obrigatório."
	        );
	    }
	
	    switch (stateName) {
	        case "PendingState":
	            transitionTo(PendingState.INSTANCE);
	            break;
	
	        case "InProgressState":
	            if (executionMode != TaskExecutionMode.TRACKED) {
	                throw new IllegalArgumentException(
	                        "Uma tarefa direta não pode estar em andamento."
	                );
	            }
	
	            transitionTo(InProgressState.INSTANCE);
	            break;
	
	        case "CompletedState":
	            if (!areSubtasksCompleted()) {
	                throw new IllegalArgumentException(
	                        "Uma tarefa concluída possui subtarefas não concluídas."
	                );
	            }
	
	            transitionTo(CompletedState.INSTANCE);
	            break;
	
	        default:
	            throw new IllegalArgumentException(
	                    "Estado armazenado inválido: " + stateName
	            );
	    }
	}
}

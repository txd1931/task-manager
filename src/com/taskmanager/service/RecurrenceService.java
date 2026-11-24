
package com.taskmanager.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.taskmanager.builder.TaskConfiguration;
import com.taskmanager.factory.NormalTaskCreator;
import com.taskmanager.factory.RecurringTaskCreator;
import com.taskmanager.factory.TaskCreator;
import com.taskmanager.model.Recurrence;
import com.taskmanager.model.RecurrenceType;
import com.taskmanager.model.RecurringTask;
import com.taskmanager.model.Task;
import com.taskmanager.util.TaskIdGenerator;

import com.taskmanager.model.RecurrenceRevision;
import com.taskmanager.model.TaskPriority;
import com.taskmanager.model.DeletionMode;

public class RecurrenceService {

    private final TaskService taskService;
    private final TaskIdGenerator taskIdGenerator;
    private final TaskIdGenerator recurrenceIdGenerator;
    private final List<Recurrence> recurrences;
    private final TaskCreator normalTaskCreator;

    public RecurrenceService(
            TaskService taskService,
            TaskIdGenerator taskIdGenerator,
            TaskIdGenerator recurrenceIdGenerator) {

        if (taskService == null
                || taskIdGenerator == null
                || recurrenceIdGenerator == null) {
            throw new IllegalArgumentException(
                    "As dependências do serviço são obrigatórias."
            );
        }

        if (taskIdGenerator == recurrenceIdGenerator) {
            throw new IllegalArgumentException(
                    "Os geradores de tarefas e recorrências devem ser independentes."
            );
        }

        this.taskService = taskService;
        this.taskIdGenerator = taskIdGenerator;
        this.recurrenceIdGenerator = recurrenceIdGenerator;
        this.recurrences = new ArrayList<>();
        this.normalTaskCreator = new NormalTaskCreator();
    }

    public RecurringTask createRecurrence(
            TaskConfiguration configuration,
            RecurrenceType type,
            int interval) {

        if (configuration == null || configuration.getDueDate() == null) {
            throw new IllegalArgumentException(
                    "Uma tarefa recorrente precisa de um prazo inicial."
            );
        }

        long recurrenceId = recurrenceIdGenerator.generateId();

        Recurrence recurrence = new Recurrence(
                recurrenceId,
                type,
                interval,
                configuration.getDueDate()
        );

		RecurrenceRevision initialRevision = new RecurrenceRevision(
		        recurrence.getStartDate(),
		        configuration.getTitle(),
		        configuration.getDescription(),
		        configuration.getPriority(),
		        0
		);
		
		recurrence.addRevision(initialRevision);


        TaskCreator creator = new RecurringTaskCreator(
                recurrenceId,
                recurrence.getStartDate()
        );

        RecurringTask firstOccurrence = (RecurringTask) creator.createTask(
                taskIdGenerator.generateId(),
                configuration,
                LocalDateTime.now()
        );

        taskService.addRootTask(firstOccurrence);

        recurrence.registerGeneratedDate(
                firstOccurrence.getScheduledDate()
        );

        recurrences.add(recurrence);

        return firstOccurrence;
    }

    public RecurringTask generateNextOccurrence(
            RecurringTask previous,
            LocalDate completionDate) {

        if (previous == null || completionDate == null) {
            throw new IllegalArgumentException(
                    "A ocorrência e a data de conclusão são obrigatórias."
            );
        }

        Task registeredTask = taskService.findTaskById(previous.getId());

        if (registeredTask != previous || !previous.isRoot()) {
            throw new IllegalArgumentException(
                    "A ocorrência não está cadastrada como tarefa raiz."
            );
        }

        if (!previous.isCompleted()) {
            throw new IllegalStateException(
                    "A ocorrência precisa estar concluída."
            );
        }

        Recurrence recurrence = findRecurrenceById(
                previous.getRecurrenceId()
        );

        if (recurrence == null) {
            throw new IllegalStateException(
                    "A série recorrente não foi encontrada."
            );
        }

        if (!recurrence.isActive()
                || recurrence.hasGeneratedSuccessor(previous.getId())) {
            return null;
        }


		LocalDate referenceDate = completionDate.isAfter(previous.getScheduledDate())
		        ? completionDate
		        : previous.getScheduledDate();
		
		LocalDate nextDate = recurrence.getNextDate(referenceDate);

        
        if (recurrence.hasGeneratedDate(nextDate)) {
            recurrence.registerGeneratedSuccessor(previous.getId());
            return null;
        }


		RecurrenceRevision revision = recurrence.getRevisionFor(nextDate);
		
		LocalDate nextDueDate = revision.getDueDateFor(nextDate);
		
		TaskConfiguration configuration =
		        new TaskConfiguration.Builder(revision.getTitle())
		                .description(revision.getDescription())
		                .priority(revision.getPriority())
		                .dueDate(nextDueDate)
		                .executionMode(previous.getExecutionMode())
		                .build();


        TaskCreator creator = new RecurringTaskCreator(
                recurrence.getId(),
                nextDate
        );

        RecurringTask next = (RecurringTask) creator.createTask(
                taskIdGenerator.generateId(),
                configuration,
                LocalDateTime.now()
        );

		long dayOffset = ChronoUnit.DAYS.between(
		        previous.getDueDate(),
		        nextDueDate
		);

        for (Task child : previous.getSubtasks()) {
            Task copy = copySubtree(child, dayOffset);
            next.addSubtask(copy);
        }

        taskService.addRootTask(next);

        recurrence.registerGeneratedDate(nextDate);
        recurrence.registerGeneratedSuccessor(previous.getId());

        return next;
    }

    private Task copySubtree(Task original, long dayOffset) {
        LocalDate newDueDate = original.getDueDate() == null
                ? null
                : original.getDueDate().plusDays(dayOffset);

        TaskConfiguration configuration =
                new TaskConfiguration.Builder(original.getTitle())
                        .description(original.getDescription())
                        .priority(original.getPriority())
                        .dueDate(newDueDate)
                        .executionMode(original.getExecutionMode())
                        .build();

        Task copy = normalTaskCreator.createTask(
                taskIdGenerator.generateId(),
                configuration,
                LocalDateTime.now()
        );

        for (Task child : original.getSubtasks()) {
            copy.addSubtask(copySubtree(child, dayOffset));
        }

        return copy;
    }

    public Recurrence findRecurrenceById(long id) {
        for (Recurrence recurrence : recurrences) {
            if (recurrence.getId() == id) {
                return recurrence;
            }
        }

        return null;
    }

    public List<Recurrence> getRecurrences() {
        return Collections.unmodifiableList(recurrences);
    }

    public void endRecurrence(long recurrenceId) {
        Recurrence recurrence = findRecurrenceById(recurrenceId);

        if (recurrence == null) {
            throw new IllegalArgumentException(
                    "Recorrência não encontrada."
            );
        }

        recurrence.end();
    }
	
	public void updateOccurrenceAndFuture(
	        long taskId,
	        String title,
	        String description,
	        TaskPriority priority,
	        LocalDate dueDate) {
	
	    Task selected = taskService.findTaskById(taskId);
	
	    if (!(selected instanceof RecurringTask)) {
	        throw new IllegalArgumentException(
	                "A tarefa selecionada não é recorrente."
	        );
	    }
	
	    RecurringTask occurrence = (RecurringTask) selected;
	
	    Recurrence recurrence = findRecurrenceById(
	            occurrence.getRecurrenceId()
	    );
	
	    if (recurrence == null) {
	        throw new IllegalStateException(
	                "A série recorrente não foi encontrada."
	        );
	    }
	
	    if (dueDate == null) {
	        throw new IllegalArgumentException(
	                "O prazo da tarefa recorrente é obrigatório."
	        );
	    }
	
	    long dueDateOffset = ChronoUnit.DAYS.between(
	            occurrence.getScheduledDate(),
	            dueDate
	    );
	
	    RecurrenceRevision revision = new RecurrenceRevision(
	            occurrence.getScheduledDate(),
	            title,
	            description,
	            priority,
	            dueDateOffset
	    );
	
	    List<RecurringTask> affected = new ArrayList<>();
	
	    for (Task task : taskService.getRootTasks()) {
	        if (!(task instanceof RecurringTask)) {
	            continue;
	        }
	
	        RecurringTask current = (RecurringTask) task;
	
	        if (current.getRecurrenceId() == recurrence.getId()
	                && !current.getScheduledDate()
	                        .isBefore(occurrence.getScheduledDate())) {
	            affected.add(current);
	        }
	    }
	
	    for (RecurringTask current : affected) {
	        current.validateRevision(revision);
	    }
	
	    for (RecurringTask current : affected) {
	        current.applyRevision(revision);
	    }
	
	    recurrence.replaceRevisionsFrom(revision);
	}


	public void deleteOccurrence(long taskId, DeletionMode mode) {
	    Task task = taskService.findTaskById(taskId);
	
	    if (!(task instanceof RecurringTask)) {
	        throw new IllegalArgumentException(
	                "A tarefa selecionada não é uma ocorrência recorrente."
	        );
	    }
	
	    RecurringTask occurrence = (RecurringTask) task;
	
	    Recurrence recurrence = findRecurrenceById(
	            occurrence.getRecurrenceId()
	    );
	
	    if (recurrence == null) {
	        throw new IllegalStateException(
	                "A série recorrente não foi encontrada."
	        );
	    }
	
	    taskService.deleteRecurringOccurrence(taskId, mode);
	}
	
	
	public void restoreRecurrences(List<Recurrence> restored) {
	    if (restored == null) {
	        throw new IllegalArgumentException(
	                "As recorrências recuperadas não podem ser nulas."
	        );
	    }
	
	    if (!recurrences.isEmpty()) {
	        throw new IllegalStateException(
	                "O serviço já possui recorrências cadastradas."
	        );
	    }
	
	    Map<Long, Recurrence> byId = new HashMap<>();
	
	    for (Recurrence recurrence : restored) {
	        if (recurrence == null) {
	            throw new IllegalArgumentException(
	                    "Uma recorrência recuperada é nula."
	            );
	        }
	
	        if (recurrence.getId() <= 0) {
	            throw new IllegalArgumentException(
	                    "Uma recorrência possui ID inválido."
	            );
	        }
	
	        if (byId.putIfAbsent(
	                recurrence.getId(),
	                recurrence
	        ) != null) {
	            throw new IllegalArgumentException(
	                    "Existem recorrências com IDs duplicados."
	            );
	        }
	
	        if (recurrence.getRevisions().isEmpty()) {
	            throw new IllegalArgumentException(
	                    "Uma recorrência não possui revisões."
	            );
	        }
	
	        if (!recurrence.getRevisions().get(0)
	                .getEffectiveDate()
	                .equals(recurrence.getStartDate())) {
	            throw new IllegalArgumentException(
	                    "A revisão inicial não corresponde ao início da recorrência."
	            );
	        }
	    }
	
	    Map<Long, Set<LocalDate>> occurrenceDates = new HashMap<>();
	
	    for (Task task : taskService.getRootTasks()) {
	        if (!(task instanceof RecurringTask)) {
	            continue;
	        }
	
	        RecurringTask occurrence = (RecurringTask) task;
	
	        Recurrence recurrence = byId.get(
	                occurrence.getRecurrenceId()
	        );
	
	        if (recurrence == null) {
	            throw new IllegalArgumentException(
	                    "A ocorrência #" + occurrence.getId()
	                            + " pertence a uma recorrência inexistente."
	            );
	        }
	
	        LocalDate scheduledDate = occurrence.getScheduledDate();
	
	        if (scheduledDate.isBefore(recurrence.getStartDate())
	                || !recurrence.getNextDate(
	                        scheduledDate.minusDays(1)
	                ).equals(scheduledDate)) {
	            throw new IllegalArgumentException(
	                    "A ocorrência #" + occurrence.getId()
	                            + " possui uma data programada inválida."
	            );
	        }
	
	        if (!recurrence.hasGeneratedDate(scheduledDate)) {
	            throw new IllegalArgumentException(
	                    "A ocorrência #" + occurrence.getId()
	                            + " não consta no histórico de geração."
	            );
	        }
	
	        Set<LocalDate> dates = occurrenceDates.computeIfAbsent(
	                recurrence.getId(),
	                id -> new HashSet<>()
	        );
	
	        if (!dates.add(scheduledDate)) {
	            throw new IllegalArgumentException(
	                    "Existem ocorrências duplicadas na mesma data."
	            );
	        }
	    }
	
	    recurrences.addAll(restored);
	}

}

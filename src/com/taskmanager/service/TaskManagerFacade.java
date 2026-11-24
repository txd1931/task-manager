
package com.taskmanager.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.taskmanager.builder.TaskConfiguration;
import com.taskmanager.factory.NormalTaskCreator;
import com.taskmanager.factory.TaskCreator;
import com.taskmanager.model.DeletionMode;
import com.taskmanager.model.RecurrenceType;
import com.taskmanager.model.RecurringTask;
import com.taskmanager.model.Task;
import com.taskmanager.model.TaskPriority;
import com.taskmanager.observer.TaskEvent;
import com.taskmanager.observer.TaskEventPublisher;
import com.taskmanager.observer.TaskEventType;
import com.taskmanager.query.SavedTaskQuery;
import com.taskmanager.query.SavedTaskQueryService;
import com.taskmanager.query.TaskQuery;
import com.taskmanager.query.TaskQueryService;
import com.taskmanager.util.TaskIdGenerator;

public class TaskManagerFacade {

    private final TaskService taskService;
    private final RecurrenceService recurrenceService;
    private final TaskIdGenerator taskIdGenerator;
    private final TaskEventPublisher eventPublisher;
    private final TaskCreator normalTaskCreator;
    private final TaskQueryService queryService;
    private final SavedTaskQueryService savedQueryService;

    private Runnable mutationListener = () -> {};

    public TaskManagerFacade(
            TaskService taskService,
            RecurrenceService recurrenceService,
            TaskIdGenerator taskIdGenerator,
            TaskEventPublisher eventPublisher) {

        this(
                taskService,
                recurrenceService,
                taskIdGenerator,
                eventPublisher,
                new SavedTaskQueryService()
        );
    }

    public TaskManagerFacade(
            TaskService taskService,
            RecurrenceService recurrenceService,
            TaskIdGenerator taskIdGenerator,
            TaskEventPublisher eventPublisher,
            SavedTaskQueryService savedQueryService) {

        if (taskService == null
                || recurrenceService == null
                || taskIdGenerator == null
                || eventPublisher == null
                || savedQueryService == null) {
            throw new IllegalArgumentException(
                    "As dependências da aplicação são obrigatórias."
            );
        }

        this.taskService = taskService;
        this.recurrenceService = recurrenceService;
        this.taskIdGenerator = taskIdGenerator;
        this.eventPublisher = eventPublisher;
        this.normalTaskCreator = new NormalTaskCreator();
        this.queryService = new TaskQueryService(taskService);
        this.savedQueryService = savedQueryService;
    }

    public void setMutationListener(Runnable mutationListener) {
        if (mutationListener == null) {
            throw new IllegalArgumentException(
                    "O listener de alterações não pode ser nulo."
            );
        }

        this.mutationListener = mutationListener;
    }

    private void notifyMutation() {
        mutationListener.run();
    }

    public Task createNormalTask(TaskConfiguration configuration) {
        Task task = normalTaskCreator.createTask(
                taskIdGenerator.generateId(),
                configuration,
                LocalDateTime.now()
        );

        taskService.addRootTask(task);

        publishEvent(
                TaskEventType.TASK_CREATED,
                task.getId(),
                "Tarefa #" + task.getId()
                        + " \"" + task.getTitle() + "\" criada."
        );

        notifyMutation();

        return task;
    }

    public RecurringTask createRecurringTask(
            TaskConfiguration configuration,
            RecurrenceType type,
            int interval) {

        RecurringTask task = recurrenceService.createRecurrence(
                configuration,
                type,
                interval
        );

        publishEvent(
                TaskEventType.TASK_CREATED,
                task.getId(),
                "Tarefa recorrente #" + task.getId()
                        + " \"" + task.getTitle() + "\" criada."
        );

        notifyMutation();

        return task;
    }

    public Task createSubtask(
            long parentId,
            TaskConfiguration configuration) {

        Task parent = requireTask(parentId);

        if (configuration == null) {
            throw new IllegalArgumentException(
                    "A configuração da subtarefa é obrigatória."
            );
        }

        Task subtask = normalTaskCreator.createTask(
                taskIdGenerator.generateId(),
                configuration,
                LocalDateTime.now()
        );

        taskService.addSubtask(parent.getId(), subtask);

        publishEvent(
                TaskEventType.TASK_CREATED,
                subtask.getId(),
                "Subtarefa #" + subtask.getId()
                        + " \"" + subtask.getTitle()
                        + "\" criada na tarefa #"
                        + parent.getId() + "."
        );

        notifyMutation();

        return subtask;
    }

    public Task findTaskById(long id) {
        return taskService.findTaskById(id);
    }

    public List<Task> getRootTasks() {
        return taskService.getRootTasks();
    }

    private Task requireTask(long id) {
        Task task = taskService.findTaskById(id);

        if (task == null) {
            throw new IllegalArgumentException(
                    "Tarefa não encontrada: " + id
            );
        }

        return task;
    }

    public void startTask(long id) {
        Task task = requireTask(id);

        task.start();

        publishEvent(
                TaskEventType.TASK_STATE_CHANGED,
                task.getId(),
                "Tarefa #" + task.getId()
                        + " \"" + task.getTitle() + "\" iniciada."
        );

        notifyMutation();
    }

    public void completeTask(long id) {
        Task task = requireTask(id);

        task.complete();

        if (task instanceof RecurringTask) {
            RecurringTask recurringTask = (RecurringTask) task;

            RecurringTask next = recurrenceService.generateNextOccurrence(
                    recurringTask,
                    LocalDate.now()
            );

            if (next != null) {
                publishEvent(
                        TaskEventType.RECURRENCE_GENERATED,
                        next.getId(),
                        "Nova ocorrência #" + next.getId()
                                + " \"" + next.getTitle() + "\" gerada."
                );
            }
        }

        publishEvent(
                TaskEventType.TASK_STATE_CHANGED,
                task.getId(),
                "Tarefa #" + task.getId()
                        + " \"" + task.getTitle() + "\" concluída."
        );

        notifyMutation();
    }

    public void reopenTask(long id) {
        Task task = requireTask(id);

        task.reopen();

        publishEvent(
                TaskEventType.TASK_STATE_CHANGED,
                task.getId(),
                "Tarefa #" + task.getId()
                        + " \"" + task.getTitle() + "\" reaberta."
        );

        notifyMutation();
    }

    public void updateTask(
            long id,
            String title,
            String description,
            TaskPriority priority,
            LocalDate dueDate) {

        Task task = requireTask(id);

        task.updateDetails(
                title,
                description,
                priority,
                dueDate
        );

        publishEvent(
                TaskEventType.TASK_UPDATED,
                task.getId(),
                "Tarefa #" + task.getId()
                        + " \"" + task.getTitle() + "\" atualizada."
        );

        notifyMutation();
    }

    public void updateRecurringTaskAndFuture(
            long taskId,
            String title,
            String description,
            TaskPriority priority,
            LocalDate dueDate) {

        recurrenceService.updateOccurrenceAndFuture(
                taskId,
                title,
                description,
                priority,
                dueDate
        );

        requireTask(taskId);

        publishEvent(
                TaskEventType.TASK_UPDATED,
                taskId,
                "Tarefa recorrente #" + taskId
                        + " e ocorrências futuras atualizadas."
        );

        notifyMutation();
    }

    public void deleteTask(long id, DeletionMode mode) {
        Task task = requireTask(id);

        if (mode == null) {
            throw new IllegalArgumentException(
                    "O modo de exclusão não pode ser nulo."
            );
        }

        String title = task.getTitle();

        taskService.deleteTask(id, mode);

        publishEvent(
                TaskEventType.TASK_DELETED,
                id,
                "Tarefa #" + id
                        + " \"" + title + "\" excluída."
        );

        notifyMutation();
    }

    public void deleteRecurringOccurrence(
            long taskId,
            DeletionMode mode) {

        Task task = requireTask(taskId);

        if (!(task instanceof RecurringTask)) {
            throw new IllegalArgumentException(
                    "A tarefa selecionada não é recorrente."
            );
        }

        String title = task.getTitle();

        recurrenceService.deleteOccurrence(taskId, mode);

        publishEvent(
                TaskEventType.TASK_DELETED,
                taskId,
                "Ocorrência recorrente #" + taskId
                        + " \"" + title + "\" excluída."
        );

        notifyMutation();
    }

    public void endRecurrence(long recurrenceId) {
        recurrenceService.endRecurrence(recurrenceId);

        Task relatedTask = null;

        for (Task task : taskService.getRootTasks()) {
            if (task instanceof RecurringTask) {
                RecurringTask recurringTask = (RecurringTask) task;

                if (recurringTask.getRecurrenceId() == recurrenceId) {
                    relatedTask = recurringTask;
                    break;
                }
            }
        }

        if (relatedTask != null) {
            publishEvent(
                    TaskEventType.RECURRENCE_ENDED,
                    relatedTask.getId(),
                    "Recorrência #" + recurrenceId + " encerrada."
            );
        }

        notifyMutation();
    }

    public List<Task> searchTasks(TaskQuery query) {
        return queryService.execute(query);
    }

    public SavedTaskQuery saveQuery(
            String name,
            TaskQuery query) {

        SavedTaskQuery saved = savedQueryService.save(name, query);

        notifyMutation();

        return saved;
    }

    public void updateSavedQuery(
            long id,
            String name,
            TaskQuery query) {

        savedQueryService.update(id, name, query);

        notifyMutation();
    }

    public void deleteSavedQuery(long id) {
        savedQueryService.delete(id);

        notifyMutation();
    }

    public SavedTaskQuery findSavedQueryById(long id) {
        return savedQueryService.findById(id);
    }

    public List<SavedTaskQuery> getSavedQueries() {
        return savedQueryService.getAll();
    }

    public List<Task> executeSavedQuery(long id) {
        SavedTaskQuery saved = savedQueryService.findById(id);

        if (saved == null) {
            throw new IllegalArgumentException(
                    "Consulta salva não encontrada: " + id
            );
        }

        return queryService.execute(saved.getQuery());
    }

    private void publishEvent(
            TaskEventType type,
            long taskId,
            String description) {

        TaskEvent event = new TaskEvent(
                type,
                taskId,
                description,
                LocalDateTime.now()
        );

        eventPublisher.publish(event);
    }
}

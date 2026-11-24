package com.taskmanager.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.taskmanager.model.DeletionMode;
import com.taskmanager.model.NormalTask;
import com.taskmanager.model.Task;
import com.taskmanager.model.RecurringTask;

public class TaskService {

    private final List<Task> rootTasks;

    public TaskService() {
        this.rootTasks = new ArrayList<>();
    }

    public void addRootTask(Task task) {
        if (task == null) {
            throw new IllegalArgumentException(
                    "A tarefa não pode ser nula."
            );
        }

        if (!task.isRoot()) {
            throw new IllegalArgumentException(
                    "A tarefa já possui uma tarefa superior."
            );
        }

        if (containsTaskReference(task)) {
            throw new IllegalArgumentException(
                    "A tarefa já está cadastrada."
            );
        }

        validateUniqueIds(task);

        rootTasks.add(task);
    }

    public List<Task> getRootTasks() {
        return Collections.unmodifiableList(rootTasks);
    }

    public Task findTaskById(long id) {
        for (Task root : rootTasks) {
            Task found = findRecursive(root, id);

            if (found != null) {
                return found;
            }
        }

        return null;
    }

    private Task findRecursive(Task task, long id) {
        if (task.getId() == id) {
            return task;
        }

        for (Task subtask : task.getSubtasks()) {
            Task found = findRecursive(subtask, id);

            if (found != null) {
                return found;
            }
        }

        return null;
    }

    private boolean containsTaskReference(Task task) {
        for (Task root : rootTasks) {
            if (containsReferenceRecursive(root, task)) {
                return true;
            }
        }

        return false;
    }

    private boolean containsReferenceRecursive(
            Task current,
            Task target) {

        if (current == target) {
            return true;
        }

        for (Task child : current.getSubtasks()) {
            if (containsReferenceRecursive(child, target)) {
                return true;
            }
        }

        return false;
    }

    private void validateUniqueIds(Task task) {
        List<Long> ids = new ArrayList<>();

        collectIds(task, ids);

        for (Long id : ids) {
            if (Collections.frequency(ids, id) > 1
                    || findTaskById(id) != null) {
                throw new IllegalArgumentException(
                        "Existe uma tarefa com identificador duplicado: " + id
                );
            }
        }
    }

    private void collectIds(Task task, List<Long> ids) {
        ids.add(task.getId());

        for (Task child : task.getSubtasks()) {
            collectIds(child, ids);
        }
    }

	
	public void deleteTask(long id, DeletionMode mode) {
	    Task task = findTaskById(id);
	
	    if (task == null) {
	        throw new IllegalArgumentException(
	                "Tarefa não encontrada."
	        );
	    }
	
	    if (!(task instanceof NormalTask)) {
	        throw new IllegalStateException(
	                "A exclusão de tarefas recorrentes exige uma operação específica."
	        );
	    }
	
	    deleteFromHierarchy(task, mode);
	}
	
	
	public void deleteRecurringOccurrence(long id, DeletionMode mode) {
	    Task task = findTaskById(id);
	
	    if (!(task instanceof RecurringTask)) {
	        throw new IllegalArgumentException(
	                "A ocorrência recorrente não foi encontrada."
	        );
	    }
	
	    if (!task.isRoot()) {
	        throw new IllegalStateException(
	                "Uma ocorrência recorrente precisa ser uma tarefa raiz."
	        );
	    }
	
	    deleteFromHierarchy(task, mode);
	}


	private void deleteFromHierarchy(Task task, DeletionMode mode) {
	    if (mode == null) {
	        throw new IllegalArgumentException(
	                "O modo de exclusão não pode ser nulo."
	        );
	    }
	
	    switch (mode) {
	        case MAKE_ROOT:
	            makeSubtasksRoot(task);
	            break;
	
	        case PROMOTE:
	            if (task.isRoot()) {
	                makeSubtasksRoot(task);
	            } else {
	                task.promoteSubtasks();
	            }
	            break;
	
	        case CASCADE:
	            break;
	
	        default:
	            throw new IllegalArgumentException(
	                    "Modo de exclusão inválido."
	            );
	    }
	
	    removeTaskFromHierarchy(task);
	}

	
	private void makeSubtasksRoot(Task task) {
	    List<Task> detached = task.detachSubtasks();
	
	    for (Task subtask : detached) {
	        rootTasks.add(subtask);
	    }
	}
	
	private void removeTaskFromHierarchy(Task task) {
	    if (task.isRoot()) {
	        rootTasks.remove(task);
	    } else {
	        task.getParent().removeSubtask(task);
	    }
	}
	
	
	public void addSubtask(long parentId, Task subtask) {
	    Task parent = findTaskById(parentId);
	
	    if (parent == null) {
	        throw new IllegalArgumentException(
	                "A tarefa superior não foi encontrada."
	        );
	    }
	
	    if (subtask == null) {
	        throw new IllegalArgumentException(
	                "A subtarefa não pode ser nula."
	        );
	    }
	
	    if (!(subtask instanceof NormalTask)) {
	        throw new IllegalArgumentException(
	                "Somente tarefas normais podem ser subtarefas."
	        );
	    }
	
	    if (!subtask.isRoot()) {
	        throw new IllegalArgumentException(
	                "A tarefa já possui uma tarefa superior."
	        );
	    }
	
	    if (containsTaskReference(subtask)) {
	        throw new IllegalArgumentException(
	                "A tarefa já está cadastrada."
	        );
	    }
	
	    validateUniqueIds(subtask);
	
	    parent.addSubtask(subtask);
	}

}

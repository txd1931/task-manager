package com.taskmanager.model;

public final class CompletedState implements TaskState {

    public static final CompletedState INSTANCE = new CompletedState();

    private CompletedState() {
    }

    @Override
    public String getDisplayName() {
        return "Concluída";
    }

    @Override
    public void start(Task task) {
        throw new IllegalStateException(
                "Uma tarefa concluída precisa ser reaberta antes de ser iniciada."
        );
    }

    @Override
    public void complete(Task task) {
        throw new IllegalStateException(
                "A tarefa já está concluída."
        );
    }

    @Override
    public void reopen(Task task) {
        task.transitionTo(PendingState.INSTANCE);
    }

    @Override
    public void reopenForDescendant(Task task) {
        if (task.getExecutionMode() == TaskExecutionMode.TRACKED) {
            task.transitionTo(InProgressState.INSTANCE);
        } else {
            task.transitionTo(PendingState.INSTANCE);
        }
    }
}

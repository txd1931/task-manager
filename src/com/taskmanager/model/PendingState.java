package com.taskmanager.model;

public final class PendingState implements TaskState {

    public static final PendingState INSTANCE = new PendingState();

    private PendingState() {
    }

    @Override
    public String getDisplayName() {
        return "Pendente";
    }

    @Override
    public void start(Task task) {
        if (task.getExecutionMode() == TaskExecutionMode.DIRECT) {
            throw new IllegalStateException(
                    "Tarefas de conclusão direta não podem ser iniciadas."
            );
        }

        task.transitionTo(InProgressState.INSTANCE);
    }

    @Override
    public void complete(Task task) {
        if (task.getExecutionMode() == TaskExecutionMode.TRACKED) {
            throw new IllegalStateException(
                    "A tarefa precisa estar em andamento antes de ser concluída."
            );
        }

        task.transitionTo(CompletedState.INSTANCE);
    }

    @Override
    public void reopen(Task task) {
        throw new IllegalStateException(
                "A tarefa já está pendente."
        );
    }

    @Override
    public void reopenForDescendant(Task task) {
    }
}

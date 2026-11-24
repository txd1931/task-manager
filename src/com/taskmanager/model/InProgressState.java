package com.taskmanager.model;

public final class InProgressState implements TaskState {

    public static final InProgressState INSTANCE = new InProgressState();

    private InProgressState() {
    }

    @Override
    public String getDisplayName() {
        return "Em andamento";
    }

    @Override
    public void start(Task task) {
        throw new IllegalStateException(
                "A tarefa já está em andamento."
        );
    }

    @Override
    public void complete(Task task) {
        task.transitionTo(CompletedState.INSTANCE);
    }

    @Override
    public void reopen(Task task) {
        throw new IllegalStateException(
                "Somente tarefas concluídas podem ser reabertas."
        );
    }

    @Override
    public void reopenForDescendant(Task task) {
    }
}

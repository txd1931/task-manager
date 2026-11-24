package com.taskmanager.persistence;

import java.io.IOException;

public final class AutoSaveManager {

    private final ApplicationPersistence persistence;
    private final ApplicationData data;
    private Runnable mutationListener = () -> {};

    public AutoSaveManager(
            ApplicationPersistence persistence,
            ApplicationData data) {

        if (persistence == null || data == null) {
            throw new IllegalArgumentException(
                    "A persistência e os dados são obrigatórios."
            );
        }

        this.persistence = persistence;
        this.data = data;
    }

    public void saveChanges() {
        try {
            persistence.save(data);
        } catch (IOException exception) {
            System.out.println(
                    "[ERRO] Não foi possível salvar os dados. "
                    + "As alterações permanecem em memória."
            );
            System.out.println(
                    "[ERRO] Detalhes: " + exception.getMessage()
            );
        }
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

}

package com.taskmanager;

import java.io.IOException;
import java.util.Scanner;

import com.taskmanager.model.Recurrence;
import com.taskmanager.model.RecurringTask;
import com.taskmanager.model.Task;
import com.taskmanager.observer.TaskEvent;
import com.taskmanager.persistence.ApplicationData;
import com.taskmanager.persistence.ApplicationPersistence;

public class RecurrenceHistoryMenu {

    private final ApplicationData data;
    private final ApplicationPersistence persistence;
    private final Scanner scanner;

    public RecurrenceHistoryMenu(
            ApplicationData data,
            ApplicationPersistence persistence,
            Scanner scanner) {

        if (data == null || persistence == null || scanner == null) {
            throw new IllegalArgumentException(
                    "As dependências do menu são obrigatórias."
            );
        }

        this.data = data;
        this.persistence = persistence;
        this.scanner = scanner;
    }

    public void start() {
        boolean running = true;

        while (running) {
            System.out.println();
            System.out.println("========== RECORRÊNCIAS E HISTÓRICO ==========");
            System.out.println("1 - Listar séries recorrentes");
            System.out.println("2 - Listar ocorrências recorrentes");
            System.out.println("3 - Visualizar histórico");
            System.out.println("4 - Limpar histórico");
            System.out.println("0 - Voltar");
            System.out.print("Opção: ");

            String option = scanner.nextLine().trim();

            switch (option) {
                case "1":
                    listRecurrences();
                    break;
                case "2":
                    listOccurrences();
                    break;
                case "3":
                    showHistory();
                    break;
                case "4":
                    clearHistory();
                    break;
                case "0":
                    running = false;
                    break;
                default:
                    System.out.println("[ERRO] Opção inválida.");
            }
        }
    }

    private void listRecurrences() {
        System.out.println();
        System.out.println("========== SÉRIES RECORRENTES ==========");

        if (data.getRecurrenceService().getRecurrences().isEmpty()) {
            System.out.println("Nenhuma recorrência cadastrada.");
            return;
        }

        for (Recurrence recurrence
                : data.getRecurrenceService().getRecurrences()) {

            System.out.println(
                    "Série #" + recurrence.getId()
                    + " | " + recurrence.getType()
                    + " | Intervalo: " + recurrence.getInterval()
                    + " | Início: " + recurrence.getStartDate()
                    + " | " + (recurrence.isActive()
                            ? "Ativa" : "Encerrada")
            );

            System.out.println(
                    "Datas já geradas: "
                    + recurrence.getGeneratedDates().size()
            );
        }
    }

    private void listOccurrences() {
        System.out.println();
        System.out.println("========== OCORRÊNCIAS ==========");

        boolean found = false;

        for (Task task : data.getTaskService().getRootTasks()) {
            if (task instanceof RecurringTask) {
                RecurringTask occurrence = (RecurringTask) task;

                System.out.println(
                        "[" + occurrence.getId() + "] "
                        + occurrence.getTitle()
                        + " | Série #" + occurrence.getRecurrenceId()
                        + " | Programada: " + occurrence.getScheduledDate()
                        + " | Prazo: " + occurrence.getDueDate()
                        + " | " + occurrence.getState().getDisplayName()
                );

                found = true;
            }
        }

        if (!found) {
            System.out.println("Nenhuma ocorrência encontrada.");
        }
    }

    private void showHistory() {
        System.out.println();
        System.out.println("========== HISTÓRICO ==========");

        if (data.getHistoryObserver().getHistory().isEmpty()) {
            System.out.println("Nenhum evento registrado.");
            return;
        }

        for (TaskEvent event
                : data.getHistoryObserver().getHistory()) {

            System.out.println(
                    event.getTimestamp()
                    + " | " + event.getDescription()
            );
        }

        System.out.println();
        System.out.println(
                "Total de eventos: "
                + data.getHistoryObserver().getHistory().size()
        );
    }

    private void clearHistory() {
        System.out.print(
                "Deseja realmente limpar todo o histórico? (s/n): "
        );

        String answer = scanner.nextLine().trim();

        if (!answer.equalsIgnoreCase("s")) {
            System.out.println("Operação cancelada.");
            return;
        }

        data.getHistoryObserver().clearHistory();

        try {
            persistence.save(data);
            System.out.println("Histórico limpo e salvo.");
        } catch (IOException exception) {
            System.out.println(
                    "[ERRO] O histórico foi limpo em memória, "
                    + "mas não foi possível salvar a alteração."
            );
            System.out.println(
                    "[ERRO] Detalhes: " + exception.getMessage()
            );
        }
    }
}

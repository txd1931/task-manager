package com.taskmanager;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

import com.taskmanager.model.Task;
import com.taskmanager.model.TaskPriority;
import com.taskmanager.query.SavedTaskQuery;
import com.taskmanager.query.TaskQuery;
import com.taskmanager.query.TaskSortField;
import com.taskmanager.query.TaskStatus;
import com.taskmanager.service.TaskManagerFacade;

public class QueryConsoleMenu {

    private final TaskManagerFacade facade;
    private final Scanner scanner;

    public QueryConsoleMenu(
            TaskManagerFacade facade,
            Scanner scanner) {

        if (facade == null || scanner == null) {
            throw new IllegalArgumentException(
                    "As dependências são obrigatórias."
            );
        }

        this.facade = facade;
        this.scanner = scanner;
    }

    public void start() {
        while (true) {
            System.out.println();
            System.out.println("========== CONSULTAS ==========");
            System.out.println("1 - Pesquisar tarefas");
            System.out.println("2 - Listar consultas salvas");
            System.out.println("3 - Executar consulta salva");
            System.out.println("4 - Editar consulta salva");
            System.out.println("5 - Excluir consulta salva");
            System.out.println("0 - Voltar");

            int option = readInt("Opção: ");

            try {
                switch (option) {
                    case 1:
                        search();
                        break;
                    case 2:
                        listSavedQueries();
                        break;
                    case 3:
                        executeSavedQuery();
                        break;
                    case 4:
                        editSavedQuery();
                        break;
                    case 5:
                        deleteSavedQuery();
                        break;
                    case 0:
                        return;
                    default:
                        System.out.println("[ERRO] Opção inválida.");
                }
            } catch (IllegalArgumentException
                    | IllegalStateException exception) {
                System.out.println("[ERRO] " + exception.getMessage());
            }
        }
    }

    private void search() {
        TaskQuery query = readQuery();

        printResults(facade.searchTasks(query));

        if (confirm("Salvar esta consulta?")) {
            String name = readRequired("Nome da consulta: ");

            SavedTaskQuery saved = facade.saveQuery(name, query);

            System.out.println(
                    "Consulta #" + saved.getId() + " salva."
            );
        }
    }

    private void listSavedQueries() {
        System.out.println();
        System.out.println("========== CONSULTAS SALVAS ==========");

        if (facade.getSavedQueries().isEmpty()) {
            System.out.println("Nenhuma consulta salva.");
            return;
        }

        for (SavedTaskQuery saved : facade.getSavedQueries()) {
            System.out.println(
                    "[" + saved.getId() + "] " + saved.getName()
            );
        }
    }

    private void executeSavedQuery() {
        listSavedQueries();

        if (facade.getSavedQueries().isEmpty()) {
            return;
        }

        long id = readLong("ID da consulta: ");

        printResults(facade.executeSavedQuery(id));
    }

    private void editSavedQuery() {
        listSavedQueries();

        if (facade.getSavedQueries().isEmpty()) {
            return;
        }

        long id = readLong("ID da consulta: ");

        SavedTaskQuery saved = facade.findSavedQueryById(id);

        if (saved == null) {
            throw new IllegalArgumentException(
                    "Consulta salva não encontrada."
            );
        }

        System.out.println(
                "Editando: " + saved.getName()
        );

        String name = readText(
                "Novo nome (vazio mantém o atual): "
        );

        if (name.isBlank()) {
            name = saved.getName();
        }

        System.out.println(
                "Configure os novos filtros da consulta."
        );

        TaskQuery query = readQuery();

        facade.updateSavedQuery(id, name, query);

        System.out.println("Consulta atualizada.");
    }

    private void deleteSavedQuery() {
        listSavedQueries();

        if (facade.getSavedQueries().isEmpty()) {
            return;
        }

        long id = readLong("ID da consulta: ");

        if (confirm("Excluir esta consulta?")) {
            facade.deleteSavedQuery(id);
            System.out.println("Consulta excluída.");
        }
    }

    private TaskQuery readQuery() {
        TaskQuery query = new TaskQuery();

        System.out.println();
        System.out.println("========== FILTROS ==========");
        System.out.println(
                "Informe vários números separados por vírgula."
        );
        System.out.println(
                "Deixe vazio para não aplicar o filtro."
        );

        System.out.println();
        System.out.println("Estados:");
        System.out.println("1 - Pendente");
        System.out.println("2 - Em andamento");
        System.out.println("3 - Concluída");

        for (Integer number : readSelections(
                "Estados (ex.: 1,2): ", 3)) {

            switch (number) {
                case 1:
                    query.addStatus(TaskStatus.PENDING);
                    break;
                case 2:
                    query.addStatus(TaskStatus.IN_PROGRESS);
                    break;
                case 3:
                    query.addStatus(TaskStatus.COMPLETED);
                    break;
            }
        }

        System.out.println();
        System.out.println("Prioridades:");
        System.out.println("1 - Muito baixa");
        System.out.println("2 - Baixa");
        System.out.println("3 - Média");
        System.out.println("4 - Alta");
        System.out.println("5 - Muito alta");

        for (Integer number : readSelections(
                "Prioridades (ex.: 4,5): ", 5)) {

            query.addPriority(TaskPriority.fromValue(number));
        }

        LocalDate from = readDate(
                "Prazo inicial (AAAA-MM-DD, vazio para nenhum): "
        );

        LocalDate to = readDate(
                "Prazo final (AAAA-MM-DD, vazio para nenhum): "
        );

        query.setDueDateRange(from, to);

        System.out.println();
        System.out.println("Ordenação:");
        System.out.println("1 - Data de criação");
        System.out.println("2 - Prazo");
        System.out.println("3 - Prioridade");
        System.out.println("4 - Título");

        TaskSortField field;

        while (true) {
            int option = readInt("Opção: ");

            switch (option) {
                case 1:
                    field = TaskSortField.CREATED_AT;
                    break;
                case 2:
                    field = TaskSortField.DUE_DATE;
                    break;
                case 3:
                    field = TaskSortField.PRIORITY;
                    break;
                case 4:
                    field = TaskSortField.TITLE;
                    break;
                default:
                    System.out.println("[ERRO] Opção inválida.");
                    continue;
            }

            break;
        }

        query.setSorting(
                field,
                confirm("Ordenar em ordem crescente?")
        );

        return query;
    }

    private List<Integer> readSelections(
            String message,
            int maximum) {

        while (true) {
            String input = readText(message);

            List<Integer> values = new java.util.ArrayList<>();

            if (input.isBlank()) {
                return values;
            }

            boolean valid = true;

            for (String part : input.split(",")) {
                try {
                    int value = Integer.parseInt(part.trim());

                    if (value < 1 || value > maximum) {
                        valid = false;
                        break;
                    }

                    if (!values.contains(value)) {
                        values.add(value);
                    }

                } catch (NumberFormatException exception) {
                    valid = false;
                    break;
                }
            }

            if (valid) {
                return values;
            }

            System.out.println(
                    "[ERRO] Informe opções válidas separadas por vírgula."
            );
        }
    }

    private void printResults(List<Task> tasks) {
        System.out.println();
        System.out.println("========== RESULTADOS ==========");

        for (Task task : tasks) {
            System.out.println(
                    "[" + task.getId() + "] "
                    + task.getTitle()
                    + " | " + task.getState().getDisplayName()
                    + " | " + task.getPriority().getDisplayName()
                    + " | Prazo: "
                    + (task.getDueDate() == null
                            ? "Sem prazo"
                            : task.getDueDate())
            );
        }

        System.out.println("Total encontrado: " + tasks.size());
    }

    private LocalDate readDate(String message) {
        while (true) {
            String input = readText(message);

            if (input.isBlank()) {
                return null;
            }

            try {
                return LocalDate.parse(input);
            } catch (DateTimeParseException exception) {
                System.out.println("[ERRO] Data inválida.");
            }
        }
    }

    private boolean confirm(String message) {
        while (true) {
            String input = readText(message + " (s/n): ");

            if (input.equalsIgnoreCase("s")) {
                return true;
            }

            if (input.equalsIgnoreCase("n")) {
                return false;
            }

            System.out.println("[ERRO] Responda s ou n.");
        }
    }

    private String readRequired(String message) {
        while (true) {
            String input = readText(message);

            if (!input.isBlank()) {
                return input;
            }

            System.out.println("[ERRO] Campo obrigatório.");
        }
    }

    private String readText(String message) {
        System.out.print(message);
        return scanner.nextLine().trim();
    }

    private int readInt(String message) {
        while (true) {
            try {
                return Integer.parseInt(readText(message));
            } catch (NumberFormatException exception) {
                System.out.println("[ERRO] Informe um número inteiro.");
            }
        }
    }

    private long readLong(String message) {
        while (true) {
            try {
                return Long.parseLong(readText(message));
            } catch (NumberFormatException exception) {
                System.out.println("[ERRO] Informe um ID válido.");
            }
        }
    }
}


package com.taskmanager;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

import com.taskmanager.builder.TaskConfiguration;
import com.taskmanager.model.DeletionMode;
import com.taskmanager.model.RecurrenceType;
import com.taskmanager.model.RecurringTask;
import com.taskmanager.model.Task;
import com.taskmanager.model.TaskExecutionMode;
import com.taskmanager.model.TaskPriority;
import com.taskmanager.query.SavedTaskQuery;
import com.taskmanager.query.TaskQuery;
import com.taskmanager.query.TaskSortField;
import com.taskmanager.query.TaskStatus;
import com.taskmanager.service.TaskManagerFacade;

public class ConsoleMenu {

    private final TaskManagerFacade facade;
    private final Scanner scanner;
    private boolean running;
    private RecurrenceHistoryMenu recurrenceHistoryMenu;

    public ConsoleMenu(TaskManagerFacade facade) {
        if (facade == null) {
            throw new IllegalArgumentException(
                    "A Facade é obrigatória."
            );
        }

        this.facade = facade;
        this.scanner = new Scanner(System.in);
        this.running = true;
    }
    
	
	public void setRecurrenceHistoryMenu(
	        RecurrenceHistoryMenu recurrenceHistoryMenu) {
	
	    if (recurrenceHistoryMenu == null) {
	        throw new IllegalArgumentException(
	                "O menu de recorrências é obrigatório."
	        );
	    }
	
	    this.recurrenceHistoryMenu = recurrenceHistoryMenu;
	}


    public void start() {
        while (running) {
            System.out.println();
            System.out.println("========== TASKMANAGER ==========");
            System.out.println("1 - Listar tarefas");
            System.out.println("2 - Criar tarefa");
            System.out.println("3 - Gerenciar tarefa");
            System.out.println("4 - Consultas");
            System.out.println("5 - Recorrências e histórico");
            System.out.println("0 - Sair");

            int option = readInt("Escolha uma opção: ");

            try {
                switch (option) {
                    case 1:
                        listTasks();
                        break;
                    case 2:
                        createTaskMenu();
                        break;
                    case 3:
                        manageTaskMenu();
                        break;
                    case 4:
                        new QueryConsoleMenu(facade, scanner).start();
                        break;
                    case 5:
                        if (recurrenceHistoryMenu != null) {
                            recurrenceHistoryMenu.start();
                        }
                        break;
                    case 0:
                        running = false;
                        System.out.println("Encerrando o TaskManager...");
                        break;
                    default:
                        System.out.println("[ERRO] Opção inválida.");
                }
            } catch (IllegalArgumentException
                    | IllegalStateException exception) {
                System.out.println(
                        "[ERRO] " + exception.getMessage()
                );
            }
        }
    }
    
	
	public Scanner getScanner() {
	    return scanner;
	}


    private void listTasks() {
        System.out.println();
        System.out.println("========== TAREFAS ==========");

        if (facade.getRootTasks().isEmpty()) {
            System.out.println("Nenhuma tarefa cadastrada.");
            return;
        }

        for (Task task : facade.getRootTasks()) {
            printTask(task, 0);
        }
    }

    private void printTask(Task task, int level) {
        String indentation = "    ".repeat(level);

        System.out.println(
                indentation
                + "[" + task.getId() + "] "
                + task.getTitle()
                + " | " + task.getState().getDisplayName()
                + " | " + task.getPriority().getDisplayName()
                + " | Prazo: " + formatDate(task.getDueDate())
        );

        if (task instanceof RecurringTask) {
            RecurringTask recurring = (RecurringTask) task;

            System.out.println(
                    indentation
                    + "    Série #" + recurring.getRecurrenceId()
                    + " | Data programada: "
                    + formatDate(recurring.getScheduledDate())
            );
        }

        for (Task child : task.getSubtasks()) {
            printTask(child, level + 1);
        }
    }

    private void createTaskMenu() {
        System.out.println();
        System.out.println("========== CRIAR TAREFA ==========");
        System.out.println("1 - Tarefa normal");
        System.out.println("2 - Tarefa recorrente");
        System.out.println("3 - Subtarefa");
        System.out.println("0 - Voltar");

        int option = readInt("Opção: ");

        if (option == 0) {
            return;
        }

        if (option < 1 || option > 3) {
            System.out.println("[ERRO] Opção inválida.");
            return;
        }

        long parentId = 0;

        if (option == 3) {
            parentId = readLong("ID da tarefa principal: ");

            if (facade.findTaskById(parentId) == null) {
                System.out.println("[ERRO] Tarefa principal não encontrada.");
                return;
            }
        }

        String title = readRequired("Título: ");
        String description = readText("Descrição (opcional): ");

        TaskPriority priority = readPriority();

        LocalDate dueDate = readDate(
                "Prazo (AAAA-MM-DD, vazio para nenhum): ",
                option == 2
        );

        TaskExecutionMode mode = readExecutionMode();

        TaskConfiguration configuration =
                new TaskConfiguration.Builder(title)
                        .description(description)
                        .priority(priority)
                        .dueDate(dueDate)
                        .executionMode(mode)
                        .build();

        if (option == 1) {
            facade.createNormalTask(configuration);
        } else if (option == 2) {
            RecurrenceType type = readRecurrenceType();
            int interval = readPositiveInt(
                    "Repetir a cada quantos períodos? "
            );

            facade.createRecurringTask(
                    configuration,
                    type,
                    interval
            );
        } else {
            facade.createSubtask(parentId, configuration);
        }
    }

    private void manageTaskMenu() {
        long id = readLong("ID da tarefa: ");

        Task task = facade.findTaskById(id);

        if (task == null) {
            System.out.println("[ERRO] Tarefa não encontrada.");
            return;
        }

        boolean managing = true;

        while (managing) {
            task = facade.findTaskById(id);

            if (task == null) {
                return;
            }

            System.out.println();
            System.out.println("========== GERENCIAR TAREFA ==========");
            printTask(task, 0);

            System.out.println("1 - Ver detalhes");
            System.out.println("2 - Iniciar");
            System.out.println("3 - Concluir");
            System.out.println("4 - Reabrir");
            System.out.println("5 - Editar");
            System.out.println("6 - Excluir");
            System.out.println("7 - Criar subtarefa");

            if (task instanceof RecurringTask) {
                System.out.println("8 - Encerrar recorrência");
            }

            System.out.println("0 - Voltar");

            int option = readInt("Opção: ");

            try {
                switch (option) {
                    case 1:
                        showDetails(task);
                        break;
                    case 2:
                        facade.startTask(id);
                        break;
                    case 3:
                        facade.completeTask(id);
                        break;
					case 4:
						System.out.println("NOVO MENU DE CONSULTAS");
					    new QueryConsoleMenu(facade, scanner).start();
					    break;
                    case 5:
                        editTask(task);
                        break;
                    case 6:
                        deleteTask(task);
                        managing = false;
                        break;
                    case 7:
                        createSubtask(task);
                        break;
                    case 8:
                        if (task instanceof RecurringTask) {
                            endRecurrence((RecurringTask) task);
                        } else {
                            System.out.println("[ERRO] Opção inválida.");
                        }
                        break;
                    case 0:
                        managing = false;
                        break;
                    default:
                        System.out.println("[ERRO] Opção inválida.");
                }
            } catch (IllegalArgumentException
                    | IllegalStateException exception) {
                System.out.println(
                        "[ERRO] " + exception.getMessage()
                );
            }
        }
    }

    private void showDetails(Task task) {
        System.out.println();
        System.out.println("========== DETALHES ==========");
        System.out.println("ID: " + task.getId());
        System.out.println("Título: " + task.getTitle());
        System.out.println("Descrição: " + task.getDescription());
        System.out.println("Prioridade: "
                + task.getPriority().getDisplayName());
        System.out.println("Estado: "
                + task.getState().getDisplayName());
        System.out.println("Modo: "
                + task.getExecutionMode().getDisplayName());
        System.out.println("Prazo: "
                + formatDate(task.getDueDate()));
        System.out.println("Criada em: " + task.getCreatedAt());

        if (task.getParent() != null) {
            System.out.println(
                    "Tarefa principal: #" + task.getParent().getId()
            );
        }

        if (task instanceof RecurringTask) {
            RecurringTask recurring = (RecurringTask) task;

            System.out.println(
                    "Série recorrente: #" + recurring.getRecurrenceId()
            );
            System.out.println(
                    "Data programada: "
                    + formatDate(recurring.getScheduledDate())
            );
        }

        System.out.println("Subtarefas diretas: "
                + task.getSubtasks().size());
    }

    private void editTask(Task task) {
        System.out.println();
        System.out.println("========== EDITAR TAREFA ==========");

        System.out.println("Pressione Enter para manter título e descrição.");

        String title = readText(
                "Título atual (" + task.getTitle() + "): "
        );

        if (title.isBlank()) {
            title = task.getTitle();
        }

        String description = readText(
                "Descrição atual (" + task.getDescription() + "): "
        );

        if (description.isBlank()) {
            description = task.getDescription();
        }

        System.out.println(
                "Prioridade atual: "
                + task.getPriority().getDisplayName()
        );

        TaskPriority priority = readPriority();

        System.out.println(
                "Prazo atual: " + formatDate(task.getDueDate())
        );

        LocalDate dueDate = readDate(
                "Novo prazo (vazio mantém o atual): ",
                false
        );

        if (dueDate == null) {
            dueDate = task.getDueDate();
        }

        if (task instanceof RecurringTask) {
            System.out.println("1 - Alterar somente esta ocorrência");
            System.out.println("2 - Alterar esta e as futuras");

            int scope = readInt("Opção: ");

            if (scope == 2) {
                facade.updateRecurringTaskAndFuture(
                        task.getId(),
                        title,
                        description,
                        priority,
                        dueDate
                );
            } else if (scope == 1) {
                facade.updateTask(
                        task.getId(),
                        title,
                        description,
                        priority,
                        dueDate
                );
            } else {
                System.out.println("[ERRO] Opção inválida.");
            }

            return;
        }

        facade.updateTask(
                task.getId(),
                title,
                description,
                priority,
                dueDate
        );
    }

    private void createSubtask(Task parent) {
        String title = readRequired("Título da subtarefa: ");
        String description = readText("Descrição: ");

        TaskPriority priority = readPriority();

        LocalDate dueDate = readDate(
                "Prazo (AAAA-MM-DD, vazio para nenhum): ",
                false
        );

        TaskExecutionMode mode = readExecutionMode();

        TaskConfiguration configuration =
                new TaskConfiguration.Builder(title)
                        .description(description)
                        .priority(priority)
                        .dueDate(dueDate)
                        .executionMode(mode)
                        .build();

        facade.createSubtask(parent.getId(), configuration);
    }

    private void deleteTask(Task task) {
        System.out.println();
        System.out.println("========== EXCLUIR TAREFA ==========");
        System.out.println("1 - Preservar subtarefas como raízes");
        System.out.println("2 - Promover subtarefas");
        System.out.println("3 - Excluir toda a hierarquia");
        System.out.println("0 - Cancelar");

        int option = readInt("Opção: ");

        DeletionMode mode;

        switch (option) {
            case 1:
                mode = DeletionMode.MAKE_ROOT;
                break;
            case 2:
                mode = DeletionMode.PROMOTE;
                break;
            case 3:
                mode = DeletionMode.CASCADE;
                break;
            default:
                return;
        }

        if (mode == DeletionMode.CASCADE
                && !readConfirmation(
                        "Excluir esta tarefa e todas as subtarefas?"
                )) {
            return;
        }

        if (task instanceof RecurringTask) {
            facade.deleteRecurringOccurrence(task.getId(), mode);
        } else {
            facade.deleteTask(task.getId(), mode);
        }
    }

    private void endRecurrence(RecurringTask task) {
        if (readConfirmation(
                "Encerrar a série #" + task.getRecurrenceId()
                        + " e preservar as ocorrências existentes?"
        )) {
            facade.endRecurrence(task.getRecurrenceId());
        }
    }

    private void queryMenu() {
        boolean consulting = true;

        while (consulting) {
            System.out.println();
            System.out.println("========== CONSULTAS ==========");
            System.out.println("1 - Pesquisar tarefas");
            System.out.println("2 - Listar consultas salvas");
            System.out.println("3 - Executar consulta salva");
            System.out.println("4 - Excluir consulta salva");
            System.out.println("0 - Voltar");

            int option = readInt("Opção: ");

            try {
                switch (option) {
                    case 1:
                        executeQuery();
                        break;
                    case 2:
                        listSavedQueries();
                        break;
                    case 3:
                        executeSavedQuery();
                        break;
                    case 4:
                        deleteSavedQuery();
                        break;
                    case 0:
                        consulting = false;
                        break;
                    default:
                        System.out.println("[ERRO] Opção inválida.");
                }
            } catch (IllegalArgumentException
                    | IllegalStateException exception) {
                System.out.println(
                        "[ERRO] " + exception.getMessage()
                );
            }
        }
    }

    private void executeQuery() {
        TaskQuery query = new TaskQuery();

        System.out.println();
        System.out.println("========== FILTROS ==========");
        System.out.println("Estado:");
        System.out.println("0 - Todos");
        System.out.println("1 - Pendente");
        System.out.println("2 - Em andamento");
        System.out.println("3 - Concluída");

        int status = readInt("Opção: ");

        switch (status) {
            case 1:
                query.addStatus(TaskStatus.PENDING);
                break;
            case 2:
                query.addStatus(TaskStatus.IN_PROGRESS);
                break;
            case 3:
                query.addStatus(TaskStatus.COMPLETED);
                break;
            case 0:
                break;
            default:
                throw new IllegalArgumentException(
                        "Estado inválido."
                );
        }

        System.out.println("Prioridade: 0 para todas, 1 a 5 para filtrar.");

        int priority = readInt("Prioridade: ");

        if (priority != 0) {
            query.addPriority(TaskPriority.fromValue(priority));
        }

        LocalDate from = readDate(
                "Prazo inicial (vazio para nenhum): ",
                false
        );

        LocalDate to = readDate(
                "Prazo final (vazio para nenhum): ",
                false
        );

        query.setDueDateRange(from, to);

        System.out.println("Ordenação:");
        System.out.println("1 - Data de criação");
        System.out.println("2 - Prazo");
        System.out.println("3 - Prioridade");
        System.out.println("4 - Título");

        int field = readInt("Opção: ");

        TaskSortField sortField;

        switch (field) {
            case 1:
                sortField = TaskSortField.CREATED_AT;
                break;
            case 2:
                sortField = TaskSortField.DUE_DATE;
                break;
            case 3:
                sortField = TaskSortField.PRIORITY;
                break;
            case 4:
                sortField = TaskSortField.TITLE;
                break;
            default:
                throw new IllegalArgumentException(
                        "Ordenação inválida."
                );
        }

        query.setSorting(
                sortField,
                readConfirmation("Ordem crescente?")
        );

        printResults(facade.searchTasks(query));

        if (readConfirmation("Salvar esta consulta?")) {
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

        long id = readLong("ID da consulta: ");

        printResults(facade.executeSavedQuery(id));
    }

    private void deleteSavedQuery() {
        listSavedQueries();

        long id = readLong("ID da consulta: ");

        if (readConfirmation("Excluir esta consulta salva?")) {
            facade.deleteSavedQuery(id);
            System.out.println("Consulta excluída.");
        }
    }

    private void printResults(List<Task> results) {
        System.out.println();
        System.out.println("========== RESULTADOS ==========");

        for (Task task : results) {
            printTaskSummary(task);
        }

        System.out.println("Total encontrado: " + results.size());
    }

    private void printTaskSummary(Task task) {
        System.out.println(
                "[" + task.getId() + "] "
                + task.getTitle()
                + " | " + task.getState().getDisplayName()
                + " | " + task.getPriority().getDisplayName()
                + " | Prazo: " + formatDate(task.getDueDate())
        );
    }

    private TaskPriority readPriority() {
        while (true) {
            System.out.println();
            System.out.println("Prioridade:");
            System.out.println("1 - Muito baixa");
            System.out.println("2 - Baixa");
            System.out.println("3 - Média");
            System.out.println("4 - Alta");
            System.out.println("5 - Muito alta");

            int value = readInt("Opção: ");

            if (value >= 1 && value <= 5) {
                return TaskPriority.fromValue(value);
            }

            System.out.println("[ERRO] Prioridade inválida.");
        }
    }

    private TaskExecutionMode readExecutionMode() {
        while (true) {
            System.out.println("Modo de execução:");
            System.out.println("1 - Direto");
            System.out.println("2 - Acompanhado");

            int option = readInt("Opção: ");

            if (option == 1) {
                return TaskExecutionMode.DIRECT;
            }

            if (option == 2) {
                return TaskExecutionMode.TRACKED;
            }

            System.out.println("[ERRO] Opção inválida.");
        }
    }

    private RecurrenceType readRecurrenceType() {
        while (true) {
            System.out.println("Tipo de recorrência:");
            System.out.println("1 - Diária");
            System.out.println("2 - Semanal");
            System.out.println("3 - Mensal");

            int option = readInt("Opção: ");

            switch (option) {
                case 1:
                    return RecurrenceType.DAILY;
                case 2:
                    return RecurrenceType.WEEKLY;
                case 3:
                    return RecurrenceType.MONTHLY;
                default:
                    System.out.println("[ERRO] Opção inválida.");
            }
        }
    }

    private LocalDate readDate(String message, boolean required) {
        while (true) {
            String text = readText(message);

            if (text.isBlank() && !required) {
                return null;
            }

            try {
                return LocalDate.parse(text);
            } catch (DateTimeParseException exception) {
                System.out.println(
                        "[ERRO] Informe uma data válida no formato AAAA-MM-DD."
                );
            }
        }
    }

    private String readRequired(String message) {
        while (true) {
            String value = readText(message);

            if (!value.isBlank()) {
                return value;
            }

            System.out.println("[ERRO] Este campo é obrigatório.");
        }
    }

    private String readText(String message) {
        System.out.print(message);
        return scanner.nextLine().trim();
    }

    private int readInt(String message) {
        while (true) {
            String text = readText(message);

            try {
                return Integer.parseInt(text);
            } catch (NumberFormatException exception) {
                System.out.println("[ERRO] Digite um número inteiro.");
            }
        }
    }

    private long readLong(String message) {
        while (true) {
            String text = readText(message);

            try {
                return Long.parseLong(text);
            } catch (NumberFormatException exception) {
                System.out.println("[ERRO] Digite um identificador válido.");
            }
        }
    }

    private int readPositiveInt(String message) {
        while (true) {
            int value = readInt(message);

            if (value > 0) {
                return value;
            }

            System.out.println(
                    "[ERRO] O número deve ser maior que zero."
            );
        }
    }

    private boolean readConfirmation(String message) {
        while (true) {
            String answer = readText(message + " (s/n): ");

            if (answer.equalsIgnoreCase("s")) {
                return true;
            }

            if (answer.equalsIgnoreCase("n")) {
                return false;
            }

            System.out.println("[ERRO] Responda com s ou n.");
        }
    }

    private String formatDate(LocalDate date) {
        return date == null ? "Sem prazo" : date.toString();
    }
}

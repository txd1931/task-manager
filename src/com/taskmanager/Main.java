
package com.taskmanager;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Scanner;

import com.taskmanager.persistence.ApplicationData;
import com.taskmanager.persistence.ApplicationPersistence;

public class Main {

    public static void main(String[] args) {

        ApplicationPersistence persistence =
                new ApplicationPersistence(
                        Path.of(
                                System.getProperty("user.home"),
                                ".taskmanager",
                                "taskmanager.json"
                        )
                );

        try {
            ApplicationData data = persistence.load();

            ApplicationContext context =
                    new ApplicationContext(data, persistence);

            ConsoleMenu menu = new ConsoleMenu(
                    context.getFacade()
            );

            RecurrenceHistoryMenu extraMenu =
                    new RecurrenceHistoryMenu(
                            data,
                            persistence,
                            menu.getScanner()
                    );

            menu.setRecurrenceHistoryMenu(extraMenu);

            menu.start();

        } catch (IOException exception) {
            System.out.println(
                    "[ERRO] Não foi possível carregar os dados: "
                    + exception.getMessage()
            );
        }
    }
}

package com.taskmanager.persistence;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class JsonStorage {

    private final Path filePath;
    private final Gson gson;

    public JsonStorage(Path filePath) {
        if (filePath == null) {
            throw new IllegalArgumentException(
                    "O caminho do arquivo é obrigatório."
            );
        }

        this.filePath = filePath.toAbsolutePath();
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();
    }

    public JsonObject load() throws IOException {
        if (!Files.exists(filePath)) {
            return null;
        }

        String content = Files.readString(
                filePath,
                StandardCharsets.UTF_8
        );

        try {
            JsonElement element = JsonParser.parseString(content);

            if (!element.isJsonObject()) {
                throw new IOException(
                        "O arquivo não contém um objeto JSON válido."
                );
            }

            return element.getAsJsonObject();

        } catch (RuntimeException exception) {
            throw new IOException(
                    "Não foi possível interpretar o arquivo JSON.",
                    exception
            );
        }
    }

    public void save(JsonObject data) throws IOException {
        if (data == null) {
            throw new IllegalArgumentException(
                    "Os dados não podem ser nulos."
            );
        }

        Path directory = filePath.getParent();

        Files.createDirectories(directory);

        Path temporaryFile = Files.createTempFile(
                directory,
                "taskmanager-",
                ".tmp"
        );

        try {
            Files.writeString(
                    temporaryFile,
                    gson.toJson(data),
                    StandardCharsets.UTF_8
            );

            try {
                Files.move(
                        temporaryFile,
                        filePath,
                        StandardCopyOption.ATOMIC_MOVE,
                        StandardCopyOption.REPLACE_EXISTING
                );

            } catch (AtomicMoveNotSupportedException exception) {
                Files.move(
                        temporaryFile,
                        filePath,
                        StandardCopyOption.REPLACE_EXISTING
                );
            }

        } finally {
            Files.deleteIfExists(temporaryFile);
        }
    }

    public Path getFilePath() {
        return filePath;
    }
}

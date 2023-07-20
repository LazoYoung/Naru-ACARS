package com.flylazo.naru_acars.servlet.repository;

import com.flylazo.naru_acars.NaruACARS;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public abstract class CSV<T> {
    protected final Logger logger = Logger.getLogger(NaruACARS.class.getName());
    protected File file;
    protected String fileName;
    private final Map<String, T> data = new HashMap<>();

    protected CSV(String fileName, int primaryColumn) {
        this(fileName, primaryColumn, 0);
    }

    protected CSV(String fileName, int primaryColumn, int skipLines) {
        this.fileName = fileName;

        try {
            file = copyCSV(false);
        } catch (RuntimeException e) {
            logger.log(Level.SEVERE, "Failed to copy " + fileName, e);
        }

        try (
                var fileReader = new FileReader(file);
                var csv = new CSVReader(fileReader)
        ) {
            String[] line;
            csv.skip(skipLines);

            while ((line = csv.readNext()) != null) {
                var primaryKey = line[primaryColumn].toLowerCase();
                if (!primaryKey.isBlank()) {
                    if (!data.containsKey(primaryKey)) {
                        T obj = parseLine(line);
                        data.put(primaryKey, obj);
                    }
                }
            }
        } catch (IOException | CsvValidationException e) {
            logger.log(Level.SEVERE, "File corrupted: " + fileName, e);
        }
    }

    public File copyCSV(boolean overwrite) throws RuntimeException {
        var resource = NaruACARS.getFlatResource(fileName);

        try (var stream = resource.getInputStream()) {
            var dest = NaruACARS.getDirectory().resolve(fileName);
            var destFile = dest.toFile();

            if (overwrite || !destFile.exists()) {
                Files.copy(stream, dest, REPLACE_EXISTING);
            }
            return destFile;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected T get(String primaryKey) {
        return (primaryKey != null) ? data.get(primaryKey.toLowerCase()) : null;
    }

    protected abstract T parseLine(String[] line);
}

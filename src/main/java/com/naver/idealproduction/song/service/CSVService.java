package com.naver.idealproduction.song.service;

import com.naver.idealproduction.song.SimOverlayNG;
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

public abstract class CSVService<T> {
    protected final Logger logger = Logger.getLogger(SimOverlayNG.class.getName());
    protected File file;
    protected String fileName;
    private final Map<String, Integer> rowLocator = new HashMap<>();

    protected CSVService(String fileName, int primaryColumn) {
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
            int row = 0;

            while ((line = csv.readNext()) != null) {
                var primaryKey = line[primaryColumn].toLowerCase();
                if (!primaryKey.isBlank()) {
                    rowLocator.put(primaryKey, row);
                }
                ++row;
            }
        } catch (IOException | CsvValidationException e) {
            logger.log(Level.SEVERE, "File corrupted: " + fileName, e);
        }
    }

    public File copyCSV(boolean overwrite) throws RuntimeException {
        var resource = SimOverlayNG.getResource(fileName);

        try (var stream = resource.getInputStream()) {
            var dest = SimOverlayNG.getDirectory().resolve(fileName);
            var destFile = dest.toFile();

            if (overwrite || !destFile.exists()) {
                Files.copy(stream, dest, REPLACE_EXISTING);
            }
            return destFile;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // todo Cache into memory (92% CPU load)
    protected T get(String primaryKey) {
        if (primaryKey == null || primaryKey.isBlank()) {
            return null;
        }

        Integer row = rowLocator.get(primaryKey.toLowerCase());

        if (file == null || row == null) {
            return null;
        }

        try (
                var fileReader = new FileReader(file);
                var csv = new CSVReader(fileReader)
        ) {
            csv.skip(row);
            return parseLine(csv.readNext());
        } catch (IOException | CsvValidationException e) {
            logger.log(Level.SEVERE, "File corrupted: " + fileName, e);
            return null;
        }
    }

    protected abstract T parseLine(String[] line);
}

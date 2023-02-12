package com.naver.idealproduction.song.service;

import com.naver.idealproduction.song.SimOverlayNG;
import com.naver.idealproduction.song.entity.Airport;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.util.logging.Level.SEVERE;

public class AirportService {
    private enum DataType {
        ICAO("gps_code"),
        IATA("iata_code"),
        NAME("name"),
        CITY("municipality"),
        LATITUDE("latitude_deg"),
        LONGITUDE("longitude_deg");

        private final String column;

        DataType(String column) {
            this.column = column;
        }

        public String getColumn() {
            return column;
        }
    }

    private final Logger logger = Logger.getLogger(SimOverlayNG.class.getName());
    private final Map<String, Integer> rowLocator = new HashMap<>();
    private final Map<DataType, Integer> columnLocator = new HashMap<>();
    private final String fileName = "airports.csv";
    private File file = null;

    public AirportService() {
        try {
            file = copyCSV(false);
        } catch (RuntimeException e) {
            logger.log(SEVERE, "Failed to copy file: " + fileName, e);
            return;
        }

        try (var reader = new FileReader(file, UTF_8);
             CSVReader csv = new CSVReader(reader)) {
            String[] firstLine = csv.readNext();
            String[] line;

            for (int i = 0; i < firstLine.length; ++i) {
                mapColumnLocator(i, firstLine[i]);
            }

            int row = 1;

            while ((line = csv.readNext()) != null) {
                int idx = columnLocator.get(DataType.ICAO);
                String icao = line[idx].toLowerCase();
                rowLocator.put(icao, row++);
            }
        } catch (IOException | CsvValidationException e) {
            logger.log(SEVERE, "Failed to read file: " + fileName, e);
        }
    }

    public Airport get(String icao) {
        if (file == null || icao.isBlank()) {
            return null;
        }

        Integer row = rowLocator.get(icao.toLowerCase());
        Airport airport = null;

        if (row != null) {
            try (var reader = new FileReader(file, UTF_8);
                 var csv = new CSVReader(reader)) {
                csv.skip(row);
                airport = parseLine(csv.readNext());
            } catch (IOException | CsvValidationException e) {
                logger.log(SEVERE, "Failed to read file: " + fileName, e);
            }
        }
        return airport;
    }

    private File copyCSV(boolean overwrite) throws RuntimeException {
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

    private Airport parseLine(String[] line) {
        String icao = line[columnLocator.get(DataType.ICAO)];
        String iata = line[columnLocator.get(DataType.IATA)];
        String name = line[columnLocator.get(DataType.NAME)];
        String city = line[columnLocator.get(DataType.CITY)];
        double latitude = Double.parseDouble(line[columnLocator.get(DataType.LATITUDE)]);
        double longitude = Double.parseDouble(line[columnLocator.get(DataType.LONGITUDE)]);
        return new Airport(icao, iata, name, city, latitude, longitude);
    }

    private void mapColumnLocator(int index, String column) {
        for (var type : DataType.values()) {
            if (type.getColumn().equals(column)) {
                columnLocator.put(type, index);
                return;
            }
        }
    }
}

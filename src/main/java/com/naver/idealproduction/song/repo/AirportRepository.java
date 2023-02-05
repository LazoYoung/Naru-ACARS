package com.naver.idealproduction.song.repo;

import com.naver.idealproduction.song.entity.Airport;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class AirportRepository {

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

    private final Map<String, Integer> rowLocator = new HashMap<>();
    private final Map<DataType, Integer> columnLocator = new HashMap<>();

    public AirportRepository() {
        File file;

        try {
            file = getCSVFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try (FileReader reader = new FileReader(file)) {
            CSVReader csv = new CSVReader(reader);
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
            throw new RuntimeException(e);
        }
    }

    public Airport get(String icao) {
        Integer row = rowLocator.get(icao.toLowerCase());
        File file;

        if (icao.isBlank() || row == null) {
            return null;
        }

        try {
            file = getCSVFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try (FileReader reader = new FileReader(file)) {
            CSVReader csv = new CSVReader(reader);
            csv.skip(row);
            return parseLine(csv.readNext());
        } catch (IOException | CsvValidationException e) {
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

    private File getCSVFile() throws IOException {
        var resource = new ClassPathResource("airports.csv");
        return resource.getFile();
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

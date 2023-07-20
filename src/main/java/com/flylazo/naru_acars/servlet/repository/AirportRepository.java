package com.flylazo.naru_acars.servlet.repository;

import com.flylazo.naru_acars.domain.Airport;
import org.springframework.stereotype.Repository;

@Repository
public class AirportRepository extends CSV<Airport> {
    private static final int COL_ICAO = 12;
    private static final int COL_IATA = 13;
    private static final int COL_NAME = 3;
    private static final int COL_CITY = 10;
    private static final int COL_LATITUDE = 4;
    private static final int COL_LONGITUDE = 5;


    public AirportRepository() {
        super("airports.csv", COL_ICAO, 1);
    }

    public Airport get(String icao) {
        return super.get(icao);
    }

    @Override
    protected Airport parseLine(String[] line) {
        String icao = line[COL_ICAO];
        String iata = line[COL_IATA];
        String name = line[COL_NAME];
        String city = line[COL_CITY];
        double latitude = Double.parseDouble(line[COL_LATITUDE]);
        double longitude = Double.parseDouble(line[COL_LONGITUDE]);
        return new Airport(icao, iata, name, city, latitude, longitude);
    }
}

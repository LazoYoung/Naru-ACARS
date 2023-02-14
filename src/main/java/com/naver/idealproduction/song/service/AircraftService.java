package com.naver.idealproduction.song.service;

import com.naver.idealproduction.song.entity.Aircraft;
import org.springframework.stereotype.Service;

@Service
public class AircraftService extends CSVService<Aircraft> {
    private static final int COL_NAME = 0;
    private static final int COL_ICAO = 2;

    protected AircraftService() {
        super("planes.dat", COL_ICAO);
    }

    public Aircraft get(String icao) {
        return super.get(icao);
    }

    @Override
    protected Aircraft parseLine(String[] line) {
        var aircraft = new Aircraft();
        aircraft.setName(line[COL_NAME]);
        aircraft.setIcaoCode(line[COL_ICAO]);
        return aircraft;
    }
}

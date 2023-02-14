package com.naver.idealproduction.song.service;

import com.naver.idealproduction.song.entity.Airline;
import org.springframework.stereotype.Service;

@Service
public class AirlineService extends CSVService<Airline> {
    private static final int COL_NAME = 1;
    private static final int COL_IATA = 3;
    private static final int COL_ICAO = 4;
    private static final int COL_CALLSIGN = 5;

    public AirlineService() {
        super("airlines.dat", COL_ICAO);
    }

    public Airline get(String icao) {
        return super.get(icao);
    }

    @Override
    protected Airline parseLine(String[] line) {
        var airline = new Airline();
        airline.setName(line[COL_NAME]);
        airline.setIata(line[COL_IATA]);
        airline.setIcao(line[COL_ICAO]);
        airline.setCallsign(line[COL_CALLSIGN]);
        return airline;
    }
}

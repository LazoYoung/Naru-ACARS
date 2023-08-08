package com.flylazo.naru_acars.servlet.repository;

import com.flylazo.naru_acars.domain.Airline;
import jakarta.annotation.Nullable;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class AirlineRepository extends CSV<Airline> {
    private static final int COL_NAME = 1;
    private static final int COL_IATA = 3;
    private static final int COL_ICAO = 4;
    private static final int COL_CALLSIGN = 5;

    public AirlineRepository() {
        super("airlines.dat", COL_ICAO, 1);
    }

    /**
     * @deprecated replaced with {@link AirlineRepository#find(String)}
     */
    @Nullable
    @Deprecated
    public Airline get(String icao) {
        return super.get(icao);
    }

    public Optional<Airline> find(String icao) {
        return Optional.ofNullable(super.get(icao));
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

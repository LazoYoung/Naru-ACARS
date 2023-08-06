package com.flylazo.naru_acars.servlet.repository;

import com.flylazo.naru_acars.domain.Aircraft;
import jakarta.annotation.Nullable;
import org.springframework.stereotype.Repository;

@Repository
public class AircraftRepository extends CSV<Aircraft> {
    private static final int COL_NAME = 0;
    private static final int COL_ICAO = 2;

    protected AircraftRepository() {
        super("planes.dat", COL_ICAO);
    }

    @Nullable
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

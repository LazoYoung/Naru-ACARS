package com.flylazo.naru_acars.servlet.repository;

import com.flylazo.naru_acars.domain.Aircraft;
import jakarta.annotation.Nullable;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class AircraftRepository extends CSV<Aircraft> {
    private static final int COL_NAME = 0;
    private static final int COL_ICAO = 2;

    protected AircraftRepository() {
        super("planes.dat", COL_ICAO);
    }

    /**
     * @deprecated replaced with {@link AircraftRepository#find(String)}
     */
    @Nullable
    @Deprecated
    public Aircraft get(String icao) {
        return super.get(icao);
    }

    public Optional<Aircraft> find(String icao) {
        return Optional.ofNullable(super.get(icao));
    }

    @Override
    protected Aircraft parseLine(String[] line) {
        var aircraft = new Aircraft();
        aircraft.setName(line[COL_NAME]);
        aircraft.setIcaoCode(line[COL_ICAO]);
        return aircraft;
    }
}

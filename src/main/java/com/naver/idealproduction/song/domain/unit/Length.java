package com.naver.idealproduction.song.domain.unit;

import java.math.BigDecimal;
import java.math.MathContext;

public enum Length {
    METER(0.000539957),
    KILOMETER(0.539957),
    FEET(0.000164579),
    NAUTICAL_MILE(1.0);

    private final double ratio;

    Length(double ratio) {
        this.ratio = ratio;
    }

    @SuppressWarnings("DuplicatedCode")
    public Double convertTo(Length unit, double value) {
        if (this == unit) {
            return value;
        }

        var ctx = MathContext.DECIMAL64;
        var dividend = BigDecimal.valueOf(unit.ratio);
        var divisor = BigDecimal.valueOf(this.ratio);
        var bigValue = BigDecimal.valueOf(value);
        return dividend.divide(divisor, ctx).multiply(bigValue, ctx).doubleValue();
    }

    public double getDistance(double lat1, double lon1, double lat2, double lon2) {
        lat1 = Math.toRadians(lat1);
        lon1 = Math.toRadians(lon1);
        lat2 = Math.toRadians(lat2);
        lon2 = Math.toRadians(lon2);
        var dLat = lat2 - lat1;
        var dLon = lon2 - lon1;
        var a = Math.pow(Math.sin(dLat / 2), 2) + Math.cos(lat1) * Math.cos(lat2) * Math.pow(Math.sin(dLon / 2), 2);
        var km = 2 * 6371 * Math.asin(Math.sqrt(a));
        return Length.KILOMETER.convertTo(this, km);
    }

}

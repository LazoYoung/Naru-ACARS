package com.naver.idealproduction.song.unit;

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

}

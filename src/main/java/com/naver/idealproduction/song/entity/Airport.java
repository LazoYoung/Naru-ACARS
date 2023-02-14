package com.naver.idealproduction.song.entity;

public class Airport {
    private final String icao;
    private final String iata;
    private final String name;
    private final String city;
    private final Double latitude;
    private final Double longitude;

    public Airport(String icao, String iata, String name, String city, Double latitude, Double longitude) {
        this.icao = icao;
        this.iata = iata;
        this.name = name.replaceAll("([Ii]nternational)", "Int'l");
        this.city = city;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getIcao() {
        return icao;
    }

    public String getIata() {
        return iata;
    }

    public String getName() {
        return name;
    }

    public String getCity() {
        return city;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }
}
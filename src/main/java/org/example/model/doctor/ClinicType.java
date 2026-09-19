package org.example.model.doctor;

public enum ClinicType {

    GRUNWALDZSK("Grunwaldzka", "Gdańsk - al. Grunwaldzka"),
    ZWYCZIENSTWA("Zwycięstwa", "Gdańsk - al. Zwycięstwa"),
    JASKOWADOLINA("Jaśkowa Dolina", "Gdańsk - ul. Jaśkowa Dolina"),
    MORSKA("Morska", "Gdynia - ul. Morska");

    private final String shortName;
    private final String fullAddress;

    ClinicType(String shortName, String fullAddress) {
        this.shortName = shortName;
        this.fullAddress = fullAddress;
    }

    public String getShortName() {
        return shortName;
    }

    public String getFullAddress() {
        return fullAddress;
    }
}

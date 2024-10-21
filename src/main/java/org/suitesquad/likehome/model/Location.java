package org.suitesquad.likehome.model;

public class Location {
    private String streetAddress;
    private String city;
    private String state;
    private String zipCode;
    private String country;
    private Coordinates coordinates;

    public static class Coordinates {
        private double latitude;
        private double longitude;
    }
}
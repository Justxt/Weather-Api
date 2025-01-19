package com.justxt.apiweather.userRequest;

public class GeocodeResult {
    private double latitude; // Latitud del lugar
    private double longitude;   //Longitud del lugar

    public GeocodeResult(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // Getters
    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}
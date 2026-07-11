package com.justxt.apiweather.userRequest;

public class GeocodeResult {
    private final double latitude;
    private final double longitude;

    public GeocodeResult(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}

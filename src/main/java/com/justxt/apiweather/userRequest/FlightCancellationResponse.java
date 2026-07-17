package com.justxt.apiweather.userRequest;

public class FlightCancellationResponse {
    private final String riskLevel;
    private final String message;
    private final WeatherDetails weatherDetails;
    private final double latitude;
    private final double longitude;

    public FlightCancellationResponse(String riskLevel,
                                      String message,
                                      WeatherDetails weatherDetails,
                                      double latitude,
                                      double longitude) {
        this.riskLevel = riskLevel;
        this.message = message;
        this.weatherDetails = weatherDetails;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public String getMessage() {
        return message;
    }

    public WeatherDetails getWeatherDetails() {
        return weatherDetails;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}

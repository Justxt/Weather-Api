package com.justxt.apiweather.userRequest;

public class FlightCancellationResponse {
    private String riskLevel; // Este sera el riesgo: Alta, Moderada, Baja
    private String message;   // Mensaje de la probabilidad
    private WeatherDetails weatherDetails; // Detalles del clima
    private double latitude;  // Latitud del lugar
    private double longitude; // Longitud del lugar

    // Constructor, Getters y Setters
    public FlightCancellationResponse(String riskLevel, String message, WeatherDetails weatherDetails, double latitude, double longitude) {
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
package com.justxt.apiweather.userRequest;

public class WeatherDetails {
    private double windSpeed;  // Velocidad del viento
    private double precipitation; // Precipitación en mm
    private double visibility;    // Visibilidad en km
    private int cloudCover;       // Cobertura de las nubes en %

    public WeatherDetails(double windSpeed, double precipitation, double visibility, int cloudCover) {
        this.windSpeed = windSpeed;
        this.precipitation = precipitation;
        this.visibility = visibility;
        this.cloudCover = cloudCover;
    }

    // Getters
    public double getWindSpeed() {
        return windSpeed;
    }

    public double getPrecipitation() {
        return precipitation;
    }

    public double getVisibility() {
        return visibility;
    }

    public int getCloudCover() {
        return cloudCover;
    }
}
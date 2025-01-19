package com.justxt.apiweather.userRequest;

public class FlightRequest {
    private String city; // Ciudad de destino
    private String date; // Fecha con formato "YYYY-MM-DD"

    // Getters y Setters
    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
package com.justxt.apiweather.userRequest;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class FlightRequest {
    @NotBlank(message = "La ciudad es obligatoria.")
    @Size(max = 100, message = "La ciudad no puede superar 100 caracteres.")
    private String city; // Ciudad de destino
    @NotBlank(message = "La fecha es obligatoria.")
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "La fecha debe usar el formato YYYY-MM-DD.")
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

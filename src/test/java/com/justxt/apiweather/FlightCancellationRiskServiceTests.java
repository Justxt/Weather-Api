package com.justxt.apiweather;

import com.justxt.apiweather.userRequest.WeatherDetails;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FlightCancellationRiskServiceTests {

    private final FlightCancellationRiskService service = new FlightCancellationRiskService();

    @Test
    void shouldReturnHighRiskWhenWeatherIsSevere() {
        WeatherDetails weather = new WeatherDetails(55.0, 12.0, 0.8, 95);

        String risk = service.evaluateRisk(weather);

        assertEquals("Alta probabilidad de cancelación", risk);
    }

    @Test
    void shouldReturnModerateRiskWhenWeatherHasIntermediateConditions() {
        WeatherDetails weather = new WeatherDetails(35.0, 2.0, 4.0, 70);

        String risk = service.evaluateRisk(weather);

        assertEquals("Probabilidad moderada de cancelación", risk);
    }

    @Test
    void shouldReturnLowRiskWhenWeatherIsFavorable() {
        WeatherDetails weather = new WeatherDetails(12.0, 0.1, 10.0, 20);

        String risk = service.evaluateRisk(weather);

        assertEquals("Baja probabilidad de cancelación", risk);
    }
}

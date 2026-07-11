package com.justxt.apiweather;

import com.justxt.apiweather.userRequest.WeatherDetails;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FlightCancellationRiskServiceTests {

    private static final String HIGH_RISK = "Alta probabilidad de cancelaci\u00f3n";
    private static final String MODERATE_RISK = "Probabilidad moderada de cancelaci\u00f3n";
    private static final String LOW_RISK = "Baja probabilidad de cancelaci\u00f3n";

    private final FlightCancellationRiskService service = new FlightCancellationRiskService();

    @Test
    void shouldReturnHighRiskWhenWeatherIsSevere() {
        WeatherDetails weather = new WeatherDetails(55.0, 12.0, 0.8, 95);

        String risk = service.evaluateRisk(weather);

        assertEquals(HIGH_RISK, risk);
    }

    @Test
    void shouldReturnHighRiskWhenVisibilityIsSevere() {
        WeatherDetails weather = new WeatherDetails(10.0, 1.0, 0.5, 20);

        String risk = service.evaluateRisk(weather);

        assertEquals(HIGH_RISK, risk);
    }

    @Test
    void shouldReturnHighRiskWhenPrecipitationIsSevere() {
        WeatherDetails weather = new WeatherDetails(10.0, 12.0, 8.0, 20);

        String risk = service.evaluateRisk(weather);

        assertEquals(HIGH_RISK, risk);
    }

    @Test
    void shouldReturnModerateRiskWhenWeatherHasIntermediateConditions() {
        WeatherDetails weather = new WeatherDetails(35.0, 2.0, 4.0, 70);

        String risk = service.evaluateRisk(weather);

        assertEquals(MODERATE_RISK, risk);
    }

    @Test
    void shouldReturnModerateRiskWhenVisibilityIsIntermediate() {
        WeatherDetails weather = new WeatherDetails(20.0, 2.0, 4.0, 70);

        String risk = service.evaluateRisk(weather);

        assertEquals(MODERATE_RISK, risk);
    }

    @Test
    void shouldReturnModerateRiskWhenPrecipitationIsIntermediate() {
        WeatherDetails weather = new WeatherDetails(20.0, 6.0, 8.0, 70);

        String risk = service.evaluateRisk(weather);

        assertEquals(MODERATE_RISK, risk);
    }

    @Test
    void shouldReturnLowRiskWhenWeatherIsFavorable() {
        WeatherDetails weather = new WeatherDetails(12.0, 0.1, 10.0, 20);

        String risk = service.evaluateRisk(weather);

        assertEquals(LOW_RISK, risk);
    }
}

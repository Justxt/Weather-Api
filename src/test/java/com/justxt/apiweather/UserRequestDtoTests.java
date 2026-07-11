package com.justxt.apiweather;

import com.justxt.apiweather.userRequest.FlightCancellationResponse;
import com.justxt.apiweather.userRequest.FlightRequest;
import com.justxt.apiweather.userRequest.GeocodeResult;
import com.justxt.apiweather.userRequest.WeatherDetails;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class UserRequestDtoTests {

    @Test
    void shouldReadAndWriteFlightRequestValues() {
        FlightRequest request = new FlightRequest();

        request.setCity("Quito");
        request.setDate("2026-07-12");

        assertEquals("Quito", request.getCity());
        assertEquals("2026-07-12", request.getDate());
    }

    @Test
    void shouldExposeGeocodeResultValues() {
        GeocodeResult result = new GeocodeResult(-0.2201641, -78.5123274);

        assertEquals(-0.2201641, result.getLatitude());
        assertEquals(-78.5123274, result.getLongitude());
    }

    @Test
    void shouldExposeWeatherDetailsValues() {
        WeatherDetails weather = new WeatherDetails(12.5, 0.4, 8.0, 35);

        assertEquals(12.5, weather.getWindSpeed());
        assertEquals(0.4, weather.getPrecipitation());
        assertEquals(8.0, weather.getVisibility());
        assertEquals(35, weather.getCloudCover());
    }

    @Test
    void shouldExposeFlightCancellationResponseValues() {
        WeatherDetails weather = new WeatherDetails(12.5, 0.4, 8.0, 35);
        FlightCancellationResponse response = new FlightCancellationResponse(
                "Baja probabilidad de cancelaciÃ³n",
                "El clima parece favorable",
                weather,
                -0.2201641,
                -78.5123274
        );

        assertEquals("Baja probabilidad de cancelaciÃ³n", response.getRiskLevel());
        assertEquals("El clima parece favorable", response.getMessage());
        assertSame(weather, response.getWeatherDetails());
        assertEquals(-0.2201641, response.getLatitude());
        assertEquals(-78.5123274, response.getLongitude());
    }
}

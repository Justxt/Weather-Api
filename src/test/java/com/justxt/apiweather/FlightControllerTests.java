package com.justxt.apiweather;

import com.justxt.apiweather.userRequest.FlightCancellationResponse;
import com.justxt.apiweather.userRequest.FlightRequest;
import com.justxt.apiweather.userRequest.GeocodeResult;
import com.justxt.apiweather.userRequest.WeatherDetails;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FlightControllerTests {

    @Test
    void shouldRejectDatesGreaterThanFifteenDays() {
        FlightController controller = new FlightController();
        FlightRequest request = new FlightRequest();
        request.setCity("Quito");
        request.setDate(LocalDate.now().plusDays(16).toString());

        StepVerifier.create(controller.getFlightCancellationRisk(request))
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    @Test
    void shouldBuildResponseWhenServicesReturnWeatherData() {
        FlightController controller = new FlightController();
        OpenMeteoService openMeteoService = mock(OpenMeteoService.class);
        FlightCancellationRiskService riskService = mock(FlightCancellationRiskService.class);
        GeocodingService geocodingService = mock(GeocodingService.class);

        ReflectionTestUtils.setField(controller, "openMeteoService", openMeteoService);
        ReflectionTestUtils.setField(controller, "riskService", riskService);
        ReflectionTestUtils.setField(controller, "geocodingService", geocodingService);

        FlightRequest request = new FlightRequest();
        request.setCity("Quito");
        request.setDate(LocalDate.now().plusDays(3).toString());

        WeatherDetails weather = new WeatherDetails(9.4, 0.1, 80.0, 90);

        when(geocodingService.getCoordinates("Quito"))
                .thenReturn(Mono.just(new GeocodeResult(-0.2201641, -78.5123274)));
        when(openMeteoService.getWeatherForecast(-0.2201641, -78.5123274, request.getDate()))
                .thenReturn(Mono.just(weather));
        when(riskService.evaluateRisk(weather))
                .thenReturn("Baja probabilidad de cancelación");

        StepVerifier.create(controller.getFlightCancellationRisk(request))
                .assertNext(response -> assertExpectedResponse(response, weather))
                .verifyComplete();
    }

    private void assertExpectedResponse(FlightCancellationResponse response, WeatherDetails weather) {
        org.junit.jupiter.api.Assertions.assertAll(
                () -> org.junit.jupiter.api.Assertions.assertEquals("Baja probabilidad de cancelación", response.getRiskLevel()),
                () -> org.junit.jupiter.api.Assertions.assertEquals(
                        "El clima parece favorable, tu vuelo probablemente no será afectado.",
                        response.getMessage()
                ),
                () -> org.junit.jupiter.api.Assertions.assertEquals(weather, response.getWeatherDetails()),
                () -> org.junit.jupiter.api.Assertions.assertEquals(-0.2201641, response.getLatitude()),
                () -> org.junit.jupiter.api.Assertions.assertEquals(-78.5123274, response.getLongitude())
        );
    }
}

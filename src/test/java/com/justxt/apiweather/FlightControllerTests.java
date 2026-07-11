package com.justxt.apiweather;

import com.justxt.apiweather.userRequest.FlightCancellationResponse;
import com.justxt.apiweather.userRequest.FlightRequest;
import com.justxt.apiweather.userRequest.GeocodeResult;
import com.justxt.apiweather.userRequest.WeatherDetails;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FlightControllerTests {

    private static final String HIGH_RISK = "Alta probabilidad de cancelaci\u00f3n";
    private static final String MODERATE_RISK = "Probabilidad moderada de cancelaci\u00f3n";
    private static final String LOW_RISK = "Baja probabilidad de cancelaci\u00f3n";

    @Test
    void shouldRejectDatesGreaterThanFifteenDays() {
        FlightController controller = new FlightController(
                mock(OpenMeteoService.class),
                mock(FlightCancellationRiskService.class),
                mock(GeocodingService.class)
        );
        FlightRequest request = new FlightRequest();
        request.setCity("Quito");
        request.setDate(LocalDate.now().plusDays(16).toString());

        StepVerifier.create(controller.getFlightCancellationRisk(request))
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    @Test
    void shouldBuildResponseWhenServicesReturnWeatherData() {
        WeatherDetails weather = new WeatherDetails(9.4, 0.1, 80.0, 90);

        StepVerifier.create(createControllerResponse(LOW_RISK, weather))
                .assertNext(response -> assertExpectedResponse(response, weather))
                .verifyComplete();
    }

    @Test
    void shouldBuildHighRiskMessage() {
        StepVerifier.create(createControllerResponse(HIGH_RISK, new WeatherDetails(55.0, 1.0, 8.0, 90)))
                .assertNext(response -> assertEquals("El clima parece severo, verifica con tu aerol\u00ednea.", response.getMessage()))
                .verifyComplete();
    }

    @Test
    void shouldBuildModerateRiskMessage() {
        StepVerifier.create(createControllerResponse(MODERATE_RISK, new WeatherDetails(35.0, 1.0, 8.0, 70)))
                .assertNext(response -> assertEquals("Existen riesgos potenciales, sigue monitoreando.", response.getMessage()))
                .verifyComplete();
    }

    private Mono<FlightCancellationResponse> createControllerResponse(String riskLevel, WeatherDetails weather) {
        OpenMeteoService openMeteoService = mock(OpenMeteoService.class);
        FlightCancellationRiskService riskService = mock(FlightCancellationRiskService.class);
        GeocodingService geocodingService = mock(GeocodingService.class);
        FlightController controller = new FlightController(openMeteoService, riskService, geocodingService);

        FlightRequest request = new FlightRequest();
        request.setCity("Quito");
        request.setDate(LocalDate.now().plusDays(3).toString());

        when(geocodingService.getCoordinates("Quito"))
                .thenReturn(Mono.just(new GeocodeResult(-0.2201641, -78.5123274)));
        when(openMeteoService.getWeatherForecast(-0.2201641, -78.5123274, request.getDate()))
                .thenReturn(Mono.just(weather));
        when(riskService.evaluateRisk(weather)).thenReturn(riskLevel);

        return controller.getFlightCancellationRisk(request);
    }

    private void assertExpectedResponse(FlightCancellationResponse response, WeatherDetails weather) {
        assertAll(
                () -> assertEquals(LOW_RISK, response.getRiskLevel()),
                () -> assertEquals("El clima parece favorable, tu vuelo probablemente no ser\u00e1 afectado.", response.getMessage()),
                () -> assertEquals(weather, response.getWeatherDetails()),
                () -> assertEquals(-0.2201641, response.getLatitude()),
                () -> assertEquals(-78.5123274, response.getLongitude())
        );
    }
}

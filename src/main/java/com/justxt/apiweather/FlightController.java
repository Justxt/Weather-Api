package com.justxt.apiweather;

import com.justxt.apiweather.userRequest.FlightCancellationResponse;
import com.justxt.apiweather.userRequest.FlightRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@RestController
@RequestMapping("/api")
public class FlightController {

    private final OpenMeteoService openMeteoService;
    private final FlightCancellationRiskService riskService;
    private final GeocodingService geocodingService;

    public FlightController(OpenMeteoService openMeteoService,
                            FlightCancellationRiskService riskService,
                            GeocodingService geocodingService) {
        this.openMeteoService = openMeteoService;
        this.riskService = riskService;
        this.geocodingService = geocodingService;
    }

    @PostMapping("/flight-cancellation-risk")
    public Mono<FlightCancellationResponse> getFlightCancellationRisk(@Valid @RequestBody FlightRequest request) {
        LocalDate requestDate = LocalDate.parse(request.getDate());
        LocalDate currentDate = LocalDate.now();

        if (requestDate.isBefore(currentDate)) {
            throw new IllegalArgumentException("La fecha no puede estar en el pasado.");
        }

        if (ChronoUnit.DAYS.between(currentDate, requestDate) > 15) {
            return Mono.error(new IllegalArgumentException("La fecha no puede ser mayor a 15 días a partir de hoy."));
        }

        // Si la fecha solicitada es menor a la fecha actual, se lanza una excepción
        return geocodingService.getCoordinates(request.getCity())
                .flatMap(coordinates -> openMeteoService.getWeatherForecast(coordinates.getLatitude(), coordinates.getLongitude(), request.getDate())
                        .map(weather -> {
                            String riskLevel = riskService.evaluateRisk(weather);
                            String message = switch (riskLevel) {
                                case "Alta probabilidad de cancelación" -> "El clima parece severo, verifica con tu aerolínea.";
                                case "Probabilidad moderada de cancelación" -> "Existen riesgos potenciales, sigue monitoreando.";
                                default -> "El clima parece favorable, tu vuelo probablemente no será afectado.";
                            };
                            return new FlightCancellationResponse(riskLevel, message, weather, coordinates.getLatitude(), coordinates.getLongitude());
                        }));
    }
}

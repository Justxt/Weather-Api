package com.justxt.apiweather;

import com.justxt.apiweather.userRequest.FlightCancellationResponse;
import com.justxt.apiweather.userRequest.FlightRequest;
import com.justxt.apiweather.userRequest.WeatherDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@RestController
@RequestMapping("/api")
public class FlightController {

    @Autowired
    private OpenMeteoService openMeteoService;

    @Autowired
    private FlightCancellationRiskService riskService;

    @Autowired
    private GeocodingService geocodingService;

    @PostMapping("/flight-cancellation-risk")
    public Mono<FlightCancellationResponse> getFlightCancellationRisk(@RequestBody FlightRequest request) {
        LocalDate requestDate = LocalDate.parse(request.getDate()); //Fecha que se solicita
        LocalDate currentDate = LocalDate.now(); //Fecha actual

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
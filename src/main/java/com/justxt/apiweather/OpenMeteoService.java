package com.justxt.apiweather;

import com.justxt.apiweather.userRequest.WeatherDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import com.fasterxml.jackson.databind.JsonNode;
import reactor.core.publisher.Mono;

@Service
public class OpenMeteoService {
    private final WebClient webClient;

    public OpenMeteoService(WebClient.Builder webClientBuilder) {
        // Hacemos un cliente para hacer peticiones a la API de Open-Meteo
        this.webClient = webClientBuilder.baseUrl("https://api.open-meteo.com/v1/forecast").build();
    }

    // Recibe las coordenadas de una ciudad y una fecha y devuelve un objeto WeatherDetails con los detalles del clima
    public Mono<WeatherDetails> getWeatherForecast(double latitude, double longitude, String date) {
        return this.webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("latitude", latitude)
                        .queryParam("longitude", longitude)
                        .queryParam("hourly", "wind_speed_10m,precipitation,visibility,cloudcover")
                        .queryParam("start_date", date)
                        .queryParam("end_date", date)
                        .build())
                .retrieve()
                .bodyToMono(JsonNode.class)
                .map(response -> {
                    JsonNode hourlyData = response.path("hourly");
                    double windSpeed = hourlyData.path("wind_speed_10m").get(0).asDouble();
                    double precipitation = hourlyData.path("precipitation").get(0).asDouble();
                    double visibility = hourlyData.path("visibility").get(0).asDouble();
                    int cloudCover = hourlyData.path("cloudcover").get(0).asInt();

                    return new WeatherDetails(windSpeed, precipitation, visibility, cloudCover);
                });
    }
}
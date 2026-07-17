package com.justxt.apiweather;

import com.justxt.apiweather.userRequest.GeocodeResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import com.fasterxml.jackson.databind.JsonNode;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Service
public class GeocodingService {

    private final WebClient webClient;

    public GeocodingService(WebClient.Builder webClientBuilder,
                            @Value("${weather.geocoding.base-url}") String baseUrl) {
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
    }

    // Recibe una ciudad y devuelve un objeto GeocodeResult con las coordenadas de la ciudad

    public Mono<GeocodeResult> getCoordinates(String city) {
        return this.webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/search")
                        .queryParam("name", city)
                        .queryParam("count", "1")
                        .queryParam("language", "es")
                        .queryParam("format", "json")
                        .build())
                .retrieve()
                .bodyToMono(JsonNode.class)
                .map(response -> {
                    JsonNode results = response.path("results");
                    if (results.isArray() && !results.isEmpty()) {
                        JsonNode firstResult = results.get(0);
                        double lat = firstResult.path("latitude").asDouble();
                        double lon = firstResult.path("longitude").asDouble();
                        return new GeocodeResult(lat, lon);
                    }
                    throw new IllegalArgumentException("Ciudad no encontrada");
                })
                .timeout(Duration.ofSeconds(10));
    }
}

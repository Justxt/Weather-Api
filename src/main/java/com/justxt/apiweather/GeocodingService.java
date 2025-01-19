package com.justxt.apiweather;

import com.justxt.apiweather.userRequest.GeocodeResult;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import com.fasterxml.jackson.databind.JsonNode;
import reactor.core.publisher.Mono;

@Service
public class GeocodingService {

    private final WebClient webClient;

    public GeocodingService(WebClient.Builder webClientBuilder) {
        // Hacemos un cliente para hacer peticiones a la API de OpenStreetMap para geocodificar ciudades
        this.webClient = webClientBuilder.baseUrl("https://nominatim.openstreetmap.org").build();
    }

    // Recibe una ciudad y devuelve un objeto GeocodeResult con las coordenadas de la ciudad

    public Mono<GeocodeResult> getCoordinates(String city) {
        return this.webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/search")
                        .queryParam("q", city)
                        .queryParam("format", "json")
                        .queryParam("addressdetails", "1")
                        .build())
                .retrieve()
                .bodyToMono(JsonNode.class)
                .map(response -> {
                    if (response.isArray() && response.size() > 0) {
                        JsonNode firstResult = response.get(0);
                        double lat = firstResult.get("lat").asDouble();
                        double lon = firstResult.get("lon").asDouble();
                        return new GeocodeResult(lat, lon);
                    } else {
                        throw new RuntimeException("Ciudad no encontrada");
                    }
                });
    }
}
package com.justxt.apiweather;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.URI;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GeocodingServiceTests {

    @Test
    void shouldReadCoordinatesFromFirstSearchResult() {
        GeocodingService service = new GeocodingService(webClientBuilder("""
                {"results":[{"latitude":-0.2201641,"longitude":-78.5123274}]}
                """), "https://geo.example.test");

        StepVerifier.create(service.getCoordinates("Quito"))
                .assertNext(result -> {
                    org.junit.jupiter.api.Assertions.assertEquals(-0.2201641, result.getLatitude());
                    org.junit.jupiter.api.Assertions.assertEquals(-78.5123274, result.getLongitude());
                })
                .verifyComplete();
    }

    @Test
    void shouldFailWhenCityIsNotFound() {
        GeocodingService service = new GeocodingService(webClientBuilder("{\"results\":[]}"), "https://geo.example.test");

        StepVerifier.create(service.getCoordinates("Unknown"))
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    @Test
    void shouldFailWhenResponseIsNotAnArray() {
        GeocodingService service = new GeocodingService(webClientBuilder("{}"), "https://geo.example.test");

        StepVerifier.create(service.getCoordinates("Unknown"))
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    @Test
    void shouldUseConfiguredBaseUrl() {
        AtomicReference<URI> requestedUri = new AtomicReference<>();
        WebClient.Builder builder = WebClient.builder()
                .exchangeFunction(request -> {
                    requestedUri.set(request.url());
                    return Mono.just(ClientResponse.create(HttpStatus.OK)
                            .header("Content-Type", "application/json")
                            .body("{\"results\":[]}")
                            .build());
                });
        GeocodingService service = new GeocodingService(builder, "https://geo.example.test");

        StepVerifier.create(service.getCoordinates("Quito"))
                .expectError(IllegalArgumentException.class)
                .verify();

        assertEquals("geo.example.test", requestedUri.get().getHost());
    }

    private WebClient.Builder webClientBuilder(String body) {
        return WebClient.builder()
                .exchangeFunction(request -> Mono.just(ClientResponse.create(HttpStatus.OK)
                        .header("Content-Type", "application/json")
                        .body(body)
                        .build()));
    }
}

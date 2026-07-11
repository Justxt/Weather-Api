package com.justxt.apiweather;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class GeocodingServiceTests {

    @Test
    void shouldReadCoordinatesFromFirstSearchResult() {
        GeocodingService service = new GeocodingService(webClientBuilder("""
                [{"lat":"-0.2201641","lon":"-78.5123274"}]
                """));

        StepVerifier.create(service.getCoordinates("Quito"))
                .assertNext(result -> {
                    org.junit.jupiter.api.Assertions.assertEquals(-0.2201641, result.getLatitude());
                    org.junit.jupiter.api.Assertions.assertEquals(-78.5123274, result.getLongitude());
                })
                .verifyComplete();
    }

    @Test
    void shouldFailWhenCityIsNotFound() {
        GeocodingService service = new GeocodingService(webClientBuilder("[]"));

        StepVerifier.create(service.getCoordinates("Unknown"))
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    @Test
    void shouldFailWhenResponseIsNotAnArray() {
        GeocodingService service = new GeocodingService(webClientBuilder("{}"));

        StepVerifier.create(service.getCoordinates("Unknown"))
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    private WebClient.Builder webClientBuilder(String body) {
        return WebClient.builder()
                .exchangeFunction(request -> Mono.just(ClientResponse.create(HttpStatus.OK)
                        .header("Content-Type", "application/json")
                        .body(body)
                        .build()));
    }
}

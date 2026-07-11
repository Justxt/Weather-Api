package com.justxt.apiweather;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class OpenMeteoServiceTests {

    @Test
    void shouldReadWeatherDetailsFromHourlyForecast() {
        OpenMeteoService service = new OpenMeteoService(webClientBuilder("""
                {
                  "hourly": {
                    "wind_speed_10m": [18.5],
                    "precipitation": [0.7],
                    "visibility": [9.0],
                    "cloudcover": [40]
                  }
                }
                """));

        StepVerifier.create(service.getWeatherForecast(-0.2201641, -78.5123274, "2026-07-12"))
                .assertNext(weather -> {
                    org.junit.jupiter.api.Assertions.assertEquals(18.5, weather.getWindSpeed());
                    org.junit.jupiter.api.Assertions.assertEquals(0.7, weather.getPrecipitation());
                    org.junit.jupiter.api.Assertions.assertEquals(9.0, weather.getVisibility());
                    org.junit.jupiter.api.Assertions.assertEquals(40, weather.getCloudCover());
                })
                .verifyComplete();
    }

    private WebClient.Builder webClientBuilder(String body) {
        return WebClient.builder()
                .exchangeFunction(request -> Mono.just(ClientResponse.create(HttpStatus.OK)
                        .header("Content-Type", "application/json")
                        .body(body)
                        .build()));
    }
}

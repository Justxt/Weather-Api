package com.justxt.apiweather;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HealthControllerTests {

    @Test
    void shouldReturnApplicationHealth() {
        HealthController controller = new HealthController();

        Map<String, Object> health = controller.health();

        assertEquals("UP", health.get("status"));
        assertEquals("weather-api", health.get("application"));
        assertTrue(health.containsKey("timestamp"));
    }
}

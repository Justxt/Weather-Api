package com.justxt.apiweather;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class HealthControllerTests {

    @Test
    void shouldReturnApplicationHealth() {
        HealthController controller = new HealthController();

        Map<String, Object> health = controller.health();

        assertEquals("UP", health.get("status"));
        assertEquals("weather-api", health.get("application"));
        assertTrue(health.containsKey("timestamp"));
    }

    @Test
    void shouldReturnDefensiveSecurityHeaders() throws Exception {
        MockMvc mockMvc = MockMvcBuilders
                .standaloneSetup(new HealthController())
                .addFilters(new SecurityHeadersFilter())
                .build();

        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(header().string("Cache-Control", "no-store"))
                .andExpect(header().string("Cross-Origin-Resource-Policy", "same-origin"));
    }
}

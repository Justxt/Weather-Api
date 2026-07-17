package com.justxt.apiweather;

import com.justxt.apiweather.userRequest.WeatherDetails;
import org.springframework.stereotype.Service;

@Service
public class FlightCancellationRiskService {
    public String evaluateRisk(WeatherDetails weather) {
        if (weather.getWindSpeed() > 50 || weather.getVisibility() < 1 || weather.getPrecipitation() > 10) {
            return "Alta probabilidad de cancelación";
        } else if (weather.getWindSpeed() > 30 || weather.getVisibility() < 5 || weather.getPrecipitation() > 5) {
            return "Probabilidad moderada de cancelación";
        }
        return "Baja probabilidad de cancelación";
    }
}

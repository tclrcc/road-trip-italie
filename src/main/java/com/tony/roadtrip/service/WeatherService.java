package com.tony.roadtrip.service;

import com.tony.roadtrip.dto.WeatherInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ThreadLocalRandom;

@Service
@Slf4j
public class WeatherService {

    private final RestClient restClient;

    public WeatherService(RestClient.Builder builder) {
        this.restClient = builder.baseUrl("https://api.open-meteo.com/v1").build();
    }

    /**
     * Récupère la météo pour une date et un lieu donnés.
     */
    public WeatherInfo getWeatherForDate(double lat, double lon, LocalDate date) {
        long daysUntilTrip = ChronoUnit.DAYS.between(LocalDate.now(), date);

        // 1. Si le voyage est dans moins de 14 jours -> VRAIE API
        if (daysUntilTrip >= 0 && daysUntilTrip <= 14) {
            return fetchRealForecast(lat, lon);
        }

        // 2. Sinon (2026) -> SIMULATION basée sur des moyennes (pour le design)
        return generateMockWeather(date);
    }

    private WeatherInfo fetchRealForecast(double lat, double lon) {
        try {
            // Appel API Open-Meteo (Gratuit, pas de clé API)
            // On demande juste le code météo et les temp max/min du jour
            String uri = String.format("/forecast?latitude=%s&longitude=%s&daily=weather_code,temperature_2m_max,temperature_2m_min&timezone=auto&forecast_days=1",
                    lat, lon);

            var response = restClient.get().uri(uri).retrieve().body(OpenMeteoResponse.class);

            if (response != null && response.daily() != null) {
                int code = response.daily().weather_code()[0];
                double max = response.daily().temperature_2m_max()[0];
                double min = response.daily().temperature_2m_min()[0];

                return new WeatherInfo(max, min, code, mapWmoCodeToIcon(code), true);
            }
        } catch (Exception e) {
            log.error("Erreur API Météo : {}", e.getMessage());
        }
        return null; // En cas d'erreur
    }

    // Génère une météo fictive cohérente pour tester l'UI avant 2026
    private WeatherInfo generateMockWeather(LocalDate date) {
        // En Mai/Juin en Italie, il fait beau (Code 0, 1, 2)
        int randomCode = ThreadLocalRandom.current().nextInt(0, 3);
        double randomTemp = ThreadLocalRandom.current().nextDouble(22.0, 28.0);

        return new WeatherInfo(randomTemp, randomTemp - 10, randomCode, mapWmoCodeToIcon(randomCode), false);
    }

    // Convertit les codes WMO (Standards météo) en icônes FontAwesome
    private String mapWmoCodeToIcon(int wmoCode) {
        return switch (wmoCode) {
            case 0 -> "fas fa-sun text-warning";                 // Ciel clair
            case 1, 2, 3 -> "fas fa-cloud-sun text-warning";     // Partiellement nuageux
            case 45, 48 -> "fas fa-smog text-secondary";         // Brouillard
            case 51, 53, 55 -> "fas fa-cloud-rain text-info";    // Bruine
            case 61, 63, 65 -> "fas fa-umbrella text-primary";   // Pluie
            case 71, 73, 75 -> "far fa-snowflake text-info";     // Neige
            case 95, 96, 99 -> "fas fa-bolt text-danger";        // Orage
            default -> "fas fa-cloud text-secondary";
        };
    }

    // Records internes pour mapper le JSON de Open-Meteo sans exposer ça dehors
    record OpenMeteoResponse(Daily daily) {}
    record Daily(int[] weather_code, double[] temperature_2m_max, double[] temperature_2m_min) {}
}

package com.tony.roadtrip.service;

import com.tony.roadtrip.dto.WeatherInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WeatherServiceTest {

    @Mock
    private RestClient.Builder restClientBuilder;

    @Mock
    private RestClient restClient;

    private WeatherService weatherService;

    @BeforeEach
    void setUp() {
        // Simulation du comportement du Builder RestClient (Fluent API)
        // Cela évite le NullPointerException dans le constructeur de WeatherService
        when(restClientBuilder.baseUrl(anyString())).thenReturn(restClientBuilder);
        when(restClientBuilder.build()).thenReturn(restClient);

        // Instanciation du service avec les mocks
        weatherService = new WeatherService(restClientBuilder);
    }

    @Test
    @DisplayName("Doit générer une météo simulée si la date est lointaine (ex: 2026)")
    void shouldReturnMockWeatherForFutureDate() {
        // GIVEN (Étant donné une date dans 1 an)
        LocalDate futureDate = LocalDate.now().plusYears(1);
        double lat = 41.9028; // Rome
        double lon = 12.4964;

        // WHEN (Quand j'appelle le service)
        WeatherInfo result = weatherService.getWeatherForDate(lat, lon, futureDate);

        // THEN (Alors j'obtiens une prévision cohérente)
        assertNotNull(result, "Le service ne doit jamais renvoyer null");

        // Vérification de la logique métier (Switch Simulation)
        assertFalse(result.isRealForecast(), "Le flag isRealForecast doit être false pour une date lointaine");

        // Vérification des données générées
        assertTrue(result.maxTemp() > 0 && result.maxTemp() < 40, "La température générée doit être réaliste (0-40°C)");
        assertNotNull(result.iconClass(), "Une icône CSS doit être attribuée");

        // Vérification qu'on n'a PAS appelé l'API réelle (Optimisation)
        verifyNoInteractions(restClient);
    }

    @Test
    @DisplayName("Doit être robuste même si l'API externe plante (Date proche)")
    void shouldHandleApiErrorGracefully() {
        // GIVEN (Une date demain → Devrait appeler l'API)
        LocalDate nearDate = LocalDate.now().plusDays(1);

        // On configure le mock pour simuler un appel, mais on force une exception/null
        // Note : Mocker toute la chaîne RestClient.get().uri()... est verbeux,
        // ici, on vérifie surtout que le try/catch du service fait son boulot et renvoie null au lieu de crasher.

        // WHEN
        WeatherInfo result = weatherService.getWeatherForDate(0.0, 0.0, nearDate);

        // THEN
        assertNull(result, "En cas d'erreur API, le service doit renvoyer null (géré par le front)");
    }
}

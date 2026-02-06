package com.tony.roadtrip.dto;

public record WeatherInfo(
        double maxTemp,
        double minTemp,
        int weatherCode,    // Code WMO (0=Soleil, 61=Pluie, etc.)
        String iconClass,   // Classe FontAwesome calculée
        boolean isRealForecast // Pour savoir si c'est une vraie prévision ou une estimation
) {}

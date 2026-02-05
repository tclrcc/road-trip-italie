package com.tony.roadtrip.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Data
@Entity
public class ItineraryDay {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate date;
    private String title;          // Ex: "Immersion dans les Cinque Terre"
    private String hubLocation;    // Ex: "Levanto"

    @Column(length = 1000)
    private String mainActivity;   // Ex: "Randonnée Vernazza-Monterosso"

    @Column(length = 1000)
    private String logisticsTip;   // Ex: "Ne pas entrer dans le tunnel du Fréjus, prendre D1006"

    private Double estimatedCost;  // Pour suivre ton budget de 4000€
    private String accommodation;  // Nom de l'Airbnb/Hôtel

    private boolean warningZTL;    // Un petit flag rouge pour les jours dangereux (Florence, Rome)
}

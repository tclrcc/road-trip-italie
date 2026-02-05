package com.tony.roadtrip.model;

import jakarta.persistence.*;
import lombok.Data;


@Data
@Entity
public class Accommodation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;           // Ex : "Appartement Levanto Center"
    private String location;       // Adresse exacte pour Google Maps

    @Column(length = 1000)
    private String airbnbLink;     // Le lien vers ta réservation

    private String checkInTime;    // Ex: "15:00"
    private String checkOutTime;   // Ex: "10:00"

    // --- Coordonnées GPS ---
    private Double latitude;       // Ex: 44.17
    private Double longitude;      // Ex: 9.61

    // --- Champs pour la Map ---
    private Integer stepNumber; // 0 pour le départ, 1, 2, 3...

    @Enumerated(EnumType.STRING) // Force l'enregistrement en texte ("START") dans la BDD
    private AccommodationType type;

    private Double cost;           // Coût total du logement
    private boolean isPaid;        // true = payé, false = à payer sur place
}

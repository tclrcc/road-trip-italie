package com.tony.roadtrip.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Data
@Entity
public class Accommodation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;           // Ex: "Appartement Levanto Center"
    private String location;       // Adresse exacte pour Google Maps

    @Column(length = 1000)
    private String airbnbLink;     // Le lien vers ta réservation

    private String checkInTime;    // Ex: "15:00"
    private String checkOutTime;   // Ex: "10:00"

    private Double cost;           // Coût total du logement
    private boolean isPaid;        // true = payé, false = à payer sur place
}

package com.tony.roadtrip.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class Activity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private Double price; // Le coût de l'activité
    private String bookingUrl; // Lien vers GetYourGuide, le musée, etc.

    @Column(name = "is_paid")
    private boolean isPaid = false;

    // Relation vers la journée concernée
    @ManyToOne
    @JoinColumn(name = "itinerary_day_id")
    private ItineraryDay itineraryDay;
}

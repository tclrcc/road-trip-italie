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
    private String title;
    private String hubLocation;

    @Column(length = 1000)
    private String mainActivity;

    @Column(length = 1000)
    private String logisticsTip;

    private Double dailyBudget;    // Budget pour la journée (essence/bouffe/activités). HORS Logement
    private boolean warningZTL;

    // --- RELATIONS ---

    @ManyToOne
    @JoinColumn(name = "accommodation_id")
    private Accommodation accommodation;
}

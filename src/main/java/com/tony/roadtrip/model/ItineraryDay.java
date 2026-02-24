package com.tony.roadtrip.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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

    @OneToMany(mappedBy = "itineraryDay", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Activity> activities = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "accommodation_id")
    private Accommodation accommodation;

    // Méthode utilitaire pour calculer le total des activités du jour
    public Double getTotalActivityCost() {
        return activities.stream()
                .mapToDouble(a -> a.getPrice() != null ? a.getPrice() : 0.0)
                .sum();
    }
}

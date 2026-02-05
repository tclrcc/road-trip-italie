package com.tony.roadtrip.repository;

import com.tony.roadtrip.model.ItineraryDay;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ItineraryRepository extends JpaRepository<ItineraryDay, Long> {
    // Une méthode pour trier les jours par date automatiquement
    List<ItineraryDay> findAllByOrderByDateAsc();
}

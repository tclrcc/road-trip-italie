package com.tony.roadtrip.repository;

import com.tony.roadtrip.model.Activity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActivityRepository extends JpaRepository<Activity, Long> {
    // Récupère uniquement les activités à réserver (non payées avec un rappel)
    List<Activity> findByIsPaidFalseAndReminderDaysBeforeIsNotNull();
}

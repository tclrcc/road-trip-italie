package com.tony.roadtrip.repository;

import com.tony.roadtrip.model.Activity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActivityRepository extends JpaRepository<Activity, Long> {
}

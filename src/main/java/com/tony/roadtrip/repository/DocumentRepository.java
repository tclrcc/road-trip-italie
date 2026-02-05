package com.tony.roadtrip.repository;

import com.tony.roadtrip.model.TripDocument;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentRepository extends JpaRepository<TripDocument, Long> {
}

package com.tony.roadtrip.repository;

import com.tony.roadtrip.model.PackingItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PackingItemRepository extends JpaRepository<PackingItem, Long> {
    // Pour afficher la liste triée par catégorie puis par nom
    List<PackingItem> findAllByOrderByCategoryAscNameAsc();

    // Pour le service de mail : compter ce qui manque et qui est vital
    long countByIsPackedFalseAndIsEssentialTrue();
}

package com.tony.roadtrip.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PackingItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name; // Ex: "Passeports", "Adaptateur prise"

    @Enumerated(EnumType.STRING)
    private ItemCategory category; // Ex: DOCUMENTS, TECH, VETEMENTS, SANTE

    private boolean isPacked; // Case à cocher

    private String assignedTo; // "Tony", "Copine" ou "Commun"

    private boolean isEssential; // Si True, rappel prioritaire
}


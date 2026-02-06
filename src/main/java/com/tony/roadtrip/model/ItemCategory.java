package com.tony.roadtrip.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ItemCategory {
    DOCUMENTS("Papiers & Résas"),
    TECH("Tech & Photo"),
    VETEMENTS("Vêtements"),
    TOILETTE("Hygiène & Santé"),
    VOITURE("Pour la route"),
    DIVERS("Divers");

    private final String label;
}

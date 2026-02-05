package com.tony.roadtrip.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class TripDocument {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;           // Nom du fichier (ex : "Billets Colisée.pdf")
    private String type;           // Type MIME (ex: "application/pdf", "image/jpeg")

    @Lob // Indique un gros contenu binaire (Large Object)
    @Column(columnDefinition = "BLOB") // Force le type BLOB pour H2
    private byte[] content;        // Le fichier lui-même
}

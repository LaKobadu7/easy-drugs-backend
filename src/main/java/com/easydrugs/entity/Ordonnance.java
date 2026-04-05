package com.easydrugs.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

// ================================================================
//  Ordonnance.java
// ================================================================
@Entity
@Table(name = "ordonnance")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Ordonnance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    /** Chemin Cloudinary de l'image de l'ordonnance */
    @Column(name = "image_path", length = 500)
    private String imagePath;

    @Column(name = "date_emission")
    private LocalDate dateEmission;

    @Size(max = 150)
    @Column(name = "nom_medecin", length = 150)
    private String nomMedecin;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}

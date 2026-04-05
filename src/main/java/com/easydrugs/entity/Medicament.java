package com.easydrugs.entity;

import com.easydrugs.enums.FormeGalenique;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entité Médicament — EASY-DRUGS.
 *
 * Un médicament est défini globalement (ex: Amoxicilline 500mg).
 * Sa disponibilité et son prix par pharmacie sont dans l'entité Stock.
 * La forme galénique suit la classification pharmaceutique officielle :
 *   SOLIDE | LIQUIDE | SEMI_SOLIDE | GAZ
 */
@Entity
@Table(name = "medicament")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Medicament {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le nom du médicament est obligatoire")
    @Size(max = 200)
    @Column(nullable = false, length = 200)
    private String nom;

    /**
     * Forme galénique officielle :
     * - SOLIDE      : comprimés, gélules, poudres, suppositoires…
     * - LIQUIDE     : sirops, solutions buvables, suspensions, gouttes…
     * - SEMI_SOLIDE : crèmes, gels, pommades, pâtes…
     * - GAZ         : aérosols doseurs, inhalateurs, sprays nasaux…
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "forme_galenique", nullable = false, length = 20)
    private FormeGalenique formeGalenique;

    @Column(columnDefinition = "TEXT")
    private String description;

    /** Ex : "Boîte de 16 gélules", "Flacon 100ml" */
    @Column(length = 150)
    private String conditionnement;

    @Column(length = 150)
    private String fabricant;

    /** URL Cloudinary de l'image du médicament */
    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    // ── Relations ───────────────────────────────────────────────

    @OneToMany(
        mappedBy = "medicament",
        cascade = CascadeType.ALL,
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    @Builder.Default
    private List<Stock> stocks = new ArrayList<>();
}

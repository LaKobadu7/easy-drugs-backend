package com.easydrugs.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entité HistoriqueRecherche — EASY-DRUGS.
 * Enregistre chaque recherche de médicament effectuée par un patient.
 * Affiché dans l'écran "Profil > Historique des recherches".
 */
@Entity
@Table(name = "historique_recherche")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class HistoriqueRecherche {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @NotBlank(message = "Le terme de recherche est obligatoire")
    @Size(max = 200)
    @Column(nullable = false, length = 200)
    private String terme;

    @Column(name = "date_recherche", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime dateRecherche = LocalDateTime.now();
}

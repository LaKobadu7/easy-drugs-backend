package com.easydrugs.entity;

import com.easydrugs.enums.StatutLitige;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entité Litige — EASY-DRUGS.
 * Module support : soumis par le patient, traité par l'administrateur.
 * Cycle : OUVERT → EN_COURS → RESOLU | REJETE
 */
@Entity
@Table(name = "litige")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Litige {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    /** Admin assigné à ce litige (null tant que non pris en charge) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id")
    private Utilisateur admin;

    @NotBlank(message = "Le sujet est obligatoire")
    @Size(max = 200)
    @Column(nullable = false, length = 200)
    private String sujet;

    @NotBlank(message = "La description est obligatoire")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private StatutLitige statut = StatutLitige.OUVERT;

    @Column(name = "date_ouverture", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime dateOuverture = LocalDateTime.now();

    @Column(name = "date_resolution")
    private LocalDateTime dateResolution;

    // ── Méthodes métier ─────────────────────────────────────────

    public void prendreEnCharge(Utilisateur admin) {
        this.admin = admin;
        this.statut = StatutLitige.EN_COURS;
    }

    public void resoudre() {
        this.statut = StatutLitige.RESOLU;
        this.dateResolution = LocalDateTime.now();
    }

    public void rejeter() {
        this.statut = StatutLitige.REJETE;
        this.dateResolution = LocalDateTime.now();
    }
}

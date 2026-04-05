package com.easydrugs.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Entité Patient — EASY-DRUGS.
 * Relation OneToOne avec Utilisateur.
 * Table propre : patient (id, utilisateur_id, adresse, lat, lon, fcm_token)
 */
@Entity
@Table(name = "patient")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utilisateur_id", nullable = false, unique = true)
    private Utilisateur utilisateur;

    @Column(length = 255)
    private String adresse;

    @DecimalMin(value = "-90.0", message = "Latitude invalide")
    @DecimalMax(value = "90.0",  message = "Latitude invalide")
    @Column(name = "latitude_actuelle")
    private Double latitudeActuelle;

    @DecimalMin(value = "-180.0", message = "Longitude invalide")
    @DecimalMax(value = "180.0",  message = "Longitude invalide")
    @Column(name = "longitude_actuelle")
    private Double longitudeActuelle;

    @Column(name = "fcm_token", length = 500)
    private String fcmToken;

    // ── Relations ───────────────────────────────────────────────

    @OneToMany(
        mappedBy = "patient",
        cascade = CascadeType.ALL,
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    @Builder.Default
    private List<Ordonnance> ordonnances = new ArrayList<>();

    @OneToMany(
        mappedBy = "patient",
        cascade = CascadeType.ALL,
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    @Builder.Default
    private List<Notification> notifications = new ArrayList<>();

    @OneToMany(
        mappedBy = "patient",
        cascade = CascadeType.ALL,
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    @Builder.Default
    private List<HistoriqueRecherche> historique = new ArrayList<>();

    @OneToMany(
        mappedBy = "patient",
        cascade = CascadeType.ALL,
        fetch = FetchType.LAZY
    )
    @Builder.Default
    private List<Litige> litiges = new ArrayList<>();

    // ── Méthodes utilitaires ────────────────────────────────────

    public boolean aPositionGps() {
        return latitudeActuelle != null && longitudeActuelle != null;
    }

    public void mettreAJourPosition(double lat, double lon) {
        this.latitudeActuelle = lat;
        this.longitudeActuelle = lon;
    }
}

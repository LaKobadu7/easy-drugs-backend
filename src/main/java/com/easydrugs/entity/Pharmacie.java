package com.easydrugs.entity;

import com.easydrugs.enums.StatutPharmacie;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.locationtech.jts.geom.Point;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entité Pharmacie — EASY-DRUGS.
 *
 * Contient les coordonnées géospatiales PostGIS (Point WGS 84)
 * pour les requêtes de proximité, ainsi que le statut temps réel
 * (ouverte / fermée / de garde).
 *
 * Une pharmacie doit être validée par l'admin avant d'apparaître
 * dans les résultats patients (prototype Sangmélima).
 */
@Entity
@Table(name = "pharmacie")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pharmacie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le nom de la pharmacie est obligatoire")
    @Size(max = 150)
    @Column(nullable = false, length = 150)
    private String nom;

    @NotBlank(message = "L'adresse est obligatoire")
    @Size(max = 255)
    @Column(nullable = false, length = 255)
    private String adresse;

    /**
     * Point géospatial PostGIS — SRID 4326 (WGS 84).
     * Utilisé pour les requêtes ST_DWithin, ST_Distance, ST_DistanceSphere.
     * Note : PostGIS utilise (longitude, latitude) — inversé par rapport au GPS classique.
     */
    @Column(
        name = "coordonnees",
        columnDefinition = "geometry(Point, 4326)",
        nullable = false
    )
    private Point coordonnees;

    /** Latitude stockée en double pour les calculs Haversine côté Java */
    @NotNull
    @DecimalMin(value = "-90.0")
    @DecimalMax(value = "90.0")
    @Column(nullable = false)
    private Double latitude;

    /** Longitude stockée en double pour les calculs Haversine côté Java */
    @NotNull
    @DecimalMin(value = "-180.0")
    @DecimalMax(value = "180.0")
    @Column(nullable = false)
    private Double longitude;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private StatutPharmacie statut = StatutPharmacie.OUVERTE;

    @Column(length = 255)
    private String horaires;

    /** True si la pharmacie est actuellement de garde */
    @Column(name = "est_de_garde", nullable = false)
    @Builder.Default
    private Boolean estDeGarde = false;

    /**
     * True si validée par l'administrateur.
     * Une pharmacie non validée n'apparaît pas dans les résultats patients.
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean validee = false;

    /**
     * Zone géographique du prototype.
     * Valeur fixe "SANGMELIMA" pour le prototype.
     */
    @Column(name = "zone_geo", nullable = false, length = 50)
    @Builder.Default
    private String zoneGeo = "SANGMELIMA";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id")
    private Utilisateur admin;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    // ── Relations ───────────────────────────────────────────────

    @OneToMany(
        mappedBy = "pharmacie",
        cascade = CascadeType.ALL,
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    @Builder.Default
    private List<Stock> stocks = new ArrayList<>();

    // ── Méthodes métier ─────────────────────────────────────────

    /** Active le mode garde */
    public void activerGarde() {
        this.estDeGarde = true;
        this.statut = StatutPharmacie.GARDE;
    }

    /** Désactive le mode garde */
    public void desactiverGarde() {
        this.estDeGarde = false;
        this.statut = StatutPharmacie.OUVERTE;
    }

    /** Ferme la pharmacie */
    public void fermer() {
        this.statut = StatutPharmacie.FERMEE;
        this.estDeGarde = false;
    }

    /** Ouvre la pharmacie */
    public void ouvrir() {
        this.statut = StatutPharmacie.OUVERTE;
    }

    /** Valide la pharmacie (action admin) */
    public void valider(Utilisateur admin) {
        this.validee = true;
        this.admin = admin;
    }
}

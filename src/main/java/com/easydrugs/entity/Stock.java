package com.easydrugs.entity;

import com.easydrugs.util.Constants;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entité Stock — EASY-DRUGS.
 *
 * Représente la disponibilité d'un médicament dans une pharmacie spécifique.
 * Contrainte d'unicité : une seule ligne par couple (pharmacie, médicament).
 *
 * Le champ seuilAlerte déclenche une notification push vers la pharmacie
 * quand la quantité passe en dessous (géré par StockService).
 */
@Entity
@Table(
    name = "stock",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uq_stock_pharmacie_medicament",
            columnNames = {"pharmacie_id", "medicament_id"}
        )
    }
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Stock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pharmacie_id", nullable = false)
    private Pharmacie pharmacie;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medicament_id", nullable = false)
    private Medicament medicament;

    @Min(value = 0, message = "La quantité ne peut pas être négative")
    @Column(nullable = false)
    @Builder.Default
    private Integer quantite = 0;

    /** Calculé automatiquement : true si quantite > 0 */
    @Column(nullable = false)
    @Builder.Default
    private Boolean disponible = false;

    @NotNull(message = "Le prix est obligatoire")
    @DecimalMin(value = "0.0", inclusive = false, message = "Le prix doit être positif")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal prix;

    /** Seuil en dessous duquel une alerte stock faible est déclenchée */
    @Min(value = 0)
    @Column(name = "seuil_alerte", nullable = false)
    @Builder.Default
    private Integer seuilAlerte = Constants.SEUIL_STOCK_DEFAUT;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // ── Méthodes métier ─────────────────────────────────────────

    /**
     * Met à jour la quantité et recalcule automatiquement la disponibilité.
     * @param nouvelleQuantite Nouvelle quantité en stock
     */
    public void mettreAJourQuantite(int nouvelleQuantite) {
        this.quantite = Math.max(0, nouvelleQuantite);
        this.disponible = this.quantite > 0;
    }

    /**
     * Incrémente le stock (réapprovisionnement).
     */
    public void approvisionner(int quantiteAjoutee) {
        mettreAJourQuantite(this.quantite + quantiteAjoutee);
    }

    /**
     * Vérifie si le stock est sous le seuil d'alerte.
     */
    public boolean estSousSeuilAlerte() {
        return this.quantite <= this.seuilAlerte;
    }

    /**
     * Vérifie si le stock est en rupture totale.
     */
    public boolean estEnRupture() {
        return this.quantite == 0;
    }

    /**
     * Retourne l'état du stock sous forme lisible.
     */
    public String getEtatStock() {
        if (estEnRupture())       return "RUPTURE";
        if (estSousSeuilAlerte()) return "FAIBLE";
        return "OK";
    }
}

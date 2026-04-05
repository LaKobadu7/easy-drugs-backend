package com.easydrugs.repository;

import com.easydrugs.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository Stock — EASY-DRUGS.
 * Gestion de la disponibilité et des alertes de stock par pharmacie.
 */
@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {

    List<Stock> findByPharmacieId(Long pharmacieId);

    Optional<Stock> findByPharmacieIdAndMedicamentId(Long pharmacieId, Long medicamentId);

    List<Stock> findByPharmacieIdAndDisponibleTrue(Long pharmacieId);

    /**
     * Stocks sous le seuil d'alerte pour une pharmacie.
     * Utilisé pour le tableau de bord et les alertes push.
     */
    @Query("""
        SELECT s FROM Stock s
        WHERE s.pharmacie.id = :pharmacieId
          AND s.quantite <= s.seuilAlerte
          AND s.quantite > 0
        ORDER BY s.quantite ASC
        """)
    List<Stock> findStocksFaibles(@Param("pharmacieId") Long pharmacieId);

    /**
     * Stocks en rupture totale pour une pharmacie.
     */
    @Query("""
        SELECT s FROM Stock s
        WHERE s.pharmacie.id = :pharmacieId
          AND s.quantite = 0
        """)
    List<Stock> findRuptures(@Param("pharmacieId") Long pharmacieId);

    /**
     * Stock d'un médicament spécifique dans toutes les pharmacies validées.
     * Résultat trié par quantité décroissante.
     */
    @Query("""
        SELECT s FROM Stock s
        WHERE s.medicament.id = :medicamentId
          AND s.pharmacie.validee = true
          AND s.disponible = true
        ORDER BY s.quantite DESC
        """)
    List<Stock> findByMedicamentDisponible(@Param("medicamentId") Long medicamentId);

    /**
     * Nombre total de médicaments distincts en stock pour une pharmacie.
     */
    @Query("SELECT COUNT(s) FROM Stock s WHERE s.pharmacie.id = :pharmacieId AND s.disponible = true")
    long countMedicamentsDisponibles(@Param("pharmacieId") Long pharmacieId);

    /**
     * Nombre d'alertes stock actives pour une pharmacie.
     */
    @Query("""
        SELECT COUNT(s) FROM Stock s
        WHERE s.pharmacie.id = :pharmacieId
          AND s.quantite <= s.seuilAlerte
        """)
    long countAlertes(@Param("pharmacieId") Long pharmacieId);

    /**
     * Mise à jour en masse de la disponibilité après modification de quantité.
     */
    @Modifying
    @Query("UPDATE Stock s SET s.disponible = (s.quantite > 0) WHERE s.pharmacie.id = :pharmacieId")
    void recalculerDisponibilite(@Param("pharmacieId") Long pharmacieId);
}

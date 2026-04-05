package com.easydrugs.repository;

import com.easydrugs.entity.Medicament;
import com.easydrugs.enums.FormeGalenique;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository Médicament — EASY-DRUGS.
 * Supporte la recherche full-text PostgreSQL et le filtrage
 * par forme galénique (SOLIDE, LIQUIDE, SEMI_SOLIDE, GAZ).
 */
@Repository
public interface MedicamentRepository extends JpaRepository<Medicament, Long> {

    /**
     * Recherche par nom (insensible à la casse, recherche partielle).
     * Utilisée pour l'autocomplétion et la recherche principale.
     */
    @Query("""
        SELECT m FROM Medicament m
        WHERE LOWER(m.nom) LIKE LOWER(CONCAT('%', :terme, '%'))
        ORDER BY m.nom ASC
        """)
    List<Medicament> rechercherParNom(@Param("terme") String terme);

    /**
     * Recherche par nom ET forme galénique.
     */
    @Query("""
        SELECT m FROM Medicament m
        WHERE LOWER(m.nom) LIKE LOWER(CONCAT('%', :terme, '%'))
          AND m.formeGalenique = :forme
        ORDER BY m.nom ASC
        """)
    List<Medicament> rechercherParNomEtForme(
        @Param("terme") String terme,
        @Param("forme") FormeGalenique forme
    );

    /**
     * Recherche full-text PostgreSQL (plus performante sur grands volumes).
     * Utilise l'index GIN créé sur la colonne nom.
     */
    @Query(value = """
        SELECT * FROM medicament
        WHERE to_tsvector('french', nom) @@ plainto_tsquery('french', :terme)
        ORDER BY ts_rank(to_tsvector('french', nom), plainto_tsquery('french', :terme)) DESC
        """, nativeQuery = true)
    List<Medicament> rechercherFullText(@Param("terme") String terme);

    List<Medicament> findByFormeGalenique(FormeGalenique formeGalenique);

    boolean existsByNomIgnoreCase(String nom);

    /**
     * Médicaments disponibles dans au moins une pharmacie validée.
     */
    @Query("""
        SELECT DISTINCT m FROM Medicament m
        INNER JOIN Stock s ON s.medicament = m
        WHERE s.disponible = true
          AND s.pharmacie.validee = true
        ORDER BY m.nom ASC
        """)
    List<Medicament> findMedicamentsDisponibles();
}

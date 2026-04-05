package com.easydrugs.repository;

import com.easydrugs.entity.HistoriqueRecherche;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository Historique de recherche — EASY-DRUGS.
 */
@Repository
public interface HistoriqueRechercheRepository extends JpaRepository<HistoriqueRecherche, Long> {

    /** Dernières recherches d'un patient, les plus récentes en premier */
    List<HistoriqueRecherche> findByPatientIdOrderByDateRechercheDesc(Long patientId, Pageable pageable);

    /** Termes distincts pour les suggestions d'autocomplétion */
    @Query("""
        SELECT DISTINCT h.terme FROM HistoriqueRecherche h
        WHERE h.patient.id = :patientId
          AND LOWER(h.terme) LIKE LOWER(CONCAT('%', :prefix, '%'))
        ORDER BY h.terme ASC
        """)
    List<String> findTermesDistincts(
        @Param("patientId") Long patientId,
        @Param("prefix") String prefix
    );

    /** Termes les plus recherchés globalement (tendances) */
    @Query(value = """
        SELECT terme, COUNT(*) as nb
        FROM historique_recherche
        GROUP BY terme
        ORDER BY nb DESC
        LIMIT :limit
        """, nativeQuery = true)
    List<Object[]> findTermesTendance(@Param("limit") int limit);

    /** Effacer tout l'historique d'un patient (RGPD) */
    @Modifying
    @Query("DELETE FROM HistoriqueRecherche h WHERE h.patient.id = :patientId")
    void effacerHistoriquePatient(@Param("patientId") Long patientId);
}

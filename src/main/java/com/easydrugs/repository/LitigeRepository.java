package com.easydrugs.repository;

import com.easydrugs.entity.Litige;
import com.easydrugs.enums.StatutLitige;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository Litige — EASY-DRUGS.
 */
@Repository
public interface LitigeRepository extends JpaRepository<Litige, Long> {

    List<Litige> findByPatientIdOrderByDateOuvertureDesc(Long patientId);

    List<Litige> findByStatutOrderByDateOuvertureDesc(StatutLitige statut);

    List<Litige> findByAdminIdOrderByDateOuvertureDesc(Long adminId);

    /** Litiges non encore pris en charge (pour le tableau de bord admin) */
    @Query("SELECT l FROM Litige l WHERE l.statut = 'OUVERT' ORDER BY l.dateOuverture ASC")
    List<Litige> findLitigesEnAttente();

    long countByStatut(StatutLitige statut);

    @Query("SELECT COUNT(l) FROM Litige l WHERE l.patient.id = :patientId AND l.statut = 'OUVERT'")
    long countLitigesOuvertsByPatient(@Param("patientId") Long patientId);
}

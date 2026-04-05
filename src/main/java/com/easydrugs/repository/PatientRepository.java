package com.easydrugs.repository;

import com.easydrugs.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository Patient — EASY-DRUGS.
 */
@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {

    /** Recherche un patient via son compte utilisateur */
    Optional<Patient> findByUtilisateurId(Long utilisateurId);

    Optional<Patient> findByUtilisateurTelephone(String telephone);

    /** Patients ayant un token FCM valide (pour les push notifications) */
    @Query("SELECT p FROM Patient p WHERE p.fcmToken IS NOT NULL AND p.fcmToken <> ''")
    List<Patient> findAllAvecFcmToken();

    /**
     * Patients proches d'une pharmacie — utilisé pour les notifications
     * "Pharmacie de garde proche" et "Médicament disponible".
     * Filtre par rayon en km via la formule Haversine côté JPQL.
     */
    @Query("""
        SELECT p FROM Patient p
        WHERE p.latitudeActuelle IS NOT NULL
          AND p.longitudeActuelle IS NOT NULL
          AND p.fcmToken IS NOT NULL
          AND (
            6371 * acos(
              cos(radians(:lat)) * cos(radians(p.latitudeActuelle))
              * cos(radians(p.longitudeActuelle) - radians(:lon))
              + sin(radians(:lat)) * sin(radians(p.latitudeActuelle))
            )
          ) <= :rayonKm
        """)
    List<Patient> findPatientsProches(
        @Param("lat") double latitude,
        @Param("lon") double longitude,
        @Param("rayonKm") double rayonKm
    );

    /** Met à jour la position GPS et le token FCM d'un patient */
    @Modifying
    @Query("""
        UPDATE Patient p SET
            p.latitudeActuelle  = :lat,
            p.longitudeActuelle = :lon,
            p.fcmToken          = :fcmToken
        WHERE p.id = :id
        """)
    void mettreAJourPositionEtToken(
        @Param("id") Long id,
        @Param("lat") double lat,
        @Param("lon") double lon,
        @Param("fcmToken") String fcmToken
    );
}

package com.easydrugs.repository;

import com.easydrugs.entity.Pharmacie;
import com.easydrugs.enums.StatutPharmacie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PharmacieRepository extends JpaRepository<Pharmacie, Long> {

    List<Pharmacie> findByValideeTrue();

    List<Pharmacie> findByValideeTrueAndStatut(StatutPharmacie statut);

    List<Pharmacie> findByValideeTrueAndEstDeGardeTrue();

    boolean existsByNomIgnoreCase(String nom);

    @Query("""
        SELECT p FROM Pharmacie p
        WHERE p.validee = true
          AND (6371 * acos(
                cos(radians(:lat)) * cos(radians(p.latitude))
                * cos(radians(p.longitude) - radians(:lon))
                + sin(radians(:lat)) * sin(radians(p.latitude))
              )) <= :rayonKm
        ORDER BY (6371 * acos(
                cos(radians(:lat)) * cos(radians(p.latitude))
                * cos(radians(p.longitude) - radians(:lon))
                + sin(radians(:lat)) * sin(radians(p.latitude))
              )) ASC
        """)
    List<Pharmacie> findPharmaciesProches(
        @Param("lat") double lat,
        @Param("lon") double lon,
        @Param("rayonKm") double rayonKm
    );

   @Query("""
    SELECT p FROM Pharmacie p
    WHERE p.validee = true
      AND EXISTS (
        SELECT s FROM Stock s
        WHERE s.pharmacie = p
          AND s.disponible = true
          AND s.quantite > 0
          AND LOWER(s.medicament.nom) LIKE LOWER(CONCAT('%', :nom, '%'))
      )
      AND (6371 * acos(
            cos(radians(:lat)) * cos(radians(p.latitude))
            * cos(radians(p.longitude) - radians(:lon))
            + sin(radians(:lat)) * sin(radians(p.latitude))
          )) <= :rayonKm
    ORDER BY (6371 * acos(
            cos(radians(:lat)) * cos(radians(p.latitude))
            * cos(radians(p.longitude) - radians(:lon))
            + sin(radians(:lat)) * sin(radians(p.latitude))
          )) ASC
    """)
List<Pharmacie> findPharmaciesAvecMedicamentProches(
    @Param("nom") String nomMedicament,
    @Param("lat") double lat,
    @Param("lon") double lon,
    @Param("rayonKm") double rayonKm
);

    @Query("""
        SELECT p FROM Pharmacie p
        WHERE p.validee = true
          AND p.estDeGarde = true
          AND (6371 * acos(
                cos(radians(:lat)) * cos(radians(p.latitude))
                * cos(radians(p.longitude) - radians(:lon))
                + sin(radians(:lat)) * sin(radians(p.latitude))
              )) <= :rayonKm
        ORDER BY (6371 * acos(
                cos(radians(:lat)) * cos(radians(p.latitude))
                * cos(radians(p.longitude) - radians(:lon))
                + sin(radians(:lat)) * sin(radians(p.latitude))
              )) ASC
        """)
    List<Pharmacie> findPharmaciesDeGardeProches(
        @Param("lat") double lat,
        @Param("lon") double lon,
        @Param("rayonKm") double rayonKm
    );
}

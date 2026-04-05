package com.easydrugs.repository;

import com.easydrugs.entity.Notification;
import com.easydrugs.enums.TypeNotification;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository Notification — EASY-DRUGS.
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /** Notifications d'un patient, les plus récentes en premier */
    List<Notification> findByPatientIdOrderByCreatedAtDesc(Long patientId, Pageable pageable);

    /** Notifications non lues d'un patient */
    List<Notification> findByPatientIdAndLueFalseOrderByCreatedAtDesc(Long patientId);

    /** Nombre de notifications non lues */
    long countByPatientIdAndLueFalse(Long patientId);

    /** Marquer toutes les notifications d'un patient comme lues */
    @Modifying
    @Query("UPDATE Notification n SET n.lue = true WHERE n.patient.id = :patientId AND n.lue = false")
    int marquerToutesLues(@Param("patientId") Long patientId);

    /** Notifications par type pour un patient */
    List<Notification> findByPatientIdAndTypeOrderByCreatedAtDesc(Long patientId, TypeNotification type);
}

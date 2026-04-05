package com.easydrugs.service;

import com.easydrugs.entity.Medicament;
import com.easydrugs.entity.Notification;
import com.easydrugs.entity.Patient;
import com.easydrugs.entity.Pharmacie;
import com.easydrugs.entity.Stock;
import com.easydrugs.enums.TypeNotification;
import com.easydrugs.exception.ResourceNotFoundException;
import com.easydrugs.repository.NotificationRepository;
import com.easydrugs.repository.PatientRepository;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service Notification — EASY-DRUGS.
 *
 * Envoie des notifications push via Firebase Cloud Messaging (FCM)
 * et les persiste en base pour l'historique du patient.
 *
 * Tous les envois FCM sont asynchrones (@Async) pour ne pas
 * bloquer les requêtes principales.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final PatientRepository patientRepository;

    private static final double RAYON_NOTIF_KM = 5.0; // Rayon pour les notifications de garde

    // ── Envoi FCM ───────────────────────────────────────────────

    /**
     * Notifie les patients proches qu'une pharmacie vient d'activer le mode garde.
     */
    @Async
    @Transactional
    public void notifierPatientsGardeProche(Pharmacie pharmacie) {
        List<Patient> patients = patientRepository.findPatientsProches(
            pharmacie.getLatitude(),
            pharmacie.getLongitude(),
            RAYON_NOTIF_KM
        );

        String titre = "🌙 Pharmacie de garde proche";
        String message = String.format(
            "%s est de garde et disponible près de vous.", pharmacie.getNom()
        );

        patients.forEach(patient -> {
            envoyerFcm(patient, titre, message, TypeNotification.PHARMACIE_GARDE);
        });

        log.info("Notification garde envoyée à {} patient(s) proches de {}",
            patients.size(), pharmacie.getNom());
    }

    /**
     * Notifie les patients proches qu'un médicament est de nouveau disponible.
     */
    @Async
    @Transactional
    public void notifierStockDisponible(Pharmacie pharmacie, Medicament medicament) {
        List<Patient> patients = patientRepository.findPatientsProches(
            pharmacie.getLatitude(),
            pharmacie.getLongitude(),
            RAYON_NOTIF_KM
        );

        String titre = "💊 Médicament disponible";
        String message = String.format(
            "%s est maintenant disponible à %s.", medicament.getNom(), pharmacie.getNom()
        );

        patients.forEach(patient -> {
            envoyerFcm(patient, titre, message, TypeNotification.STOCK_DISPONIBLE);
        });

        log.info("Notification stock disponible envoyée pour {} à {}",
            medicament.getNom(), pharmacie.getNom());
    }

    /**
     * Alerte la pharmacie de ses stocks faibles via notification système.
     * (La pharmacie est aussi un utilisateur — on notifie via FCM si token disponible).
     */
    @Async
    public void notifierStocksFaibles(Pharmacie pharmacie, List<Stock> stocksFaibles) {
        // Les pharmacies reçoivent les alertes via le dashboard, pas via FCM patient
        // Cette méthode peut envoyer un email ou une notification interne
        stocksFaibles.forEach(stock ->
            log.warn("Alerte stock faible : {} à {} ({} restants / seuil {})",
                stock.getMedicament().getNom(),
                pharmacie.getNom(),
                stock.getQuantite(),
                stock.getSeuilAlerte())
        );
    }

    // ── Gestion des notifications patients ──────────────────────

    @Transactional(readOnly = true)
    public List<Notification> findByPatient(Long patientId, int page, int taille) {
        return notificationRepository.findByPatientIdOrderByCreatedAtDesc(
            patientId,
            PageRequest.of(page, taille)
        );
    }

    @Transactional(readOnly = true)
    public List<Notification> findNonLues(Long patientId) {
        return notificationRepository.findByPatientIdAndLueFalseOrderByCreatedAtDesc(patientId);
    }

    @Transactional(readOnly = true)
    public long countNonLues(Long patientId) {
        return notificationRepository.countByPatientIdAndLueFalse(patientId);
    }

    @Transactional
    public int marquerToutesLues(Long patientId) {
        return notificationRepository.marquerToutesLues(patientId);
    }

    @Transactional
    public void marquerLue(Long notificationId, Long patientId) {
        Notification notification = notificationRepository.findById(notificationId)
            .orElseThrow(() -> new ResourceNotFoundException("Notification introuvable"));
        if (!notification.getPatient().getId().equals(patientId)) {
            throw new ResourceNotFoundException("Notification introuvable pour ce patient");
        }
        notification.setLue(true);
        notificationRepository.save(notification);
    }

    // ── Helper FCM ──────────────────────────────────────────────

    private void envoyerFcm(
            Patient patient, String titre, String corps, TypeNotification type) {

        // Persister en base (historique)
        Notification notif = Notification.builder()
            .patient(patient)
            .titre(titre)
            .message(corps)
            .type(type)
            .lue(false)
            .build();
        notificationRepository.save(notif);

        // Envoi FCM si token disponible
        if (patient.getFcmToken() == null || patient.getFcmToken().isBlank()) {
            return;
        }

        try {
            Message fcmMessage = Message.builder()
                .setNotification(
                    com.google.firebase.messaging.Notification.builder()
                        .setTitle(titre)
                        .setBody(corps)
                        .build()
                )
                .setToken(patient.getFcmToken())
                .putData("type", type.name())
                .build();

            String reponse = FirebaseMessaging.getInstance().send(fcmMessage);
            log.debug("FCM envoyé à patient {} : {}", patient.getId(), reponse);

        } catch (Exception e) {
            log.error("Erreur FCM pour patient {} : {}", patient.getId(), e.getMessage());
            // On ne propage pas l'erreur — les notifications push sont non-critiques
        }
    }
}

package com.easydrugs.service;

import com.easydrugs.entity.Patient;
import com.easydrugs.entity.Utilisateur;
import com.easydrugs.exception.ResourceNotFoundException;
import com.easydrugs.repository.PatientRepository;
import com.easydrugs.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Patient — EASY-DRUGS.
 * Gestion du profil, position GPS, FCM token et suppression RGPD.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PatientService {

    private final PatientRepository patientRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final HistoriqueRechercheService historiqueService;

    @Transactional(readOnly = true)
    public Patient findById(Long id) {
        return patientRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Patient introuvable : id=" + id));
    }

    @Transactional(readOnly = true)
    public Patient findByTelephone(String telephone) {
        return patientRepository.findByUtilisateurTelephone(telephone)
            .orElseThrow(() -> new ResourceNotFoundException("Patient introuvable : " + telephone));
    }

    /**
     * Met à jour la position GPS et le token FCM du patient.
     * Appelé à chaque ouverture de l'application.
     */
    @Transactional
    public void mettreAJourPositionEtToken(Long patientId, double lat, double lon, String fcmToken) {
        patientRepository.mettreAJourPositionEtToken(patientId, lat, lon, fcmToken);
        log.debug("Position mise à jour pour patient {} : ({}, {})", patientId, lat, lon);
    }

    /**
     * Suppression RGPD : anonymise toutes les données personnelles.
     * Conserve le compte utilisateur désactivé pour l'intégrité référentielle.
     */
    @Transactional
    public void supprimerDonnees(Long patientId) {
        Patient patient = findById(patientId);
        Utilisateur utilisateur = patient.getUtilisateur();

        // Anonymiser
        utilisateur.setNom("SUPPRIMÉ");
        utilisateur.setPrenom("SUPPRIMÉ");
        utilisateur.setEmail(null);
        utilisateur.setTelephone("+237000000" + patientId);
        utilisateur.setActif(false);
        utilisateurRepository.save(utilisateur);

        patient.setAdresse(null);
        patient.setLatitudeActuelle(null);
        patient.setLongitudeActuelle(null);
        patient.setFcmToken(null);
        patientRepository.save(patient);

        // Effacer l'historique de recherche (RGPD)
        historiqueService.effacer(patientId);

        log.info("Données du patient {} anonymisées (RGPD)", patientId);
    }
}

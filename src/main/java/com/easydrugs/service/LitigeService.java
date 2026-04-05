package com.easydrugs.service;

import com.easydrugs.dto.request.LitigeRequest;
import com.easydrugs.entity.Litige;
import com.easydrugs.entity.Patient;
import com.easydrugs.entity.Utilisateur;
import com.easydrugs.enums.StatutLitige;
import com.easydrugs.exception.ResourceNotFoundException;
import com.easydrugs.exception.UnauthorizedException;
import com.easydrugs.repository.LitigeRepository;
import com.easydrugs.repository.PatientRepository;
import com.easydrugs.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service Litige — EASY-DRUGS.
 * Soumission par le patient, traitement par l'administrateur.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LitigeService {

    private final LitigeRepository litigeRepository;
    private final PatientRepository patientRepository;
    private final UtilisateurRepository utilisateurRepository;

    @Transactional
    public Litige soumettre(Long patientId, LitigeRequest request) {
        Patient patient = patientRepository.findById(patientId)
            .orElseThrow(() -> new ResourceNotFoundException("Patient introuvable"));

        Litige litige = Litige.builder()
            .patient(patient)
            .sujet(request.sujet())
            .description(request.description())
            .statut(StatutLitige.OUVERT)
            .build();

        litige = litigeRepository.save(litige);
        log.info("Litige soumis par patient {} : {}", patientId, request.sujet());
        return litige;
    }

    @Transactional(readOnly = true)
    public List<Litige> findByPatient(Long patientId) {
        return litigeRepository.findByPatientIdOrderByDateOuvertureDesc(patientId);
    }

    @Transactional(readOnly = true)
    public List<Litige> findEnAttente() {
        return litigeRepository.findLitigesEnAttente();
    }

    @Transactional(readOnly = true)
    public List<Litige> findByStatut(StatutLitige statut) {
        return litigeRepository.findByStatutOrderByDateOuvertureDesc(statut);
    }

    @Transactional
    public Litige prendreEnCharge(Long litigeId, Long adminId) {
        Litige litige = getLitigeOuException(litigeId);
        Utilisateur admin = utilisateurRepository.findById(adminId)
            .orElseThrow(() -> new ResourceNotFoundException("Admin introuvable"));
        litige.prendreEnCharge(admin);
        log.info("Litige {} pris en charge par admin {}", litigeId, adminId);
        return litigeRepository.save(litige);
    }

    @Transactional
    public Litige resoudre(Long litigeId) {
        Litige litige = getLitigeOuException(litigeId);
        litige.resoudre();
        log.info("Litige {} résolu", litigeId);
        return litigeRepository.save(litige);
    }

    @Transactional
    public Litige rejeter(Long litigeId) {
        Litige litige = getLitigeOuException(litigeId);
        litige.rejeter();
        log.info("Litige {} rejeté", litigeId);
        return litigeRepository.save(litige);
    }

    private Litige getLitigeOuException(Long id) {
        return litigeRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Litige introuvable : id=" + id));
    }
}

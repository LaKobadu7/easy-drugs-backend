package com.easydrugs.service;

import com.easydrugs.entity.HistoriqueRecherche;
import com.easydrugs.entity.Patient;
import com.easydrugs.exception.ResourceNotFoundException;
import com.easydrugs.repository.HistoriqueRechercheRepository;
import com.easydrugs.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service Historique de recherche — EASY-DRUGS.
 * Enregistrement asynchrone pour ne pas ralentir la recherche principale.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class HistoriqueRechercheService {

    private final HistoriqueRechercheRepository historiqueRepository;
    private final PatientRepository patientRepository;

    /** Enregistrement asynchrone d'une recherche */
    @Async
    @Transactional
    public void enregistrer(Long patientId, String terme) {
        try {
            Patient patient = patientRepository.findById(patientId)
                .orElse(null);
            if (patient == null) return;

            HistoriqueRecherche entree = HistoriqueRecherche.builder()
                .patient(patient)
                .terme(terme.trim())
                .build();
            historiqueRepository.save(entree);
        } catch (Exception e) {
            log.error("Erreur enregistrement historique : {}", e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public List<HistoriqueRecherche> findByPatient(Long patientId, int taille) {
        return historiqueRepository.findByPatientIdOrderByDateRechercheDesc(
            patientId, PageRequest.of(0, taille)
        );
    }

    @Transactional(readOnly = true)
    public List<String> suggestions(Long patientId, String prefix) {
        return historiqueRepository.findTermesDistincts(patientId, prefix);
    }

    @Transactional(readOnly = true)
    public List<Object[]> tendances(int limit) {
        return historiqueRepository.findTermesTendance(limit);
    }

    /** Effacement RGPD de tout l'historique d'un patient */
    @Transactional
    public void effacer(Long patientId) {
        historiqueRepository.effacerHistoriquePatient(patientId);
        log.info("Historique effacé pour patient {}", patientId);
    }
}

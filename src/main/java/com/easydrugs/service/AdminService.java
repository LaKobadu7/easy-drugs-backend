package com.easydrugs.service;

import com.easydrugs.entity.Utilisateur;
import com.easydrugs.enums.Role;
import com.easydrugs.exception.ResourceNotFoundException;
import com.easydrugs.repository.LitigeRepository;
import com.easydrugs.repository.PharmacieRepository;
import com.easydrugs.repository.StockRepository;
import com.easydrugs.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * Service Admin — EASY-DRUGS.
 * Supervision globale : utilisateurs, statistiques, litiges, pharmacies.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {

    private final UtilisateurRepository utilisateurRepository;
    private final PharmacieRepository pharmacieRepository;
    private final StockRepository stockRepository;
    private final LitigeRepository litigeRepository;
    private final PasswordEncoder passwordEncoder;

    // ── Gestion utilisateurs ─────────────────────────────────────

    @Transactional(readOnly = true)
    public List<Utilisateur> listerUtilisateurs() {
        return utilisateurRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Utilisateur> listerParRole(Role role) {
        return utilisateurRepository.findByRole(role);
    }

    @Transactional
    public void desactiverCompte(Long utilisateurId) {
        Utilisateur u = utilisateurRepository.findById(utilisateurId)
            .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));
        u.setActif(false);
        utilisateurRepository.save(u);
        log.info("Compte désactivé : {}", u.getTelephone());
    }

    @Transactional
    public void reactiverCompte(Long utilisateurId) {
        Utilisateur u = utilisateurRepository.findById(utilisateurId)
            .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));
        u.setActif(true);
        utilisateurRepository.save(u);
        log.info("Compte réactivé : {}", u.getTelephone());
    }

    /**
     * Crée un compte Pharmacie (seul l'admin peut le faire).
     */
    @Transactional
    public Utilisateur creerComptesPharmacie(String nom, String prenom,
            String telephone, String motDePasse) {
        if (utilisateurRepository.existsByTelephone(telephone)) {
            throw new IllegalArgumentException("Téléphone déjà utilisé : " + telephone);
        }
        Utilisateur u = Utilisateur.builder()
            .nom(nom).prenom(prenom).telephone(telephone)
            .motDePasse(passwordEncoder.encode(motDePasse))
            .role(Role.PHARMACIE).actif(true)
            .build();
        return utilisateurRepository.save(u);
    }

    // ── Statistiques globales ────────────────────────────────────

    /**
     * Tableau de bord admin : statistiques générales.
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getStatistiques() {
        long totalPatients    = utilisateurRepository.findByRole(Role.PATIENT).size();
        long totalPharmacie   = pharmacieRepository.findByValideeTrue().size();
        long totalNonValidees = pharmacieRepository.count()
                              - pharmacieRepository.findByValideeTrue().size();
        long litgesOuverts    = litigeRepository.countByStatut(
            com.easydrugs.enums.StatutLitige.OUVERT
        );

        return Map.of(
            "totalPatients",           totalPatients,
            "totalPharmaciesValidees", totalPharmacie,
            "totalPharmaciesEnAttente",totalNonValidees,
            "litgesOuverts",           litgesOuverts,
            "zonePrototype",           "Sangmélima (rayon 20 km)"
        );
    }
}

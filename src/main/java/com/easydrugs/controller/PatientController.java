package com.easydrugs.controller;

import com.easydrugs.entity.HistoriqueRecherche;
import com.easydrugs.entity.Patient;
import com.easydrugs.entity.Utilisateur;
import com.easydrugs.service.HistoriqueRechercheService;
import com.easydrugs.service.PatientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controller Patient — EASY-DRUGS.
 *
 * GET    /api/patients/me                  → Profil du patient connecté
 * PUT    /api/patients/me/position         → Mise à jour GPS + FCM token
 * GET    /api/patients/me/historique       → Historique des recherches
 * GET    /api/patients/me/historique/suggestions → Suggestions autocomplétion
 * DELETE /api/patients/me/historique       → Effacer historique (RGPD)
 * DELETE /api/patients/me                  → Supprimer compte (RGPD)
 * GET    /api/patients/{id}                → Profil d'un patient (ADMIN)
 */
@RestController
@RequestMapping("/api/patients")
@RequiredArgsConstructor
@Tag(name = "Patients", description = "Profil et historique patient")
@SecurityRequirement(name = "Bearer Authentication")
public class PatientController {

    private final PatientService patientService;
    private final HistoriqueRechercheService historiqueService;

    // ── Profil ──────────────────────────────────────────────────

    @Operation(summary = "Profil du patient connecté")
    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('PATIENT', 'ADMIN')")
    public ResponseEntity<Patient> monProfil(
            @AuthenticationPrincipal Utilisateur utilisateur) {
        Patient patient = patientService.findByTelephone(utilisateur.getTelephone());
        return ResponseEntity.ok(patient);
    }

    @Operation(summary = "Profil d'un patient par ID (admin uniquement)")
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Patient> findById(@PathVariable Long id) {
        return ResponseEntity.ok(patientService.findById(id));
    }

    // ── Position & Token FCM ─────────────────────────────────────

    @Operation(summary = "Mettre à jour la position GPS et le token FCM")
    @PutMapping("/me/position")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<Map<String, String>> mettreAJourPosition(
            @AuthenticationPrincipal Utilisateur utilisateur,
            @RequestParam Double lat,
            @RequestParam Double lon,
            @RequestParam(required = false) String fcmToken) {

        Patient patient = patientService.findByTelephone(utilisateur.getTelephone());
        patientService.mettreAJourPositionEtToken(
            patient.getId(), lat, lon,
            fcmToken != null ? fcmToken : patient.getFcmToken()
        );
        return ResponseEntity.ok(Map.of("message", "Position mise à jour"));
    }

    // ── Historique de recherche ──────────────────────────────────

    @Operation(summary = "Historique des recherches (20 dernières)")
    @GetMapping("/me/historique")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<List<HistoriqueRecherche>> monHistorique(
            @AuthenticationPrincipal Utilisateur utilisateur,
            @RequestParam(defaultValue = "20") int taille) {
        Patient patient = patientService.findByTelephone(utilisateur.getTelephone());
        return ResponseEntity.ok(
            historiqueService.findByPatient(patient.getId(), taille)
        );
    }

    @Operation(summary = "Suggestions d'autocomplétion basées sur l'historique")
    @GetMapping("/me/historique/suggestions")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<List<String>> suggestions(
            @AuthenticationPrincipal Utilisateur utilisateur,
            @RequestParam String prefix) {
        Patient patient = patientService.findByTelephone(utilisateur.getTelephone());
        return ResponseEntity.ok(
            historiqueService.suggestions(patient.getId(), prefix)
        );
    }

    @Operation(summary = "Effacer tout l'historique de recherche (RGPD)")
    @DeleteMapping("/me/historique")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<Map<String, String>> effacerHistorique(
            @AuthenticationPrincipal Utilisateur utilisateur) {
        Patient patient = patientService.findByTelephone(utilisateur.getTelephone());
        historiqueService.effacer(patient.getId());
        return ResponseEntity.ok(Map.of("message", "Historique effacé"));
    }

    // ── Suppression RGPD ─────────────────────────────────────────

    @Operation(summary = "Supprimer mon compte et toutes mes données (RGPD)")
    @DeleteMapping("/me")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<Map<String, String>> supprimerMonCompte(
            @AuthenticationPrincipal Utilisateur utilisateur) {
        Patient patient = patientService.findByTelephone(utilisateur.getTelephone());
        patientService.supprimerDonnees(patient.getId());
        return ResponseEntity.ok(
            Map.of("message", "Votre compte et vos données ont été supprimés.")
        );
    }
}

package com.easydrugs.controller;

import com.easydrugs.dto.request.LitigeRequest;
import com.easydrugs.entity.Litige;
import com.easydrugs.entity.Patient;
import com.easydrugs.entity.Utilisateur;
import com.easydrugs.enums.StatutLitige;
import com.easydrugs.service.LitigeService;
import com.easydrugs.service.PatientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller Litige — EASY-DRUGS.
 *
 * POST /api/litiges                          → Soumettre un litige (PATIENT)
 * GET  /api/litiges/me                       → Mes litiges (PATIENT)
 * GET  /api/litiges                          → Tous les litiges (ADMIN)
 * GET  /api/litiges/en-attente               → Litiges non pris en charge (ADMIN)
 * GET  /api/litiges/statut/{statut}          → Filtrer par statut (ADMIN)
 * PUT  /api/litiges/{id}/prendre-en-charge   → Prise en charge (ADMIN)
 * PUT  /api/litiges/{id}/resoudre            → Résoudre (ADMIN)
 * PUT  /api/litiges/{id}/rejeter             → Rejeter (ADMIN)
 */
@RestController
@RequestMapping("/api/litiges")
@RequiredArgsConstructor
@Tag(name = "Litiges", description = "Gestion des litiges et support")
@SecurityRequirement(name = "Bearer Authentication")
public class LitigeController {

    private final LitigeService litigeService;
    private final PatientService patientService;

    // ── Patient ──────────────────────────────────────────────────

    @Operation(summary = "Soumettre un litige")
    @PostMapping
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<Litige> soumettre(
            @AuthenticationPrincipal Utilisateur utilisateur,
            @Valid @RequestBody LitigeRequest request) {
        Patient patient = patientService.findByTelephone(utilisateur.getTelephone());
        Litige litige = litigeService.soumettre(patient.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(litige);
    }

    @Operation(summary = "Mes litiges")
    @GetMapping("/me")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<List<Litige>> mesLitiges(
            @AuthenticationPrincipal Utilisateur utilisateur) {
        Patient patient = patientService.findByTelephone(utilisateur.getTelephone());
        return ResponseEntity.ok(litigeService.findByPatient(patient.getId()));
    }

    // ── Admin ────────────────────────────────────────────────────

    @Operation(summary = "Tous les litiges (admin)")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Litige>> tousLesLitiges() {
        return ResponseEntity.ok(litigeService.findByStatut(StatutLitige.OUVERT));
    }

    @Operation(summary = "Litiges en attente de prise en charge")
    @GetMapping("/en-attente")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Litige>> enAttente() {
        return ResponseEntity.ok(litigeService.findEnAttente());
    }

    @Operation(summary = "Litiges filtrés par statut")
    @GetMapping("/statut/{statut}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Litige>> parStatut(
            @PathVariable StatutLitige statut) {
        return ResponseEntity.ok(litigeService.findByStatut(statut));
    }

    @Operation(summary = "Prendre en charge un litige")
    @PutMapping("/{id}/prendre-en-charge")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Litige> prendreEnCharge(
            @PathVariable Long id,
            @AuthenticationPrincipal Utilisateur admin) {
        return ResponseEntity.ok(litigeService.prendreEnCharge(id, admin.getId()));
    }

    @Operation(summary = "Résoudre un litige")
    @PutMapping("/{id}/resoudre")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Litige> resoudre(@PathVariable Long id) {
        return ResponseEntity.ok(litigeService.resoudre(id));
    }

    @Operation(summary = "Rejeter un litige")
    @PutMapping("/{id}/rejeter")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Litige> rejeter(@PathVariable Long id) {
        return ResponseEntity.ok(litigeService.rejeter(id));
    }
}

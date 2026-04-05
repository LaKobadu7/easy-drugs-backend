package com.easydrugs.controller;

import com.easydrugs.dto.request.PharmacieRequest;
import com.easydrugs.dto.response.PharmacieResponse;
import com.easydrugs.entity.Utilisateur;
import com.easydrugs.enums.StatutPharmacie;
import com.easydrugs.service.PharmacieService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller Pharmacie — EASY-DRUGS.
 *
 * GET  /api/pharmacies/nearby            → Pharmacies proches (public)
 * GET  /api/pharmacies/garde             → Pharmacies de garde (public)
 * GET  /api/pharmacies/{id}              → Détail pharmacie (public)
 * GET  /api/pharmacies                   → Toutes les pharmacies (ADMIN)
 * POST /api/pharmacies                   → Créer (ADMIN)
 * PUT  /api/pharmacies/{id}/statut       → Changer statut (PHARMACIE, ADMIN)
 * PUT  /api/pharmacies/{id}/garde        → Activer/désactiver garde (PHARMACIE)
 * PUT  /api/pharmacies/{id}/valider      → Valider (ADMIN)
 * DELETE /api/pharmacies/{id}            → Supprimer (ADMIN)
 */
@RestController
@RequestMapping("/api/pharmacies")
@RequiredArgsConstructor
@Tag(name = "Pharmacies", description = "Géolocalisation et gestion des pharmacies")
public class PharmacieController {

    private final PharmacieService pharmacieService;

    // ── Endpoints publics ───────────────────────────────────────

    @Operation(summary = "Pharmacies proches d'un point GPS")
    @GetMapping("/nearby")
    public ResponseEntity<List<PharmacieResponse>> proches(
            @RequestParam @NotNull @DecimalMin("-90") @DecimalMax("90")   Double lat,
            @RequestParam @NotNull @DecimalMin("-180") @DecimalMax("180") Double lon) {
        return ResponseEntity.ok(pharmacieService.findProches(lat, lon));
    }

    @Operation(summary = "Pharmacies de garde proches")
    @GetMapping("/garde")
    public ResponseEntity<List<PharmacieResponse>> deGarde(
            @RequestParam @NotNull Double lat,
            @RequestParam @NotNull Double lon) {
        return ResponseEntity.ok(pharmacieService.findDeGarde(lat, lon));
    }

    @Operation(summary = "Détail d'une pharmacie")
    @GetMapping("/{id}")
    public ResponseEntity<PharmacieResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(pharmacieService.findById(id));
    }

    // ── Endpoints protégés ──────────────────────────────────────

    @Operation(
        summary = "Liste toutes les pharmacies (admin)",
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PharmacieResponse>> listerToutes() {
        return ResponseEntity.ok(pharmacieService.findToutes());
    }

    @Operation(
        summary = "Créer une pharmacie (admin)",
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PharmacieResponse> creer(
            @Valid @RequestBody PharmacieRequest request,
            @AuthenticationPrincipal Utilisateur admin) {
        PharmacieResponse response = pharmacieService.creer(request, admin.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
        summary = "Mettre à jour le statut et les horaires",
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @PutMapping("/{id}/statut")
    @PreAuthorize("hasAnyRole('PHARMACIE', 'ADMIN')")
    public ResponseEntity<PharmacieResponse> mettreAJourStatut(
            @PathVariable Long id,
            @RequestParam StatutPharmacie statut,
            @RequestParam(required = false) String horaires) {
        return ResponseEntity.ok(
            pharmacieService.mettreAJourStatut(id, statut, horaires)
        );
    }

    @Operation(
        summary = "Activer le mode garde",
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @PutMapping("/{id}/garde/activer")
    @PreAuthorize("hasAnyRole('PHARMACIE', 'ADMIN')")
    public ResponseEntity<PharmacieResponse> activerGarde(@PathVariable Long id) {
        return ResponseEntity.ok(pharmacieService.activerGarde(id));
    }

    @Operation(
        summary = "Désactiver le mode garde",
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @PutMapping("/{id}/garde/desactiver")
    @PreAuthorize("hasAnyRole('PHARMACIE', 'ADMIN')")
    public ResponseEntity<PharmacieResponse> desactiverGarde(@PathVariable Long id) {
        return ResponseEntity.ok(pharmacieService.desactiverGarde(id));
    }

    @Operation(
        summary = "Valider une pharmacie (admin)",
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @PutMapping("/{id}/valider")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PharmacieResponse> valider(
            @PathVariable Long id,
            @AuthenticationPrincipal Utilisateur admin) {
        return ResponseEntity.ok(pharmacieService.valider(id, admin.getId()));
    }

    @Operation(
        summary = "Supprimer une pharmacie (admin)",
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        pharmacieService.supprimer(id);
        return ResponseEntity.noContent().build();
    }
}

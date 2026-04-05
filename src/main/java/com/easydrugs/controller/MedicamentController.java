package com.easydrugs.controller;

import com.easydrugs.dto.request.MedicamentRequest;
import com.easydrugs.dto.request.RechercheRequest;
import com.easydrugs.dto.response.RechercheResultResponse;
import com.easydrugs.entity.Medicament;
import com.easydrugs.enums.FormeGalenique;
import com.easydrugs.service.MedicamentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller Médicament — EASY-DRUGS.
 *
 * GET  /api/medicaments/search           → Recherche principale (public)
 * GET  /api/medicaments/autocomplete     → Autocomplétion (public)
 * GET  /api/medicaments/disponibles      → Médicaments disponibles (public)
 * GET  /api/medicaments/{id}             → Détail médicament (public)
 * GET  /api/medicaments                  → Liste complète (public)
 * POST /api/medicaments                  → Créer (PHARMACIE, ADMIN)
 * PUT  /api/medicaments/{id}             → Modifier (PHARMACIE, ADMIN)
 * DELETE /api/medicaments/{id}           → Supprimer (ADMIN)
 */
@RestController
@RequestMapping("/api/medicaments")
@RequiredArgsConstructor
@Tag(name = "Médicaments", description = "Recherche et gestion des médicaments")
public class MedicamentController {

    private final MedicamentService medicamentService;

    // ── Endpoints publics ───────────────────────────────────────

    /**
     * Recherche principale : retourne les pharmacies proches
     * ayant le médicament demandé, avec stock, prix et distance.
     */
    @Operation(summary = "Rechercher un médicament (résultats avec pharmacies proches)")
    @PostMapping("/search")
    public ResponseEntity<List<RechercheResultResponse>> rechercher(
            @Valid @RequestBody RechercheRequest request) {
        List<RechercheResultResponse> resultats = medicamentService.rechercher(request);
        return ResponseEntity.ok(resultats);
    }

    /**
     * Autocomplétion pour la barre de recherche mobile.
     */
    @Operation(summary = "Autocomplétion — suggestions de noms")
    @GetMapping("/autocomplete")
    public ResponseEntity<List<Medicament>> autocompleter(
            @RequestParam String terme) {
        return ResponseEntity.ok(medicamentService.autoCompleter(terme));
    }

    @Operation(summary = "Médicaments disponibles dans au moins une pharmacie")
    @GetMapping("/disponibles")
    public ResponseEntity<List<Medicament>> disponibles() {
        return ResponseEntity.ok(medicamentService.listerDisponibles());
    }

    @Operation(summary = "Filtrer par forme galénique")
    @GetMapping("/forme/{forme}")
    public ResponseEntity<List<Medicament>> parForme(
            @PathVariable FormeGalenique forme) {
        return ResponseEntity.ok(medicamentService.filtrerParForme(forme));
    }

    @Operation(summary = "Détail d'un médicament")
    @GetMapping("/{id}")
    public ResponseEntity<Medicament> findById(@PathVariable Long id) {
        return ResponseEntity.ok(medicamentService.findById(id));
    }

    @Operation(summary = "Liste complète des médicaments")
    @GetMapping
    public ResponseEntity<List<Medicament>> listerTous() {
        return ResponseEntity.ok(medicamentService.listerTous());
    }

    // ── Endpoints protégés ──────────────────────────────────────

    @Operation(summary = "Créer un médicament", security = @SecurityRequirement(name = "Bearer Authentication"))
    @PostMapping
    @PreAuthorize("hasAnyRole('PHARMACIE', 'ADMIN')")
    public ResponseEntity<Medicament> creer(
            @Valid @RequestBody MedicamentRequest request) {
        Medicament medicament = medicamentService.creer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(medicament);
    }

    @Operation(summary = "Modifier un médicament", security = @SecurityRequirement(name = "Bearer Authentication"))
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('PHARMACIE', 'ADMIN')")
    public ResponseEntity<Medicament> modifier(
            @PathVariable Long id,
            @Valid @RequestBody MedicamentRequest request) {
        return ResponseEntity.ok(medicamentService.modifier(id, request));
    }

    @Operation(summary = "Supprimer un médicament", security = @SecurityRequirement(name = "Bearer Authentication"))
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        medicamentService.supprimer(id);
        return ResponseEntity.noContent().build();
    }
}

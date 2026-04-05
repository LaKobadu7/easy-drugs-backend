package com.easydrugs.controller;

import com.easydrugs.dto.request.StockUpdateRequest;
import com.easydrugs.dto.response.StockResponse;
import com.easydrugs.service.StockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controller Stock — EASY-DRUGS.
 * Tous les endpoints nécessitent PHARMACIE ou ADMIN.
 *
 * GET    /api/stocks/pharmacie/{id}              → Stock complet d'une pharmacie
 * GET    /api/stocks/pharmacie/{id}/faibles      → Stocks sous le seuil d'alerte
 * GET    /api/stocks/pharmacie/{id}/ruptures     → Stocks épuisés
 * GET    /api/stocks/pharmacie/{id}/alertes/count→ Nombre d'alertes actives
 * PUT    /api/stocks/pharmacie/{id}              → Créer/mettre à jour un stock
 * PUT    /api/stocks/pharmacie/{id}/masse        → Mise à jour en masse
 * DELETE /api/stocks/pharmacie/{id}/medicament/{mid} → Retirer un médicament
 */
@RestController
@RequestMapping("/api/stocks")
@RequiredArgsConstructor
@Tag(name = "Stock", description = "Gestion du stock des pharmacies")
@SecurityRequirement(name = "Bearer Authentication")
public class StockController {

    private final StockService stockService;

    // ── Lecture ─────────────────────────────────────────────────

    @Operation(summary = "Stock complet d'une pharmacie")
    @GetMapping("/pharmacie/{pharmacieId}")
    @PreAuthorize("hasAnyRole('PHARMACIE', 'ADMIN')")
    public ResponseEntity<List<StockResponse>> findByPharmacie(
            @PathVariable Long pharmacieId) {
        return ResponseEntity.ok(stockService.findByPharmacie(pharmacieId));
    }

    @Operation(summary = "Stocks faibles (sous le seuil d'alerte)")
    @GetMapping("/pharmacie/{pharmacieId}/faibles")
    @PreAuthorize("hasAnyRole('PHARMACIE', 'ADMIN')")
    public ResponseEntity<List<StockResponse>> stocksFaibles(
            @PathVariable Long pharmacieId) {
        return ResponseEntity.ok(stockService.findStocksFaibles(pharmacieId));
    }

    @Operation(summary = "Ruptures de stock (quantité = 0)")
    @GetMapping("/pharmacie/{pharmacieId}/ruptures")
    @PreAuthorize("hasAnyRole('PHARMACIE', 'ADMIN')")
    public ResponseEntity<List<StockResponse>> ruptures(
            @PathVariable Long pharmacieId) {
        return ResponseEntity.ok(stockService.findRuptures(pharmacieId));
    }

    @Operation(summary = "Nombre d'alertes stock actives")
    @GetMapping("/pharmacie/{pharmacieId}/alertes/count")
    @PreAuthorize("hasAnyRole('PHARMACIE', 'ADMIN')")
    public ResponseEntity<Map<String, Long>> countAlertes(
            @PathVariable Long pharmacieId) {
        long count = stockService.countAlertes(pharmacieId);
        return ResponseEntity.ok(Map.of("alertes", count));
    }

    // ── Écriture ─────────────────────────────────────────────────

    @Operation(summary = "Créer ou mettre à jour un stock (upsert)")
    @PutMapping("/pharmacie/{pharmacieId}")
    @PreAuthorize("hasAnyRole('PHARMACIE', 'ADMIN')")
    public ResponseEntity<StockResponse> mettreAJour(
            @PathVariable Long pharmacieId,
            @Valid @RequestBody StockUpdateRequest request) {
        return ResponseEntity.ok(stockService.mettreAJour(pharmacieId, request));
    }

    @Operation(summary = "Mise à jour en masse (import CSV traité)")
    @PutMapping("/pharmacie/{pharmacieId}/masse")
    @PreAuthorize("hasAnyRole('PHARMACIE', 'ADMIN')")
    public ResponseEntity<List<StockResponse>> mettreAJourEnMasse(
            @PathVariable Long pharmacieId,
            @RequestBody List<@Valid StockUpdateRequest> requests) {
        return ResponseEntity.ok(stockService.mettreAJourEnMasse(pharmacieId, requests));
    }

    @Operation(summary = "Retirer un médicament du stock")
    @DeleteMapping("/pharmacie/{pharmacieId}/medicament/{medicamentId}")
    @PreAuthorize("hasAnyRole('PHARMACIE', 'ADMIN')")
    public ResponseEntity<Void> supprimer(
            @PathVariable Long pharmacieId,
            @PathVariable Long medicamentId) {
        stockService.supprimer(pharmacieId, medicamentId);
        return ResponseEntity.noContent().build();
    }
}

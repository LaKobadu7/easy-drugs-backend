package com.easydrugs.controller;

import com.easydrugs.entity.Utilisateur;
import com.easydrugs.enums.Role;
import com.easydrugs.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controller Admin — EASY-DRUGS.
 * Tous les endpoints nécessitent le rôle ADMIN.
 *
 * GET  /api/admin/stats                      → Statistiques globales
 * GET  /api/admin/utilisateurs               → Tous les utilisateurs
 * GET  /api/admin/utilisateurs/role/{role}   → Utilisateurs par rôle
 * PUT  /api/admin/utilisateurs/{id}/desactiver
 * PUT  /api/admin/utilisateurs/{id}/reactiver
 * POST /api/admin/pharmacies/compte          → Créer un compte pharmacie
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Administration", description = "Supervision et gestion globale")
@SecurityRequirement(name = "Bearer Authentication")
@PreAuthorize("hasRole('ADMIN')")   // Tous les endpoints de ce controller
public class AdminController {

    private final AdminService adminService;

    // ── Statistiques ─────────────────────────────────────────────

    @Operation(summary = "Tableau de bord — statistiques globales")
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> statistiques() {
        return ResponseEntity.ok(adminService.getStatistiques());
    }

    // ── Gestion utilisateurs ─────────────────────────────────────

    @Operation(summary = "Lister tous les utilisateurs")
    @GetMapping("/utilisateurs")
    public ResponseEntity<List<Utilisateur>> listerUtilisateurs() {
        return ResponseEntity.ok(adminService.listerUtilisateurs());
    }

    @Operation(summary = "Lister les utilisateurs par rôle")
    @GetMapping("/utilisateurs/role/{role}")
    public ResponseEntity<List<Utilisateur>> parRole(@PathVariable Role role) {
        return ResponseEntity.ok(adminService.listerParRole(role));
    }

    @Operation(summary = "Désactiver un compte utilisateur")
    @PutMapping("/utilisateurs/{id}/desactiver")
    public ResponseEntity<Map<String, String>> desactiver(@PathVariable Long id) {
        adminService.desactiverCompte(id);
        return ResponseEntity.ok(Map.of("message", "Compte désactivé"));
    }

    @Operation(summary = "Réactiver un compte utilisateur")
    @PutMapping("/utilisateurs/{id}/reactiver")
    public ResponseEntity<Map<String, String>> reactiver(@PathVariable Long id) {
        adminService.reactiverCompte(id);
        return ResponseEntity.ok(Map.of("message", "Compte réactivé"));
    }

    // ── Création compte pharmacie ────────────────────────────────

    @Operation(summary = "Créer un compte pour une pharmacie partenaire")
    @PostMapping("/pharmacies/compte")
    public ResponseEntity<Utilisateur> creerComptesPharmacie(
            @RequestParam String nom,
            @RequestParam String prenom,
            @RequestParam String telephone,
            @RequestParam String motDePasse) {
        Utilisateur compte = adminService.creerComptesPharmacie(
            nom, prenom, telephone, motDePasse
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(compte);
    }
}

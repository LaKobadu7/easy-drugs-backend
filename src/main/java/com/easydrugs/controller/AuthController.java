package com.easydrugs.controller;

import com.easydrugs.dto.request.LoginRequest;
import com.easydrugs.dto.request.RegisterRequest;
import com.easydrugs.dto.response.AuthResponse;
import com.easydrugs.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller Authentification — EASY-DRUGS.
 *
 * POST /api/auth/register  → Inscription patient
 * POST /api/auth/login     → Connexion (tous rôles)
 * POST /api/auth/refresh   → Rafraîchir le token JWT
 *
 * Tous ces endpoints sont PUBLICS (aucun token requis).
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentification", description = "Inscription, connexion et gestion des tokens JWT")
public class AuthController {

    private final AuthService authService;

    // ── POST /api/auth/register ──────────────────────────────────
    @Operation(
        summary = "Inscription d'un nouveau patient",
        description = "Crée un compte PATIENT et retourne un token JWT immédiatement utilisable."
    )
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> inscrire(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.inscrire(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ── POST /api/auth/login ─────────────────────────────────────
    @Operation(
        summary = "Connexion — retourne un token JWT",
        description = "Fonctionne pour les 3 rôles : PATIENT, PHARMACIE, ADMIN. " +
                      "Le token est à inclure dans le header Authorization: Bearer <token>."
    )
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> seConnecter(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.seConnecter(request));
    }

    // ── POST /api/auth/refresh ───────────────────────────────────
    @Operation(
        summary = "Rafraîchir le token JWT",
        description = "Génère un nouveau token d'accès à partir du refresh token (valide 7 jours)."
    )
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> rafraichir(@RequestParam String refreshToken) {
        return ResponseEntity.ok(authService.rafraichirToken(refreshToken));
    }
}

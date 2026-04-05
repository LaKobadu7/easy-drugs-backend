package com.easydrugs.dto.response;

import lombok.Builder;

/**
 * Réponse retournée après une inscription ou connexion réussie.
 * Contient le token JWT à stocker côté client (React Native SecureStore).
 */
@Builder
public record AuthResponse(

    /** Token JWT principal — durée 24h */
    String token,

    /** Refresh token — durée 7 jours */
    String refreshToken,

    Long utilisateurId,
    String nomComplet,
    String telephone,

    /** Rôle : PATIENT | PHARMACIE | ADMIN */
    String role

) {}

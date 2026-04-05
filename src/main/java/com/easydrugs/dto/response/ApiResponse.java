package com.easydrugs.dto.response;

import java.time.LocalDateTime;

/**
 * Wrapper générique pour toutes les réponses API EASY-DRUGS.
 * Assure une structure JSON cohérente sur tous les endpoints.
 *
 * Exemple de réponse succès :
 * {
 *   "succes": true,
 *   "message": "Succès",
 *   "data": { ... },
 *   "timestamp": "2026-04-03T10:30:00"
 * }
 */
public record ApiResponse<T>(
    boolean succes,
    String message,
    T data,
    LocalDateTime timestamp
) {

    /** Réponse succès avec données */
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, "Succès", data, LocalDateTime.now());
    }

    /** Réponse succès avec message personnalisé */
    public static <T> ApiResponse<T> ok(String message, T data) {
        return new ApiResponse<>(true, message, data, LocalDateTime.now());
    }

    /** Réponse succès sans données */
    public static <T> ApiResponse<T> ok(String message) {
        return new ApiResponse<>(true, message, null, LocalDateTime.now());
    }

    /** Réponse erreur */
    public static <T> ApiResponse<T> erreur(String message) {
        return new ApiResponse<>(false, message, null, LocalDateTime.now());
    }
}

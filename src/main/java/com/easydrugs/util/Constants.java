package com.easydrugs.util;

/**
 * Constantes globales de l'application EASY-DRUGS.
 */
public final class Constants {

    private Constants() {} // Classe utilitaire — pas d'instanciation

    // ── Géolocalisation ──────────────────────────────────────────
    /** Latitude du centre de Sangmélima */
    public static final double SANGMELIMA_LAT = 3.0167;
    /** Longitude du centre de Sangmélima */
    public static final double SANGMELIMA_LON = 11.9833;
    /** Rayon maximal du prototype en km */
    public static final double RAYON_PROTOTYPE_KM = 20.0;
    /** SRID utilisé pour PostGIS (WGS 84) */
    public static final int SRID = 4326;

    // ── Stock ────────────────────────────────────────────────────
    /** Seuil d'alerte stock faible par défaut */
    public static final int SEUIL_STOCK_DEFAUT = 10;

    // ── JWT ──────────────────────────────────────────────────────
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String HEADER_AUTHORIZATION = "Authorization";

    // ── Rôles ────────────────────────────────────────────────────
    public static final String ROLE_PATIENT   = "ROLE_PATIENT";
    public static final String ROLE_PHARMACIE = "ROLE_PHARMACIE";
    public static final String ROLE_ADMIN     = "ROLE_ADMIN";

    // ── Pagination ───────────────────────────────────────────────
    public static final int PAGE_SIZE_DEFAULT = 20;
    public static final int PAGE_SIZE_MAX     = 100;

    // ── Cache (noms des caches Redis) ────────────────────────────
    public static final String CACHE_PHARMACIES  = "pharmacies";
    public static final String CACHE_MEDICAMENTS = "medicaments";
    public static final String CACHE_STOCKS      = "stocks";

    // ── Messages d'erreur ────────────────────────────────────────
    public static final String ERR_USER_NOT_FOUND       = "Utilisateur introuvable";
    public static final String ERR_PHARMACIE_NOT_FOUND  = "Pharmacie introuvable";
    public static final String ERR_MEDICAMENT_NOT_FOUND = "Médicament introuvable";
    public static final String ERR_STOCK_NOT_FOUND      = "Stock introuvable";
    public static final String ERR_ZONE_INVALIDE        =
        "Cette pharmacie est hors de la zone prototype de Sangmélima (rayon %s km)";
    public static final String ERR_PHARMACIE_NON_VALIDEE =
        "Cette pharmacie n'a pas encore été validée par l'administrateur";
}

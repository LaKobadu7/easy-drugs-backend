package com.easydrugs.dto.response;

import lombok.Builder;
import java.math.BigDecimal;

/**
 * Résultat enrichi d'une recherche médicament — EASY-DRUGS.
 *
 * Chaque entrée représente UN médicament disponible DANS UNE pharmacie.
 * Si le médicament est dans 2 pharmacies, on aura 2 entrées distinctes.
 * Les résultats sont triés par distance croissante.
 */
@Builder
public record RechercheResultResponse(

    // ── Pharmacie ────────────────────────────────────────────────
    Long pharmacieId,
    String pharmacieNom,
    String pharmacieAdresse,

    /** OUVERTE | FERMEE | GARDE */
    String pharmacieStatut,

    Boolean pharmacieEstDeGarde,
    String pharmacieHoraires,

    /** Distance en kilomètres (non formatée, pour tri côté client) */
    Double distanceKm,

    /** Distance formatée pour l'affichage : "0.8 km" ou "800 m" */
    String distanceFormatee,

    // ── Médicament ────────────────────────────────────────────────
    Long medicamentId,
    String medicamentNom,

    /** SOLIDE | LIQUIDE | SEMI_SOLIDE | GAZ */
    String formeGalenique,

    /** Ex : "Boîte de 16 gélules", "Flacon 100ml" */
    String conditionnement,

    String fabricant,
    String imageUrl,

    // ── Stock ─────────────────────────────────────────────────────
    Integer quantiteDisponible,

    /** Prix en FCFA */
    BigDecimal prix

) {}

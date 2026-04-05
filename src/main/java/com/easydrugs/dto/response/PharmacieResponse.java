package com.easydrugs.dto.response;

import lombok.Builder;

/**
 * Réponse Pharmacie — données retournées au client React Native.
 */
@Builder
public record PharmacieResponse(

    Long id,
    String nom,
    String adresse,
    Double latitude,
    Double longitude,

    /** OUVERTE | FERMEE | GARDE */
    String statut,

    String horaires,
    Boolean estDeGarde,
    Boolean validee,

    /**
     * Distance formatée depuis la position du patient.
     * Ex : "0.8 km" ou "800 m"
     * Null si la requête ne fournit pas de coordonnées patient.
     */
    String distanceFormatee

) {}

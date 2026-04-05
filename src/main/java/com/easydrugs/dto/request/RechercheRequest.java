package com.easydrugs.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RechercheRequest(

    @NotBlank(message = "Le terme de recherche est obligatoire")
    String terme,

    @NotNull(message = "La latitude est obligatoire")
    Double latitude,

    @NotNull(message = "La longitude est obligatoire")
    Double longitude,

    /** Identifiant du patient connecté — pour l'historique (optionnel si public) */
    Long patientId,

    /**
     * Filtre optionnel par forme galénique.
     * Valeurs acceptées : SOLIDE | LIQUIDE | SEMI_SOLIDE | GAZ
     */
    String formeGalenique

) {}

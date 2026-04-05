package com.easydrugs.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record StockUpdateRequest(

    @NotNull(message = "L'identifiant du médicament est obligatoire")
    Long medicamentId,

    @Min(value = 0, message = "La quantité ne peut pas être négative")
    int quantite,

    @NotNull(message = "Le prix est obligatoire")
    @DecimalMin(value = "0.0", inclusive = false, message = "Le prix doit être positif")
    double prix,

    /** Optionnel — seuil d'alerte personnalisé (défaut : 10) */
    Integer seuilAlerte

) {}

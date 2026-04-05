package com.easydrugs.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PharmacieRequest(

    @NotBlank(message = "Le nom de la pharmacie est obligatoire")
    String nom,

    @NotBlank(message = "L'adresse est obligatoire")
    String adresse,

    @NotNull(message = "La latitude est obligatoire")
    @DecimalMin(value = "-90.0", message = "Latitude invalide")
    @DecimalMax(value = "90.0",  message = "Latitude invalide")
    Double latitude,

    @NotNull(message = "La longitude est obligatoire")
    @DecimalMin(value = "-180.0", message = "Longitude invalide")
    @DecimalMax(value = "180.0",  message = "Longitude invalide")
    Double longitude,

    /** Ex : "Lun-Sam : 7h30-20h00 | Dim : 9h-13h" */
    String horaires

) {}

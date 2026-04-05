package com.easydrugs.dto.request;

import jakarta.validation.constraints.NotBlank;

public record MedicamentRequest(

    @NotBlank(message = "Le nom du médicament est obligatoire")
    String nom,

    /**
     * Forme galénique : SOLIDE | LIQUIDE | SEMI_SOLIDE | GAZ
     */
    @NotBlank(message = "La forme galénique est obligatoire")
    String formeGalenique,

    String description,

    /** Ex : "Boîte de 16 gélules", "Flacon 100ml" */
    String conditionnement,

    String fabricant,

    /** URL Cloudinary de l'image */
    String imageUrl

) {}

package com.easydrugs.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record LoginRequest(

    @NotBlank(message = "Le numéro de téléphone est obligatoire")
    @Pattern(
        regexp = "^\\+237[0-9]{9}$",
        message = "Format attendu : +237XXXXXXXXX"
    )
    String telephone,

    @NotBlank(message = "Le mot de passe est obligatoire")
    String motDePasse

) {}

package com.easydrugs.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LitigeRequest(

    @NotBlank(message = "Le sujet est obligatoire")
    @Size(max = 200, message = "Le sujet ne peut pas dépasser 200 caractères")
    String sujet,

    @NotBlank(message = "La description est obligatoire")
    String description

) {}

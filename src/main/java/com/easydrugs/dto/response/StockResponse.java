package com.easydrugs.dto.response;

import lombok.Builder;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Réponse Stock — état du stock d'un médicament dans une pharmacie.
 */
@Builder
public record StockResponse(

    Long id,
    Long pharmacieId,
    String pharmacieNom,
    Long medicamentId,
    String medicamentNom,

    /** SOLIDE | LIQUIDE | SEMI_SOLIDE | GAZ */
    String formeGalenique,

    /** Ex : "Boîte de 16 gélules" */
    String conditionnement,

    Integer quantite,
    Boolean disponible,

    /** Prix en FCFA */
    BigDecimal prix,

    /** Quantité minimale avant alerte */
    Integer seuilAlerte,

    /** OK | FAIBLE | RUPTURE */
    String etatStock,

    LocalDateTime updatedAt

) {}

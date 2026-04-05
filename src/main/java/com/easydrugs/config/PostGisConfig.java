// ================================================================
//  PostGisConfig.java
//  Enregistre le type géométrique PostGIS auprès d'Hibernate
// ================================================================
package com.easydrugs.config;

import org.hibernate.boot.model.TypeContributions;
import org.hibernate.boot.model.TypeContributor;
import org.hibernate.service.ServiceRegistry;
import org.springframework.context.annotation.Configuration;

/**
 * PostGIS est géré automatiquement par hibernate-spatial.
 * Cette classe documente l'intention — aucune config manuelle nécessaire
 * avec Spring Boot 3 + hibernate-spatial 6.x.
 *
 * Le dialect PostgreSQLDialect (défini dans application.yml) suffit.
 * Les colonnes geometry(Point, 4326) sont mappées avec @Column(columnDefinition = "geometry(Point, 4326)").
 */
@Configuration
public class PostGisConfig {
    // Hibernate Spatial auto-configure le support PostGIS
    // via le dialect PostgreSQLDialect déclaré dans application.yml
}

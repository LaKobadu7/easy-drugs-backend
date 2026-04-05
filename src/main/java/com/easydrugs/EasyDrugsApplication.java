package com.easydrugs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * =====================================================
 *  EASY-DRUGS — Application principale Spring Boot 3
 *  Géolocalisation de médicaments — Sangmélima, Cameroun
 *  Version prototype : 2 pharmacies pilotes
 * =====================================================
 */
@SpringBootApplication
@EnableCaching       // Active le cache Redis
@EnableScheduling    // Pour les tâches planifiées (ex: alertes stock)
public class EasyDrugsApplication {

    public static void main(String[] args) {
        SpringApplication.run(EasyDrugsApplication.class, args);
    }
}

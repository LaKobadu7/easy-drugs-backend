package com.easydrugs.service;

import com.easydrugs.dto.request.StockUpdateRequest;
import com.easydrugs.dto.response.StockResponse;
import com.easydrugs.entity.Medicament;
import com.easydrugs.entity.Pharmacie;
import com.easydrugs.entity.Stock;
import com.easydrugs.exception.ResourceNotFoundException;
import com.easydrugs.repository.MedicamentRepository;
import com.easydrugs.repository.PharmacieRepository;
import com.easydrugs.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Service Stock — EASY-DRUGS.
 *
 * Gère la disponibilité des médicaments par pharmacie.
 * Inclut les alertes de stock faible et la mise à jour en masse (CSV).
 * Une tâche planifiée vérifie les alertes toutes les heures.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StockService {

    private final StockRepository stockRepository;
    private final PharmacieRepository pharmacieRepository;
    private final MedicamentRepository medicamentRepository;
    private final NotificationService notificationService;

    // ── Lecture ─────────────────────────────────────────────────

    @Cacheable(value = "stocks", key = "'pharmacie_' + #pharmacieId")
    @Transactional(readOnly = true)
    public List<StockResponse> findByPharmacie(Long pharmacieId) {
        return stockRepository.findByPharmacieId(pharmacieId)
            .stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<StockResponse> findStocksFaibles(Long pharmacieId) {
        return stockRepository.findStocksFaibles(pharmacieId)
            .stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<StockResponse> findRuptures(Long pharmacieId) {
        return stockRepository.findRuptures(pharmacieId)
            .stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public long countAlertes(Long pharmacieId) {
        return stockRepository.countAlertes(pharmacieId);
    }

    // ── Écriture (pharmacie) ─────────────────────────────────────

    /**
     * Crée ou met à jour le stock d'un médicament dans une pharmacie.
     * Si le stock n'existe pas encore, il est créé (upsert).
     */
    @CacheEvict(value = "stocks", key = "'pharmacie_' + #pharmacieId")
    @Transactional
    public StockResponse mettreAJour(Long pharmacieId, StockUpdateRequest request) {
        Pharmacie pharmacie = pharmacieRepository.findById(pharmacieId)
            .orElseThrow(() -> new ResourceNotFoundException("Pharmacie introuvable : " + pharmacieId));

        Medicament medicament = medicamentRepository.findById(request.medicamentId())
            .orElseThrow(() -> new ResourceNotFoundException("Médicament introuvable : " + request.medicamentId()));

        // Upsert : créer ou mettre à jour
        Optional<Stock> existant = stockRepository
            .findByPharmacieIdAndMedicamentId(pharmacieId, request.medicamentId());

        Stock stock;
        boolean etaitIndisponible;

        if (existant.isPresent()) {
            stock = existant.get();
            etaitIndisponible = !stock.getDisponible();
            stock.mettreAJourQuantite(request.quantite());
            stock.setPrix(BigDecimal.valueOf(request.prix()));
            if (request.seuilAlerte() != null) stock.setSeuilAlerte(request.seuilAlerte());
        } else {
            etaitIndisponible = true;
            stock = Stock.builder()
                .pharmacie(pharmacie)
                .medicament(medicament)
                .quantite(request.quantite())
                .disponible(request.quantite() > 0)
                .prix(BigDecimal.valueOf(request.prix()))
                .seuilAlerte(request.seuilAlerte() != null ? request.seuilAlerte() : 10)
                .build();
        }

        stock = stockRepository.save(stock);

        // Notifier les patients si le médicament était indisponible et est maintenant disponible
        if (etaitIndisponible && stock.getDisponible()) {
            notificationService.notifierStockDisponible(pharmacie, medicament);
        }

        // Alerte stock faible pour la pharmacie
        if (stock.estSousSeuilAlerte() && !stock.estEnRupture()) {
            log.warn("Stock faible détecté : {} à {} ({} restants)",
                medicament.getNom(), pharmacie.getNom(), stock.getQuantite());
        }

        log.info("Stock mis à jour : {} à {} → {} unités",
            medicament.getNom(), pharmacie.getNom(), stock.getQuantite());

        return toResponse(stock);
    }

    /**
     * Mise à jour en masse depuis un tableau de requêtes (import CSV côté service).
     * Chaque élément est traité individuellement dans la même transaction.
     */
    @CacheEvict(value = "stocks", key = "'pharmacie_' + #pharmacieId")
    @Transactional
    public List<StockResponse> mettreAJourEnMasse(Long pharmacieId, List<StockUpdateRequest> requests) {
        return requests.stream()
            .map(req -> mettreAJour(pharmacieId, req))
            .toList();
    }

    /**
     * Supprime un stock (médicament retiré du catalogue pharmacie).
     */
    @CacheEvict(value = "stocks", key = "'pharmacie_' + #pharmacieId")
    @Transactional
    public void supprimer(Long pharmacieId, Long medicamentId) {
        Stock stock = stockRepository
            .findByPharmacieIdAndMedicamentId(pharmacieId, medicamentId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Stock introuvable pour pharmacie=" + pharmacieId + " medicament=" + medicamentId
            ));
        stockRepository.delete(stock);
    }

    // ── Tâche planifiée : vérification des alertes ─────────────

    /**
     * Vérification automatique des stocks faibles toutes les heures.
     * Envoie des alertes push aux pharmacies concernées.
     */
    @Scheduled(fixedRate = 3_600_000) // toutes les heures
    @Transactional(readOnly = true)
    public void verifierAlertesPeriodiques() {
        log.debug("Vérification périodique des stocks faibles...");
        pharmacieRepository.findByValideeTrue().forEach(pharmacie -> {
            List<Stock> faibles = stockRepository.findStocksFaibles(pharmacie.getId());
            if (!faibles.isEmpty()) {
                log.info("Alertes stock pour {} : {} médicament(s) sous le seuil",
                    pharmacie.getNom(), faibles.size());
                // La notification est traitée par NotificationService
                notificationService.notifierStocksFaibles(pharmacie, faibles);
            }
        });
    }

    // ── Mapper ──────────────────────────────────────────────────

    private StockResponse toResponse(Stock s) {
        return StockResponse.builder()
            .id(s.getId())
            .pharmacieId(s.getPharmacie().getId())
            .pharmacieNom(s.getPharmacie().getNom())
            .medicamentId(s.getMedicament().getId())
            .medicamentNom(s.getMedicament().getNom())
            .formeGalenique(s.getMedicament().getFormeGalenique().name())
            .conditionnement(s.getMedicament().getConditionnement())
            .quantite(s.getQuantite())
            .disponible(s.getDisponible())
            .prix(s.getPrix())
            .seuilAlerte(s.getSeuilAlerte())
            .etatStock(s.getEtatStock())
            .updatedAt(s.getUpdatedAt())
            .build();
    }
}

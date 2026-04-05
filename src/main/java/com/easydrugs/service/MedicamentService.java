package com.easydrugs.service;

import com.easydrugs.dto.request.MedicamentRequest;
import com.easydrugs.dto.request.RechercheRequest;
import com.easydrugs.dto.response.RechercheResultResponse;
import com.easydrugs.entity.Medicament;
import com.easydrugs.entity.Pharmacie;
import com.easydrugs.entity.Stock;
import com.easydrugs.enums.FormeGalenique;
import com.easydrugs.exception.ResourceNotFoundException;
import com.easydrugs.repository.MedicamentRepository;
import com.easydrugs.repository.PharmacieRepository;
import com.easydrugs.repository.StockRepository;
import com.easydrugs.util.GeoUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MedicamentService {

    private final MedicamentRepository medicamentRepository;
    private final PharmacieRepository pharmacieRepository;
    private final StockRepository stockRepository;
    private final HistoriqueRechercheService historiqueService;

    private static final double RAYON_RECHERCHE_KM = 20.0;

    @Transactional
    public List<RechercheResultResponse> rechercher(RechercheRequest request) {
        log.debug("Recherche : '{}' depuis ({}, {})",
            request.terme(), request.latitude(), request.longitude());

        List<Pharmacie> pharmacies = pharmacieRepository.findPharmaciesAvecMedicamentProches(
            request.terme(),
            request.latitude(),
            request.longitude(),
            RAYON_RECHERCHE_KM
        );

        List<RechercheResultResponse> resultats = new ArrayList<>();

        for (Pharmacie pharmacie : pharmacies) {
            List<Stock> stocks = stockRepository.findByPharmacieId(pharmacie.getId())
                .stream()
                .filter(s -> s.getDisponible()
                    && s.getMedicament().getNom().toLowerCase()
                       .contains(request.terme().toLowerCase()))
                .toList();

            if (request.formeGalenique() != null) {
                stocks = stocks.stream()
                    .filter(s -> s.getMedicament().getFormeGalenique()
                        == FormeGalenique.valueOf(request.formeGalenique()))
                    .toList();
            }

            for (Stock stock : stocks) {
                double distanceKm = GeoUtils.distanceHaversineKm(
                    request.latitude(), request.longitude(),
                    pharmacie.getLatitude(), pharmacie.getLongitude()
                );

                resultats.add(RechercheResultResponse.builder()
                    .pharmacieId(pharmacie.getId())
                    .pharmacieNom(pharmacie.getNom())
                    .pharmacieAdresse(pharmacie.getAdresse())
                    .pharmacieStatut(pharmacie.getStatut().name())
                    .pharmacieEstDeGarde(pharmacie.getEstDeGarde())
                    .pharmacieHoraires(pharmacie.getHoraires())
                    .distanceKm(distanceKm)
                    .distanceFormatee(GeoUtils.formaterDistance(distanceKm))
                    .medicamentId(stock.getMedicament().getId())
                    .medicamentNom(stock.getMedicament().getNom())
                    .formeGalenique(stock.getMedicament().getFormeGalenique().name())
                    .conditionnement(stock.getMedicament().getConditionnement())
                    .fabricant(stock.getMedicament().getFabricant())
                    .imageUrl(stock.getMedicament().getImageUrl())
                    .quantiteDisponible(stock.getQuantite())
                    .prix(stock.getPrix())
                    .build()
                );
            }
        }

        if (request.patientId() != null && !request.terme().isBlank()) {
            historiqueService.enregistrer(request.patientId(), request.terme());
        }

        log.info("Recherche '{}' : {} résultats", request.terme(), resultats.size());
        return resultats;
    }

    @Cacheable(value = "medicaments", key = "#id")
    @Transactional(readOnly = true)
    public Medicament findById(Long id) {
        return medicamentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Médicament introuvable : id=" + id
            ));
    }

    @Transactional(readOnly = true)
    public List<Medicament> listerTous() {
        return medicamentRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Medicament> listerDisponibles() {
        return medicamentRepository.findMedicamentsDisponibles();
    }

    @Transactional(readOnly = true)
    public List<Medicament> filtrerParForme(FormeGalenique forme) {
        return medicamentRepository.findByFormeGalenique(forme);
    }

    @Transactional(readOnly = true)
    public List<Medicament> autoCompleter(String terme) {
        return medicamentRepository.rechercherParNom(terme);
    }

    @CacheEvict(value = "medicaments", allEntries = true)
    @Transactional
    public Medicament creer(MedicamentRequest request) {
        if (medicamentRepository.existsByNomIgnoreCase(request.nom())) {
            throw new IllegalArgumentException(
                "Un médicament avec ce nom existe déjà : " + request.nom()
            );
        }
        Medicament medicament = Medicament.builder()
            .nom(request.nom())
            .formeGalenique(FormeGalenique.valueOf(request.formeGalenique()))
            .description(request.description())
            .conditionnement(request.conditionnement())
            .fabricant(request.fabricant())
            .imageUrl(request.imageUrl())
            .build();
        return medicamentRepository.save(medicament);
    }

    @CacheEvict(value = "medicaments", key = "#id")
    @Transactional
    public Medicament modifier(Long id, MedicamentRequest request) {
        Medicament medicament = findById(id);
        medicament.setNom(request.nom());
        medicament.setFormeGalenique(FormeGalenique.valueOf(request.formeGalenique()));
        medicament.setDescription(request.description());
        medicament.setConditionnement(request.conditionnement());
        medicament.setFabricant(request.fabricant());
        if (request.imageUrl() != null) medicament.setImageUrl(request.imageUrl());
        return medicamentRepository.save(medicament);
    }

    @CacheEvict(value = "medicaments", key = "#id")
    @Transactional
    public void supprimer(Long id) {
        findById(id);
        medicamentRepository.deleteById(id);
    }
}

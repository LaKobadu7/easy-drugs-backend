package com.easydrugs.service;

import com.easydrugs.dto.request.PharmacieRequest;
import com.easydrugs.dto.response.PharmacieResponse;
import com.easydrugs.entity.Pharmacie;
import com.easydrugs.entity.Utilisateur;
import com.easydrugs.enums.StatutPharmacie;
import com.easydrugs.exception.ResourceNotFoundException;
import com.easydrugs.exception.ZoneGeographiqueException;
import com.easydrugs.repository.PharmacieRepository;
import com.easydrugs.repository.UtilisateurRepository;
import com.easydrugs.util.GeoUtils;
import com.easydrugs.util.SangmelimaGeoFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PharmacieService {

    private final PharmacieRepository pharmacieRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final SangmelimaGeoFilter geoFilter;
    private final NotificationService notificationService;

    private static final GeometryFactory GEOMETRY_FACTORY =
        new GeometryFactory(new PrecisionModel(), 4326);

    private static final double RAYON_PROCHE_KM = 5.0;
    private static final double RAYON_GARDE_KM  = 20.0;

    @Transactional(readOnly = true)
    public List<PharmacieResponse> findProches(double lat, double lon) {
        List<Pharmacie> pharmacies = pharmacieRepository
            .findPharmaciesProches(lat, lon, RAYON_PROCHE_KM);
        return pharmacies.stream()
            .map(p -> toResponse(p, lat, lon))
            .toList();
    }

    @Transactional(readOnly = true)
    public List<PharmacieResponse> findDeGarde(double lat, double lon) {
        List<Pharmacie> pharmacies = pharmacieRepository
            .findPharmaciesDeGardeProches(lat, lon, RAYON_GARDE_KM);
        return pharmacies.stream()
            .map(p -> toResponse(p, lat, lon))
            .toList();
    }

    @Cacheable(value = "pharmacies", key = "#id")
    @Transactional(readOnly = true)
    public PharmacieResponse findById(Long id) {
        Pharmacie pharmacie = getPharmacieOuException(id);
        return toResponse(pharmacie, null, null);
    }

    @Transactional(readOnly = true)
    public List<PharmacieResponse> findToutes() {
        return pharmacieRepository.findByValideeTrue()
            .stream()
            .map(p -> toResponse(p, null, null))
            .toList();
    }

    @CacheEvict(value = "pharmacies", key = "#pharmacieId")
    @Transactional
    public PharmacieResponse activerGarde(Long pharmacieId) {
        Pharmacie pharmacie = getPharmacieValideOuException(pharmacieId);
        pharmacie.activerGarde();
        pharmacieRepository.save(pharmacie);
        notificationService.notifierPatientsGardeProche(pharmacie);
        log.info("Mode garde activé : {}", pharmacie.getNom());
        return toResponse(pharmacie, null, null);
    }

    @CacheEvict(value = "pharmacies", key = "#pharmacieId")
    @Transactional
    public PharmacieResponse desactiverGarde(Long pharmacieId) {
        Pharmacie pharmacie = getPharmacieValideOuException(pharmacieId);
        pharmacie.desactiverGarde();
        pharmacieRepository.save(pharmacie);
        log.info("Mode garde désactivé : {}", pharmacie.getNom());
        return toResponse(pharmacie, null, null);
    }

    @CacheEvict(value = "pharmacies", key = "#pharmacieId")
    @Transactional
    public PharmacieResponse mettreAJourStatut(
            Long pharmacieId, StatutPharmacie statut, String horaires) {
        Pharmacie pharmacie = getPharmacieValideOuException(pharmacieId);
        pharmacie.setStatut(statut);
        if (horaires != null) pharmacie.setHoraires(horaires);
        pharmacieRepository.save(pharmacie);
        return toResponse(pharmacie, null, null);
    }

    @Transactional
    public PharmacieResponse creer(PharmacieRequest request, Long adminId) {
        geoFilter.valider(request.latitude(), request.longitude());

        if (pharmacieRepository.existsByNomIgnoreCase(request.nom())) {
            throw new IllegalArgumentException(
                "Une pharmacie avec ce nom existe déjà : " + request.nom()
            );
        }

        Utilisateur admin = utilisateurRepository.findById(adminId)
            .orElseThrow(() -> new ResourceNotFoundException("Admin introuvable"));

        Point coordonnees = GEOMETRY_FACTORY.createPoint(
            new Coordinate(request.longitude(), request.latitude())
        );

        Pharmacie pharmacie = Pharmacie.builder()
            .nom(request.nom())
            .adresse(request.adresse())
            .coordonnees(coordonnees)
            .latitude(request.latitude())
            .longitude(request.longitude())
            .horaires(request.horaires())
            .statut(StatutPharmacie.OUVERTE)
            .validee(false)
            .zoneGeo("SANGMELIMA")
            .admin(admin)
            .build();

        pharmacie = pharmacieRepository.save(pharmacie);
        log.info("Pharmacie créée (en attente de validation) : {}", pharmacie.getNom());
        return toResponse(pharmacie, null, null);
    }

    @CacheEvict(value = "pharmacies", allEntries = true)
    @Transactional
    public PharmacieResponse valider(Long pharmacieId, Long adminId) {
        Pharmacie pharmacie = getPharmacieOuException(pharmacieId);
        Utilisateur admin = utilisateurRepository.findById(adminId)
            .orElseThrow(() -> new ResourceNotFoundException("Admin introuvable"));
        pharmacie.valider(admin);
        pharmacieRepository.save(pharmacie);
        log.info("Pharmacie validée : {}", pharmacie.getNom());
        return toResponse(pharmacie, null, null);
    }

    @CacheEvict(value = "pharmacies", allEntries = true)
    @Transactional
    public void supprimer(Long pharmacieId) {
        getPharmacieOuException(pharmacieId);
        pharmacieRepository.deleteById(pharmacieId);
        log.info("Pharmacie supprimée : id={}", pharmacieId);
    }

    private Pharmacie getPharmacieOuException(Long id) {
        return pharmacieRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Pharmacie introuvable : id=" + id
            ));
    }

    private Pharmacie getPharmacieValideOuException(Long id) {
        Pharmacie p = getPharmacieOuException(id);
        if (!p.getValidee()) {
            throw new ZoneGeographiqueException(
                "Cette pharmacie n'est pas encore validée par l'administrateur."
            );
        }
        return p;
    }

    private PharmacieResponse toResponse(Pharmacie p, Double patLat, Double patLon) {
        String distanceFormatee = null;
        if (patLat != null && patLon != null) {
            double km = GeoUtils.distanceHaversineKm(
                patLat, patLon, p.getLatitude(), p.getLongitude()
            );
            distanceFormatee = GeoUtils.formaterDistance(km);
        }
        return PharmacieResponse.builder()
            .id(p.getId())
            .nom(p.getNom())
            .adresse(p.getAdresse())
            .latitude(p.getLatitude())
            .longitude(p.getLongitude())
            .statut(p.getStatut().name())
            .horaires(p.getHoraires())
            .estDeGarde(p.getEstDeGarde())
            .validee(p.getValidee())
            .distanceFormatee(distanceFormatee)
            .build();
    }
}

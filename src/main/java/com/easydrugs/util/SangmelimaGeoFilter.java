package com.easydrugs.util;

import com.easydrugs.exception.ZoneGeographiqueException;
import org.springframework.stereotype.Component;

/**
 * Filtre géographique du prototype EASY-DRUGS.
 *
 * Toute pharmacie ou coordonnée en dehors d'un rayon de 20 km
 * autour du centre de Sangmélima est rejetée.
 * Ce filtre sera désactivé lors du déploiement national.
 */
@Component
public class SangmelimaGeoFilter {

    /**
     * Vérifie qu'une coordonnée est dans la zone prototype.
     *
     * @param latitude  Latitude à vérifier
     * @param longitude Longitude à vérifier
     * @throws ZoneGeographiqueException si hors zone
     */
    public void valider(double latitude, double longitude) {
        double distanceKm = GeoUtils.distanceHaversineKm(
            Constants.SANGMELIMA_LAT, Constants.SANGMELIMA_LON,
            latitude, longitude
        );

        if (distanceKm > Constants.RAYON_PROTOTYPE_KM) {
            throw new ZoneGeographiqueException(
                String.format(
                    Constants.ERR_ZONE_INVALIDE,
                    Constants.RAYON_PROTOTYPE_KM
                ) + String.format(" (distance calculée : %.1f km)", distanceKm)
            );
        }
    }

    /**
     * Retourne true si la coordonnée est dans la zone, sans lever d'exception.
     */
    public boolean estDansZone(double latitude, double longitude) {
        double distanceKm = GeoUtils.distanceHaversineKm(
            Constants.SANGMELIMA_LAT, Constants.SANGMELIMA_LON,
            latitude, longitude
        );
        return distanceKm <= Constants.RAYON_PROTOTYPE_KM;
    }
}

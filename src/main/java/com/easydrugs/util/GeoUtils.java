package com.easydrugs.util;

/**
 * Utilitaire de calcul géographique pour EASY-DRUGS.
 * Formule de Haversine : distance entre deux points GPS en km.
 */
public final class GeoUtils {

    private GeoUtils() {}

    private static final double RAYON_TERRE_KM = 6371.0;

    /**
     * Calcule la distance en km entre deux coordonnées GPS.
     * Formule de Haversine.
     *
     * @param lat1 Latitude point 1 (degrés décimaux)
     * @param lon1 Longitude point 1
     * @param lat2 Latitude point 2
     * @param lon2 Longitude point 2
     * @return Distance en kilomètres
     */
    public static double distanceHaversineKm(
            double lat1, double lon1,
            double lat2, double lon2) {

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                 + Math.cos(Math.toRadians(lat1))
                 * Math.cos(Math.toRadians(lat2))
                 * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return RAYON_TERRE_KM * c;
    }

    /**
     * Convertit une distance km en mètres.
     */
    public static double kmToMetres(double km) {
        return km * 1000.0;
    }

    /**
     * Formate une distance pour l'affichage mobile.
     * Ex : 0.8 km → "800 m" | 1.4 km → "1.4 km"
     */
    public static String formaterDistance(double km) {
        if (km < 1.0) {
            return String.format("%.0f m", km * 1000);
        }
        return String.format("%.1f km", km);
    }
}

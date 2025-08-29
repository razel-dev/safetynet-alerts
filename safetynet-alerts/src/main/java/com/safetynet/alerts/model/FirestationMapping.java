package com.safetynet.alerts.model;


import java.util.Objects;

/**
 * Association adresse ↔ numéro de caserne telle que décrite dans
 * le tableau firestations du fichier data.json
 * address— adresse couverte par la caserne
 * station— numéro de caserne

 *
 * <p><strong>Identité logique :</strong> <code>address+station</code>.</p>
  */

public class FirestationMapping {
    /** Adresse couverte par la caserne. */
    private String address;
    /** Numéro de caserne; conservé en chaîne pour coller au JSON (ex: "3"). */
    private String station;


    public FirestationMapping() {}

    /**
     * Constructeur d'initialisation complet.
     *
     * @param address l'adresse couverte
     * @param station le numéro de caserne (chaîne)
     */
    public FirestationMapping(String address, String station) {
        this.address = address;
        this.station = station;
    }

    /** @return l'adresse couverte */
    public String getAddress() { return address; }
    /** @param address l'adresse couverte */
    public void setAddress(String address) { this.address = address; }


    /** @return le numéro de caserne (chaîne) */
    public String getStation() { return station; }
    /** @param station le numéro de caserne (chaîne) */
    public void setStation(String station) { this.station = station; }


    /**
     * Identité basée sur le couple <code>address</code>/<code>station</code>.
     *
     * @param o autre objet à comparer
     * @return {@code true} si égaux, sinon {@code false}
     */
    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FirestationMapping)) return false;
        FirestationMapping that = (FirestationMapping) o;
        return Objects.equals(address, that.address) &&
                Objects.equals(station, that.station);
    }


    /** @return hash basé sur address et station */
    @Override public int hashCode() { return Objects.hash(address, station); }


    /** @return représentation textuelle utile aux logs de debug */
    @Override public String toString() {
        return "FirestationMapping{" +
                "address='" + address + '\'' +
                ", station='" + station + '\'' +
                '}';
    }
}
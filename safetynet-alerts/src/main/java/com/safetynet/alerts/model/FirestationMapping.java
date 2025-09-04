package com.safetynet.alerts.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Représente l’association adresse ↔ numéro de caserne telle que décrite
 * dans le tableau firestations du fichier <code>data.json</code>.
 * Champs :
 *   <code>address</code> — adresse couverte par la caserne
 *   <code>station</code> — numéro de caserne (chaîne)
 * <strong>Identité logique :</strong> (address, station).
 * Notes d’implémentation :
 *  L’égalité et le hashCode sont générés par Lombok sur les champs
 *   <code>address</code> et <code>station</code> via {@link EqualsAndHashCode#of()}.</li>
 *   Un constructeur sans argument est fourni pour la (dé)sérialisation (Jackson) et les proxys.</li>

 */
@Setter
@Getter
@ToString
@EqualsAndHashCode(of = {"address", "station"})
public class FirestationMapping {

    /** Adresse postale couverte par la caserne. */
    private String address;

    /** Numéro de caserne desservant l’adresse (représenté sous forme de chaîne). */
    private String station;

    /**
     * Constructeur sans argument requis par les frameworks (Jackson, Spring, etc.).
     */
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
}

package com.safetynet.alerts.model; // Déclare le package Java. Ça place la classe dans l’espace com.safetynet.alerts.model, cohérent avec l’architecture MVC : model = couche domaine

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Objects; // Importe la classe utilitaire Objects (méthodes equals/hash null-safe) utilisée dans equals et hashCode

/**
 * Représentation d'une personne telle que décrite dans le tableau
 * persons du fichier data.json
 * Les champs sont strictement alignés sur le schéma JSON afin de
 * garantir une correspondance champ par champ pour les endpoints de reporting
 * et les opérations CRUD.
 */
@Getter
@Setter
@ToString

public class Person { // Déclaration de la classe publique Person

    // attributs privés pour respecter l’encapsulation, puis on expose des getters/setters (dé)sérialiser proprement.

    /** Prénom de la personne. */
    private String firstName; // Prénom. private impose l’encapsulation (accès via getters/setters)
    /** Nom de famille de la personne. */
    private String lastName; // Nom de famille. Même logique.
    /** Adresse postale de la personne. */
    private String address;
    /** Ville de résidence. */
    private String city;
    /** Code postal, conservé sous forme de chaîne pour coller au JSON. */
    private String zip; // Code postal stocké en String (et non int) pour respecter le JSON et éviter les soucis de zéros initiaux / formats non numériques
    /** Numéro de téléphone. */
    private String phone; // Numéro de téléphone (String aussi : tirets, espaces, indicatifs possibles)
    /** Adresse e‑mail. */
    private String email;


    public Person() {}// Constructeur par défaut sans argument. Indispensable pour Jackson/Spring qui instancient l’objet par réflexion avant de setter les champs.
    /**
     * Constructeur d'initialisation complet.
     * @param firstName le prénom
     * @param lastName le nom
     * @param address l'adresse postale
     * @param city la ville
     * @param zip le code postal (chaîne)
     * @param phone le numéro de téléphone
     * @param email l'adresse e‑mail
     */
    public Person(String firstName, String lastName, String address, String city,
                  String zip, String phone, String email) { // Constructeur d’initialisation : pratique pour créer un objet complet en une ligne (tests, fixtures, etc.)
        this.firstName = firstName;
        this.lastName = lastName;
        this.address = address;
        this.city = city;
        this.zip = zip;
        this.phone = phone;
        this.email = email;
    }

    // equals / hashCode
    /**
     * Deux personnes sont considérées égales si elles partagent le même
     * couple <code>firstName</code> / <code>lastName</code>.
     *
     * @param o autre objet à comparer
     * @return {@code true} si identités égales, sinon {@code false}
     */
    @Override
    public boolean equals(Object o) {                            // Indique qu’on redéfinit equals depuis java.lang.Object
        if (this == o) return true;                             // Optimisation : mêmes références ⇒ mêmes objets
        if (!(o instanceof Person person)) return false;              // Sécurité de type : si ce n’est pas une Person, ce n’est pas égal

        return Objects.equals(firstName, person.firstName)
            && Objects.equals(lastName, person.lastName);   // Comparaison null-safe des deux champs d’identité
    }


    /** @return le hash basé sur <code>firstName</code> et <code>lastName</code> */
    @Override
    public int hashCode()   // un int est un entier signé sur 32 bits (complément à deux), donc valeurs de −2³¹ à 2³¹−1 (−2 147 483 648 à 2 147 483 647)
                            // Une HashMap/HashSet range chaque objet dans un « casier » calculé à partir des 32 bits de son hashCode,
                            // puis utilise equals pour départager les collisions car possi de meme valeur binaire sur 32 bits.
    { return Objects.hash(firstName, lastName); } // Cohérence contrat equals/hashCode : mêmes champs que equals. Indispensable pour le bon comportement en HashSet / HashMap.


}
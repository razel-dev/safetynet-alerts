package com.safetynet.alerts.model; // Déclare le package Java. Ça place la classe dans l’espace com.safetynet.alerts.model, cohérent avec l’architecture MVC : model = couche domaine

import java.util.Objects; // Importe la classe utilitaire Objects (méthodes equals/hash null-safe) utilisée dans equals et hashCode

/**
 * Représentation d'une personne telle que décrite dans le tableau
 * persons du fichier data.json
 * Les champs sont strictement alignés sur le schéma JSON afin de
 * garantir une correspondance champ par champ pour les endpoints de reporting
 * et les opérations CRUD.
 */
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

    // Getters / Setters
    // Désérialiser = prendre du JSON et remplir un objet Java (Person) avec ces valeurs.
    // Sérialiser = prendre un objet Java et produire du JSON pour la réponse HTTP ou pour écrire un fichier.
    // Dans notre cas, on fait surtout de la désérialisation au démarrage :
    // on lit data.json (dans src/main/resources)
    // et on remplit nos objets Java (Person, FirestationMapping, MedicalRecord).
    // Ensuite, on stocke ces objets dans le repository mémoire pour que les services/contrôleurs puissent travailler dessus

    /** @return le prénom */
    public String getFirstName() { return firstName; } // Getter sans paramètre qui renvoie la valeur du champ privé firstName.
                                                      // Sert surtout à la sérialisation (objet → JSON) et à lire la valeur côté code
    /** @param firstName le prénom */
    public void setFirstName(String firstName) // Setter qui ne retourne rien (void) et reçoit une chaîne en paramètre pour affecter le champ privé.
    { this.firstName = firstName; }           // C’est lui que Jackson appelle pour hydrater l’objet depuis le JSON.


    /** @return le nom */
    public String getLastName() { return lastName; }
    /** @param lastName le nom */
    public void setLastName(String lastName) { this.lastName = lastName; }


    /** @return l'adresse postale */
    public String getAddress() { return address; }
    /** @param address l'adresse postale */
    public void setAddress(String address) { this.address = address; }


    /** @return la ville */
    public String getCity() { return city; }
    /** @param city la ville */
    public void setCity(String city) { this.city = city; }


    /** @return le code postal (chaîne) */
    public String getZip() { return zip; }
    /** @param zip le code postal (chaîne) */
    public void setZip(String zip) { this.zip = zip; }


    /** @return le numéro de téléphone */
    public String getPhone() { return phone; }
    /** @param phone le numéro de téléphone */
    public void setPhone(String phone) { this.phone = phone; }


    /** @return l'adresse e‑mail */
    public String getEmail() { return email; }
    /** @param email l'adresse e‑mail */
    public void setEmail(String email) { this.email = email; }



    // equals / hashCode
    /**
     * Deux personnes sont considérées égales si elles partagent le même
     * couple <code>firstName</code> / <code>lastName</code>.
     *
     * @param o autre objet à comparer
     * @return {@code true} si identités égales, sinon {@code false}
     */
    @Override public boolean equals(Object o) {                  // Indique qu’on redéfinit equals depuis java.lang.Object
        if (this == o) return true;                             // Optimisation : mêmes références ⇒ mêmes objets
        if (!(o instanceof Person)) return false;              // Sécurité de type : si ce n’est pas une Person, ce n’est pas égal
        Person person = (Person) o;                           // Cast (on vient de vérifier le type).
        return Objects.equals(firstName, person.firstName)
            && Objects.equals(lastName, person.lastName);   // Comparaison null-safe des deux champs d’identité
    }


    /** @return le hash basé sur <code>firstName</code> et <code>lastName</code> */
    @Override public int hashCode() { return Objects.hash(firstName, lastName); } // Cohérence contrat equals/hashCode : mêmes champs que equals. Indispensable pour le bon comportement en HashSet / HashMap.


    /** @return représentation textuelle utile aux logs de debug */
    @Override public String toString() {                // Redéfinition de toString utile en logs/debug (affichage lisible des valeurs).
        return "Person{" +                             // Concaténation champ par champ.
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", address='" + address + '\'' +
                ", city='" + city + '\'' +
                ", zip='" + zip + '\'' +
                ", phone='" + phone + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
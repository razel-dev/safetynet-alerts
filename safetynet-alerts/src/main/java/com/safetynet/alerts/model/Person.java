package com.safetynet.alerts.model;

import java.util.Objects;

/**
 * Représentation d'une personne telle que décrite dans le tableau
 * persons du fichier data.json
 * Les champs sont strictement alignés sur le schéma JSON afin de
 * garantir une correspondance 1‑pour‑1 pour les endpoints de reporting
 * et les opérations CRUD.
 */
public class Person {
    /** Prénom de la personne. */
    private String firstName;
    /** Nom de famille de la personne. */
    private String lastName;
    /** Adresse postale de la personne. */
    private String address;
    /** Ville de résidence. */
    private String city;
    /** Code postal, conservé sous forme de chaîne pour coller au JSON. */
    private String zip;
    /** Numéro de téléphone. */
    private String phone;
    /** Adresse e‑mail. */
    private String email;


    public Person() {}
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
                  String zip, String phone, String email) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.address = address;
        this.city = city;
        this.zip = zip;
        this.phone = phone;
        this.email = email;
    }

    /** @return le prénom */
    public String getFirstName() { return firstName; }
    /** @param firstName le prénom */
    public void setFirstName(String firstName) { this.firstName = firstName; }


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


    /**
     * Deux personnes sont considérées égales si elles partagent le même
     * couple <code>firstName</code> / <code>lastName</code>.
     *
     * @param o autre objet à comparer
     * @return {@code true} si identités égales, sinon {@code false}
     */
    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Person)) return false;
        Person person = (Person) o;
        return Objects.equals(firstName, person.firstName) &&
                Objects.equals(lastName, person.lastName);
    }

    /** @return le hash basé sur <code>firstName</code> et <code>lastName</code> */
    @Override public int hashCode() { return Objects.hash(firstName, lastName); }


    /** @return représentation textuelle utile aux logs de debug */
    @Override public String toString() {
        return "Person{" +
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
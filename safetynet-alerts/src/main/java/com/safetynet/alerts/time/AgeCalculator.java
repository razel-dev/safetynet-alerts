package com.safetynet.alerts.time;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;


/**
 * Utilitaire de calcul d'âge à partir d'une date de naissance fournie sous forme de chaîne.
 *
 * <p>Format accepté (strict) {@code M/d/uuuu}
 * <ul>
 *   <li>M et: mois et jour sur 1 ou 2 chiffres (ex. {@code 1/5/2010} ou {@code 01/05/2010})</li>
 *   <li>uuuu: année en calendrier proleptique ISO (ex. {@code 2010})</li>
 * </ul>
 *
 * <p>Règles de parsing:
 * <ul>
 *   <li>Mode strict: les dates impossibles sont refusées (ex. {@code 02/30/2012}).</li>
 *   <li>Gestion des années bissextiles (ex. {@code 02/29/2012} accepté, {@code 02/29/2011} refusé).</li>
 *   <li>Insensible à la casse (sans incidence ici, le format est numérique).</li>
 * </ul>
 *
 * <p>Convention d'erreur: lorsque l'entrée est nulle, vide, invalide, impossible ou future,
 * le calcul d'âge retourne {@code -1}.</p>
 *
 * <p>Thread-safety: le formatter {@link #FMT} est immuable et thread-safe.</p>
 *
 * <p>Remarques:
 * <ul>
 *   <li>Le calcul utilise {@link LocalDate#now()} (fuseau par défaut de la JVM). Pour des tests déterministes,
 *       envisager une surcharge avec {@link java.time.Clock}.</li>
 *   <li>{@code uuuu} est utilisé à la place de {@code yyyy} pour une cohérence avec le calendrier ISO proleptique.</li>
 * </ul>
 *
 * <p>Exemples:
 * <pre>
 * computeAge("1/5/2010")     // OK
 * computeAge("01/05/2010")   // OK
 * computeAge("2/29/2012")    // OK (bissextile)
 * computeAge("2/29/2011")    // -1 (date impossible)
 * computeAge("  ")           // -1
 * computeAge(null)           // -1
 * </pre>
 */
public final class AgeCalculator {

    // Parser STRICT et immuable :
    // - parseCaseInsensitive : sans effet ici (format purement numérique), mais inoffensif.
    // - parseStrict          : refuse les dates invalides (ex. 02/30) et respecte les règles de longueur liées au motif.
    // - appendPattern        : "M/d/uuuu" permet 1 ou 2 chiffres pour mois et jour ; "uuuu" cible l'année ISO proleptique.
    // - toFormatter()        : construit un DateTimeFormatter thread-safe.
    private static final DateTimeFormatter FMT = new DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .parseStrict()
            .appendPattern("M/d/uuuu")
            .toFormatter();

    // Constructeur privé : classe utilitaire non-instanciable.
    private AgeCalculator() {}

    /**
     * Calcule l'âge en années révolues à partir d'une date de naissance au format {@code M/d/uuuu}.
     *
     * <p>Retourne {@code -1} si :
     * <ul>
     *   <li>la chaîne est {@code null} ou vide (après trim),</li>
     *   <li>le motif ne correspond pas au format attendu,</li>
     *   <li>la date est impossible (ex. 02/30/2010),</li>
     *   <li>la date est future par rapport à la date du jour.</li>
     * </ul>
     *
     * <p>Le calcul utilise {@link Period#between(LocalDate, LocalDate)} pour gérer correctement le passage
     * d'anniversaire (l'âge n'est incrémenté que lorsque l'anniversaire de l'année en cours est atteint).
     *
     * @param birthdate chaîne représentant la date de naissance, format {@code M/d/uuuu}.
     * @return l'âge en années, ou {@code -1} en cas d'entrée invalide/impossible/future.
     */
    public static int computeAge(String birthdate) {
        // Cas d'entrée null -> convention d'erreur
        if (birthdate == null) return -1;
        // Normalisation basique de l'entrée
        String s = birthdate.trim();
        if (s.isEmpty()) return -1;

        try {
            // Parsing strict selon le motif et les règles ISO
            LocalDate dob = LocalDate.parse(s, FMT);
            // Date courante (ZoneId par défaut de la JVM)
            LocalDate today = LocalDate.now();
            // Si la date de naissance est dans le futur, l'entrée est rejetée
            if (dob.isAfter(today)) return -1;

            // Calcul d'âge : Period#between gère correctement le passage d'anniversaire
            return Period.between(dob, today).getYears();
        } catch (DateTimeException e) {
            // Toute erreur de parsing ou de construction de date conduit à la convention d'erreur
            return -1;
        }
    }

    /**
     * Indique si une personne est considérée comme enfant selon la règle: âge ≤ 18 ans.
     *
     * <p>Les entrées invalides (où {@link #computeAge(String)} renvoie {@code -1}) retournent {@code false}.
     *
     * @param birthdate chaîne représentant la date de naissance, format {@code M/d/uuuu}.
     * @return {@code true} si l'âge est compris entre 0 et 18 inclus; {@code false} sinon.
     */
    public static boolean isChild(String birthdate) {
        int age = computeAge(birthdate);
        return age >= 0 && age <= 18;
    }
}

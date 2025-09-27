package com.safetynet.alerts.alerts.time;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.ResolverStyle;

/** Calcule l'âge à partir d'une date "M/d/uuuu" (accepte aussi "MM/dd/yyyy") */
public final class AgeCalculator {

    // Parser STRICT: refuse 02/30, gère bissextiles; accepte 1 ou 2 chiffres pour mois/jour
    private static final DateTimeFormatter FMT = new DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .parseStrict()
            .appendPattern("M/d/uuuu")
            .toFormatter();

    private AgeCalculator() {}

    /** Âge en années, ou -1 si null/vide/motif invalide/date impossible/future. */
    public static int computeAge(String birthdate) {
        if (birthdate == null) return -1;
        String s = birthdate.trim();
        if (s.isEmpty()) return -1;

        try {
            LocalDate dob = LocalDate.parse(s, FMT);
            LocalDate today = LocalDate.now();
            if (dob.isAfter(today)) return -1;

            return Period.between(dob, today).getYears();
        } catch (DateTimeException e) {
            return -1;
        }
    }

    /** Enfant = âge ≤ 18. */
    public static boolean isChild(String birthdate) {
        int age = computeAge(birthdate);
        return age >= 0 && age <= 18;
    }
}

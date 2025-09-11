package com.safetynet.alerts.time;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.Period;
import java.util.regex.Pattern;

/** Calcule l'âge à partir d'une date "MM/dd/yyyy"  */
public final class AgeCalculator {
    private static final Pattern RX = Pattern.compile("\\d{2}/\\d{2}/\\d{4}");

    private AgeCalculator() {}

    /** Âge en années, ou -1 si null/vide/motif invalide/date impossible/future. */
    public static int computeAge(String birthdate) {
        if (birthdate == null || birthdate.isEmpty()) return -1;
        String s = birthdate.trim();
        if (!RX.matcher(s).matches()) return -1; // impose strictement MM/dd/yyyy

        try {

            int mm = Integer.parseInt(s.substring(0, 2));
            int dd = Integer.parseInt(s.substring(3, 5));
            int yyyy = Integer.parseInt(s.substring(6, 10));

            LocalDate dob = LocalDate.of(yyyy, mm, dd); // validera 02/30 → exception
            LocalDate today = LocalDate.now();
            if (dob.isAfter(today)) return -1;

            return Period.between(dob, today).getYears();
        } catch (NumberFormatException | DateTimeException e) {
            return -1;
        }
    }

    /** Enfant = âge ≤ 18. */
    public static boolean isChild(String birthdate) {
        int age = computeAge(birthdate);
        return age >= 0 && age <= 18;
    }
}

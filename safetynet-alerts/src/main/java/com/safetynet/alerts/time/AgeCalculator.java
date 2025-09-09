package com.safetynet.alerts.time;

import lombok.experimental.UtilityClass;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@UtilityClass
public class AgeCalculator {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    public static int computeAge(String birthdate) {
        if (birthdate == null || birthdate.isBlank()) return -1;
        try {
            LocalDate dob = LocalDate.parse(birthdate.trim(), FORMATTER);
            LocalDate today = LocalDate.now();
            if (dob.isAfter(today)) return -1;
            return Period.between(dob, today).getYears();
        } catch (DateTimeParseException e) {
            return -1;
        }
    }

    /** Enfant = âge ≤ 18 (utile pour /childAlert). */
    public static boolean isChild(String birthdate) {
        int age = computeAge(birthdate);
        return age >= 0 && age <= 18;
    }
}

package com.fittrack.whoop.service;

import com.fittrack.training.domain.TrainingCategory;
import java.util.Locale;
import java.util.Map;

/**
 * Maps a WHOOP sport onto one of our training categories.
 *
 * <p>WHOOP v2 payloads carry {@code sport_name} where available and {@code sport_id} otherwise, so
 * both are handled. The list is intentionally partial: anything unrecognised falls back to
 * {@code OTHER} while the original label is preserved on the session, which keeps a new WHOOP
 * sport from being lost or mis-filed.
 */
final class WhoopSportCatalog {

    private static final Map<Integer, String> SPORT_NAMES_BY_ID = Map.ofEntries(
            Map.entry(-1, "Activity"),
            Map.entry(0, "Running"),
            Map.entry(1, "Cycling"),
            Map.entry(16, "Baseball"),
            Map.entry(17, "Basketball"),
            Map.entry(18, "Rowing"),
            Map.entry(22, "Golf"),
            Map.entry(24, "Ice Hockey"),
            Map.entry(27, "Rugby"),
            Map.entry(29, "Soccer"),
            Map.entry(30, "Softball"),
            Map.entry(33, "Swimming"),
            Map.entry(34, "Tennis"),
            Map.entry(36, "Volleyball"),
            Map.entry(39, "Boxing"),
            Map.entry(42, "Dance"),
            Map.entry(43, "Pilates"),
            Map.entry(44, "Yoga"),
            Map.entry(45, "Weightlifting"),
            Map.entry(48, "Functional Fitness"),
            Map.entry(52, "Hiking/Rucking"),
            Map.entry(63, "Walking"),
            Map.entry(65, "Elliptical"),
            Map.entry(66, "Stairmaster"),
            Map.entry(71, "Crossfit"),
            Map.entry(73, "Meditation"),
            Map.entry(82, "HIIT"),
            Map.entry(83, "Spin"),
            Map.entry(84, "Jiu Jitsu"),
            Map.entry(85, "Manual Labor"),
            Map.entry(86, "Cricket"),
            Map.entry(96, "Assault Bike"),
            Map.entry(97, "Kickboxing"),
            Map.entry(101, "Barre"),
            Map.entry(102, "Stretching"),
            Map.entry(123, "Strength Trainer"));

    private WhoopSportCatalog() {}

    static String sportName(String sportName, Integer sportId) {
        if (sportName != null && !sportName.isBlank()) {
            return sportName;
        }
        if (sportId != null) {
            return SPORT_NAMES_BY_ID.getOrDefault(sportId, "Workout");
        }
        return "Workout";
    }

    static TrainingCategory categoryFor(String sportName) {
        if (sportName == null) {
            return TrainingCategory.OTHER;
        }
        String normalised = sportName.toLowerCase(Locale.ROOT);
        if (contains(normalised, "weight", "strength", "crossfit", "functional")) {
            return TrainingCategory.STRENGTH;
        }
        if (contains(normalised, "run", "jog", "treadmill")) {
            return TrainingCategory.RUNNING;
        }
        if (contains(normalised, "cycl", "spin", "bike")) {
            return TrainingCategory.CYCLING;
        }
        if (contains(normalised, "swim")) {
            return TrainingCategory.SWIMMING;
        }
        if (contains(normalised, "row")) {
            return TrainingCategory.ROWING;
        }
        if (contains(normalised, "hiit", "interval")) {
            return TrainingCategory.HIIT;
        }
        if (contains(normalised, "yoga", "pilates", "stretch", "barre", "mobility", "meditation")) {
            return TrainingCategory.MOBILITY;
        }
        if (contains(normalised, "walk", "hik", "ruck")) {
            return TrainingCategory.WALKING;
        }
        if (contains(normalised, "box", "jiu", "kickbox", "martial")) {
            return TrainingCategory.CLASS;
        }
        if (contains(normalised,
                "soccer", "football", "basketball", "tennis", "hockey", "rugby", "golf",
                "volleyball", "cricket", "baseball", "softball")) {
            return TrainingCategory.SPORT;
        }
        return TrainingCategory.OTHER;
    }

    private static boolean contains(String value, String... needles) {
        for (String needle : needles) {
            if (value.contains(needle)) {
                return true;
            }
        }
        return false;
    }
}

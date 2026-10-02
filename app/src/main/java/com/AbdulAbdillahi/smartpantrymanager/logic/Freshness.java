package com.AbdulAbdillahi.smartpantrymanager.logic;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;

/**
 * How urgently a pantry item needs to be used, based on its expiry date.
 * Pure Java (no Android code), so it can be unit tested.
 */
public enum Freshness {
    FRESH, USE_SOON, USE_TODAY, EXPIRED;

    /** Items expiring within this many days count as "use soon". Will come from Settings later. */
    public static final int DEFAULT_SOON_DAYS = 3;

    /**
     * @param expiryDate "yyyy-MM-dd", or null if the item doesn't expire
     * @param today      passed in (not read inside) so tests can use any date
     */
    public static Freshness of(String expiryDate, LocalDate today, int soonDays) {
        if (expiryDate == null || expiryDate.isEmpty()) {
            return FRESH;
        }
        LocalDate expiry;
        try {
            expiry = LocalDate.parse(expiryDate);
        } catch (DateTimeParseException e) {
            return FRESH; // bad data shouldn't crash the whole list
        }
        long daysLeft = ChronoUnit.DAYS.between(today, expiry);
        if (daysLeft < 0) return EXPIRED;
        if (daysLeft == 0) return USE_TODAY;
        if (daysLeft <= soonDays) return USE_SOON;
        return FRESH;
    }

    /** True for anything the user should act on. */
    public boolean needsAttention() {
        return this != FRESH;
    }
}
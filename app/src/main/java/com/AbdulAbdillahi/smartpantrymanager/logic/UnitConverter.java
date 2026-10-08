package com.AbdulAbdillahi.smartpantrymanager.logic;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Converts quantities to one base unit per kind of measurement, so that
 * "1 kg" vs "1000 g", or "1 L" vs "4 cups", can be compared:
 *   MASS   -> grams
 *   VOLUME -> millilitres
 *   COUNT  -> pieces
 * Different kinds can't be compared (grams vs pieces). That's deliberate:
 * we can't know how much one onion weighs, so we don't guess.
 */
public final class UnitConverter {

    public enum Dimension { MASS, VOLUME, COUNT }

    /** A quantity expressed in its dimension's base unit. */
    public static final class Measure {
        public final Dimension dimension;
        public final double amount;

        Measure(Dimension dimension, double amount) {
            this.dimension = dimension;
            this.amount = amount;
        }
    }

    /** One known unit: its kind, and how many base units it equals. */
    private static final class Unit {
        final Dimension dimension;
        final double toBase;

        Unit(Dimension dimension, double toBase) {
            this.dimension = dimension;
            this.toBase = toBase;
        }
    }

    private static final Map<String, Unit> UNITS = new HashMap<>();

    static {
        register(Dimension.MASS, 1, "g", "gram", "grams");
        register(Dimension.MASS, 1000, "kg", "kilogram", "kilograms");
        register(Dimension.VOLUME, 1, "ml", "millilitre", "millilitres", "milliliter", "milliliters");
        register(Dimension.VOLUME, 1000, "l", "litre", "litres", "liter", "liters");
        register(Dimension.VOLUME, 5, "tsp", "teaspoon", "teaspoons");
        register(Dimension.VOLUME, 15, "tbsp", "tablespoon", "tablespoons");
        register(Dimension.VOLUME, 250, "cup", "cups");      // South African metric cup
        register(Dimension.COUNT, 1, "pcs", "pc", "piece", "pieces");
    }

    private UnitConverter() {
        // Utility class: never instantiated
    }

    private static void register(Dimension dimension, double toBase, String... names) {
        Unit unit = new Unit(dimension, toBase);
        for (String name : names) {
            UNITS.put(name, unit);
        }
    }

    /** Converts e.g. (2, "kg") to MASS 2000. Returns null for a unit we don't recognise. */
    public static Measure toBase(double amount, String unit) {
        if (unit == null) {
            return null;
        }
        Unit known = UNITS.get(unit.trim().toLowerCase(Locale.ROOT));
        return known == null ? null : new Measure(known.dimension, amount * known.toBase);
    }

    /**
     * True if what you have is at least what you need, after converting both.
     * False if the units can't be compared (e.g. grams vs pieces) or aren't recognised.
     */
    public static boolean isEnough(double haveAmount, String haveUnit,
                                   double needAmount, String needUnit) {
        Measure have = toBase(haveAmount, haveUnit);
        Measure need = toBase(needAmount, needUnit);
        if (have == null || need == null || have.dimension != need.dimension) {
            return false;
        }
        // Tiny tolerance so decimal rounding (0.1 + 0.2 = 0.30000000000000004) can't fail a match
        return have.amount + 1e-9 >= need.amount;
    }
}
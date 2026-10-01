package com.AbdulAbdillahi.smartpantrymanager.model;

/**
 * One ingredient the user has at home.
 * Quantities are doubles so values like 0.5 kg are allowed.
 */
public class PantryItem {

    private long id;
    private String name;
    private double quantity;
    private double initialQuantity; // amount when first added: drives the jar fill level
    private String unit;
    private String expiryDate;      // "yyyy-MM-dd", or null if it doesn't expire

    // For a brand-new item: the database assigns the id, and "full" = what was added
    public PantryItem(String name, double quantity, String unit, String expiryDate) {
        this(-1, name, quantity, quantity, unit, expiryDate);
    }

    // For an item loaded from the database
    public PantryItem(long id, String name, double quantity, double initialQuantity,
                      String unit, String expiryDate) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
        this.initialQuantity = initialQuantity;
        this.unit = unit;
        this.expiryDate = expiryDate;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getQuantity() { return quantity; }
    public void setQuantity(double quantity) { this.quantity = quantity; }

    public double getInitialQuantity() { return initialQuantity; }
    public void setInitialQuantity(double initialQuantity) { this.initialQuantity = initialQuantity; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public String getExpiryDate() { return expiryDate; }
    public void setExpiryDate(String expiryDate) { this.expiryDate = expiryDate; }

    public boolean hasExpiryDate() {
        return expiryDate != null && !expiryDate.isEmpty();
    }
}
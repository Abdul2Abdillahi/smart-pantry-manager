package com.AbdulAbdillahi.smartpantrymanager.model;

import java.util.ArrayList;
import java.util.List;

/** A recipe: its details plus the list of ingredients it requires. */
public class Recipe {

    private long id;
    private final String name;
    private final String steps;
    private final int prepMinutes;
    private final int servings;
    private final List<RecipeIngredient> ingredients = new ArrayList<>();

    public Recipe(long id, String name, String steps, int prepMinutes, int servings) {
        this.id = id;
        this.name = name;
        this.steps = steps;
        this.prepMinutes = prepMinutes;
        this.servings = servings;
    }

    public void addIngredient(RecipeIngredient ingredient) {
        ingredients.add(ingredient);
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getName() { return name; }
    public String getSteps() { return steps; }
    public int getPrepMinutes() { return prepMinutes; }
    public int getServings() { return servings; }
    public List<RecipeIngredient> getIngredients() { return ingredients; }
}
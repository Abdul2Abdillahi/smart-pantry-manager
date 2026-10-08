package com.AbdulAbdillahi.smartpantrymanager.data;

import static com.AbdulAbdillahi.smartpantrymanager.data.DatabaseHelper.*;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

/**
 * Pre-loads the starter recipes into the database.
 * Each ingredient is written as "name|quantity|unit".
 * Steps are separated by "\n" so the detail screen can number them.
 * Water is never listed: like most recipe apps, we assume every kitchen has it.
 */
final class RecipeSeeder {

    private RecipeSeeder() {
        // Utility class: never instantiated
    }

    static void seed(SQLiteDatabase db) {
        addRecipe(db, "Garlic Butter Pasta", 25, 2,
                "Boil the pasta in salted water until tender, then drain.\n"
                        + "Melt the butter in a pan and gently fry the chopped onion and garlic for 3 minutes.\n"
                        + "Toss the pasta through the garlic butter.\n"
                        + "Grate the cheddar over the top and serve.",
                "pasta|200|g", "butter|50|g", "garlic|3|pcs", "cheddar|50|g", "onion|1|pcs");

        addRecipe(db, "Tomato & Onion Omelette", 15, 1,
                "Whisk the eggs with the milk.\n"
                        + "Fry the chopped onion and tomato in a non-stick pan for 3 minutes.\n"
                        + "Pour in the eggs and cook gently until almost set.\n"
                        + "Sprinkle over the grated cheddar, fold in half and serve.",
                "eggs|3|pcs", "tomato|1|pcs", "onion|1|pcs", "milk|50|ml", "cheddar|30|g");

        addRecipe(db, "Cheesy Tomato Rice", 30, 3,
                "Melt the butter in a pot and soften the chopped onion.\n"
                        + "Add the rice and chopped tomatoes, cover with water and simmer for 18 minutes.\n"
                        + "Stir in the grated cheddar until melted.\n"
                        + "Rest for 2 minutes before serving.",
                "rice|250|g", "tomatoes|2|pcs", "cheddar|80|g", "onion|1|pcs", "butter|20|g");

        addRecipe(db, "Egg Fried Rice", 20, 2,
                "Cook the rice and let it cool (day-old rice works best).\n"
                        + "Heat the oil and fry the chopped onion and garlic for 2 minutes.\n"
                        + "Push to one side, scramble the eggs, then mix everything together.\n"
                        + "Add the rice and soy sauce and stir-fry for 4 minutes.",
                "rice|200|g", "eggs|2|pcs", "onion|1|pcs", "garlic|2|pcs",
                "oil|2|tbsp", "soy sauce|2|tbsp");

        addRecipe(db, "Scrambled Eggs on Toast", 10, 1,
                "Toast the bread.\n"
                        + "Whisk the eggs with the milk.\n"
                        + "Melt the butter in a pan over low heat, add the eggs and stir slowly until just set.\n"
                        + "Spoon onto the toast and serve.",
                "eggs|3|pcs", "milk|30|ml", "butter|10|g", "bread|2|pcs");

        addRecipe(db, "French Toast", 15, 2,
                "Whisk the eggs, milk and sugar in a shallow dish.\n"
                        + "Soak each slice of bread for a few seconds on both sides.\n"
                        + "Fry in the butter for 2 minutes per side until golden.",
                "bread|4|pcs", "eggs|2|pcs", "milk|100|ml", "sugar|15|g", "butter|20|g");

        addRecipe(db, "Pancakes", 25, 4,
                "Whisk the flour, sugar, eggs and milk into a smooth batter.\n"
                        + "Rest the batter for 10 minutes.\n"
                        + "Melt a little butter in a pan and cook ladlefuls of batter for 1 minute per side.\n"
                        + "Repeat until all the batter is used.",
                "flour|200|g", "milk|300|ml", "eggs|2|pcs", "sugar|25|g", "butter|30|g");

        addRecipe(db, "Classic Tomato Soup", 35, 4,
                "Melt the butter and soften the chopped onion and garlic for 5 minutes.\n"
                        + "Add the chopped tomatoes and stock, then simmer for 20 minutes.\n"
                        + "Blend until smooth.\n"
                        + "Season to taste and serve hot.",
                "tomatoes|6|pcs", "onion|1|pcs", "garlic|2|pcs", "butter|30|g",
                "vegetable stock|500|ml");

        addRecipe(db, "Macaroni Cheese", 30, 4,
                "Boil the macaroni until tender, then drain.\n"
                        + "Melt the butter in a pot, stir in the flour and cook for 1 minute.\n"
                        + "Slowly whisk in the milk until the sauce thickens.\n"
                        + "Stir in most of the cheddar and the macaroni, top with the rest and grill until golden.",
                "macaroni|250|g", "cheddar|150|g", "milk|400|ml", "butter|30|g", "flour|20|g");

        addRecipe(db, "Cheese Toastie", 10, 1,
                "Butter the outside of both slices of bread.\n"
                        + "Place the cheddar between the slices, buttered sides out.\n"
                        + "Fry in a pan for 3 minutes per side until golden and melted.",
                "bread|2|pcs", "cheddar|50|g", "butter|10|g");

        addRecipe(db, "Potato & Onion Fry-up", 30, 2,
                "Peel and dice the potatoes, then boil for 8 minutes and drain.\n"
                        + "Heat the oil and fry the sliced onion until soft.\n"
                        + "Add the potatoes and fry until crisp and golden.\n"
                        + "Season with salt and serve.",
                "potatoes|4|pcs", "onion|1|pcs", "oil|2|tbsp", "salt|5|g");

        addRecipe(db, "Creamy Mashed Potatoes", 30, 4,
                "Peel and quarter the potatoes, then boil for 20 minutes until soft.\n"
                        + "Drain and mash well.\n"
                        + "Beat in the butter and warm milk until smooth.\n"
                        + "Season with salt.",
                "potatoes|6|pcs", "butter|40|g", "milk|100|ml", "salt|5|g");

        addRecipe(db, "Chakalaka", 30, 4,
                "Heat the oil and fry the chopped onion until soft.\n"
                        + "Add the curry powder and cook for 1 minute.\n"
                        + "Add the grated carrots, chopped pepper and tomatoes and cook for 10 minutes.\n"
                        + "Stir in the baked beans and simmer for 5 minutes. Serve warm or cold.",
                "onion|1|pcs", "carrots|2|pcs", "green pepper|1|pcs", "tomatoes|2|pcs",
                "baked beans|410|g", "curry powder|10|g", "oil|2|tbsp");

        addRecipe(db, "Mielie Pap", 25, 4,
                "Bring a pot of salted water to the boil.\n"
                        + "Slowly stir in the maize meal to avoid lumps.\n"
                        + "Cover and cook on low heat for 20 minutes, stirring now and then.\n"
                        + "Stir in the butter and serve.",
                "maize meal|250|g", "salt|5|g", "butter|20|g");

        addRecipe(db, "Chicken Stir-Fry", 25, 2,
                "Cook the rice.\n"
                        + "Slice the chicken into strips and fry in hot oil until cooked through.\n"
                        + "Add the sliced onion, pepper and garlic and stir-fry for 4 minutes.\n"
                        + "Add the soy sauce and serve over the rice.",
                "chicken breast|2|pcs", "green pepper|1|pcs", "onion|1|pcs", "garlic|2|pcs",
                "soy sauce|3|tbsp", "rice|200|g", "oil|1|tbsp");

        addRecipe(db, "Spaghetti Bolognese", 40, 4,
                "Fry the chopped onion and garlic in the oil for 3 minutes.\n"
                        + "Add the mince and brown it all over.\n"
                        + "Add the chopped tomatoes and simmer for 20 minutes.\n"
                        + "Meanwhile, boil the spaghetti, then serve the sauce on top.",
                "spaghetti|400|g", "beef mince|500|g", "onion|1|pcs", "garlic|2|pcs",
                "tomatoes|4|pcs", "oil|1|tbsp");

        addRecipe(db, "Tuna Pasta Salad", 15, 2,
                "Boil the pasta, then drain and cool under cold water.\n"
                        + "Flake the tuna and dice the cucumber.\n"
                        + "Mix everything with the mayonnaise and chill before serving.",
                "pasta|200|g", "tuna|170|g", "mayonnaise|45|g", "cucumber|1|pcs");

        addRecipe(db, "Banana Bread", 60, 8,
                "Heat the oven to 180 °C and grease a loaf tin.\n"
                        + "Mash the bananas, then beat in the melted butter, sugar and eggs.\n"
                        + "Fold in the flour and baking powder.\n"
                        + "Pour into the tin and bake for 50 minutes.",
                "bananas|3|pcs", "flour|250|g", "sugar|100|g", "butter|100|g",
                "eggs|2|pcs", "baking powder|10|g");

        addRecipe(db, "Vegetable Curry", 40, 4,
                "Fry the chopped onion in the oil until soft, then stir in the curry powder for 1 minute.\n"
                        + "Add the diced potatoes, carrots and chopped tomatoes.\n"
                        + "Add enough water to cover and simmer for 25 minutes until the potatoes are tender.\n"
                        + "Serve with rice or pap.",
                "potatoes|3|pcs", "carrots|2|pcs", "onion|1|pcs", "tomatoes|2|pcs",
                "curry powder|20|g", "oil|2|tbsp");

        addRecipe(db, "Banana Oat Porridge", 10, 1,
                "Put the oats and milk in a pot and bring to a gentle simmer.\n"
                        + "Cook for 5 minutes, stirring often, until thick and creamy.\n"
                        + "Stir in the sugar and top with the sliced banana.",
                "oats|80|g", "milk|250|ml", "sugar|10|g", "banana|1|pcs");
    }

    /** Inserts one recipe, then each of its ingredients linked by the new recipe's id. */
    private static void addRecipe(SQLiteDatabase db, String name, int minutes, int servings,
                                  String steps, String... ingredients) {
        ContentValues recipe = new ContentValues();
        recipe.put(COL_RECIPE_NAME, name);
        recipe.put(COL_RECIPE_STEPS, steps);
        recipe.put(COL_RECIPE_MINUTES, minutes);
        recipe.put(COL_RECIPE_SERVINGS, servings);
        long recipeId = db.insert(TABLE_RECIPES, null, recipe);

        for (String entry : ingredients) {
            String[] parts = entry.split("\\|"); // "|" must be escaped in a regex
            ContentValues ingredient = new ContentValues();
            ingredient.put(COL_RI_RECIPE_ID, recipeId);
            ingredient.put(COL_RI_NAME, parts[0]);
            ingredient.put(COL_RI_NORMALIZED, normalize(parts[0]));
            ingredient.put(COL_RI_QUANTITY, Double.parseDouble(parts[1]));
            ingredient.put(COL_RI_UNIT, parts[2]);
            db.insert(TABLE_RECIPE_INGREDIENTS, null, ingredient);
        }
    }
}
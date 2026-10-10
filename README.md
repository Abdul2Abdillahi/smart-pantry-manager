# Smart Pantry Manager

An Android app, written in Java, that helps reduce food waste. It tracks the
ingredients you have at home and suggests recipes you can cook using **only**
what is already in your pantry. No shopping trip needed.

## The strict-matching rule

A recipe is suggested only if **every** ingredient it needs is in the pantry
in **at least** the required quantity. One missing or short ingredient means
the recipe is not suggested. Matching handles real-world messiness:

- **Names:** "Tomatoes", "tomato" and "Fresh tomatoes" all match (plurals,
  capitals, spacing, describing words, and aliases such as "mealie meal").
- **Units:** 1 L covers 250 ml, and 0.5 kg covers 200 g. Different kinds of
  unit (grams vs pieces) are never guessed.

The rule is covered by 29 JUnit tests (see "Running the tests").

## Features

- Pantry shown as jars on shelves: lid colour shows freshness, fill level shows
  how much is left
- Add, edit and delete ingredients, with validation and a date picker
- 20 pre-loaded recipes
- **Cook tonight:** strict suggestions, with recipes that use up soon-to-expire
  food listed first
- "Recipe unlocked" banner when a pantry change completes a recipe
- Recipe detail with an ingredient checklist and numbered method
- Settings: expiry warnings, warning window (1-7 days), default unit
- Bonus: **Almost there** list of recipes missing exactly one ingredient,
  on a separate screen

## Database choice: SQLite (SQLiteOpenHelper)

Pantry data is personal and belongs on the user's device, so a local database
fits better than a cloud one. SQLite works fully offline, needs no account or
server, and keeps data after the app is closed.

- Three tables: `pantry_items`, `recipes`, `recipe_ingredients` (one recipe has
  many ingredients, linked by a foreign key with cascading delete)
- Database versions are upgraded step by step in `onUpgrade`, so updates never
  wipe the user's pantry
- Settings use SharedPreferences, which suits a few simple values

## Tech stack

- Java, Android Studio
- SQLite via SQLiteOpenHelper
- Material Design 3 components
- JUnit 4 for unit tests
- Minimum SDK: API 26 (Android 8.0)

## Setup and running

1. Clone this repository:
   `git clone https://github.com/Abdul2Abdillahi/smart-pantry-manager.git`
2. Open the project folder in Android Studio.
3. Wait for Gradle to finish syncing.
4. Run the app on an emulator or device with API 26 or higher.

## Running the tests

In Android Studio, right-click the `com.AbdulAbdillahi.smartpantrymanager (test)`
folder and choose **Run 'Tests in...'**. All 29 tests should pass.

## Project structure

| Package | Contents |
|---|---|
| `data` | DatabaseHelper, RecipeSeeder, AppSettings |
| `model` | PantryItem, Recipe, RecipeIngredient |
| `logic` | RecipeMatcher, IngredientNormalizer, UnitConverter, Freshness |
| `UI` | Screens, adapters and shared UI helpers |

## Fonts

Fraunces, Nunito Sans and Caveat, from Google Fonts (SIL Open Font License).

## Author

Abdul Abdillahi, 402111750
Mobile App Development 700, Richfield Graduate Institute of Technology
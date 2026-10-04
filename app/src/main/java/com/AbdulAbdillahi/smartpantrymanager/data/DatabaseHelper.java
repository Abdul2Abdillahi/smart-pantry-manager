package com.AbdulAbdillahi.smartpantrymanager.data;

import android.content.Context;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.ContentValues;
import android.database.Cursor;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import com.AbdulAbdillahi.smartpantrymanager.model.PantryItem;

/**
 * Creates and manages the app's local SQLite database.
 * All table and column names are defined once here as constants,
 * so a typo can't silently break a query somewhere else.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "smart_pantry.db";
    private static final int DATABASE_VERSION = 1;

    // ----- pantry_items table -----
    public static final String TABLE_PANTRY = "pantry_items";
    public static final String COL_PANTRY_ID = "id";
    public static final String COL_PANTRY_NAME = "name";
    public static final String COL_PANTRY_NORMALIZED = "normalized_name";
    public static final String COL_PANTRY_QUANTITY = "quantity";
    public static final String COL_PANTRY_INITIAL_QTY = "initial_quantity";
    public static final String COL_PANTRY_UNIT = "unit";
    public static final String COL_PANTRY_EXPIRY = "expiry_date";

    // ----- recipes table -----
    public static final String TABLE_RECIPES = "recipes";
    public static final String COL_RECIPE_ID = "id";
    public static final String COL_RECIPE_NAME = "name";
    public static final String COL_RECIPE_STEPS = "steps";
    public static final String COL_RECIPE_MINUTES = "prep_minutes";
    public static final String COL_RECIPE_SERVINGS = "servings";

    // ----- recipe_ingredients table -----
    public static final String TABLE_RECIPE_INGREDIENTS = "recipe_ingredients";
    public static final String COL_RI_ID = "id";
    public static final String COL_RI_RECIPE_ID = "recipe_id";
    public static final String COL_RI_NAME = "name";
    public static final String COL_RI_NORMALIZED = "normalized_name";
    public static final String COL_RI_QUANTITY = "quantity";
    public static final String COL_RI_UNIT = "unit";

    // Only one DatabaseHelper exists for the whole app (the Singleton pattern),
    // so every screen shares the same database connection.
    private static DatabaseHelper instance;

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            // Application context avoids leaking an Activity
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        // SQLite ignores foreign keys unless we switch them on
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // UNIQUE on normalized_name stops the same ingredient being added twice
        // (e.g. "Tomato" and "tomatoes"); the user edits the existing item instead.
        db.execSQL("CREATE TABLE " + TABLE_PANTRY + " ("
                + COL_PANTRY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_PANTRY_NAME + " TEXT NOT NULL, "
                + COL_PANTRY_NORMALIZED + " TEXT NOT NULL UNIQUE, "
                + COL_PANTRY_QUANTITY + " REAL NOT NULL CHECK (" + COL_PANTRY_QUANTITY + " >= 0), "
                + COL_PANTRY_INITIAL_QTY + " REAL NOT NULL, "
                + COL_PANTRY_UNIT + " TEXT NOT NULL, "
                + COL_PANTRY_EXPIRY + " TEXT)");   // "yyyy-MM-dd", or NULL if no expiry

        db.execSQL("CREATE TABLE " + TABLE_RECIPES + " ("
                + COL_RECIPE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_RECIPE_NAME + " TEXT NOT NULL UNIQUE, "
                + COL_RECIPE_STEPS + " TEXT NOT NULL, "
                + COL_RECIPE_MINUTES + " INTEGER, "
                + COL_RECIPE_SERVINGS + " INTEGER)");

        // ON DELETE CASCADE: deleting a recipe automatically deletes its ingredients
        db.execSQL("CREATE TABLE " + TABLE_RECIPE_INGREDIENTS + " ("
                + COL_RI_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_RI_RECIPE_ID + " INTEGER NOT NULL REFERENCES "
                + TABLE_RECIPES + "(" + COL_RECIPE_ID + ") ON DELETE CASCADE, "
                + COL_RI_NAME + " TEXT NOT NULL, "
                + COL_RI_NORMALIZED + " TEXT NOT NULL, "
                + COL_RI_QUANTITY + " REAL NOT NULL CHECK (" + COL_RI_QUANTITY + " > 0), "
                + COL_RI_UNIT + " TEXT NOT NULL)");

        // Index speeds up "get all ingredients for recipe X", which the matcher runs often
        db.execSQL("CREATE INDEX idx_ri_recipe ON " + TABLE_RECIPE_INGREDIENTS
                + "(" + COL_RI_RECIPE_ID + ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Simple strategy for this project: rebuild the tables on a schema change.
        // (A production app would migrate the data instead of dropping it.)
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECIPE_INGREDIENTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECIPES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PANTRY);
        onCreate(db);
    }
    // ===================== PANTRY CRUD =====================

    /** CREATE: returns the new row's id, or -1 if it failed (e.g. a duplicate ingredient). */
    public long insertPantryItem(PantryItem item) {
        SQLiteDatabase db = getWritableDatabase();
        long id = db.insert(TABLE_PANTRY, null, toContentValues(item));
        if (id != -1) {
            item.setId(id);
        }
        return id;
    }

    /** READ (all): soonest-expiring first, items with no expiry date last. */
    public List<PantryItem> getAllPantryItems() {
        List<PantryItem> items = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        String orderBy = "CASE WHEN " + COL_PANTRY_EXPIRY + " IS NULL THEN 1 ELSE 0 END, "
                + COL_PANTRY_EXPIRY + " ASC, "
                + COL_PANTRY_NAME + " COLLATE NOCASE ASC";

        // try-with-resources closes the cursor automatically, preventing memory leaks
        try (Cursor cursor = db.query(TABLE_PANTRY, null, null, null, null, null, orderBy)) {
            while (cursor.moveToNext()) {
                items.add(pantryItemFromCursor(cursor));
            }
        }
        return items;
    }

    /** READ (one): returns null if no item has this id. */
    public PantryItem getPantryItem(long id) {
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor cursor = db.query(TABLE_PANTRY, null,
                COL_PANTRY_ID + " = ?", new String[]{String.valueOf(id)},
                null, null, null)) {
            return cursor.moveToFirst() ? pantryItemFromCursor(cursor) : null;
        }
    }

    /** UPDATE: returns true if a row was changed. */
    /** UPDATE: returns true if a row was changed, false if it failed (e.g. a duplicate name). */
    public boolean updatePantryItem(PantryItem item) {
        // If the user tops up above the original amount, that becomes the new "full jar"
        if (item.getQuantity() > item.getInitialQuantity()) {
            item.setInitialQuantity(item.getQuantity());
        }
        SQLiteDatabase db = getWritableDatabase();
        try {
            int rows = db.update(TABLE_PANTRY, toContentValues(item),
                    COL_PANTRY_ID + " = ?", new String[]{String.valueOf(item.getId())});
            return rows > 0;
        } catch (SQLiteConstraintException e) {
            // The UNIQUE rule rejected it: another item already has this name
            return false;
        }
    }

    /** DELETE: returns true if a row was removed. */
    public boolean deletePantryItem(long id) {
        SQLiteDatabase db = getWritableDatabase();
        int rows = db.delete(TABLE_PANTRY, COL_PANTRY_ID + " = ?",
                new String[]{String.valueOf(id)});
        return rows > 0;
    }

    // ----- helpers -----

    /** Converts a PantryItem into column/value pairs for inserting or updating. */
    private ContentValues toContentValues(PantryItem item) {
        ContentValues values = new ContentValues();
        values.put(COL_PANTRY_NAME, item.getName().trim());
        values.put(COL_PANTRY_NORMALIZED, normalize(item.getName()));
        values.put(COL_PANTRY_QUANTITY, item.getQuantity());
        values.put(COL_PANTRY_INITIAL_QTY, item.getInitialQuantity());
        values.put(COL_PANTRY_UNIT, item.getUnit());
        if (item.hasExpiryDate()) {
            values.put(COL_PANTRY_EXPIRY, item.getExpiryDate());
        } else {
            values.putNull(COL_PANTRY_EXPIRY);
        }
        return values;
    }

    /** Builds a PantryItem from the row the cursor is currently pointing at. */
    private PantryItem pantryItemFromCursor(Cursor c) {
        int expiryIndex = c.getColumnIndexOrThrow(COL_PANTRY_EXPIRY);
        return new PantryItem(
                c.getLong(c.getColumnIndexOrThrow(COL_PANTRY_ID)),
                c.getString(c.getColumnIndexOrThrow(COL_PANTRY_NAME)),
                c.getDouble(c.getColumnIndexOrThrow(COL_PANTRY_QUANTITY)),
                c.getDouble(c.getColumnIndexOrThrow(COL_PANTRY_INITIAL_QTY)),
                c.getString(c.getColumnIndexOrThrow(COL_PANTRY_UNIT)),
                c.isNull(expiryIndex) ? null : c.getString(expiryIndex));
    }

    /** Temporary: lowercase + trim. Replaced by the full normaliser on 9 October. */
    private String normalize(String name) {
        return name.trim().toLowerCase(Locale.ROOT);
    }
}
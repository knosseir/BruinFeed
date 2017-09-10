package com.example.admin.bruinfeed;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHandler extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static String DATABASE_NAME = "mealItemManager";

    // MealItems table name
    private static final String TABLE_MEAL_ITEMS= "mealItems";

    // MealItems Table Columns names
    private static final String KEY_NAME = "name";
    private static final String KEY_DESCRIPTION = "description";
    private static final String KEY_URL = "url";
    private static final String KEY_HALL = "hall";
    private static final String KEY_MEAL = "meal";
    private static final String KEY_SECTION = "section";
    private static final String KEY_DESCRIPTORS = "descriptors";
    private static final String KEY_DATE = "date";

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_MEAL_ITEMS_TABLE = "CREATE TABLE " + TABLE_MEAL_ITEMS + "("
                + KEY_NAME + " TEXT," + KEY_DESCRIPTION + " TEXT,"
                + KEY_URL + " TEXT," + KEY_HALL + " TEXT," + KEY_MEAL
                + " TEXT," + KEY_SECTION + " TEXT," + KEY_DESCRIPTORS
                + " TEXT," + KEY_DATE + " TEXT" + ")";
        db.execSQL(CREATE_MEAL_ITEMS_TABLE);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MEAL_ITEMS);

        // Create tables again
        onCreate(db);
    }

    // Add new MealItem
    void addMealItem(MealItem mealItem) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_NAME, mealItem.getName());
        values.put(KEY_DESCRIPTION, mealItem.getDescription());
        values.put(KEY_URL, mealItem.getUrl());
        values.put(KEY_HALL, mealItem.getHall());
        values.put(KEY_MEAL, mealItem.getMeal());
        values.put(KEY_SECTION, mealItem.getSection());
        values.put(KEY_DESCRIPTORS, mealItem.getDescriptors());
        values.put(KEY_DATE, mealItem.getDate());

        // Insert Row
        db.insert(TABLE_MEAL_ITEMS, null, values);
        db.close(); // Closing database connection
    }

    // Get single MealItem
    MealItem getMealItem(String name) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_MEAL_ITEMS, null, KEY_NAME + " = ?",
                new String[] { name }, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();

        return new MealItem(cursor.getString(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4), cursor.getString(5), cursor.getString(6), cursor.getString(7));
    }

    // Getting All MealItems
    List<MealItem> getAllMealItems() {
        List<MealItem> mealItemList = new ArrayList<>();
        // Select All Query
        String selectQuery = "SELECT * FROM " + TABLE_MEAL_ITEMS;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // loop through all rows and add to list
        if (cursor.moveToFirst()) {
            do {
                MealItem mealItem = new MealItem();
                mealItem.setName(cursor.getString(0));
                mealItem.setDescription(cursor.getString(1));
                mealItem.setUrl(cursor.getString(2));
                mealItem.setHall(cursor.getString(3));
                mealItem.setMeal(cursor.getString(4));
                mealItem.setSection(cursor.getString(5));
                mealItem.setDescrptors(cursor.getString(6));
                mealItem.setDate(cursor.getString(7));

                // Add mealItem to list
                mealItemList.add(mealItem);
            } while (cursor.moveToNext());
        }
        cursor.close();

        // return mealItem list
        return mealItemList;
    }

    // Update a single mealItem
    public int updateMealItem(MealItem mealItem) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_NAME, mealItem.getName());
        values.put(KEY_DESCRIPTION, mealItem.getDescription());
        values.put(KEY_URL, mealItem.getUrl());
        values.put(KEY_HALL, mealItem.getHall());
        values.put(KEY_MEAL, mealItem.getMeal());
        values.put(KEY_SECTION, mealItem.getSection());
        values.put(KEY_DESCRIPTORS, mealItem.getDescriptors());
        values.put(KEY_DATE, mealItem.getDate());

        // updating row
        return db.update(TABLE_MEAL_ITEMS, values, KEY_NAME + " = ?",
                new String[]{mealItem.getName()});
    }

    // Delete a single mealItem
    public void deleteMealItem(String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_MEAL_ITEMS, KEY_NAME + " = ?",
                new String[]{name});
        db.close();
    }

    // Getting mealItems Count
    public int getMealItemsCount() {
        String countQuery = "SELECT  * FROM " + TABLE_MEAL_ITEMS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        cursor.close();

        // return count
        return cursor.getCount();
    }

    public void clear()
    {   SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_MEAL_ITEMS, null, null);
    }
}
package com.example.admin.bruinfeed;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHandler extends SQLiteOpenHelper {

    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "mealItemManager";

    // MealItems table name
    private static final String TABLE_MEAL_ITEMS = "mealItems";

    // MealItems Table Columns names
    private static final String KEY_NAME = "name";
    private static final String KEY_DESCRIPTION = "description";
    private static final String KEY_URL = "url";

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_MEAL_ITEMS_TABLE = "CREATE TABLE " + TABLE_MEAL_ITEMS + "("
                + KEY_NAME + " TEXT," + KEY_DESCRIPTION + " TEXT,"
                + KEY_URL + " TEXT" + ")";
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

        // Insert Row
        db.insert(TABLE_MEAL_ITEMS, null, values);
        db.close(); // Closing database connection
    }

    // Get single MealItem
    MealItem getMealItem(int id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_MEAL_ITEMS, new String[]{KEY_NAME,
                        KEY_DESCRIPTION, KEY_URL}, KEY_NAME + "=?",
                new String[]{String.valueOf(id)}, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();

        cursor.close();

        return new MealItem(cursor.getString(0), cursor.getString(1), cursor.getString(2));
    }

    // Getting All MealItems
    List<MealItem> getAllMealItems() {
        List<MealItem> mealItemList = new ArrayList<>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_MEAL_ITEMS;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // loop through all rows and add to list
        if (cursor.moveToFirst()) {
            do {
                MealItem mealItem = new MealItem();
                mealItem.setName(cursor.getString(1));      // TODO: WHY ARE THESE REVERSED?
                mealItem.setDescription(cursor.getString(0));
                mealItem.setUrl(cursor.getString(2));

                // Add mealItem to list
                mealItemList.add(mealItem);
            } while (cursor.moveToNext());
        }
        cursor.close();

        // return mealItem list
        return mealItemList;
    }

    // Update a single contact
    public int updateMealItem(MealItem mealItem) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_NAME, mealItem.getName());
        values.put(KEY_DESCRIPTION, mealItem.getDescription());
        values.put(KEY_URL, mealItem.getUrl());

        // updating row
        return db.update(TABLE_MEAL_ITEMS, values, KEY_NAME,
                new String[]{mealItem.getName()});
    }

    // Delete a single contact
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
}
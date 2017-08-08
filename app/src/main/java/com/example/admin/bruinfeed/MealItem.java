package com.example.admin.bruinfeed;

import android.database.Cursor;

public class MealItem {
    private String mName, mDescription, mUrl;

    public MealItem() {
        mName = "Name";
        mDescription = "Description";
        mUrl = "URL";
    }

    public MealItem(String name, String url) {
        mName = name;
        mDescription = "No description available";
        mUrl = url;
    }

    public MealItem(String name, String description, String url) {
        mName = name;
        mDescription = description;
        mUrl = url;
    }

    public static MealItem getMealItem(Cursor cursor) {
        return new MealItem(cursor.getString(0), cursor.getString(1), cursor.getString(2));
    }

    public String getName() { return mName; }

    public String getDescription() {
        return mDescription;
    }

    public String getUrl() {
        return mUrl;
    }

    public void setName(String name) {
        mName = name;
    }

    public void setDescription(String description) {
        mDescription = description;
    }

    public void setUrl(String url) {
        mUrl = url;
    }
}

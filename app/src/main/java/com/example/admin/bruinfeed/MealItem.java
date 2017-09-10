package com.example.admin.bruinfeed;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Parcel;
import android.os.Parcelable;

public class MealItem implements Parcelable, Comparable {
    private String mName, mDescription, mUrl, mHall, mMeal, mSection, mDescriptors, mDate;
    boolean mFavorite;

    public static final String FAVORITE_PREFERENCES_NAME = "FavPrefs";

    public MealItem() {
        mName = "Name";
        mDescription = "Description";
        mUrl = "URL";
        mHall = "Hall";
        mMeal = "Meal";
        mSection = "Section";
        mDescriptors = "Descriptors";
        mDate = "Date";
    }

    public MealItem(String name, String description, String url, String hall, String meal, String section) {
        mName = name;
        mDescription = description;
        mUrl = url;
        mHall = hall;
        mMeal = meal;
        mSection = section;
        mDescriptors = "";
    }

    public MealItem(String name, String description, String url, String hall, String meal, String section, String descriptors, String date) {
        mName = name;
        mDescription = description;
        mUrl = url;
        mHall = hall;
        mMeal = meal;
        mSection = section;
        mDescriptors = descriptors;
        mDate = date;
    }

    public MealItem(Parcel in) {
        readFromParcel(in);
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mName);
        dest.writeString(mDescription);
        dest.writeString(mUrl);
        dest.writeString(mHall);
        dest.writeString(mMeal);
        dest.writeString(mSection);
        dest.writeString(mDescriptors);
    }

    public int describeContents() {
        return 0;
    }

    private void readFromParcel(Parcel in) {
        mName = in.readString();
        mDescription = in.readString();
        mUrl = in.readString();
        mHall = in.readString();
        mMeal = in.readString();
        mSection = in.readString();
        mDescriptors = in.readString();
    }

    @Override
    public boolean equals(Object other) {
        MealItem m = (MealItem) other;
        return other != null && mName.equals(m.getName());
    }

    @Override
    public int compareTo(Object o) {
        MealItem m = (MealItem) o;
        return (mName.compareTo(m.getName()));
    }

    public static final Parcelable.Creator CREATOR =
            new Parcelable.Creator() {
                public MealItem createFromParcel(Parcel in) {
                    return new MealItem(in);
                }

                public MealItem[] newArray(int size) {
                    return new MealItem[size];
                }
            };


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

    public String getHall() { return mHall; }

    public void setHall(String hall) { mHall = hall; }

    public String getMeal() { return mMeal; }

    public void setMeal(String meal) { mMeal = meal; }

    public String getSection() { return mSection; }

    public void setSection(String section) { mSection = section; }

    public String getDescriptors() { return mDescriptors; }

    public void addDescriptor(String descriptor) { mDescriptors += descriptor + '\n'; }

    public void setDescrptors(String descriptors) { mDescriptors = descriptors; }

    public String getDate() { return mDate; }

    public void setDate(String date) { mDate = date; }

    public void setFavorite(Context context, boolean favorite) {
        SharedPreferences favPrefs = context.getSharedPreferences(FAVORITE_PREFERENCES_NAME, 0);
        SharedPreferences.Editor editor = favPrefs.edit();

        editor.putBoolean(mName, favorite);
        editor.apply();
    }

    public boolean getFavorite(Context context) {
        SharedPreferences favPrefs = context.getSharedPreferences(FAVORITE_PREFERENCES_NAME, 0);
        return favPrefs.getBoolean(mName, false);
    }
}

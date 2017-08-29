package com.example.admin.bruinfeed;

import android.os.Parcel;
import android.os.Parcelable;

public class MealItem implements Parcelable {
    private String mName, mDescription, mUrl, mHall, mMeal, mSection;

    public MealItem() {
        mName = "Name";
        mDescription = "Description";
        mUrl = "URL";
        mHall = "Hall";
        mMeal = "Meal";
        mSection = "Section";
    }

    public MealItem(String name, String description, String url, String hall, String meal, String section) {
        mName = name;
        mDescription = description;
        mUrl = url;
        mHall = hall;
        mMeal = meal;
        mSection = section;
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
    }

    public boolean equals(Object other) {
        MealItem m = (MealItem) other;
        return other != null && mName.equals(m.getName());
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
}

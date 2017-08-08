package com.example.admin.bruinfeed;

import android.os.Parcel;
import android.os.Parcelable;

public class MealItem implements Parcelable {
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

    public MealItem(Parcel in) {
        readFromParcel(in);
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mName);
        dest.writeString(mDescription);
        dest.writeString(mUrl);
    }

    public int describeContents() {
        return 0;
    }

    private void readFromParcel(Parcel in) {
        mName = in.readString();
        mDescription = in.readString();
        mUrl = in.readString();
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
}

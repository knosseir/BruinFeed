package com.example.admin.bruinfeed;

public class MenuItem {
    private String mName, mDescription, mUrl;

    public MenuItem(String name, String description, String url) {
        mName = name;
        mDescription = description;
        mUrl = url;
    }

    public String getName() {
        return mName;
    }

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

    public void setmUrl(String url) {
        mUrl = url;
    }

}

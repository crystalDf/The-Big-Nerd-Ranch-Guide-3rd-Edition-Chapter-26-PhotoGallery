package com.star.photogallery.model;


import com.google.gson.annotations.SerializedName;

public class Photo {

    @SerializedName("id")
    private String mId;

    @SerializedName("title")
    private String mTitle;

    @SerializedName("url_s")
    private String mUrlS;

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        mId = id;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public String getUrlS() {
        return mUrlS;
    }

    public void setUrlS(String urlS) {
        mUrlS = urlS;
    }
}

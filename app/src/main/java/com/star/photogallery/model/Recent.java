package com.star.photogallery.model;


import com.google.gson.annotations.SerializedName;

public class Recent {

    @SerializedName("photos")
    private Photos mPhotos;

    public Photos getPhotos() {
        return mPhotos;
    }

    public void setPhotos(Photos photos) {
        mPhotos = photos;
    }
}

package com.star.photogallery;


public class GalleryItem {

    private String mCaption;
    private String mId;
    private String mUrl;

    public void setCaption(String caption) {
        mCaption = caption;
    }

    public void setId(String id) {
        mId = id;
    }

    public void setUrl(String url) {
        mUrl = url;
    }

    public String getUrl() {
        return mUrl;
    }
    @Override
    public String toString() {
        return mCaption;
    }
}

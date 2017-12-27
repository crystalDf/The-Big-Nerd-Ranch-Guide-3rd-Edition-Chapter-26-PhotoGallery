package com.star.photogallery;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.v4.util.LruCache;
import android.util.Log;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


public class ThumbnailDownloader<T> extends HandlerThread {

    private static final String TAG = "ThumbnailDownloader";

    private static final int MESSAGE_DOWNLOAD = 0;
    private static final int MESSAGE_PRELOAD = 1;

    private Boolean mHasQuit = false;
    private Handler mRequestHandler;
    private ConcurrentMap<T, String> mRequestMap = new ConcurrentHashMap<>();

    private Handler mResponseHandler;
    private ThumbnailDownloadListener<T> mThumbnailDownloadListener;

    private LruCache<String, Bitmap> mLruCache;

    public interface ThumbnailDownloadListener<T> {
        void onThumbnailDownloaded(T targetView, Bitmap thumbnail);
    }

    public void setThumbnailDownloadListener(
            ThumbnailDownloadListener<T> thumbnailDownloadListener) {
        mThumbnailDownloadListener = thumbnailDownloadListener;
    }

    public ThumbnailDownloader(Handler responseHandler) {
        super(TAG);

        mResponseHandler = responseHandler;

        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        final int catchSize = maxMemory / 8;

        mLruCache = new LruCache<>(catchSize);
    }

    public LruCache<String, Bitmap> getLruCache() {
        return mLruCache;
    }

    @Override
    protected void onLooperPrepared() {
        mRequestHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case MESSAGE_DOWNLOAD:
                        T targetView = (T) msg.obj;
                        Log.i(TAG, "Got a request from a URL: " + mRequestMap.get(targetView));
                        handleRequest(targetView);
                        break;

                    case MESSAGE_PRELOAD:
                        String url = (String) msg.obj;
                        downloadImage(url);
                        break;
                }
            }
        };
    }

    @Override
    public boolean quit() {
        mHasQuit = true;
        return super.quit();
    }

    public void queueThumbnail(T targetView, String url) {
        Log.i(TAG, "Got a URL " + url);

        if (url == null) {
            mRequestMap.remove(targetView);
        } else {
            mRequestMap.put(targetView, url);
            mRequestHandler.obtainMessage(MESSAGE_DOWNLOAD, targetView).sendToTarget();
        }
    }

    public void preloadImage(String url) {
        mRequestHandler.obtainMessage(MESSAGE_PRELOAD, url).sendToTarget();
    }

    public void clearQueue() {
        mRequestHandler.removeMessages(MESSAGE_DOWNLOAD);
        mRequestHandler.removeMessages(MESSAGE_PRELOAD);
        mRequestMap.clear();
    }

    public void clearPreloadQueue() {
        mRequestHandler.removeMessages(MESSAGE_PRELOAD);
    }

    private void handleRequest(final T targetView) {

        final String url = mRequestMap.get(targetView);

        if (url == null) {
            return;
        }

        final Bitmap bitmap = downloadImage(url);

        Log.i(TAG, "Bitmap created");

        mResponseHandler.post(() -> {
            if (!Objects.equals(mRequestMap.get(targetView), url) || mHasQuit) {
                return;
            }

            mRequestMap.remove(targetView);
            mThumbnailDownloadListener.onThumbnailDownloaded(targetView, bitmap);
        });
    }

    private Bitmap downloadImage(String url) {

        if (url == null) {
            return null;
        }

        Bitmap bitmap = mLruCache.get(url);

        if (bitmap != null) {
            return bitmap;
        }

        try {
            byte[] bitmapBytes = new FlickrFetchr().getUrlBytes(url);

            bitmap = BitmapFactory.decodeByteArray(bitmapBytes, 0, bitmapBytes.length);

            mLruCache.put(url, bitmap);

            return bitmap;
        } catch (IOException e) {
            Log.e(TAG, "Error downloading image", e);
        }

        return null;
    }
}

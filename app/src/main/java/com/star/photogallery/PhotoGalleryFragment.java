package com.star.photogallery;


import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class PhotoGalleryFragment extends Fragment {

    private static final String TAG = "PhotoGalleryFragment";

    private static final int DEFAULT_COLUMN_NUM = 3;
    private static final int ITEM_WIDTH = 100;

    private RecyclerView mPhotoRecyclerView;
    private GridLayoutManager mGridLayoutManager;
    private List<GalleryItem> mGalleryItems;

    private int mCurrentPage = 1;
    private int mFetchedPage = 0;
    private int mCurrentPosition = 0;

    public static PhotoGalleryFragment newInstance() {
        return new PhotoGalleryFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        new FetchItemsTask().execute(mCurrentPage);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_photo_gallery, container, false);

        mPhotoRecyclerView = view.findViewById(R.id.fragment_photo_gallery_recycler_view);

        mGridLayoutManager = new GridLayoutManager(getActivity(), DEFAULT_COLUMN_NUM);

        mPhotoRecyclerView.setLayoutManager(mGridLayoutManager);

        mPhotoRecyclerView.getViewTreeObserver().addOnGlobalLayoutListener(
                () -> {
                    int spanCount = convertPxToDp(mPhotoRecyclerView.getWidth()) / ITEM_WIDTH;
                    mGridLayoutManager.setSpanCount(spanCount);
                });

        mPhotoRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                updateCurrentPage();
            }
        });

        setupAdapter();
        
        return view;
    }

    private int convertPxToDp(float sizeInPx) {
        DisplayMetrics displayMetrics = Resources.getSystem().getDisplayMetrics();

        return (int) (sizeInPx / displayMetrics.density);
    }

    private void updateCurrentPage() {
        int firstVisibleItemPosition = mGridLayoutManager.findFirstVisibleItemPosition();
        int lastVisibleItemPosition = mGridLayoutManager.findLastVisibleItemPosition();

        if (lastVisibleItemPosition == (mGridLayoutManager.getItemCount() - 1) &&
                mCurrentPage == mFetchedPage ) {
            mCurrentPosition = firstVisibleItemPosition + DEFAULT_COLUMN_NUM;
            mCurrentPage++;
            new FetchItemsTask().execute(mCurrentPage);
        }
    }

    private void setupAdapter() {
        if (isAdded()) {
            if (mGalleryItems != null) {
                mPhotoRecyclerView.setAdapter(new PhotoAdapter(mGalleryItems));
            } else {
                mPhotoRecyclerView.setAdapter(null);
            }
            mPhotoRecyclerView.scrollToPosition(mCurrentPosition);
        }
    }

    private class PhotoHolder extends RecyclerView.ViewHolder {

        private TextView mTitleTextView;

        public PhotoHolder(View itemView) {
            super(itemView);

            mTitleTextView = (TextView) itemView;
        }

        public void bindGalleryItem(GalleryItem item) {
            mTitleTextView.setText(item.toString());
        }
    }

    private class PhotoAdapter extends RecyclerView.Adapter<PhotoHolder> {

        private List<GalleryItem> mGalleryItems;

        public PhotoAdapter(List<GalleryItem> galleryItems) {
            mGalleryItems = galleryItems;
        }

        @Override
        public PhotoHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            TextView textView = new TextView(getActivity());
            return new PhotoHolder(textView);
        }

        @Override
        public void onBindViewHolder(PhotoHolder holder, int position) {
            GalleryItem galleryItem = mGalleryItems.get(position);
            holder.bindGalleryItem(galleryItem);
        }

        @Override
        public int getItemCount() {
            return mGalleryItems.size();
        }
    }

    private class FetchItemsTask extends AsyncTask<Integer, Void, List<GalleryItem>> {

        @Override
        protected List<GalleryItem> doInBackground(Integer... params) {
            return new FlickrFetchr().fetchItems(params[0]);
        }

        @Override
        protected void onPostExecute(List<GalleryItem> items) {
            if (mGalleryItems == null) {
                mGalleryItems = items;
            } else {
                if (items != null) {
                    mGalleryItems.addAll(items);
                }
            }

            mFetchedPage++;

            setupAdapter();
        }
    }
}

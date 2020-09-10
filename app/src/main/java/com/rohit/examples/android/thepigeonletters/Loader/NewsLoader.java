package com.rohit.examples.android.thepigeonletters.Loader;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.AsyncTaskLoader;

import com.rohit.examples.android.thepigeonletters.Model.News;
import com.rohit.examples.android.thepigeonletters.Utils.Utils;

import java.util.List;

/**
 * Loader class to handle NewsLoader
 * It provides an AsyncTask to do the work
 */
public class NewsLoader extends AsyncTaskLoader<List<News>> {

    // Instantiating request URL
    private final String requestUrl;

    /**
     * Constructor initialization to make request URL for remote API call
     *
     * @param context    current state of application/object
     * @param requestUrl URL to query request
     */
    public NewsLoader(@NonNull Context context, String requestUrl) {
        super(context);
        this.requestUrl = requestUrl;
    }

    /**
     * Method to take care of loading data
     */
    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    /**
     * Called on a worker thread to perform loading data
     *
     * @return the result of the load operation
     */
    @Nullable
    @Override
    public List<News> loadInBackground() {
        if (requestUrl == null) {
            return null;
        }
        return Utils.fetchNewsContent(requestUrl);
    }
}
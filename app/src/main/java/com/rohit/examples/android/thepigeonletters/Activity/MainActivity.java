package com.rohit.examples.android.thepigeonletters.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.rohit.examples.android.thepigeonletters.Adapter.NewsAdapter;
import com.rohit.examples.android.thepigeonletters.BuildConfig;
import com.rohit.examples.android.thepigeonletters.Listener.OnMoreLoadListener;
import com.rohit.examples.android.thepigeonletters.Loader.NewsLoader;
import com.rohit.examples.android.thepigeonletters.Model.News;
import com.rohit.examples.android.thepigeonletters.R;

import java.util.ArrayList;
import java.util.List;

import static com.rohit.examples.android.thepigeonletters.Utils.Utils.isNetworkConnected;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<News>>, SwipeRefreshLayout.OnRefreshListener, OnMoreLoadListener {

    /**
     * Constants string variables storing Preference Option values
     */
    public static final String QUERY_SEARCH_BY = "q";
    public static final String QUERY_SORT_BY = "order-by";

    /**
     * Constants string variables storing API call back responses
     */
    private static final String GUARDIAN_REQ_URL = "http://content.guardianapis.com/search";
    private static final String API_KEY = BuildConfig.GUARDIAN_API_KEY;
    private static final String QUERY_API_KEY = "api-key";
    private static final String QUERY_THUMBNAIL = "show-fields";
    private static final String QUERY_AUTHOR = "show-tags";
    private static final String THUMBNAIL_VALUE = "thumbnail";
    private static final String AUTHOR_VALUE = "contributor";
    private static final String QUERY_PAGE = "page";

    // View Variable declaration for UI elements
    private RecyclerView recyclerView;
    private TextView infoTextView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar progressBar;

    /**
     * Variables for loader manager, News items list
     */
    private LoaderManager loaderManager;
    private List<News> newsList;
    private NewsAdapter newsAdapter;

    /**
     * Variables for article page (1 for first page by default) and search query to fetch from API
     */
    private int initialPage = 1;
    private String searchQueryText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Fetching View IDs from ID resource
        // Custom Toolbar as an App Bar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        recyclerView = findViewById(R.id.recyclerView);
        infoTextView = findViewById(R.id.notify_textView);
        swipeRefreshLayout = findViewById(R.id.swipeRefresh);
        progressBar = findViewById(R.id.progress_bar);

        recyclerView = findViewById(R.id.recyclerView);
        infoTextView = findViewById(R.id.notify_textView);

        // Getting Loader Manager from the activity
        loaderManager = getSupportLoaderManager();

        // Logic to handle Network connectivity to fetch remote API call responses
        if (isNetworkConnected(this)) {

            Log.d("TAG", "InitLoader");
            /*  Initializing a new loader
                Ensures a loader is initialized and active
                Setting the default query parameter value to be used for API call
             */
            searchQueryText = getString(R.string.settings_search_by_default);
            loaderManager.initLoader(1, null, this);
        } else {
            //Displaying helpful text to user if the internet connection is not available
            infoTextView.setText(R.string.no_internet);
            infoTextView.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.no_internet, 0, 0);
            infoTextView.setCompoundDrawablePadding(16);
            progressBar.setVisibility(View.GONE);
        }

        swipeRefreshLayout.setOnRefreshListener(this);

        // Instantiating new ArrayList for News items
        newsList = new ArrayList<>();

        // Setting up the recycler view to display list vertically
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerView.setHasFixedSize(true);

        // Instantiating the adapter to populate list items
        newsAdapter = new NewsAdapter(this, newsList, recyclerView);

        recyclerView.setAdapter(newsAdapter);

        //Setting up the OnLoadMoreListener to enable pagination
        newsAdapter.setOnLoadMoreListener(this);
    }

    /**
     * Called when a swipe gesture triggers a refresh.
     */
    @Override
    public void onRefresh() {

        //Hiding progress bar as refresh layout has it's own progress indicator
        progressBar.setVisibility(View.GONE);

        //Clearing existing data on refresh and notify the adapter
        newsList.clear();
        newsAdapter.notifyDataSetChanged();

        //Removing pagination progress loading footer item
        newsAdapter.removeLoadingFooter(null);

        //Disabling pagination while refreshing
        newsAdapter.setContentLoaded();
        newsAdapter.setOnLoadMoreListener(null);

        //Resetting the initial page
        initialPage = 1;

        // Logic to handle Network connectivity to fetch remote API call responses
        if (isNetworkConnected(this)) {
            Log.d("TAG", "RestartLoader on Refresh");
            //Starting a new or restarting an existing Loader in existing loader manager
            loaderManager.restartLoader(1, null, this);
        } else {
            swipeRefreshLayout.setRefreshing(false);
            //Updating the empty state views for no internet connection
            updateEmptyState(R.string.no_internet, R.drawable.no_internet);
        }
    }

    /**
     * Method to update empty state views accordingly
     *
     * @param emptyStringId display string for the empty state
     * @param emptyImageId  vector drawable for the empty state
     */
    private void updateEmptyState(int emptyStringId, int emptyImageId) {
        infoTextView.setText(emptyStringId);
        infoTextView.setCompoundDrawablesWithIntrinsicBounds(0, emptyImageId, 0, 0);
        infoTextView.setCompoundDrawablePadding(8);
        infoTextView.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
    }

    /**
     * Initializes the contents of the Activity's standard options menu
     *
     * @param menu the options menu in which you place your items
     * @return returns true for the menu to be displayed, false to hide menu
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    /**
     * Called whenever an item in options menu is selected
     *
     * @param item the menu item that was selected
     * @return returns false to allow normal menu processing to proceed, true to consume it here
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.settings) {
            Intent intentSettings = new Intent(this, SettingsActivity.class);
            startActivity(intentSettings);
        }
        return true;
    }

    /**
     * Instantiate and return a new Loader for the given ID.
     *
     * @param i      the ID whose loader is to be created.
     * @param bundle any arguments supplied by the caller.
     * @return a new Loader instance that is ready to start loading.
     */
    @NonNull
    @Override
    public android.support.v4.content.Loader<List<News>> onCreateLoader(int i, @Nullable Bundle bundle) {

        Log.d("TAG", "OnCreateLoader");

        /*
            Getting a SharedPreferences instance that points to the default file
            that is used by the preference framework in the given context.
         */
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        //Fetching search_by value from SharedPreferences
        searchQueryText = sharedPreferences.getString(getString(R.string.settings_search_by_key), getString(R.string.settings_search_by_default));

        //Fetching sort_by value from SharedPreferences
        String sortNewsBy = sharedPreferences.getString(getString(R.string.settings_sort_by_key), getString(R.string.settings_sort_by_default));

        //Parsing the base URL for Guardian API
        Uri.Builder builder = Uri.parse(GUARDIAN_REQ_URL).buildUpon();

        //Setting up the query parameters - search term, sort method, api key, topic for the news, author name and news thumbnail
        builder
                .appendQueryParameter(QUERY_SEARCH_BY, searchQueryText)
                .appendQueryParameter(QUERY_SORT_BY, sortNewsBy)
                .appendQueryParameter(QUERY_API_KEY, API_KEY)
                .appendQueryParameter(QUERY_PAGE, String.valueOf(initialPage))
                .appendQueryParameter(QUERY_AUTHOR, AUTHOR_VALUE)
                .appendQueryParameter(QUERY_THUMBNAIL, THUMBNAIL_VALUE);

        return new NewsLoader(this, builder.toString());
    }

    /**
     * Called when a previously created loader has finished its load
     *
     * @param loader   the Loader that has finished.
     * @param newsData the data generated by the Loader
     */
    @Override
    public void onLoadFinished(@NonNull android.support.v4.content.Loader<List<News>> loader, List<News> newsData) {
        Log.d("TAG", "OnLoadFinished");

        //Hiding the progress indicator once the loading is complete
        progressBar.setVisibility(View.GONE);

        //Removing pagination progress loading footer item
        newsAdapter.removeLoadingFooter(null);

        //Disabling pagination
        newsAdapter.setContentLoaded();

        //Enabling the listener if it was previously disabled on refresh
        newsAdapter.setOnLoadMoreListener(this);

        //Updating the empty stat views for server error
        if (newsData == null) {
            updateEmptyState(R.string.server_error, R.drawable.ic_error_image);
            return;
        }

        //Verifying if the data loaded is empty
        if (!newsData.isEmpty()) {
            newsList.addAll(newsData);
            newsAdapter.notifyItemRangeChanged(newsList.size() + 1, newsData.size());
            initialPage++;

            //Displaying the recycler view and hiding the empty state views
            recyclerView.setVisibility(View.VISIBLE);
            infoTextView.setVisibility(View.GONE);
        } else {
            //Updating the empty stat views if no relevant data fetched/ found
            updateEmptyState(R.string.no_data_found, R.drawable.ic_search);
        }

        //Hiding refresh progress indicator
        swipeRefreshLayout.setRefreshing(false);
        //Enabling refresh layout once the data is fetched
        swipeRefreshLayout.setEnabled(true);

        //Destroying loader to prevent unnecessary calls to load finished
        loaderManager.destroyLoader(1);
    }

    /**
     * Called when a previously created loader is being reset, and thus making its data unavailable
     *
     * @param loader the Loader that is being reset.
     */
    @Override
    public void onLoaderReset(@NonNull android.support.v4.content.Loader<List<News>> loader) {
        Log.d("TAG", "OnLoaderReset - not clearing data anymore!");
        newsList.clear();
        newsAdapter.notifyDataSetChanged();
    }

    /**
     * Handling pagination - calls loader to fetch data from the next page
     */
    @Override
    public void onLoadMore() {
        // Logic to handle Network connectivity to fetch remote API call responses
        if (isNetworkConnected(this)) {
            //Disabling swipe to refresh
            swipeRefreshLayout.setEnabled(false);

            // Adding null, so the adapter will show progress bar at the bottom after resolving the view type
            newsAdapter.addLoadingFooter(null);

            /*
                Restart loader to be called with the updated initial page number
                Handling the call as a separate callback to prevent updating recycler view inside scroll callback
             */
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    Log.d("TAG", "RestartLoader on LoadMore");
                    loaderManager.restartLoader(1, null, MainActivity.this);
                }
            });
        } else {
            //Displaying internet connectivity error
            updateEmptyState(R.string.no_internet, R.drawable.no_internet);
        }
    }
}
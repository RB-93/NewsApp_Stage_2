package com.rohit.examples.android.thepigeonletters.Adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.rohit.examples.android.thepigeonletters.Listener.OnMoreLoadListener;
import com.rohit.examples.android.thepigeonletters.Model.News;
import com.rohit.examples.android.thepigeonletters.R;

import java.util.List;

import static com.rohit.examples.android.thepigeonletters.Utils.Utils.formatTimeStamp;
import static com.rohit.examples.android.thepigeonletters.Utils.Utils.setupNewsImage;

/**
 * Custom NewsAdapter to handle recycler view and teh underlying data for the view
 */
public class NewsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


    private static final int VIEW_ITEM = 1;
    private static final int VIEW_PROGRESS = 0;
    private final Context context;
    private final List<News> newsList;
    private final int margin = 1;
    private int totalItems;
    private int lastItem;
    private boolean isContentLoading;
    private OnMoreLoadListener onMoreLoadListener;

    /**
     * Constructor to handle context object, newsList and recyclerView
     *
     * @param context      to preserve application state
     * @param newsList     to retrieve list items
     * @param recyclerView to populate the data on it
     */
    public NewsAdapter(Context context, List<News> newsList, RecyclerView recyclerView) {
        this.context = context;
        this.newsList = newsList;

        // Getting reference of recycler view layout manager
        if (recyclerView.getLayoutManager() instanceof LinearLayoutManager) {
            final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();

            // add on scroll interface call to handle pagination on user scrolls on recycler view
            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);

                    /*
                        User scroll logic to check if the current pagination is in scrolling mode
                        To check if the recycler view has reached to the last item
                     */
                    totalItems = linearLayoutManager.getItemCount();
                    lastItem = linearLayoutManager.findLastVisibleItemPosition();

                    if (!isContentLoading && totalItems <= (lastItem + margin)) {
                        if (onMoreLoadListener != null) {
                            onMoreLoadListener.onLoadMore();
                        }
                        isContentLoading = true;
                    }
                }
            });
        }
    }

    /**
     * Called when RecyclerView needs a new RecyclerView.ViewHolder of the given type to represent an item.
     *
     * @param viewGroup the ViewGroup into which the new View will be added
     * @param viewType  the view type of the new View.
     * @return A new ViewHolder that holds a View of the given view type.
     */
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {

        // Inflating the recycler view layout based on the VIEW_ITEM
        RecyclerView.ViewHolder viewHolder;
        if (viewType == VIEW_ITEM) {
            View view = LayoutInflater
                    .from(viewGroup.getContext())
                    .inflate(R.layout.layout_news_list_item, viewGroup, false);

            viewHolder = new NewsViewHolder(view);
        } else {
            View view = LayoutInflater
                    .from(viewGroup.getContext())
                    .inflate(R.layout.layout_progress, viewGroup, false);

            viewHolder = new FooterViewHolder(view);
        }
        return viewHolder;
    }

    /**
     * Called by RecyclerView to display the data at the specified position.
     *
     * @param viewHolder the ViewHolder resolved to represent the item at the given position.
     * @param position   the position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        if (viewHolder instanceof NewsViewHolder) {
            // Getting current position reference
            final News news = newsList.get(position);

            NewsViewHolder newsViewHolder = (NewsViewHolder) viewHolder;

            // Setup for Glide RequestOptions to add placeholder image in News ImageView
            RequestOptions requestOptions = setupNewsImage();

            // Loading news image with Glide support
            Glide.with(context)
                    .setDefaultRequestOptions(requestOptions)
                    .load(news.getNewsImageUrl())
                    .into(newsViewHolder.imageView);

            // Displaying the required field values into the UI
            newsViewHolder.newsTitle.setText(news.getNewsTitle());
            newsViewHolder.newsTitle.setMaxLines(2);
            newsViewHolder.newsAuthor.setText(news.getNewsAuthor());
            newsViewHolder.newsSection.setText(news.getNewsSection());

            // Formatting the Date received from JSON response
            String timeStampFormat = formatTimeStamp(news.getNewsTimeStamp());
            newsViewHolder.newsTimeStamp.setText(timeStampFormat);

            // Click listener for each list news item to open news in external browser application.
            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent newsViewIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(news.getNewsUrl()));
                    Toast.makeText(context, R.string.news_open_text, Toast.LENGTH_SHORT).show();
                    context.startActivity(newsViewIntent);
                }
            });
        } else {
            ((FooterViewHolder) viewHolder).progressBar.setIndeterminate(true);
        }
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return the total number of items in this adapter.
     */
    @Override
    public int getItemCount() {
        return newsList == null ? 0 : newsList.size();
    }

    /**
     * Return the view type of the item at position.
     *
     * @param position position to query
     * @return the type of the view needed to represent the item at position.
     */
    @Override
    public int getItemViewType(int position) {
        return newsList.get(position) != null ? VIEW_ITEM : VIEW_PROGRESS;
    }

    /**
     * Disabling loading flag as soon as current page loading is complete via pagination
     */
    public void setContentLoaded() {
        isContentLoading = false;
    }

    /**
     * Listener to initialize @link onLoadMore method
     *
     * @param onLoadMoreListener listener reference to Interface
     */
    public void setOnLoadMoreListener(OnMoreLoadListener onLoadMoreListener) {
        this.onMoreLoadListener = onLoadMoreListener;
    }

    /**
     * Method to remove the progress bar loading item added as a recycler view footer
     *
     * @param object reference object to be removed
     *               *        object will be null, casted to News type to handle suspicious remove warning
     */
    public void removeLoadingFooter(Object object) {
        News news = (News) object;
        newsList.remove(news);

        // Notify that the item previously located at position has been removed from the data set
        notifyItemRemoved(newsList.size());
    }

    /**
     * Method to add a progress bar item as the recycler view footer
     *
     * @param object reference to object to be added
     */
    public void addLoadingFooter(final Object object) {

        /*
            Handle   adding new item to adapter as a separate callback as updating adapter data source in the same callback
            to refrain from the recycler view scroll callback unexpected response,
            and the recycler view layout might be undergoing modifications
         */
        Handler handler = new Handler();
        final Runnable runnable = new Runnable() {
            public void run() {
                News news = (News) object;
                newsList.add(news);

                // Notify that the item reflected at position has been newly inserted
                notifyItemInserted(newsList.size());
            }
        };
        handler.post(runnable);
    }

    /**
     * Custom ViewHolder to resolve references for each footer item layout
     */
    static class FooterViewHolder extends RecyclerView.ViewHolder {

        // View variable declaration for UI elements
        final ProgressBar progressBar;

        FooterViewHolder(@NonNull View itemView) {
            super(itemView);

            // Fetching View IDs from ID resource
            progressBar = itemView.findViewById(R.id.progressBar);
        }
    }

    /**
     * Custom ViewHolder to resolve references for each news list item.
     */
    class NewsViewHolder extends RecyclerView.ViewHolder {

        // View variable declaration for UI elements
        final ImageView imageView;
        final TextView newsTitle;
        final TextView newsAuthor;
        final TextView newsTimeStamp;
        final TextView newsSection;

        NewsViewHolder(@NonNull View itemView) {
            super(itemView);

            // Fetching View IDs from ID resource
            imageView = itemView.findViewById(R.id.newsImage);
            imageView.setClipToOutline(true);
            newsTitle = itemView.findViewById(R.id.news_text);
            newsAuthor = itemView.findViewById(R.id.author_text);
            newsTimeStamp = itemView.findViewById(R.id.timestamp_text);
            newsSection = itemView.findViewById(R.id.section_text);
        }
    }
}
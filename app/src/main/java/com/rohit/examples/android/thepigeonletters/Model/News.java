package com.rohit.examples.android.thepigeonletters.Model;

/**
 * Model class definition to handle raw data of UI elements
 */
public class News {
    private final String newsImageUrl;
    private final String newsUrl;
    private final String newsTitle;
    private final String newsAuthor;
    private final String newsTimeStamp;
    private final String newsSection;

    public News(String newsImageUrl, String newsUrl, String newsTitle, String newsAuthor, String newsTimeStamp, String newsSection) {
        this.newsImageUrl = newsImageUrl;
        this.newsUrl = newsUrl;
        this.newsTitle = newsTitle;
        this.newsAuthor = newsAuthor;
        this.newsTimeStamp = newsTimeStamp;
        this.newsSection = newsSection;
    }

    /**
     * Getter methods to fetch values from User interface and assigning to its appropriate views
     *
     * @return the fetched value
     */
    public String getNewsImageUrl() {
        return newsImageUrl;
    }

    public String getNewsTitle() {
        return newsTitle;
    }

    public String getNewsUrl() {
        return newsUrl;
    }

    public String getNewsAuthor() {
        return newsAuthor;
    }

    public String getNewsTimeStamp() {
        return newsTimeStamp;
    }

    public String getNewsSection() {
        return newsSection;
    }
}
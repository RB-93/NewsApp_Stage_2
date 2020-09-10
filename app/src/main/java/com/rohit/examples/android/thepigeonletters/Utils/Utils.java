package com.rohit.examples.android.thepigeonletters.Utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;
import android.util.Log;

import com.bumptech.glide.request.RequestOptions;
import com.rohit.examples.android.thepigeonletters.Model.News;
import com.rohit.examples.android.thepigeonletters.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static android.content.Context.CONNECTIVITY_SERVICE;

/**
 * Helper methods related to requesting and receiving news data from Guardians API.
 */
public class Utils {

    /**
     * Constant Time values and request values to handle receiving API calls
     */
    private static final int TIMEOUT = 10000;
    private static final int CONNECT_TIMEOUT = 15000;
    private static final String REQUEST_METHOD = "GET";
    private static final long RETRY_DELAY = 3000;
    private static final int RETRIES = 3;

    private Utils() {
    }

    /**
     * checks network connectivity
     *
     * @param context to preserve application/ object state
     * @return returns status of network connectivity
     */
    public static boolean isNetworkConnected(Context context) {
        //Getting reference to connectivity manager
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = null;
        if (connectivityManager != null) {
            //Getting the status of the network
            activeNetwork = connectivityManager.getActiveNetworkInfo();
        }
        return activeNetwork != null && activeNetwork.isConnected();
    }

    /**
     * Method to setup Glide placeholder images
     *
     * @return returns reference to appropriate RequestOptions
     */
    @SuppressLint("CheckResult")
    public static RequestOptions setupNewsImage() {
        RequestOptions requestOptions = new RequestOptions();
        requestOptions.placeholder(R.drawable.news_placeholder_image);
        requestOptions.error(R.drawable.ic_error_image);

        return requestOptions;
    }

    /**
     * formats the date in the required format
     *
     * @param timeStamp date-timestamp to be formatted
     * @return returns formatted date-timestamp in the form of a string
     */
    public static String formatTimeStamp(String timeStamp) {
        String formattedTimeStamp = "";
        //Defining input date format
        SimpleDateFormat inputSdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
        //Defining output date format
        SimpleDateFormat outputSdf = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());
        try {
            //Parsing and formatting the input date
            Date date = inputSdf.parse(timeStamp);
            formattedTimeStamp = outputSdf.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return formattedTimeStamp;
    }

    /**
     * Called by loader to fetch the data
     *
     * @param requestUrl API request URL from which to fetch the data
     * @return returns list of custom objects parsed from JSON response
     */
    public static List<News> fetchNewsContent(String requestUrl) {
        String jsonResponse = "";
        try {
            jsonResponse = makeHttpRequest(createUrl(requestUrl));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return extractNews(jsonResponse);
    }

    /**
     * Parsing JSON response obtained by the API call
     *
     * @param jsonResponse json string to be parsed
     * @return returns JSON string to be parsed
     */
    private static List<News> extractNews(String jsonResponse) {
        //validate if the json string is empty
        if (TextUtils.isEmpty(jsonResponse)) {
            return null;
        }

        List<News> newsList = new ArrayList<>();

        try {
            //Creating new JSON object for the JSON string
            JSONObject jsonObject = new JSONObject(jsonResponse);
            //Extracting the root object
            JSONObject response = jsonObject.optJSONObject("response");

            if (response != null) {
                //Fetching the results array
                JSONArray jsonArray = response.optJSONArray("results");

                if (jsonArray != null) {
                    //Looping through the array elements and parse the individual fields
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject newsObj = jsonArray.optJSONObject(i);

                        if (newsObj != null) {
                            String title = newsObj.optString("webTitle");
                            String section = newsObj.optString("sectionName");
                            String publishDate = newsObj.optString("webPublicationDate");
                            String webUrl = newsObj.optString("webUrl");

                            String thumbnailUrl = "";
                            JSONObject fields = newsObj.optJSONObject("fields");
                            if (fields != null) {
                                thumbnailUrl = fields.optString("thumbnail");
                            }

                            JSONArray tags = newsObj.optJSONArray("tags");
                            String authorName = "";
                            if (tags != null && tags.length() > 0) {
                                JSONObject authorProfile = (JSONObject) tags.get(0);
                                authorName = authorProfile.optString("webTitle");
                            }

                            //Initializing and add news items to the array list
                            News news = new News(thumbnailUrl, webUrl, title, authorName, publishDate, section);
                            newsList.add(news);
                        }
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return newsList;
    }

    /**
     * Opens a http request for the required url, reads the data from the input stream
     *
     * @param url url from which to fetch the results
     * @return returns JSON response from the url
     * @throws IOException while reading from stream objects
     */
    private static String makeHttpRequest(URL url) throws IOException {
        String jsonResponse = "";
        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;

        //Validating if the request url is null
        if (url == null) {
            return null;
        }

        try {
            //Setting up the http url connection
            urlConnection = verifyConnectionResponse(url);

            if (urlConnection != null) {
                urlConnection.setRequestMethod(REQUEST_METHOD);
                urlConnection.setReadTimeout(TIMEOUT);
                urlConnection.setConnectTimeout(CONNECT_TIMEOUT);
                urlConnection.connect();

                //Getting the input stream to read data from
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            //Disconnecting the url connection
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            //Closing input stream
            if (inputStream != null) {
                inputStream.close();
            }
        }

        return jsonResponse;
    }

    /**
     * Checks a http request - response for the requested url
     *
     * @param url url from which to check the responses
     * @return returns URL connection back from where requested
     * @throws InterruptedException while a thread is waiting, sleeping, or occupied
     * @throws IOException          while reading from stream objects
     */
    private static HttpURLConnection verifyConnectionResponse(URL url) throws InterruptedException, IOException {
        int retry = 0;
        boolean delay = false;
        HttpURLConnection urlConnection;
        do {
            if (delay) {
                Thread.sleep(RETRY_DELAY);
            }
            urlConnection = (HttpURLConnection) url.openConnection();
            //Verifying http connection response status
            switch (urlConnection.getResponseCode()) {
                case HttpURLConnection.HTTP_OK:
                    Log.d("HttpConnection", " **OK**");
                    return urlConnection;
                case HttpURLConnection.HTTP_GATEWAY_TIMEOUT:
                    Log.w("HttpConnection", " **GATEWAY TIMEOUT**");
                    break;
                case HttpURLConnection.HTTP_UNAVAILABLE:
                    Log.w("HttpConnection", " **UNAVAILABLE**");
                    break;
                default:
                    Log.e("HttpConnection", " **UNKNOWN RESPONSE CODE**");
                    break;
            }
            urlConnection.disconnect();

            retry++;
            Log.w("HttpConnection", "Failed retry " + retry + "/" + RETRIES);
            delay = true;

        } while (retry < RETRIES);
        Log.e("HttpConnection", " Aborting download of data");
        return urlConnection;
    }

    /**
     * Reads the data from the input stream
     *
     * @param inputStream stream of data to be read
     * @return returns read data in the form of string
     * @throws IOException while reading from the input stream
     */
    private static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            //Initializing an input stream reader
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
            //Connecting the input stream reader to a buffered reader
            BufferedReader reader = new BufferedReader(inputStreamReader);
            //Reading the data
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }

    /**
     * Creates a url from a string
     *
     * @param reqUrl request url in string format
     * @return returns URL for the API call
     */
    private static URL createUrl(String reqUrl) {
        URL url = null;
        try {
            url = new URL(reqUrl);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return url;
    }
}

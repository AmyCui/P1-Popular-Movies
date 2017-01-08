package com.amy.android.popularmovies;

import android.support.v4.content.AsyncTaskLoader;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class MoviesResultLoader extends AsyncTaskLoader<Object> {


    private final static String LOG_TAG = MoviesResultLoader.class.getName();
    private URL mUrl;
    private Context mContext;

    /**
     * AsyncTaskLoader that loads movie data from theMovieDB
     * @param context
     * @param url the http request url
     */
    public MoviesResultLoader(Context context, URL url) {
        super(context);
        mContext = context;
        mUrl = url;
    }

    public void setUrl(URL url){
        mUrl = url;
    }


    @Override
    public Object loadInBackground() {

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        String moviesJsonStr = null;

        try {


            urlConnection = (HttpURLConnection) mUrl.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return null;
            }

            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            long count = 0;
            while ((line = reader.readLine()) != null) {
                count += line.length();
                buffer.append(line + "\n");
            }
            moviesJsonStr = buffer.toString();
            //List<String[]> movieDetails = Utility.GetMovieDetailFromJson(moviesJsonStr);
            return moviesJsonStr;

        } catch (Exception e) {
            Log.e(LOG_TAG, "Error ", e);
            return null;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }
    }

}

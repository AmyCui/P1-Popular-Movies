package com.amy.android.popularmovies;

import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/*
* This class has the helper functions and enums for the App
 */
public class Utility {

    //movies HTTP request


    public static List<String[]> GetMovieDetailFromJson(String movieJsonData) throws JSONException {

        JSONObject movieDataObject = new JSONObject(movieJsonData);
        JSONArray resultArray = movieDataObject.getJSONArray("results");
        int numOfResults = resultArray.length();
        List<String[]> result = new ArrayList<>();
        for(int i=0; i<numOfResults; i++)
        {
            JSONObject movie = resultArray.getJSONObject(i);
            String[] movieDetail = new String[5];
            String title = movie.getString(MovieDetailItem.title.toString());
            movieDetail[0] = title;
            String release_date = movie.getString(MovieDetailItem.release_date.toString());
            movieDetail[1] = release_date;
            String poster_path = movie.getString(MovieDetailItem.poster_path.toString());
            movieDetail[2] = poster_path;
            String vote_average = movie.getString(MovieDetailItem.vote_average.toString());
            movieDetail[3] = vote_average;
            String overview = movie.getString(MovieDetailItem.overview.toString());
            movieDetail[4] = overview;
            result.add(movieDetail);
        }
        return result;
    }

    public static String GetThumbnailUrlString(String poster_path, String size){

        final String THUMBNAIL_BASE_URL = "http://image.tmdb.org/t/p";
        final String SIZE_PATH = size;

        Uri builtUri = Uri.parse(THUMBNAIL_BASE_URL).buildUpon()
                .appendPath(SIZE_PATH)
                .appendEncodedPath(poster_path)
                .build();

        return builtUri.toString();
    }

    public static enum PosterSize {
        w92,
        w154,
        w185,
        w342,
        w500,
        w780,
        original
    }

    public static enum MovieDetailItem{
        title,
        release_date,
        poster_path,
        vote_average,
        overview
    }

}

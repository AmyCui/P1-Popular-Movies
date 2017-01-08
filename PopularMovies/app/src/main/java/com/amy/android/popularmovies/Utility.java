package com.amy.android.popularmovies;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import com.amy.android.popularmovies.data.MovieContract;

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
import java.util.ArrayList;
import java.util.List;

/*
* This class has the helper functions and enums for the App
 */
public class Utility {

    public static final String MovieWithIDSelection = MovieContract.FavoritesEntry.TABLE_NAME + "." + MovieContract.FavoritesEntry.COLUMN_MOVIE_ID + " = ?";

    public static List<String[]> GetMovieSortTypeResultFromJson(String movieJsonData) throws JSONException{

        JSONObject movieDataObject = new JSONObject(movieJsonData);
        JSONArray resultArray = movieDataObject.getJSONArray("results");
        int numOfResults = resultArray.length();
        List<String[]> result = new ArrayList<>();
        // get number of entries per result based on dataType input
        int numOfEntries = MovieSortTypeItem.values().length;

        //parse result
        for(int i=0; i<numOfResults; i++) {

            JSONObject movie = resultArray.getJSONObject(i);
            String[] movieResult = new String[numOfEntries];

            for(int j=0; j<numOfEntries; j++){
                String resultString  = movie.getString(MovieSortTypeItem.values()[j].toString());
                movieResult[j] = resultString;
            }

            result.add(movieResult);
        }

        return result;
    }

    public static String[] GetMovieDetailsFromJson(String movieJsonData) throws JSONException{

        JSONObject movieDataObject = new JSONObject(movieJsonData);
        int numOfEntries = MovieDetailItem.values().length;
        String[] movieResult = new String[numOfEntries];

        for(int j=0; j<numOfEntries; j++){
            String resultString  = movieDataObject.getString(MovieDetailItem.values()[j].toString());
            movieResult[j] = resultString;
        }

        return movieResult;
    }

    public static List<String[]> GetMovieTrailersFromJson(String movieJsonData) throws  JSONException{
        JSONObject movieDataObject = new JSONObject(movieJsonData);
        JSONObject videosArray = movieDataObject.getJSONObject("videos");
        JSONArray resultArray = videosArray.getJSONArray("results");
        int numOfResults = resultArray.length();
        List<String[]> result = new ArrayList<>();
        // get number of entries per result based on dataType input
        int numOfEntries = MovieTrailerItem.values().length;

        //parse result
        for(int i=0; i<numOfResults; i++) {

            JSONObject movie = resultArray.getJSONObject(i);
            String[] trailerResult = new String[numOfEntries];

            for(int j=0; j<numOfEntries; j++){
                String resultString  = movie.getString(MovieTrailerItem.values()[j].toString());
                trailerResult[j] = resultString;
            }

            result.add(trailerResult);
        }

        return result ;
    }

    public static List<String[]> GetMovieReviewsResultFromJson(String movieJsonData) throws JSONException{
        JSONObject movieDataObject = new JSONObject(movieJsonData);
        JSONObject videosArray = movieDataObject.getJSONObject("reviews");
        JSONArray resultArray = videosArray.getJSONArray("results");
        int numOfResults = resultArray.length();
        List<String[]> result = new ArrayList<>();
        // get number of entries per result based on dataType input
        int numOfEntries = MovieReviewItem.values().length;

        //parse result
        for(int i=0; i<numOfResults; i++) {

            JSONObject movie = resultArray.getJSONObject(i);
            String[] reviewsResult = new String[numOfEntries];

            for(int j=0; j<numOfEntries; j++){
                String resultString  = movie.getString(MovieReviewItem.values()[j].toString());
                reviewsResult[j] = resultString;
            }

            result.add(reviewsResult);
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

    public static Uri GetTrailerUrlString(String key) throws MalformedURLException {
        final String YOUTUBE_BASE_URL = "https://www.youtube.com/watch";
        final String VIDEO_PARAM = "v";

        Uri uri = Uri.parse(YOUTUBE_BASE_URL).buildUpon()
                .appendQueryParameter(VIDEO_PARAM, key)
                .build();

        return uri;
    }
    // function from this stackoverflow post:
    //http://stackoverflow.com/questions/1560788/how-to-check-internet-access-on-android-inetaddress-never-times-out
    public static boolean IsInternetAvailable(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    public static List<String[]> getMovieThumbnailsFromFavoriteCursor(Cursor cursor){
        List<String[]> result = new ArrayList<>();
        if(cursor != null && cursor.moveToFirst()){
            do{
                String[] movieResult = new String[2];
                movieResult[0] = cursor.getString(cursor.getColumnIndex(MovieContract.FavoritesEntry.COLUMN_POSTER_FILE_PATH));
                movieResult[1] = cursor.getString(cursor.getColumnIndex(MovieContract.FavoritesEntry.COLUMN_MOVIE_ID));
                result.add(movieResult);
            }while(cursor.moveToNext());

        }

        return result;
    }

    public static String[] getMovieDetailsFromeFavoriteCursor(Cursor cursor){
        String[] results = new String[MovieDetailItem.values().length];

        if(cursor != null && cursor.moveToFirst()){
            results[0] = cursor.getString(cursor.getColumnIndex(MovieContract.FavoritesEntry.COLUMN_TITLE));
            results[1] = cursor.getString(cursor.getColumnIndex(MovieContract.FavoritesEntry.COLUMN_RELEASE_DATE));
            results[2] = cursor.getString(cursor.getColumnIndex(MovieContract.FavoritesEntry.COLUMN_POSTER_FILE_PATH));
            results[3] = cursor.getString(cursor.getColumnIndex(MovieContract.FavoritesEntry.COLUMN_VOTE_AVE));
            results[4] = cursor.getString(cursor.getColumnIndex(MovieContract.FavoritesEntry.COLUMN_PLOT));

        }

        return results;
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


    public static enum MovieSortTypeItem{
        poster_path,
        id
    }

    public static enum MovieDetailItem{
        title,
        release_date,
        poster_path,
        vote_average,
        overview
    }

    public static enum MovieTrailerItem{
        name,
        key
    }

    public static enum MovieReviewItem{
        author,
        content
    }

}

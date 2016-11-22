package com.amy.android.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

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
* This class is a Fragment that gets the movie data from themoviedb and display them to a GridView
*/
public class MovieThumbnailsFragment extends Fragment {

    private final static String LOG_TAG = MovieThumbnailsFragment.class.getName();

    private GridViewAdapter mThumbnailImageAdapter;

    public MovieThumbnailsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //inflate fragment layout
        View rootView = inflater.inflate(R.layout.fragment_movie_thumbnails, container, false);
        GridView thumbnailsGrid = (GridView) rootView.findViewById(R.id.thumbnail_gridview);
        //arrayAdapter
        mThumbnailImageAdapter = new GridViewAdapter(getActivity(),R.layout.grid_item_layout, new ArrayList());
        thumbnailsGrid.setAdapter(mThumbnailImageAdapter);
        thumbnailsGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent detailView = new Intent(getActivity(), MovieDetailActivity.class);
                detailView.putExtra( Intent.EXTRA_TEXT, mThumbnailImageAdapter.getItem(i));
                startActivity(detailView);
            }
        });
        //Gets movie data
        new FetchMoviesTask().execute();
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.thumbnail_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId())
        {
            case R.id.action_settings:
                Intent settings = new Intent(getActivity(), SettingsActivity.class);
                startActivity(settings);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        new FetchMoviesTask().execute();
    }

    public class FetchMoviesTask extends AsyncTask<Void, Void, List<String[]>> {

        @Override
        protected List<String[]> doInBackground(Void... voids) {

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String moviesJsonStr = null;
            // Page number to request from 1 to 1000
            // not currently taking into account
            int numPages = 1;

            try {
                final String FORECAST_BASE_URL = "http://api.themoviedb.org/3/movie";
                final String PAGE_PARAM = "page";
                final String APPID_PARAM = "api_key";

                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
                String sortType = preferences.getString(getString(R.string.pref_sort_by_key), getString(R.string.pref_sort_popular_value));

                Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                        .appendPath(sortType)
                        .appendQueryParameter(PAGE_PARAM, Integer.toString(numPages))
                        .appendQueryParameter(APPID_PARAM, BuildConfig.MOVIEDB_API_KEY)
                        .build();

                URL url = new URL(builtUri.toString());

                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
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
                List<String[]> movieDetails = Utility.GetMovieDetailFromJson(moviesJsonStr);
                return movieDetails;

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

        @Override
        protected void onPostExecute(List<String[]> strings) {
            super.onPostExecute(strings);
            if(strings != null) {
                List<String[]> resultArray = strings;
                //build thumbnails
                mThumbnailImageAdapter.clear();
                mThumbnailImageAdapter.addAll(resultArray);
            }
            else{
                Toast.makeText(getActivity(), "Please check your internet connection!", Toast.LENGTH_SHORT).show();
            }

        }

    }

}

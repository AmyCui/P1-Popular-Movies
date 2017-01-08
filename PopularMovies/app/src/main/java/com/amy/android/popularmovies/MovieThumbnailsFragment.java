package com.amy.android.popularmovies;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import com.amy.android.popularmovies.data.MovieContract;

import org.json.JSONException;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * This class is a Fragment that gets the movie data from themoviedb, or Favorites database and display them to a GridView
 */
public class MovieThumbnailsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Object> {

    //region constants
    private final static String LOG_TAG = MovieThumbnailsFragment.class.getName();
    private final static int MOVIES_LOADER = 100;
    private final static int FAVORITES_LOADER = 101;
    //endregion

    //region fields
    private GridViewAdapter mThumbnailImageAdapter;
    private String mSortType;
    public static boolean mIsInFavoritesMode = false;
    //endregion

    //region constructor
    public MovieThumbnailsFragment() {
        // Required empty public constructor
    }
    //endregion

    //region fragment overrides
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
                detailView.putExtra( Intent.EXTRA_TEXT, mThumbnailImageAdapter.getItem(i)[Utility.MovieSortTypeItem.id.ordinal()]);
                startActivity(detailView);
            }
        });

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        // check if user settings is in show favorites mode or not
        // then set the loader accordingly
        mIsInFavoritesMode = getFavoriteModeFromSetting();
        if(mIsInFavoritesMode){
            getLoaderManager().initLoader(FAVORITES_LOADER,null, this).forceLoad();
        }else{
            mSortType = getSortTypeFromSetting();
            getLoaderManager().initLoader(MOVIES_LOADER,null,this).forceLoad();
        }
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume() {

        if(getFavoriteModeFromSetting()) {
            // if user just changed from sort type mode to favorites mode, start the CursorLoader for Favorites database
            if(mIsInFavoritesMode != getFavoriteModeFromSetting()) {
                mIsInFavoritesMode = getFavoriteModeFromSetting();
                getLoaderManager().initLoader(FAVORITES_LOADER, null, this).forceLoad();
            }
        }else{
            if(mIsInFavoritesMode != getFavoriteModeFromSetting()){
                //if user just changed from the favorites mode to sort type mode,
                // starts the movies loader
                mIsInFavoritesMode = getFavoriteModeFromSetting();
                getLoaderManager().initLoader(MOVIES_LOADER, null, this).forceLoad();
            }else {
                //if user has been in sort type mode, check if user has changed the sort type setting
                String sortType = getSortTypeFromSetting();
                if (sortType != null && sortType != mSortType) {
                    mSortType = sortType;
                    onSortTypeChanged();
                }
            }
        }

        super.onResume();
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
                mIsInFavoritesMode = false;
                Intent settings = new Intent(getActivity(), SettingsActivity.class);
                startActivity(settings);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    //endregion

    //region loader callbacks
    @Override
    public Loader<Object> onCreateLoader(int id, Bundle args) {
        // for movies loader, start the MoviesResultLoader to query data from theMovieDb
        // for favorites loader, start a CursorLoader to query data from Favorites database
        switch (id){
            case MOVIES_LOADER:
                URL moviesUrl = null;
                try {
                    moviesUrl = buildSortTypeRequestURL();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                return new MoviesResultLoader(getActivity(),moviesUrl);

            case FAVORITES_LOADER:
                return (Loader)(new CursorLoader(getActivity(), MovieContract.FavoritesEntry.CONTENT_URI,null,null,null,null));

            default:
                Log.e(LOG_TAG, "Unknown loader id");
                return null;
        }
    }



    @Override
    public void onLoadFinished(Loader<Object> loader, Object data) {
        // for movies loader, parse the data from JSON format and set to view
        // for favorites loader, load data from Cursor and set to view
        if(loader.getId() == MOVIES_LOADER) {
            if (data != null) {
                try {
                    List<String[]> movieDetails = Utility.GetMovieSortTypeResultFromJson(data.toString());
                    setmThumbnailImageAdapter(movieDetails);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }else if(loader.getId() == FAVORITES_LOADER){
            Cursor resultCursor = (Cursor)data;
            List<String[]> favoriteMovies = Utility.getMovieThumbnailsFromFavoriteCursor(resultCursor);
            setmThumbnailImageAdapter(favoriteMovies);
        }
    }

    @Override
    public void onLoaderReset(Loader<Object> loader) {

    }
    //endregion

    //region private methods

    /**
     * restart the movies loader when sort type changes
     */
    private void onSortTypeChanged()
    {
        getLoaderManager().restartLoader(MOVIES_LOADER, null, this).forceLoad();
    }

    /**
     * get current sort type from setting
     * @return by popular, or by top-rated
     */
    private String getSortTypeFromSetting(){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String sortType = preferences.getString(getString(R.string.pref_sort_by_key), getString(R.string.pref_sort_popular_value));
        return sortType;
    }

    /**
     * get if is in show favorite mode or not from setting
     * @return
     */
    private boolean getFavoriteModeFromSetting(){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        boolean inFavoriteMode = preferences.getBoolean(getResources().getString(R.string.pref_favorite_key),false);
        return inFavoriteMode;
    }
    //endregion

    //region public methods

    /**
     * builds the theMovieDb http request url for movie id and poster_path
     * @return
     * @throws MalformedURLException
     */
    public URL buildSortTypeRequestURL() throws MalformedURLException {
        final String MOVIES_BASE_URL = "http://api.themoviedb.org/3/movie";
        final String PAGE_PARAM = "page";
        final String APPID_PARAM = "api_key";


        Uri builtUri = Uri.parse(MOVIES_BASE_URL).buildUpon()
                .appendPath(mSortType)
                .appendQueryParameter(APPID_PARAM, BuildConfig.MOVIEDB_API_KEY)
                .build();

        URL url = new URL(builtUri.toString());
        return url;
    }

    /**
     * set the thumbnail image to the current adapter
     * @param strings
     */
    public void setmThumbnailImageAdapter(List<String[]> strings)
    {
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
    //endregion



}

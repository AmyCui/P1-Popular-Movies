package com.amy.android.popularmovies;


import android.app.ActionBar;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;

import android.support.v4.content.CursorLoader;

import android.text.Layout;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.amy.android.popularmovies.data.MovieContract;
import com.squareup.picasso.Picasso;

import org.json.JSONException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


/*
* This class is the Fragment that contains movie detail content
 */
public class MovieDetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Object> {

    //region constants
    private static final String LOG_TAG = MovieDetailFragment.class.getName();
    private static final int DETAIL_LOADER = 200;
    private static final int FAVORITE_DETAL_LOADER = 201;
    //endregion

    //region view bindings
    @BindView(R.id.thumbnail_image) ImageView mImageView;
    @BindView(R.id.title_text) TextView mTitleText;
    @BindView(R.id.release_date_text) TextView mReleaseDateText;
    @BindView(R.id.vote_average_text) TextView mVoteAverageText;
    @BindView(R.id.synopsis_text) TextView mSynopsisText;
    @BindView(R.id.trailer_linearlayout) LinearLayout mTrailerRoot;
    @BindView(R.id.review_list) ListView mReviewList;
    @BindView(R.id.save_as_favorite_btn) ToggleButton mFavoriteBtn;
    //endregion

    //region fields
    private String mCurrentMovieId;
    private ReviewsListViewAdapater mReviewListAdapter;
    //endregion

    //region constructor
    public MovieDetailFragment() {
        // Required empty public constructor
    }
    //endregion

    //region fragment overrides
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_movie_detail, container, false);
        ButterKnife.bind(this, rootView);
        // Initiate list adapter for reviews listView
        mReviewListAdapter = new ReviewsListViewAdapater(getActivity(), R.layout.review_list_item_layout, new ArrayList());
        mReviewList.setAdapter(mReviewListAdapter);
        //setup save as favorite toggle button
        mFavoriteBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if(isChecked){
                    addToFavorites();
                }else{
                    removeFromFavorites();
                }
            }
        });
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        mCurrentMovieId = getActivity().getIntent().getStringExtra(Intent.EXTRA_TEXT);
        mFavoriteBtn.setChecked(movieIsAFavorite(mCurrentMovieId));
        // if no internet available but is a favorite, partial data can be retrieved from database
        if(!Utility.IsInternetAvailable(getContext()) && mFavoriteBtn.isChecked()){
            getLoaderManager().initLoader(FAVORITE_DETAL_LOADER,null, this).forceLoad();
        } else {
            getLoaderManager().initLoader(DETAIL_LOADER,null,this).forceLoad();
        }

        super.onActivityCreated(savedInstanceState);
    }

    //endregion

    //region LoaderManager callbacks
    @Override
    public Loader<Object> onCreateLoader(int id, Bundle args) {
        // if is the detail_loader, then use MovieResultLoader, and query data from internet
        if(id == DETAIL_LOADER) {
            URL detailsUrl = null;
            try {
                detailsUrl = buildDetailsRequestURL();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            return new MoviesResultLoader(getActivity(), detailsUrl);
        }
        // if is favorite_detail_loader, use a CursorLoader to query from Favorites database
        else if(id == FAVORITE_DETAL_LOADER){
            return (Loader)new CursorLoader(getActivity(), MovieContract.FavoritesEntry.CONTENT_URI, null, Utility.MovieWithIDSelection, new String[]{mCurrentMovieId},null);
        }
        // otherwise is unknown source. return null.
        else{
            return null;
        }

    }

    @Override
    public void onLoadFinished(Loader<Object> loader, Object data) {
        // if is from the detail_loader, returned result is a JSON string
        // process results accordingly and set to views
        if(loader.getId() == DETAIL_LOADER){

            try {
                //get movie details information: title, release date, rating, synopsis
                String[] movieDetails = Utility.GetMovieDetailsFromJson(data.toString());
                setMovieDetailsToView(movieDetails, false);
                List<String[]> movieTrailers = Utility.GetMovieTrailersFromJson(data.toString());
                setMovieTrailersToView(movieTrailers);
                List<String[]> movieReviews = Utility.GetMovieReviewsResultFromJson(data.toString());
                setMovieReviewsToView(movieReviews);
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }

        }
        // if is from favorite_detail_loader, then result is a Cursor
        // process results accordingly and set to views.
        else if(loader.getId() == FAVORITE_DETAL_LOADER){
            String[] movieDetails = Utility.getMovieDetailsFromeFavoriteCursor((Cursor)data);
            setMovieDetailsToView(movieDetails, true);
        }
    }

    @Override
    public void onLoaderReset(Loader<Object> loader) {

    }
    //endregion

    //region methods

    /**
     * This function returns a theMovieDB query url for movie details, reviews, and trailers.
     * @return
     * @throws MalformedURLException
     */
    public URL buildDetailsRequestURL() throws MalformedURLException {
        final String MOVIES_BASE_URL = "http://api.themoviedb.org/3/movie";
        final String APPID_PARAM = "api_key";
        final String APPEND_PARAM = "append_to_response";
        final String VIDEOS_PARAM = "videos";
        final String REVIEWS_PARAM = "reviews";

        Uri builtUri = Uri.parse(MOVIES_BASE_URL).buildUpon()
                .appendEncodedPath(mCurrentMovieId)
                .appendQueryParameter(APPID_PARAM, BuildConfig.MOVIEDB_API_KEY)
                .appendQueryParameter(APPEND_PARAM, VIDEOS_PARAM + "," + REVIEWS_PARAM)
                .build();

        URL url = null;
        try {
            url = new URL(URLDecoder.decode(builtUri.toString(), "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return url;
    }

    /**
     * This function sets the movie title, poster, rating, plot, release date to view
     * @param details the string array for details
     * @param isFromDatabase if result is coming from the Favorites database
     */
    private void setMovieDetailsToView(String[] details, boolean isFromDatabase){
        // Set movie detail data to view
        String posterPath = details[Utility.MovieDetailItem.poster_path.ordinal()];
        //if is from database, the poster path is the file path in external storage
        // otherwise need to build the url to load image online
        if(isFromDatabase) {
            mImageView.setImageBitmap(BitmapFactory.decodeFile(posterPath));
        } else {
            String posterUrl = Utility.GetThumbnailUrlString(posterPath, Utility.PosterSize.w500.toString());
            Picasso.with(getActivity()).load(posterUrl).into(mImageView);
        }
        mTitleText.setText(details[Utility.MovieDetailItem.title.ordinal()]);
        getActivity().setTitle(mTitleText.getText());
        mReleaseDateText.setText(details[Utility.MovieDetailItem.release_date.ordinal()]);
        mVoteAverageText.setText(details[Utility.MovieDetailItem.vote_average.ordinal()]);
        mSynopsisText.setText(details[Utility.MovieDetailItem.overview.ordinal()]);
    }

    /**
     * This function creates ui representation for each of the trailers for the input trailers data
     * @param trailers trailers list
     * @throws MalformedURLException
     */
    private void setMovieTrailersToView(List<String[]> trailers) throws MalformedURLException {
        for(int i=0;i<trailers.size();i++)
        {
            // extract title and video link
            String title = trailers.get(i)[Utility.MovieTrailerItem.name.ordinal()];
            final String videoKey = trailers.get(i)[Utility.MovieTrailerItem.key.ordinal()];
            final Uri videoUrl = Utility.GetTrailerUrlString(videoKey);

            // build a linear layout that contains an youtube icon and title text
            LinearLayout trailerView = new LinearLayout(getActivity());
            trailerView.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams viewParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
            viewParams.setMargins(10,0,10,0);
            trailerView.setLayoutParams(viewParams);
            ImageView icon = new ImageView(getActivity());
            icon.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.youtube));
            icon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {

                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube://" + videoKey));
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);

                    }catch(ActivityNotFoundException e) {

                        // youtube is not installed.Will be opened in other available apps
                        if(videoUrl != null) {
                            Intent i = new Intent(Intent.ACTION_VIEW, videoUrl);
                            startActivity(i);
                        }
                    }
                }
            });
            TextView text = new TextView(getActivity());
            text.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            int imWidth = getActivity().getResources().getDrawable(R.drawable.youtube).getIntrinsicWidth();
            LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(imWidth * 2, LinearLayout.LayoutParams.WRAP_CONTENT);
            textParams.setMargins(10,10,10,10);
            textParams.gravity = Gravity.CENTER_HORIZONTAL;
            text.setLayoutParams(textParams);
            text.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            text.setText(title);
            trailerView.addView(icon);
            trailerView.addView(text);
            //add new view to root linearlayout
            if(mTrailerRoot != null)
                mTrailerRoot.addView(trailerView);

        }
    }

    /**
     * This function sets the reviews result to a review listview
     * @param reviews
     */
    private void setMovieReviewsToView(List<String[]> reviews){

        if(reviews.size() == 0){
            String[] emptyReview = {"None",""};
            ArrayList<String[]> emptyList = new ArrayList<String[]>();
            emptyList.add(emptyReview);
            mReviewListAdapter.clear();
            mReviewListAdapter.addAll(emptyList);
        }else{
            mReviewListAdapter.clear();
            mReviewListAdapter.addAll(reviews);
        }
    }
    //endregion

    //region methods

    /**
     * Add current movie to the Favorites database
     */
    private void addToFavorites(){
        if(!movieIsAFavorite(mCurrentMovieId)) {
            ContentValues movieValues = new ContentValues();
            movieValues.put(MovieContract.FavoritesEntry.COLUMN_MOVIE_ID, mCurrentMovieId);
            movieValues.put(MovieContract.FavoritesEntry.COLUMN_TITLE, mTitleText.getText().toString());
            String path = saveMoviePoster();
            movieValues.put(MovieContract.FavoritesEntry.COLUMN_POSTER_FILE_PATH, path);
            movieValues.put(MovieContract.FavoritesEntry.COLUMN_RELEASE_DATE, mReleaseDateText.getText().toString());
            movieValues.put(MovieContract.FavoritesEntry.COLUMN_PLOT, mSynopsisText.getText().toString());
            movieValues.put(MovieContract.FavoritesEntry.COLUMN_VOTE_AVE, mVoteAverageText.getText().toString());

            Uri row = getActivity().getContentResolver().insert(MovieContract.FavoritesEntry.CONTENT_URI, movieValues);
        }
    }

    /**
     * Remove current movie from the Favorites database
     */
    private void removeFromFavorites(){
            int rowsDeleted = getActivity().getContentResolver().delete(MovieContract.FavoritesEntry.buildMovieWithIDUri(mCurrentMovieId),null, new String[]{mCurrentMovieId});
    }

    /**
     * Save poster file and return file path
     * @return
     */
    private String saveMoviePoster(){
        Bitmap image = ((BitmapDrawable)mImageView.getDrawable()).getBitmap();
        File pictureFile = getOutputMediaFile(mTitleText.getText().toString());
        String filePath = "";
        if(!pictureFile.exists()) {

            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                image.compress(Bitmap.CompressFormat.JPEG, 90, fos);
                fos.close();
                filePath = pictureFile.getPath();
            } catch (FileNotFoundException e) {
                Log.d(LOG_TAG, "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.d(LOG_TAG, "Error accessing file: " + e.getMessage());
            }
        }
        else
            filePath = pictureFile.getPath();
        return filePath;
    }

    /**
     * Create a File for saving an image or video
     * @param filename
     * @return
     */
    private  File getOutputMediaFile(String filename){
        File mediaFile;
        String mImageName= filename +".jpg";
        mediaFile = new File(getActivity().getExternalFilesDir(null), mImageName);
        return mediaFile;
    }

    /**
     * Checks if a movie is already in the Favorites database
     * @param id movie id
     * @return
     */
    private boolean movieIsAFavorite(String id){
        Cursor result = getActivity().getContentResolver().query(
                MovieContract.FavoritesEntry.buildMovieWithIDUri(id),
                null,
                null,
                new String[]{id},
                null
        );
        if(result.moveToFirst())
            return true;

        return false;
    }
    //endregion
}

package com.amy.android.popularmovies;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import butterknife.BindView;

/*
* This class is the Fragment that contains movie detail content
 */
public class MovieDetailFragment extends Fragment {


    @BindView(R.id.thumbnail_image) ImageView mImageView;
    @BindView(R.id.title_text) TextView mTitleText;
    @BindView(R.id.release_date_text) TextView mReleaseDateText;
    @BindView(R.id.vote_average_text) TextView mVoteAverageText;
    @BindView(R.id.synopsis_text) TextView mSynopsisText;

    public MovieDetailFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_movie_detail, container, false);


        // Get movie detail data from Intent
        String[] movieData = getActivity().getIntent().getStringArrayExtra(Intent.EXTRA_TEXT);

        // Set movie detail data to view
        String posterPath = Utility.GetThumbnailUrlString(movieData[Utility.MovieDetailItem.poster_path.ordinal()],Utility.PosterSize.w500.toString());
        Picasso.with(getActivity()).load(posterPath).into(mImageView);
        mTitleText.setText(movieData[Utility.MovieDetailItem.title.ordinal()]);
        mReleaseDateText.setText(movieData[Utility.MovieDetailItem.release_date.ordinal()]);
        mVoteAverageText.setText(movieData[Utility.MovieDetailItem.vote_average.ordinal()]);
        mSynopsisText.setText(movieData[Utility.MovieDetailItem.overview.ordinal()]);

        return rootView;
    }

}

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
/*
* This class is the Fragment that contains movie detail content
 */
public class MovieDetailFragment extends Fragment {


    public MovieDetailFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_movie_detail, container, false);

        // Find view objects
        ImageView imageView = (ImageView)rootView.findViewById(R.id.thumbnail_image);
        TextView titleText = (TextView)rootView.findViewById(R.id.title_text);
        TextView releaseDateText = (TextView)rootView.findViewById(R.id.release_date_text);
        TextView voteAverageText = (TextView)rootView.findViewById(R.id.vote_average_text);
        TextView synopsisText = (TextView)rootView.findViewById(R.id.synopsis_text);

        // Get movie detail data from Intent
        String[] movieData = getActivity().getIntent().getStringArrayExtra(Intent.EXTRA_TEXT);

        // Set movie detail data to view
        String posterPath = Utility.GetThumbnailUrlString(movieData[Utility.MovieDetailItem.poster_path.ordinal()],Utility.PosterSize.w500.toString());
        Picasso.with(getActivity()).load(posterPath).into(imageView);
        titleText.setText(movieData[Utility.MovieDetailItem.title.ordinal()]);
        releaseDateText.setText(movieData[Utility.MovieDetailItem.release_date.ordinal()]);
        voteAverageText.setText(movieData[Utility.MovieDetailItem.vote_average.ordinal()]);
        synopsisText.setText(movieData[Utility.MovieDetailItem.overview.ordinal()]);

        return rootView;
    }

}

package com.amy.android.popularmovies;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
/*
* This class is a custom GridView ArrayAdapter that gets/sets ImageView tot he GridView
*/
public class GridViewAdapter extends ArrayAdapter {

    private Context context;
    private int layoutResourceId;
    private ArrayList data = new ArrayList();

    public GridViewAdapter(Context context, int layoutResourceId, ArrayList data) {
        super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        View row = view;
        ImageView image;

        if (row == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);
        }

        image = (ImageView) row.findViewById(R.id.image);
        if(data != null) {
            String thumbnailPath = ((String[])data.get(position))[Utility.MovieSortTypeItem.poster_path.ordinal()];
            //when is in favorite mode, can just load the thumbnail from local.
            //otherwise query from internet
            if(MovieThumbnailsFragment.mIsInFavoritesMode){
                image.setImageBitmap(BitmapFactory.decodeFile(thumbnailPath));
            }else{
                String thumbnailUrl = Utility.GetThumbnailUrlString(thumbnailPath, Utility.PosterSize.w185.toString());
                Picasso.with(this.context).load(thumbnailUrl).error(R.drawable.defaul_image).into(image);
            }
        }
        return row;
    }

    public void SetData(ArrayList newdata)
    {
        this.data.clear();
        this.data = newdata;
    }

    @Override
    public void clear() {
        super.clear();
        this.data.clear();
    }

    @Override
    public void addAll(Object[] items) {
        super.addAll(items);
        this.data = new ArrayList();
        for(int i=0; i< items.length; i++)
        {
            this.data.add(i, items[i]);
        }
    }

    @Nullable
    @Override
    public String[] getItem(int position) {
        return (String[])data.get(position);
    }
}



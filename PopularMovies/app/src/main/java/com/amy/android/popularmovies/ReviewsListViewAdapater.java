package com.amy.android.popularmovies;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ReviewsListViewAdapater extends ArrayAdapter{


    private Context context;
    private int layoutResourceId;
    private ArrayList data = new ArrayList();

    public ReviewsListViewAdapater(Context context, int layoutResourceId, ArrayList data){
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

        ViewHolder holder = new ViewHolder(row);

        if(data != null) {
            holder.setAuthorText(((String[])data.get(position))[Utility.MovieReviewItem.author.ordinal()]);
            holder.setContentText(((String[])data.get(position))[Utility.MovieReviewItem.content.ordinal()]);
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

    static class ViewHolder{
        @BindView(R.id.author_text) TextView mAuthorText;
        @BindView(R.id.content_text) TextView mContentText;

        public ViewHolder(View view){
            ButterKnife.bind(this, view);
        }

        public void setAuthorText(String author){
            mAuthorText.setText(author);
        }

        public void setContentText(String content){
            mContentText.setText(content);
        }
    }

}

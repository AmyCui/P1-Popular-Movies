package com.amy.android.popularmovies.data;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.amy.android.popularmovies.data.MovieContract.FavoritesEntry;


public class MovieDBHelper extends SQLiteOpenHelper{

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "favorites.db";

    public MovieDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        // Create favorites table
        final String SQL_CREATE_FAVORITES_TABLE = "CREATE TABLE " + FavoritesEntry.TABLE_NAME + " (" +
                FavoritesEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                FavoritesEntry.COLUMN_MOVIE_ID + " INTEGER UNIQUE NOT NULL," +
                FavoritesEntry.COLUMN_TITLE + " TEXT NOT NULL," +
                FavoritesEntry.COLUMN_POSTER_FILE_PATH + " TEXT NOT NULL," +
                FavoritesEntry.COLUMN_RELEASE_DATE + " TEXT NOT NULL," +
                FavoritesEntry.COLUMN_VOTE_AVE + " REAL NOT NULL," +
                FavoritesEntry.COLUMN_PLOT + " TEXT NOT NULL" +
                ")";

        sqLiteDatabase.execSQL(SQL_CREATE_FAVORITES_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        // Re-create database on upgrade
        onCreate(sqLiteDatabase);
    }
}

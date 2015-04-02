package com.eccoTCPAsync.app;

/**
 * Created by cmcooney on 7/24/14.
 */

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;

public class AddBookmark {
    private final static String TAG = "AddBookmark";
    private final Context context;
    private final DB_Helper dbHelper;
    public SQLiteDatabase bookmarkDB;
    private static String DATABASE_TABLE = "bookmarks";
    public String uri_authority = "condorcet.uchicago.edu";
    public String philo_dir = "philologic4";
    public String build_name = "ecco_tcp_demo";

    public AddBookmark(Context context){
        this.context = context;
        dbHelper = new DB_Helper(context);
    }

    public AddBookmark createDataBase() throws SQLException {

        try {
            dbHelper.createDatabase();
            Log.i(TAG, " database created....");
        }
        catch (IOException mIOException) {
            Log.e(TAG, mIOException.toString() + " unable to create DB!");
            throw new Error("Unable to create Database!");
        }
        return this;
    }

    public AddBookmark open() throws SQLException {
        try {
            dbHelper.openDataBase();

            //dbHelper.close();
            //bookmarkDB = dbHelper.getReadableDatabase();
            Log.i(TAG, " BookmarkDB open worked!");
        }
        catch (SQLException mSQLException) {
            Log.e(TAG, "open >>"+ mSQLException.toString());
            throw mSQLException;
        }
        return this;
    }

    public void close() {
        Log.i(TAG, " Your DB is closed...");
        dbHelper.close();
    }

    public AddBookmark addBookmarkItem(String bookmarkPhiloId, String bookmarkShrtCit) throws SQLException {
        bookmarkDB = dbHelper.openDataBase();
        Log.i(TAG, "Got your items: " + bookmarkPhiloId + " " + bookmarkShrtCit);
        try {
            String UPDATE_BOOKMARKS_TABLE = "INSERT INTO " + DATABASE_TABLE + " (philoID, bookmarkName,) VALUES (?,?),"
                    + bookmarkPhiloId + ", " + bookmarkShrtCit + ");";
            Log.i(TAG, " Your update command: " + UPDATE_BOOKMARKS_TABLE);


            String[] values = {bookmarkPhiloId, bookmarkShrtCit};
            //bookmarkDB.execSQL(UPDATE_BOOKMARKS_TABLE);
            bookmarkDB.execSQL("INSERT into bookmarks (philoID, bookmarkName) VALUES (?,?)", values);
            Log.i(TAG, " DB update probably worked.");
            Toast.makeText(context.getApplicationContext(), "Bookmarking " + bookmarkShrtCit, Toast.LENGTH_SHORT).show();
        }
        catch (SQLException mSQLException){
            Toast.makeText(context.getApplicationContext(), bookmarkShrtCit + " already bookmarked", Toast.LENGTH_SHORT).show();
            Log.i(TAG, " Duplicate bookmark. User appropriately scolded.");
            //throw mSQLException;
        }
        bookmarkDB.close();
        return this;
    }

    public Cursor showBookmarkItems(){
        bookmarkDB = dbHelper.openDataBase();
        Cursor cursor = bookmarkDB.rawQuery("SELECT * from " + DATABASE_TABLE, null);
        if (cursor == null){
            return null;
        }
        else if (!cursor.moveToFirst()){
            cursor.close();
            return null;
        }
        Log.i(TAG, " Your bookmarks: " + cursor.toString());
        bookmarkDB.close();
        return cursor;
    }

    public String getBookmarkedText(String bookmark_to_get) {
        bookmarkDB = dbHelper.openDataBase();
        String[] bookmark_query_vals = {bookmark_to_get};
        Cursor cursor = bookmarkDB.rawQuery("SELECT philoID from " + DATABASE_TABLE + " where bookmarkName =? ",  bookmark_query_vals);
        if (cursor == null) {
            return null;
        }
        else if (!cursor.moveToFirst()) {
            cursor.close();
            return null;
        }
        String[] bookmark_philoID = cursor.getString(0).split("/");
        Log.i(TAG, " Your bookmark philoid: " + bookmark_philoID);
        Uri.Builder builder = new Uri.Builder();
        builder.scheme("http").authority(uri_authority)
                .appendPath(philo_dir).appendPath(build_name).appendPath("dispatcher.py")
                .appendPath(bookmark_philoID[0]).appendPath(bookmark_philoID[1]).appendPath(bookmark_philoID[2])
                .appendQueryParameter("format", "json");
        String bookmark_uri = builder.toString();
        Log.i(TAG, " Your bookmark URI: " + bookmark_uri);
        bookmarkDB.close();
        return bookmark_uri;
    }

    public void deleteBookmark(String bookmark_to_delete) {
        bookmarkDB = dbHelper.openDataBase();
        String[] bookmark_delete_val = {bookmark_to_delete};
        bookmarkDB.execSQL("DELETE FROM " + DATABASE_TABLE + " where bookmarkName =? ", bookmark_delete_val);
        Log.i(TAG, " I do believe your record was deleted.");
        bookmarkDB.close();
    }
}

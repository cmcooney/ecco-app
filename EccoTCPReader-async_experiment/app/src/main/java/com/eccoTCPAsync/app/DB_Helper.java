package com.eccoTCPAsync.app;

import android.database.sqlite.SQLiteOpenHelper;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.SQLException;

import android.util.Log;

import java.io.IOException;

/**
 * Created by cmcooney on 7/24/14.
 */
public class DB_Helper extends SQLiteOpenHelper{

    private static final String TAG = "DB_Helper";
    private static String DB_PATH = "/data/data/com.eccoTCPAsync.app/databases/";
    private static String DATABASE_NAME = "eccotcp_bookmarks.sqlite";
    private static String DATABASE_TABLE = "bookmarks";
    private static String KEY_ID = "id";
    private static String philoID = "philoID";
    private static String bookmarkName = "bookmarkName";
    private String CREATE_BOOKMARKS_TABLE = "Create TABLE " + DATABASE_TABLE + " (" +
            //PRIMARY KEY (" + philoID + "), " +
            philoID + " TEXT PRIMARY KEY, " +
            bookmarkName + " TEXT);";
            //KEY_ID + " TEXT PRIMARY KEY);";
    private static final int DATABASE_VERSION = 1;
    public SQLiteDatabase bookmarkDB;
    public Context context;


    public DB_Helper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
        Log.i(TAG, " In DB_Helper!");
    }

    private boolean checkDataBase(){

        Log.i(TAG, " Checking database.");
        SQLiteDatabase checkDB = null;

        try {
            String myPath = DB_PATH + DATABASE_NAME;
            Log.i(TAG, "Looking in: " + myPath);
            checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READWRITE);
        }

        catch(SQLiteException e) {
        }

        if (checkDB !=null) {
            checkDB.close();
        }

        return checkDB != null ? true : false;

    }

    public void createDatabase() throws IOException {
        boolean dbExist = checkDataBase();

        if (dbExist){
            Log.i(TAG, "We got a bookmarks db...");
            }
        else {
            bookmarkDB = context.openOrCreateDatabase(DATABASE_NAME, DATABASE_VERSION, null);
            Log.i(TAG + " DB Create Command: ", CREATE_BOOKMARKS_TABLE + " DB: " + bookmarkDB.toString());
            bookmarkDB.execSQL(CREATE_BOOKMARKS_TABLE);
            Log.i(TAG, " Your table is created...");
        }

    }
    public SQLiteDatabase openDataBase() throws SQLException {
        Log.i(TAG, " Opening DB.");
        String myPath = DB_PATH + DATABASE_NAME;
        return bookmarkDB = SQLiteDatabase.openDatabase(myPath,  null, SQLiteDatabase.OPEN_READWRITE);
    }

    @Override
    public synchronized void close() {

        if(bookmarkDB != null) {
            bookmarkDB.close();
            Log.i(TAG, " DB closed.");
        }
        super.close();
    }

    @Override

    public void onCreate(SQLiteDatabase db) {
        /**
         * don't need?
         db.execSQL(DATABASE_CREATE);
         */
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        /**
         * don't need?
         db.execSQL("drop table if exists " + TABLE_NAME);
         onCreate(db);
         */
    }

}

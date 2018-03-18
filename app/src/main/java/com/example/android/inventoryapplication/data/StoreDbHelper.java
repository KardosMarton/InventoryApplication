package com.example.android.inventoryapplication.data;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


public class StoreDbHelper extends SQLiteOpenHelper {

    public static final String LOG_TAG = StoreDbHelper.class.getSimpleName();

    /**
     * Name of the database file
     */
    private static final String DATABASE_NAME = "inventory.db";

    /**
     * Database version
     */
    private static final int DATABASE_VERSION = 1;


    public StoreDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        // Create a String that contains the SQL statement to create the activities table
        String SQL_CREATE_ACTIVITIES_TABLE = "CREATE TABLE " + StoreContract.StoreEntry.TABLE_NAME + " ("
                + StoreContract.StoreEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + StoreContract.StoreEntry.COLUMN_NAME + " TEXT NOT NULL, "
                + StoreContract.StoreEntry.COLUMN_TYPE + " INTEGER NOT NULL DEFAULT 0,"
                + StoreContract.StoreEntry.COLUMN_PRICE + " INTEGER NOT NULL, "
                + StoreContract.StoreEntry.COLUMN_QUANTITY + " INTEGER DEFAULT 0,"
                + StoreContract.StoreEntry.COLUMN_IMAGE + " TEXT, "
                + StoreContract.StoreEntry.COLUMN_SUPPLIER_NAME + " TEXT, "
                + StoreContract.StoreEntry.COLUMN_SUPPLIER_EMAIL + " TEXT );";

        //Create the database
        db.execSQL(SQL_CREATE_ACTIVITIES_TABLE);

        Log.i(LOG_TAG, SQL_CREATE_ACTIVITIES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("DROP TABLE IF EXISTS " + StoreContract.StoreEntry.TABLE_NAME);
        onCreate(db);
    }
}

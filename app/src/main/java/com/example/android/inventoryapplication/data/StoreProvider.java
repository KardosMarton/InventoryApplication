package com.example.android.inventoryapplication.data;


import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

public class StoreProvider extends ContentProvider {

    /** Tag for the log messages */
    public static final String LOG_TAG = StoreProvider.class.getSimpleName();

    /** URI matcher code for the content URI for the PRODUCTS table */
    private static final int PRODUCTS = 100;

    /** URI matcher code for the content URI for a single item in the PRODUCTS table */
    private static final int PRODUCT_ID = 101;

    /** UriMatcher object to match a content URI to a corresponding code */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    /** Static initializer - run the first time anything is called from this class */
    static {
        // This URI is used to provide access to multiple table rows.
        sUriMatcher.addURI(StoreContract.CONTENT_AUTHORITY, StoreContract.PATH_ITEMS, PRODUCTS);

        // This is used to provide access to single row of the table.
        sUriMatcher.addURI(StoreContract.CONTENT_AUTHORITY, StoreContract.PATH_ITEMS + "/#", PRODUCT_ID);
    }

    /** Database Helper Object */
    private StoreDbHelper mDbHelper;

    @Override
    public boolean onCreate() {
        mDbHelper = new StoreDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        // Get instance of readable database
        SQLiteDatabase sqLiteDBReadable = mDbHelper.getReadableDatabase();

        // Cursor to hold the query result
        Cursor cursor;

        // Check if the uri matches to a specific URI CODE
        int match =  sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                cursor =  sqLiteDBReadable.query(StoreContract.StoreEntry.TABLE_NAME,
                        projection, selection, selectionArgs, null, null, sortOrder);
                break;

            case PRODUCT_ID:
                selection = StoreContract.StoreEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri))};
                cursor =  sqLiteDBReadable.query(StoreContract.StoreEntry.TABLE_NAME,
                        projection, selection, selectionArgs, null, null, sortOrder);
                break;

            default:
                throw new IllegalArgumentException("Cannot convert URI " +uri);
        }

        // Set notification URI on Cursor so it knows when to update in the event the data in cursor changes
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                return insertProduct(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion not supported for " +uri);
        }
    }

    private Uri insertProduct(Uri uri, ContentValues values){

        // Check that the required values: can't be null or empty
        String name = values.getAsString(StoreContract.StoreEntry.COLUMN_NAME);
        if (name == null || name.trim().length() == 0){
            throw new IllegalArgumentException("Name is not valid!");
        }

        Integer price = values.getAsInteger(StoreContract.StoreEntry.COLUMN_PRICE);
        if(price == null || price < 0 ){
            throw new IllegalArgumentException("Not valid price!");
        }

        Integer quantity = values.getAsInteger(StoreContract.StoreEntry.COLUMN_QUANTITY);
        if(quantity == null || quantity < 0){
            throw new IllegalArgumentException("Not valid quantity!");
        }

        Integer type = values.getAsInteger(StoreContract.StoreEntry.COLUMN_TYPE);
        if(type == null || !StoreContract.StoreEntry.isValidType(type)){
            throw new IllegalArgumentException("Not valid type");
        }

        //Other attributes can be null, the image check defined elsewhere

        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Insert the new item with the given values
        long id = database.insert(StoreContract.StoreEntry.TABLE_NAME, null, values);

        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        // Notify all listeners that the data has changed for the item content URI
        getContext().getContentResolver().notifyChange(uri, null);

        // Return the new URI with the ID (of the newly inserted row) appended at the end
        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                return updateProduct(uri, contentValues, selection, selectionArgs);
            case PRODUCT_ID:
                selection = StoreContract.StoreEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                return updateProduct(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    public int updateProduct(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        if(values.containsKey(StoreContract.StoreEntry.COLUMN_NAME)){
            String name = values.getAsString(StoreContract.StoreEntry.COLUMN_NAME);
            if (name == null || name.trim().length() == 0){
                throw new IllegalArgumentException("Name is not valid!");
            }
        }

        if(values.containsKey(StoreContract.StoreEntry.COLUMN_PRICE)){
            Integer price = values.getAsInteger(StoreContract.StoreEntry.COLUMN_PRICE);
            if(price == null || price < 0 ){
                throw new IllegalArgumentException("Not valid price!");
            }
        }

        if(values.containsKey(StoreContract.StoreEntry.COLUMN_QUANTITY)){
            Integer quantity = values.getAsInteger(StoreContract.StoreEntry.COLUMN_QUANTITY);
            if(quantity == null || quantity < 0){
                throw new IllegalArgumentException("Not valid quantity!");
            }
        }

        if(values.containsKey(StoreContract.StoreEntry.COLUMN_TYPE)){
            Integer type = values.getAsInteger(StoreContract.StoreEntry.COLUMN_TYPE);
            if(type == null || !StoreContract.StoreEntry.isValidType(type)){
                throw new IllegalArgumentException("Not valid type");
            }
        }

        // If there are no values to update, then don't try to update the database
        if (values.size() == 0) {
            return 0;
        }

        // Otherwise, get writeable database to update the data
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Perform the update on the database and get the number of rows affected
        int rowsUpdated =database.update(StoreContract.StoreEntry.TABLE_NAME, values, selection, selectionArgs);

        // If 1 or more rows were updated, then notify all listeners that the data at the
        // given URI has changed
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        // Return the number of rows updated
        return rowsUpdated;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Track the number of rows that were deleted
        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                // Delete all rows that match the selection and selection args
                rowsDeleted = database.delete(StoreContract.StoreEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case PRODUCT_ID:
                // Delete a single row given by the ID in the URI
                selection = StoreContract.StoreEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(StoreContract.StoreEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

        // If 1 or more rows were deleted, then notify all listeners that the data at the
        // given URI has changed
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        // Return the number of rows deleted
        return rowsDeleted;
    }

    @Override
    public String getType(Uri uri) {

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                return StoreContract.StoreEntry.CONTENT_LIST_TYPE;
            case PRODUCT_ID:
                return StoreContract.StoreEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }
}

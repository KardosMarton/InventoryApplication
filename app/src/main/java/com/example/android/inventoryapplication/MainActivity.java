package com.example.android.inventoryapplication;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventoryapplication.data.StoreContract.StoreEntry;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String LOG_TAG = MainActivity.class.getName();
    private static final int PRODUCT_LOADER = 0;
    private ProductCursorAdapter mCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //UI components:
        ImageButton btnAddItem = (ImageButton) findViewById(R.id.img_main_add_item);
        TextView txtTitle = (TextView) findViewById(R.id.txt_main_empty_title);
        TextView txtMessage = (TextView) findViewById(R.id.txt_main_empty_message);

        //On click listener for the add button
        btnAddItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        //Listview
        ListView listView = (ListView) findViewById(R.id.list);

        //Shows the empty message when the list is empty
        View emptyView = findViewById(R.id.empty_main_view_holder);
        listView.setEmptyView(emptyView);

        // Setup an Adapter to create a list item for each row of pet in the Cursor.
        mCursorAdapter = new ProductCursorAdapter(this, null);
        listView.setAdapter(mCursorAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Intent newIntent = new Intent(MainActivity.this, EditorActivity.class);

                Uri currentItemUri = ContentUris.withAppendedId(StoreEntry.CONTENT_URI, id);

                newIntent.setData(currentItemUri);

                startActivity(newIntent);
            }
        });

        // Kick off the loader
        getLoaderManager().initLoader(PRODUCT_LOADER, null, this);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all:
                showDeleteConfirmationDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.main_dialog_delete);

        builder.setPositiveButton(R.string.main_dialog_yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete all" button, confirming deleting all products
                deleteAll();
            }
        });
        builder.setNegativeButton(R.string.main_dialog_no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }


    private void deleteAll() {
        int rowsDeleted = getContentResolver().delete(StoreEntry.CONTENT_URI, null, null);
        if (rowsDeleted > 0) {
            Toast.makeText(MainActivity.this, getString(R.string.main_delete_all_product),
                    Toast.LENGTH_SHORT).show();
        } else {
            Log.e(LOG_TAG, getString(R.string.main_error_delete_all));
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        //Define projection that specifies the columns we want to show
        String[] projection = {
                StoreEntry._ID,
                StoreEntry.COLUMN_NAME,
                StoreEntry.COLUMN_TYPE,
                StoreEntry.COLUMN_PRICE,
                StoreEntry.COLUMN_QUANTITY};

        //This will execute the ContentProvider's query method on background thread
        return new CursorLoader(this,           // Parent activity context
                StoreEntry.CONTENT_URI,    // Provider content URI to query
                projection,                     // Columns to include in the resulting Cursor
                null,                           // No selection clause
                null,                           // No selection arguments
                null);                          // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        //Update ItemCursorAdapter with this new cursor containing update item data
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        //Callback called when the data needs to be deleted
        mCursorAdapter.swapCursor(null);
    }
}

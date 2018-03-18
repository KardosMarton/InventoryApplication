package com.example.android.inventoryapplication;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventoryapplication.data.StoreContract;
import com.example.android.inventoryapplication.data.StoreContract.StoreEntry;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String LOG_TAG = EditorActivity.class.getName();

    private static final int IMAGE_REQUEST_CODE = 0;
    private static final int EXISTING_ITEM_LOADER = 0;
    private static final String STATE_IMAGE_URI = "STATE_IMAGE_URI";
    final Context mContext = this;
    //Components
    TextView lblItemName;
    TextView txtItemName;
    TextView lblitemType;
    Spinner mSpinnerType;
    TextView lblItemPrice;
    TextView txtItemPrice;
    TextView lblItemQuantity;
    TextView txtItemQuantity;
    Button btnAddQuantity;
    Button btnMinusQuantity;
    TextView lblSupplierName;
    TextView txtSupplierName;
    TextView lblSupplierMail;
    TextView txtSupplierMail;
    Button btnAddImage;
    Button btnOrderItem;
    ImageView imgItemImage;
    TextView missingImage;
    private Uri mCurrentProductUri;
    private Uri mImageUri;

    //The possible values for type:
    //TYPE_DAIRY_PROD = 1 , TYPE_BUTCHERS_MEAT = 2, TYPE_SPICES = 3,
    //TYPE_SWEETS = 4, TYPES_SNACKSANDCANDIES = 5
    private int mType = StoreEntry.TYPE_UNKNOWN;

    private boolean mItemHasChanged = false;

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mItemHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // Initialize all UI components
        uiComponents();

        Intent intent = getIntent();
        mCurrentProductUri = intent.getData();

        if (mCurrentProductUri == null) {
            setTitle(getTitle() + getString(R.string.title_editor_add));
            //The increase/decrease and order buttons are hidden for the new items
            btnAddQuantity.setVisibility(View.GONE);
            btnMinusQuantity.setVisibility(View.GONE);
            btnOrderItem.setVisibility(View.GONE);
        } else {
            setTitle(getTitle() + getString(R.string.title_editor_edit));
            getLoaderManager().initLoader(EXISTING_ITEM_LOADER, null, this);
            //The increase/decrease and order buttons are visible for the existing items
            btnAddQuantity.setVisibility(View.VISIBLE);
            btnMinusQuantity.setVisibility(View.VISIBLE);
            btnOrderItem.setVisibility(View.VISIBLE);
        }

        setupSpinner();

        if (mCurrentProductUri == null) {
            btnAddImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    btnImageClick();
                }
            });
        } else {
            btnAddImage.setVisibility(View.GONE);
        }
    }

    /**
     * Method to select a picture from device's media storage
     */
    private void btnImageClick() {
        Intent intent;

        if (Build.VERSION.SDK_INT < 19) {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
        } else {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
        }
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, getString(R.string.select_picture)), IMAGE_REQUEST_CODE);
    }

    private void setupSpinner() {
        // Create adapter for spinner. The list options are from the String array it will use
        // the spinner will use the default layout
        ArrayAdapter typeSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_type_option, android.R.layout.simple_spinner_item);

        // Specify dropdown layout style - simple list view with 1 item per line
        typeSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_expandable_list_item_1);

        // Apply the adapter to the spinner
        mSpinnerType.setAdapter(typeSpinnerAdapter);

        // Set the integer mSelected to the constant values
        mSpinnerType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.type_butchers_meat))) {
                        mType = StoreEntry.TYPE_BUTCHERS_MEAT;
                    } else if (selection.equals(getString(R.string.type_dairy_prod))) {
                        mType = StoreEntry.TYPE_DAIRY_PROD;
                    } else if (selection.equals(getString(R.string.type_snacks_and_candies))) {
                        mType = StoreEntry.TYPES_SNACKSANDCANDIES;
                    } else if (selection.equals(getString(R.string.type_species))) {
                        mType = StoreEntry.TYPE_SPICES;
                    } else if (selection.equals(getString(R.string.type_sweets))) {
                        mType = StoreEntry.TYPE_SWEETS;
                    } else {
                        mType = StoreEntry.TYPE_UNKNOWN;
                    }
                }
            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mType = StoreEntry.TYPE_UNKNOWN;
            }
        });
    }

    public void uiComponents() {
        lblItemName = (TextView) findViewById(R.id.lbl_editor_name);
        txtItemName = (TextView) findViewById(R.id.etxt_editor_name);
        lblitemType = (TextView) findViewById(R.id.lbl_editor_type);
        mSpinnerType = (Spinner) findViewById(R.id.spinner_type);
        lblItemPrice = (TextView) findViewById(R.id.lbl_editor_price);
        txtItemPrice = (TextView) findViewById(R.id.etxt_editor_price);
        lblItemQuantity = (TextView) findViewById(R.id.lbl_editor_quantity);
        txtItemQuantity = (TextView) findViewById(R.id.etxt_editor_quantity);
        btnAddQuantity = (Button) findViewById(R.id.btn_editor_plus_quantity);
        btnMinusQuantity = (Button) findViewById(R.id.btn_editor_decrease_quantity);
        lblSupplierName = (TextView) findViewById(R.id.lbl_editor_supplier_name);
        txtSupplierName = (TextView) findViewById(R.id.etxt_editor_supplier_name);
        lblSupplierMail = (TextView) findViewById(R.id.lbl_editor_supplier_email);
        txtSupplierMail = (TextView) findViewById(R.id.etxt_editor_supplier_email);
        btnAddImage = (Button) findViewById(R.id.btn_editor_add_image);
        btnOrderItem = (Button) findViewById(R.id.btn_editor_order_item);
        imgItemImage = (ImageView) findViewById(R.id.img_editor_picture);
        missingImage = (TextView) findViewById(R.id.lbl_editor_error_image);

        txtItemName.setOnTouchListener(mTouchListener);
        txtItemPrice.setOnTouchListener(mTouchListener);
        txtItemQuantity.setOnTouchListener(mTouchListener);
        btnAddQuantity.setOnTouchListener(mTouchListener);
        btnMinusQuantity.setOnTouchListener(mTouchListener);
        txtSupplierName.setOnTouchListener(mTouchListener);
        txtSupplierMail.setOnTouchListener(mTouchListener);
        btnAddImage.setOnTouchListener(mTouchListener);
        btnOrderItem.setOnTouchListener(mTouchListener);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mImageUri != null) {
            outState.putString(STATE_IMAGE_URI, mImageUri.toString());
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        if (savedInstanceState.containsKey(STATE_IMAGE_URI) &&
                !savedInstanceState.getString(STATE_IMAGE_URI).equals("")) {
            mImageUri = Uri.parse(savedInstanceState.getString(STATE_IMAGE_URI));

            ViewTreeObserver viewTreeObserver = imgItemImage.getViewTreeObserver();
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    imgItemImage.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    imgItemImage.setImageBitmap(getBitmapFromUri(mImageUri));
                }
            });
        }
    }

    /**
     * Method to set selected image to ImageView holder if request is successful
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_REQUEST_CODE && (resultCode == RESULT_OK)) {
            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.  Pull that uri using "resultData.getData()"

            if (data != null) {
                mImageUri = data.getData();
                Log.i(LOG_TAG, "Uri: " + mImageUri.toString());

                imgItemImage.setImageBitmap(getBitmapFromUri(mImageUri));
            }
        }
    }

    public Bitmap getBitmapFromUri(Uri uri) {

        if (uri == null || uri.toString().isEmpty())
            return null;

        // Get the dimensions of the View
        int targetW = 300;
        int targetH = 300;

        InputStream input = null;
        try {
            input = this.getContentResolver().openInputStream(uri);

            // Get the dimensions of the bitmap
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(input, null, bmOptions);
            input.close();

            int photoW = bmOptions.outWidth;
            int photoH = bmOptions.outHeight;

            // Determine how much to scale down the image
            int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

            // Decode the image file into a Bitmap sized to fill the View
            bmOptions.inJustDecodeBounds = false;
            bmOptions.inSampleSize = scaleFactor;
            bmOptions.inPurgeable = true;

            input = this.getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(input, null, bmOptions);
            input.close();
            return bitmap;

        } catch (FileNotFoundException fne) {
            Log.e(LOG_TAG, "Failed to load image.", fne);
            return null;
        } catch (Exception e) {
            Log.e(LOG_TAG, "Failed to load image.", e);
            return null;
        } finally {
            try {
                input.close();
            } catch (IOException ioe) {

            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            case R.id.action_save:
                if (checkFields()){
                //save the item
                addProduct();
                // Exit activity
                finish();}
                return true;

            case R.id.action_delete:
                // Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog();
                return true;

            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the item hasn't changed, continue with navigating up to parent activity
                if (!mItemHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * This method is called when the back button is pressed.
     */
    @Override
    public void onBackPressed() {
        // If the item hasn't changed, continue with handling back button press
        if (!mItemHasChanged) {
            super.onBackPressed();
            return;
        }
    }

    private void showUnsavedChangesDialog(DialogInterface.OnClickListener discardButtonClickListener) {

        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the item.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Prompt the user to confirm that they want to deleteProduct this item.
     */
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so deleteProduct the item.
                deleteProduct();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the item.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteProduct() {

        // Only perform the delete if this is an existing item.
        if (mCurrentProductUri != null) {
            // Call the ContentResolver to delete the item at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentProductUri
            // content URI already identifies the item that we want.
            int rowsDeleted = getContentResolver().delete(mCurrentProductUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.delete_item_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.delete_item_successful),
                        Toast.LENGTH_SHORT).show();
            }
            // Close the activity
            finish();
        }
    }

    public void addProduct() {
        //Checks for the required fileds, if one of those are empty, no action performed
        if (checkFields()) {

            // Read from input fields
            String itemName = txtItemName.getText().toString().trim();
            String itemPrice = txtItemPrice.getText().toString().trim();
            String itemImagePath = mImageUri.toString();
            String itemSupplierName = txtSupplierName.getText().toString().trim();
            String itemSupplierMail = txtSupplierMail.getText().toString().trim();
            String itemQuantity = txtItemQuantity.getText().toString().trim();

            // Check if this is supposed to be a new item
            // and check if all the fields in the editor are blank
            if (mCurrentProductUri == null &&
                    TextUtils.isEmpty(itemName) && TextUtils.isEmpty(itemPrice) &&
                    TextUtils.isEmpty(itemImagePath) && TextUtils.isEmpty(itemSupplierName) &&
                    TextUtils.isEmpty(itemSupplierMail) && TextUtils.isEmpty(itemQuantity) &&
                    mType == StoreEntry.TYPE_UNKNOWN) {
                // Since no fields were modified, we can return early without creating a new item.
                // No need to create ContentValues and no need to do any ContentProvider operations.
                return;
            }

            // Create a ContentValues object where column names are the keys,
            // and item attributes from the editor are the values.
            ContentValues values = new ContentValues();
            values.put(StoreEntry.COLUMN_NAME, itemName);
            values.put(StoreEntry.COLUMN_PRICE, itemPrice);
            values.put(StoreEntry.COLUMN_IMAGE, itemImagePath);
            values.put(StoreEntry.COLUMN_SUPPLIER_NAME, itemSupplierName);
            values.put(StoreEntry.COLUMN_SUPPLIER_EMAIL, itemSupplierMail);
            values.put(StoreEntry.COLUMN_QUANTITY, itemQuantity);
            values.put(StoreEntry.COLUMN_TYPE, mType);

            // Determine if this is a new or existing item by checking if mCurrentProductUri is null or not
            if (mCurrentProductUri == null) {
                // This is a NEW item, so insert a new item into the provider,
                // returning the content URI for the new item.
                Uri newUri = getContentResolver().insert(StoreEntry.CONTENT_URI, values);

                // Show a toast message depending on whether or not the insertion was successful.
                if (newUri == null) {
                    // If the new content URI is null, then there was an error with insertion.
                    Toast.makeText(this, getString(R.string.error_insert_failed),
                            Toast.LENGTH_SHORT).show();
                } else {
                    // Otherwise, the insertion was successful and we can display a toast.
                    Toast.makeText(this, getString(R.string.confirm_insert_successful),
                            Toast.LENGTH_SHORT).show();
                }
            } else {
                // Otherwise this is an EXISTING item, so update the item with content URI
                // and pass in the new ContentValues. Pass in null for the selection and selection args
                // because mCurrentProductUri will already identify the correct row in the database that
                // we want to modify.
                int rowsAffected = getContentResolver().update(mCurrentProductUri, values, null, null);

                // Show a toast message depending on whether or not the update was successful.
                if (rowsAffected == 0) {
                    // If no rows were affected, then there was an error with the update.
                    Toast.makeText(this, getString(R.string.update_item_failed),
                            Toast.LENGTH_SHORT).show();
                } else {
                    // Otherwise, the update was successful and we can display a toast.
                    Toast.makeText(this, getString(R.string.update_item_successful),
                            Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            //Do nothing
        }
    }

    //Checks the fields for valid values, required fields cannot be empty
    private boolean checkFields() {
        String name = txtItemName.getText().toString();
        if (name.isEmpty() || name.trim().length() == 0) {
            Toast.makeText(this, getString(R.string.not_valid_name), Toast.LENGTH_SHORT).show();
            return false;
        }

        String price = txtItemPrice.getText().toString();
        if (price.isEmpty() || price.trim().length() == 0 || price.contains("-")) {
            Toast.makeText(this, getString(R.string.not_valid_price), Toast.LENGTH_SHORT).show();
            return false;
        }

        String quantity = txtItemQuantity.getText().toString();
        if(quantity.isEmpty() || quantity.trim().length() == 0 || quantity.contains("-") ){
            Toast.makeText(this, getString(R.string.not_valid_quantity), Toast.LENGTH_SHORT).show();
            return false;
        }

        // Check if image selected
        if (mCurrentProductUri == null) {
            if (mImageUri == null){
                Toast.makeText(this, getString(R.string.image_not_found), Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        return true;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Define a projection that contains all columns from the products table
        String[] projection = {
                StoreEntry._ID,
                StoreEntry.COLUMN_NAME,
                StoreEntry.COLUMN_TYPE,
                StoreEntry.COLUMN_PRICE,
                StoreEntry.COLUMN_QUANTITY,
                StoreEntry.COLUMN_IMAGE,
                StoreEntry.COLUMN_SUPPLIER_NAME,
                StoreEntry.COLUMN_SUPPLIER_EMAIL};


        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,       // Parent activity context
                mCurrentProductUri,         // Query the content URI for the current product
                projection,                 // Columns to include in the resulting Cursor
                null,                       // No selection clause
                null,                       // No selection arguments
                null);                      // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {
            // Find the columns of items attributes that we're interested in
            final int productId = cursor.getInt(cursor.getColumnIndex(StoreContract.StoreEntry._ID));
            int nameColunmnIndex = cursor.getColumnIndex(StoreEntry.COLUMN_NAME);
            int priceColumnIndex = cursor.getColumnIndex(StoreEntry.COLUMN_PRICE);
            int typeColumnIndex = cursor.getColumnIndex(StoreEntry.COLUMN_TYPE);
            int imageColumnIndex = cursor.getColumnIndex(StoreEntry.COLUMN_IMAGE);
            int quantityColumnIndex = cursor.getColumnIndex(StoreEntry.COLUMN_QUANTITY);
            int supplierNameColumnIndex = cursor.getColumnIndex(StoreEntry.COLUMN_SUPPLIER_NAME);
            int supplierMailColumnIndex = cursor.getColumnIndex(StoreEntry.COLUMN_SUPPLIER_EMAIL);

            // Extract out the value from the Cursor for the respective column index
            final String name = cursor.getString(nameColunmnIndex);
            int price = cursor.getInt(priceColumnIndex);
            String image = cursor.getString(imageColumnIndex);
            final int quantity = cursor.getInt(quantityColumnIndex);
            final String supplier = cursor.getString(supplierNameColumnIndex);
            final String supplierMail = cursor.getString(supplierMailColumnIndex);
            int type = cursor.getInt(typeColumnIndex);

            txtItemName.setText(name);
            String helperPrice = price + "";
            txtItemPrice.setText(helperPrice);
            String helperQuantity = quantity + "";
            txtItemQuantity.setText(helperQuantity);
            txtSupplierMail.setText(supplierMail);
            txtSupplierName.setText(supplier);

            imgItemImage.setImageBitmap(getBitmapFromUri(Uri.parse(image)));
            mImageUri = Uri.parse(image);

            switch (type) {
                case StoreEntry.TYPE_DAIRY_PROD:
                    mSpinnerType.setSelection(1);
                    break;
                case StoreEntry.TYPE_BUTCHERS_MEAT:
                    mSpinnerType.setSelection(2);
                    break;
                case StoreEntry.TYPE_SPICES:
                    mSpinnerType.setSelection(3);
                    break;
                case StoreEntry.TYPE_SWEETS:
                    mSpinnerType.setSelection(4);
                    break;
                case StoreEntry.TYPES_SNACKSANDCANDIES:
                    mSpinnerType.setSelection(5);
                    break;
                default:
                    mSpinnerType.setSelection(0);
                    break;
            }

            //Define the buttons and the behaviors: increase/decrease quantity, order via mail
            btnAddQuantity.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int modifiedQuantity;
                    if (quantity >= 0) {
                        modifiedQuantity = quantity + 1;
                        // Decrease the quantity value
                        ContentValues contentValues = new ContentValues();
                        contentValues.put(StoreContract.StoreEntry.COLUMN_QUANTITY, modifiedQuantity);

                        Uri recordUri = ContentUris.withAppendedId(StoreEntry.CONTENT_URI, productId);
                        int numRowsUpdated = EditorActivity.this.getContentResolver().update(recordUri, contentValues, null, null);
                    } else {
                        Log.e(LOG_TAG, mContext.getString(R.string.no_more_product));
                    }

                }
            });

            btnMinusQuantity.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int modifiedQuantity;
                    if (quantity > 0) {
                        modifiedQuantity = quantity - 1;
                        // Decrease the quantity value
                        ContentValues contentValues = new ContentValues();
                        contentValues.put(StoreContract.StoreEntry.COLUMN_QUANTITY, modifiedQuantity);

                        Uri recordUri = ContentUris.withAppendedId(StoreEntry.CONTENT_URI, productId);
                        int numRowsUpdated = EditorActivity.this.getContentResolver().update(recordUri, contentValues, null, null);
                    }
                }
            });

            btnOrderItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String mailMessage = "Dear " + supplier + ", \nI want to order the following product: " + name +
                            "\nAmount: (type amount) .\nDate: ASAP.\nThank You in advance!";
                    Intent intent = new Intent(Intent.ACTION_SENDTO);
                    intent.setData(Uri.parse("mailto:")); // only email apps should handle this
                    intent.putExtra(Intent.EXTRA_EMAIL, supplierMail);
                    intent.putExtra(Intent.EXTRA_SUBJECT, supplier);
                    intent.putExtra(Intent.EXTRA_TEXT, mailMessage);
                    if (intent.resolveActivity(getPackageManager()) != null) {
                        startActivity(intent);
                    }
                }
            });
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        txtItemName.setText("");
        txtSupplierName.setText("");
        txtSupplierMail.setText("");
        txtItemQuantity.setText("");
        txtItemPrice.setText("");
        mSpinnerType.setSelection(0);
    }
}

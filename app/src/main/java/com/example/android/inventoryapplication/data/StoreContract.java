package com.example.android.inventoryapplication.data;


import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public class StoreContract {

    /**
     * ContentProvider Name
     */
    public static final String CONTENT_AUTHORITY = "com.example.android.inventoryapplication";
    /**
     * ContentProvider Base Uri
     */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    /**
     * Path appended to base URI for possible URI's
     */
    public static final String PATH_ITEMS = "products";

    //An empty private constructor makes sure that the class is not going to be initialised.
    public StoreContract() {
    }

    /**
     * Inner class that defines constant values for the Products table.
     * Each entry in the table represents a single product.
     */
    public static final class StoreEntry implements BaseColumns {

        // The content URI to access the data in the provider
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_ITEMS);

        // MIME type of the {@link #CONTENT_URI} for a list of items
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ITEMS;

        // MIME type of the {@link #CONTENT_URI} for a single item
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ITEMS;

        // Name of database table
        public static final String TABLE_NAME = "products";

        // Unique ID: INTEGER
        public final static String _ID = BaseColumns._ID;

        // Name : TEXT
        public final static String COLUMN_NAME = "name";

        //Type : INTEGER
        public final static String COLUMN_TYPE = "type";

        // Price: INTEGER
        public final static String COLUMN_PRICE = "price";

        // Quantity: INTEGER
        public final static String COLUMN_QUANTITY = "quantity";

        // Image: TEXT
        public final static String COLUMN_IMAGE = "image";

        // Supplier Name: TEXT
        public final static String COLUMN_SUPPLIER_NAME = "supplier_name";

        // Supplier Email: TEXT
        public final static String COLUMN_SUPPLIER_EMAIL = "supplier_email";

        //To categorise the products only the following types can be used:
        public final static int TYPE_UNKNOWN = 0;
        public final static int TYPE_DAIRY_PROD = 1;
        public final static int TYPE_BUTCHERS_MEAT = 2;
        public final static int TYPE_SPICES = 3;
        public final static int TYPE_SWEETS = 4;
        public final static int TYPES_SNACKSANDCANDIES = 5;

        //Checks the ITEM_TYPE value if it is valid value or not
        public static boolean isValidType(int itemType) {
            if (itemType == TYPE_UNKNOWN || itemType == TYPE_DAIRY_PROD || itemType == TYPE_BUTCHERS_MEAT
                    || itemType == TYPE_SPICES || itemType == TYPE_SWEETS || itemType == TYPES_SNACKSANDCANDIES) {
                return true;
            }
            return false;
        }
    }

}


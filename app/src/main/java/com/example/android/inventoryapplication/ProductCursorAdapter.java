package com.example.android.inventoryapplication;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.android.inventoryapplication.data.StoreContract;


public class ProductCursorAdapter extends CursorAdapter {

    public static final String LOG_TAG = ProductCursorAdapter.class.getName();


    public ProductCursorAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {

        TextView txtName = (TextView) view.findViewById(R.id.txt_list_name);
        TextView txtType = (TextView) view.findViewById(R.id.txt_list_type);
        TextView txtPrice = (TextView) view.findViewById(R.id.txt_list_price);
        TextView txtQuantity = (TextView) view.findViewById(R.id.txt_list_quantity);
        Button btnBuy = (Button) view.findViewById(R.id.btn_list_buy);

        final int productId = cursor.getInt(cursor.getColumnIndex(StoreContract.StoreEntry._ID));
        String name = cursor.getString(cursor.getColumnIndex(StoreContract.StoreEntry.COLUMN_NAME));
        int price = cursor.getInt(cursor.getColumnIndex(StoreContract.StoreEntry.COLUMN_PRICE));
        int type = cursor.getInt(cursor.getColumnIndex(StoreContract.StoreEntry.COLUMN_TYPE));
        final int quantity = cursor.getInt(cursor.getColumnIndex(StoreContract.StoreEntry.COLUMN_QUANTITY));

        txtName.setText(name);
        String convertTypeToString;
        switch (type) {
            case 1:
                convertTypeToString = context.getString(R.string.type_dairy_prod);
                break;
            case 2:
                convertTypeToString = context.getString(R.string.type_butchers_meat);
                break;
            case 3:
                convertTypeToString = context.getString(R.string.type_species);
                break;
            case 4:
                convertTypeToString = context.getString(R.string.type_sweets);
                break;
            case 5:
                convertTypeToString = context.getString(R.string.type_snacks_and_candies);
                break;
            default:
                convertTypeToString = context.getString(R.string.type_unknown);
        }
        String priceToText = price + " $";
        String quantityToText = quantity + "";
        txtType.setText(convertTypeToString);
        txtPrice.setText(priceToText);
        txtQuantity.setText(quantityToText);

        btnBuy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri productUri = ContentUris.withAppendedId(StoreContract.StoreEntry.CONTENT_URI, productId);
                saleItem(context, productUri, quantity);
            }
        });
    }

    //Item sold
    private void saleItem(Context context, Uri productUri, int currentQuantity) {

        if (currentQuantity >= 1) {

            currentQuantity--;

            // Decrease the quantity value
            ContentValues contentValues = new ContentValues();
            contentValues.put(StoreContract.StoreEntry.COLUMN_QUANTITY, currentQuantity);
            int numRowsUpdated = context.getContentResolver().update(productUri, contentValues, null, null);
        } else {
            Log.e(LOG_TAG, context.getString(R.string.no_more_product));
        }
    }
}

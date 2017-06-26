package com.miymayster.olya.inventoryapp.data;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.miymayster.olya.inventoryapp.R;

public class ProductAdapter extends CursorAdapter {

    public ProductAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.product, parent, false);
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        TextView productNameTextView = ((TextView) view.findViewById(R.id.product_name));
        TextView productQuantityTextView = ((TextView) view.findViewById(R.id.product_quantity));
        TextView productPriceTextView = ((TextView) view.findViewById(R.id.product_price));
        Button productSoldButton = ((Button) view.findViewById(R.id.product_sold));

        int columnNameIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_NAME);
        int columnQuantityIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_QUANTITY);
        int columnPriceIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRICE);
        int columnSalesIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_SALES);

        String productName = cursor.getString(columnNameIndex);
        double productPrice = cursor.getDouble(columnPriceIndex);
        final int productQuantity = cursor.getInt(columnQuantityIndex);
        final int productSales = cursor.getInt(columnSalesIndex);

        productNameTextView.setText(productName);
        productPriceTextView.setText(String.valueOf(productPrice));

        if (productQuantity == 0) {
            productSoldButton.setVisibility(View.GONE);
            productQuantityTextView.setText(R.string.no_left);
        } else {
            productSoldButton.setVisibility(View.VISIBLE);
            productQuantityTextView.setText(String.valueOf(productQuantity));
            final long id = cursor.getLong(cursor.getColumnIndex(ProductContract.ProductEntry._ID));
            productSoldButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int newProductQuantity = productQuantity - 1;
                    int newProductSales = productSales + 1;
                    ContentValues values = new ContentValues();
                    values.put(ProductContract.ProductEntry.COLUMN_QUANTITY, newProductQuantity);
                    values.put(ProductContract.ProductEntry.COLUMN_SALES, newProductSales);
                    Uri uri = ContentUris.withAppendedId(ProductContract.ProductEntry.CONTENT_URI, id);
                    context.getContentResolver().update(uri, values, null, null);
                }
            });
        }

    }
}

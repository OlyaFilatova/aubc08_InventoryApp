package com.miymayster.olya.inventoryapp.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;


public class ProductProvider extends ContentProvider {
    private ProductDBHelper productDBHelper;
    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    private static final int PRODUCT = 100;
    private static final int PRODUCT_ID = 101;

    static {
        uriMatcher.addURI(ProductContract.CONTENT_AUTHORITY, ProductContract.PATH_PRODUCT, PRODUCT);
        uriMatcher.addURI(ProductContract.CONTENT_AUTHORITY, ProductContract.PATH_PRODUCT + "/#", PRODUCT_ID);
    }

    @Override
    public boolean onCreate() {
        productDBHelper = new ProductDBHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
                        @Nullable String[] selectionArgs, @Nullable String sortOrder)
            throws IllegalArgumentException {
        SQLiteDatabase db = productDBHelper.getReadableDatabase();
        Cursor cursor;
        switch (uriMatcher.match(uri)) {
            case PRODUCT:
                cursor = db.query(ProductContract.ProductEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            case PRODUCT_ID:
                long id = ContentUris.parseId(uri);
                selection = ProductContract.ProductEntry._ID + "=?";
                selectionArgs = new String[]{
                        String.valueOf(id)
                };
                cursor = db.query(ProductContract.ProductEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Query is not supported for " + uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) throws IllegalArgumentException {
        switch (uriMatcher.match(uri)) {
            case PRODUCT:
                return ProductContract.ProductEntry.CONTENT_LIST_TYPE;
            case PRODUCT_ID:
                return ProductContract.ProductEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values)
            throws IllegalArgumentException {
        SQLiteDatabase db = productDBHelper.getWritableDatabase();
        switch (uriMatcher.match(uri)) {
            case PRODUCT:
                String name = values.getAsString(ProductContract.ProductEntry.COLUMN_NAME);
                if (TextUtils.isEmpty(name)) {
                    throw new IllegalArgumentException("Product requires a name");
                }
                String price = values.getAsString(ProductContract.ProductEntry.COLUMN_PRICE);
                if (TextUtils.isEmpty(price)) {
                    throw new IllegalArgumentException("Product requires a price");
                }
                String quantity = values.getAsString(ProductContract.ProductEntry.COLUMN_QUANTITY);
                if (TextUtils.isEmpty(quantity)) {
                    throw new IllegalArgumentException("Product requires quantity");
                }
                String supplier = values.getAsString(ProductContract.ProductEntry.COLUMN_SUPPLIER);
                if (TextUtils.isEmpty(supplier)) {
                    throw new IllegalArgumentException("Product requires supplier's contact");
                }
                long id = db.insert(ProductContract.ProductEntry.TABLE_NAME, null, values);
                getContext().getContentResolver().notifyChange(uri, null);
                return ContentUris.withAppendedId(uri, id);
            default:
                throw new IllegalArgumentException("Insert is not supported for " + uri);
        }
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection,
                      @Nullable String[] selectionArgs) throws IllegalArgumentException {
        SQLiteDatabase db = productDBHelper.getWritableDatabase();
        int count;
        switch (uriMatcher.match(uri)) {
            case PRODUCT:
                count = db.delete(ProductContract.ProductEntry.TABLE_NAME, selection, selectionArgs);
                if (count > 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                return count;
            case PRODUCT_ID:
                long id = ContentUris.parseId(uri);
                selection = ProductContract.ProductEntry._ID + "=?";
                selectionArgs = new String[]{
                        String.valueOf(id)
                };
                count = db.delete(ProductContract.ProductEntry.TABLE_NAME, selection, selectionArgs);
                if (count > 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                return count;
            default:
                throw new IllegalArgumentException("Delete is not supported for " + uri);
        }
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection,
                      @Nullable String[] selectionArgs) throws IllegalArgumentException {
        switch (uriMatcher.match(uri)) {
            case PRODUCT:
                return updateProduct(uri, values, selection, selectionArgs);
            case PRODUCT_ID:
                long id = ContentUris.parseId(uri);
                selection = ProductContract.ProductEntry._ID + "=?";
                selectionArgs = new String[]{
                        String.valueOf(id)
                };
                return updateProduct(uri, values, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }


    /**
     * Handles requests to update one or more rows.
     * Updates all rows matching the selection to set the columns according to the provided values map.
     * Calls notifyChange() after updating.
     *
     * @param uri           The URI to query. This can potentially have a record ID if this is an update request for a specific record.
     * @param values        A set of column_name/value pairs to update in the database. This must not be null.
     * @param selection     An optional filter to match rows to update.
     * @param selectionArgs values for the filter
     * @return the number of rows affected.
     * @throws IllegalArgumentException if values are present but empty
     */
    private int updateProduct(@NonNull Uri uri, @Nullable ContentValues values,
                              @Nullable String selection, @Nullable String[] selectionArgs)
            throws IllegalArgumentException {
        SQLiteDatabase db = productDBHelper.getWritableDatabase();
        String name = values.getAsString(ProductContract.ProductEntry.COLUMN_NAME);
        if (name != null && name.equals("")) {
            throw new IllegalArgumentException("Product name must not be empty");
        }
        String price = values.getAsString(ProductContract.ProductEntry.COLUMN_PRICE);
        if (price != null && price.equals("")) {
            throw new IllegalArgumentException("Product price must not be empty");
        }
        String quantity = values.getAsString(ProductContract.ProductEntry.COLUMN_QUANTITY);
        if (quantity != null && quantity.equals("")) {
            throw new IllegalArgumentException("Product quantity must not be empty");
        }
        String supplier = values.getAsString(ProductContract.ProductEntry.COLUMN_SUPPLIER);
        if (supplier != null && supplier.equals("")) {
            throw new IllegalArgumentException("Product supplier's contact must not be empty");
        }

        int count = db.update(ProductContract.ProductEntry.TABLE_NAME, values, selection, selectionArgs);
        if (count > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return count;
    }
}

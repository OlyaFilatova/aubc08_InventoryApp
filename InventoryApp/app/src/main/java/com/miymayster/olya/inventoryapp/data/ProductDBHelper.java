package com.miymayster.olya.inventoryapp.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class ProductDBHelper extends SQLiteOpenHelper {
    private final static String DATABASE_NAME = "products.db";
    private static final int DATABASE_VERSION = 2;
    private static final String CREATE_ENTRIES = "CREATE TABLE " +
            ProductContract.ProductEntry.TABLE_NAME + "(" +
            ProductContract.ProductEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            ProductContract.ProductEntry.COLUMN_NAME + " TEXT NOT NULL," +
            ProductContract.ProductEntry.COLUMN_PRICE + " REAL NOT NULL," +
            ProductContract.ProductEntry.COLUMN_QUANTITY + " REAL DEFAULT 0," +
            ProductContract.ProductEntry.COLUMN_SUPPLIER + " TEXT NOT NULL," +
            ProductContract.ProductEntry.COLUMN_SALES + " INTEGER DEFAULT 0" +
            ");";
    private static final String DROP_ENTRIES = "DROP TABLE IF EXISTS " + ProductContract.ProductEntry.TABLE_NAME;

    public ProductDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DROP_ENTRIES);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}

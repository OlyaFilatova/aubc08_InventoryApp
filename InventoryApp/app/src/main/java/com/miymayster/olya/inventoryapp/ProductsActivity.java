package com.miymayster.olya.inventoryapp;

import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.miymayster.olya.inventoryapp.data.ProductAdapter;
import com.miymayster.olya.inventoryapp.data.ProductContract;

public class ProductsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int PRODUCTS_LOADER = 1;
    private ProductAdapter mAdapter;
    private ListView mListView;
    private TextView mEmptyListTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_products);
        mListView = (ListView) findViewById(R.id.products_list);
        mEmptyListTextView = (TextView) findViewById(R.id.empty_list);
        mListView.setEmptyView(mEmptyListTextView);
        mAdapter = new ProductAdapter(this, null);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(ProductsActivity.this, ProductActivity.class);
                intent.setData(ContentUris.withAppendedId(ProductContract.ProductEntry.CONTENT_URI, id));
                startActivity(intent);
            }
        });

        getSupportActionBar().setTitle(R.string.catalog);
        getSupportLoaderManager().initLoader(PRODUCTS_LOADER, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.products, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add:
                Intent intent = new Intent(ProductsActivity.this, ProductActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_delete_all:
                showDeleteConfirmationDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_all_products);
        builder.setPositiveButton(getString(R.string.delete), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deleteProducts();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteProducts() {
        int rowsDeleted = getContentResolver().delete(ProductContract.ProductEntry.CONTENT_URI, null, null);
        if (rowsDeleted == 0) {
            Toast.makeText(this, R.string.no_products_deleted, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, getString(R.string.deleted_n_products, rowsDeleted), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case PRODUCTS_LOADER:
                String[] projection = new String[]{
                        ProductContract.ProductEntry._ID,
                        ProductContract.ProductEntry.COLUMN_NAME,
                        ProductContract.ProductEntry.COLUMN_PRICE,
                        ProductContract.ProductEntry.COLUMN_QUANTITY,
                        ProductContract.ProductEntry.COLUMN_SALES
                };
                return new CursorLoader(this, ProductContract.ProductEntry.CONTENT_URI, projection, null, null, null);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);
        if (data.getCount() == 0) {
            mEmptyListTextView.setText(R.string.no_products);
        } else {
            mEmptyListTextView.setText("");
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }
}

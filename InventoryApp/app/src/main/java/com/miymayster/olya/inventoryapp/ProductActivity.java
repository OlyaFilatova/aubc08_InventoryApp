package com.miymayster.olya.inventoryapp;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.miymayster.olya.inventoryapp.data.ProductContract;

public class ProductActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final int PRODUCT_ID_LOADER = 3;
    EditText mProductNameEditText;
    EditText mProductPriceEditText;
    EditText mSupplierEditText;
    EditText mQuantityEditText;
    TextView mSalesCountTextView;
    Uri currentUri;
    private boolean mProductHasChanged = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product);
        mProductNameEditText = (EditText) findViewById(R.id.et_product_name);
        mProductPriceEditText = (EditText) findViewById(R.id.et_product_price);
        mSupplierEditText = (EditText) findViewById(R.id.et_supplier_phone);
        mQuantityEditText = (EditText) findViewById(R.id.et_product_quantity);
        mSalesCountTextView = (TextView) findViewById(R.id.tv_sales_count);

        mProductNameEditText.setOnTouchListener(mTouchListener);
        mProductPriceEditText.setOnTouchListener(mTouchListener);
        mQuantityEditText.setOnTouchListener(mTouchListener);
        mSupplierEditText.setOnTouchListener(mTouchListener);
        findViewById(R.id.b_order_more).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phone = mSupplierEditText.getText().toString();
                openDialer(phone);
            }
        });
        findViewById(R.id.b_add_quantity).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int quantity = 0;
                try {
                    quantity = Integer.valueOf(mQuantityEditText.getText().toString());
                } catch (NumberFormatException e) {
                    Log.e(ProductActivity.class.getSimpleName(), e.getMessage());
                }
                quantity += 1;
                mQuantityEditText.setText(String.valueOf(quantity));
                mProductHasChanged = true;
            }
        });
        findViewById(R.id.b_remove_quantity).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int quantity = 0;
                try {
                    quantity = Integer.valueOf(mQuantityEditText.getText().toString());
                } catch (NumberFormatException e) {
                    Log.e(ProductActivity.class.getSimpleName(), e.getMessage());
                }
                if (quantity > 0) {
                    quantity -= 1;
                    mQuantityEditText.setText(String.valueOf(quantity));
                }
                mProductHasChanged = true;
            }
        });
        if (getIntent() != null && getIntent().getData() != null) {
            currentUri = getIntent().getData();
            getSupportLoaderManager().initLoader(PRODUCT_ID_LOADER, null, this);
            getSupportActionBar().setTitle(R.string.edit_product);
        } else {
            getSupportActionBar().setTitle(R.string.create_product);
        }
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_this_product);
        builder.setPositiveButton(getString(R.string.delete), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deleteProduct();
            }
        });
        builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteProduct() {
        getSupportLoaderManager().destroyLoader(PRODUCT_ID_LOADER);
        int rowsDeleted = getContentResolver().delete(currentUri, null, null);
        if (rowsDeleted == 0) {
            Toast.makeText(this, R.string.delete_failed, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, R.string.delete_successful, Toast.LENGTH_SHORT).show();
        }
        finish();
    }

    private void saveAction() {
        if (saveProduct()) {
            finish();
        }
    }

    private void openDialer(String phone) {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + phone));
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    private boolean saveProduct() {
        String productName = mProductNameEditText.getText().toString();
        String productPrice = mProductPriceEditText.getText().toString();
        String productQuantity = mQuantityEditText.getText().toString();
        String supplierPhone = mSupplierEditText.getText().toString();
        if (TextUtils.isEmpty(productName) &&
                TextUtils.isEmpty(productPrice) &&
                (TextUtils.isEmpty(productQuantity) || productQuantity.equals("0")) &&
                TextUtils.isEmpty(supplierPhone)) {
            return false;
        }
        boolean gotError = false;
        if (TextUtils.isEmpty(productName)) {
            Toast.makeText(this, R.string.product_requares_a_name, Toast.LENGTH_SHORT).show();
            gotError = true;
        }
        if (TextUtils.isEmpty(productPrice)) {
            Toast.makeText(this, R.string.product_requires_a_price, Toast.LENGTH_SHORT).show();
            gotError = true;
        }
        if (TextUtils.isEmpty(productQuantity)) {
            Toast.makeText(this, R.string.product_requires_quantity, Toast.LENGTH_SHORT).show();
            gotError = true;
        }
        if (TextUtils.isEmpty(supplierPhone)) {
            Toast.makeText(this, R.string.product_requires_supplier, Toast.LENGTH_SHORT).show();
            gotError = true;
        }
        if (gotError) {
            return false;
        }
        ContentValues values = new ContentValues();
        values.put(ProductContract.ProductEntry.COLUMN_NAME, productName);
        values.put(ProductContract.ProductEntry.COLUMN_PRICE, productPrice);
        values.put(ProductContract.ProductEntry.COLUMN_QUANTITY, productQuantity);
        values.put(ProductContract.ProductEntry.COLUMN_SUPPLIER, supplierPhone);
        if (currentUri == null) {
            getContentResolver().insert(ProductContract.ProductEntry.CONTENT_URI, values);
        } else {
            getContentResolver().update(currentUri, values, null, null);
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.product, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (currentUri == null) {
            menu.findItem(R.id.action_delete).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                saveAction();
                return true;
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
            case android.R.id.home:
                if (!mProductHasChanged) {
                    NavUtils.navigateUpFromSameTask(ProductActivity.this);
                    return true;
                }
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                NavUtils.navigateUpFromSameTask(ProductActivity.this);
                            }
                        };

                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case PRODUCT_ID_LOADER:
                String[] projection = new String[]{
                        ProductContract.ProductEntry._ID,
                        ProductContract.ProductEntry.COLUMN_NAME,
                        ProductContract.ProductEntry.COLUMN_PRICE,
                        ProductContract.ProductEntry.COLUMN_QUANTITY,
                        ProductContract.ProductEntry.COLUMN_SUPPLIER,
                        ProductContract.ProductEntry.COLUMN_SALES
                };
                String selection = ProductContract.ProductEntry._ID + "=?";
                String[] selectionArgs = new String[]{
                        String.valueOf(ContentUris.parseId(currentUri))
                };
                return new CursorLoader(this, currentUri, projection, selection, selectionArgs, null);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        data.moveToFirst();
        int nameColumnIndex = data.getColumnIndex(ProductContract.ProductEntry.COLUMN_NAME);
        int priceColumnIndex = data.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRICE);
        int quantityColumnIndex = data.getColumnIndex(ProductContract.ProductEntry.COLUMN_QUANTITY);
        int supplierColumnIndex = data.getColumnIndex(ProductContract.ProductEntry.COLUMN_SUPPLIER);
        int salesColumnIndex = data.getColumnIndex(ProductContract.ProductEntry.COLUMN_SALES);

        String name = data.getString(nameColumnIndex);
        String price = data.getString(priceColumnIndex);
        String quantity = data.getString(quantityColumnIndex);
        String supplier = data.getString(supplierColumnIndex);
        String sales = data.getString(salesColumnIndex);

        mProductNameEditText.setText(name);
        mProductPriceEditText.setText(price);
        mSupplierEditText.setText(supplier);
        mQuantityEditText.setText(quantity);
        mSalesCountTextView.setText(sales);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mProductNameEditText.setText("");
        mProductPriceEditText.setText("");
        mSupplierEditText.setText("");
        mQuantityEditText.setText("0");
        mSalesCountTextView.setText("0");
    }

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mProductHasChanged = true;
            return false;
        }
    };

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.discard_all_changes);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void onBackPressed() {
        if (!mProductHasChanged) {
            super.onBackPressed();
            return;
        }
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                };
        showUnsavedChangesDialog(discardButtonClickListener);
    }
}

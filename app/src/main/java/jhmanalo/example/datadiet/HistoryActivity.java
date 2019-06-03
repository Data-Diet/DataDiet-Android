package jhmanalo.example.datadiet;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.database.Cursor;
import android.widget.Toast;
import android.widget.ImageButton;
import android.support.v7.app.AlertDialog;
import android.content.DialogInterface;
import android.widget.ListView;
import java.util.ArrayList;
import android.widget.ArrayAdapter;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.util.Log;
import android.widget.TextView;
import android.content.Intent;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;

public class HistoryActivity extends AppCompatActivity {

    ListView listView;
    ProductDbHelper productDB;
    Cursor cursor;
    ArrayList<String> listItems;
    ArrayAdapter adapter;
    View listItemSwiped;
    GestureDetector gestureDetector;
    ImageButton delete;
    ConstraintLayout historyLayout;
    String productDeleted, prodDelURL, prodDelWarn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        historyLayout = findViewById(R.id.historyLayout);
        listView = findViewById(R.id.list_view);
        productDB = new ProductDbHelper(this);
        cursor = productDB.getAllProducts();
        listItems = new ArrayList<>();
        displayProductList();
        View productListView = findViewById(R.id.list_view);
        gestureDetector = new GestureDetector(this, new GestureListener());
        productListView.setOnTouchListener(touchListener);
    }

    public void displayProductList() {
        if (productDB.getAllProducts().getCount() != 0) {
            cursor = productDB.getLatestProduct();
            Log.d("HistoryActivity", "displayProduct " + cursor.getString(1));
            do {
                listItems.add(cursor.getString(1));
            } while (cursor.moveToPrevious());
            adapter = new ArrayAdapter<>(this, R.layout.list_view_product, R.id.productName, listItems);
            listView.setAdapter(adapter);
        } else
            Toast.makeText(this, "No history to display", Toast.LENGTH_LONG).show();
    }

    public void showPopUp(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.popUpTitle);
        builder.setMessage(R.string.pupUpMessage);
        builder.setPositiveButton(R.string.clear, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                clearHistory();
            }

        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {}

        });
        builder.setIcon(R.drawable.ic_warning_yellow_24dp);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void clearHistory() {
        productDB.erase();
        adapter.clear();
        displayProductList();
    }

    public void closeHistory(View view) {
        finish();
    }

    View.OnTouchListener touchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            return gestureDetector.onTouchEvent(event);
        }
    };

    public class GestureListener extends GestureDetector.SimpleOnGestureListener {
        private static final int minSwipeDistance = 10;
        private static final int minSwipeVelocity = 10;

        @Override
        public boolean onDown(MotionEvent event) {
            Log.d("HistoryActivity", "OnDown");
            if (delete != null && delete.getVisibility() == View.VISIBLE)
                delete.setVisibility(View.INVISIBLE);
            return true;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent motionEvent) {
            Log.d("HistoryActivity", "OnSingleTap");
            int tapIndex = getIndexOfGesture(motionEvent);
            View listItemTapped = listView.getChildAt(tapIndex);
            if (listItemTapped != null) {
                TextView product = listItemTapped.findViewById(R.id.productName);
                String productName = product.getText().toString();
                String productURL = getProductURL(productName);
                String productIngredients = getProductIngredients(productName);
                productDB.deleteName(productName);
                productDB.insert(productName, productURL, productIngredients);
                Intent intent = new Intent(HistoryActivity.this, ProductActivity.class);
                startActivity(intent);
            } return true;
        }

        @Override
        public boolean onFling(MotionEvent mEvent1, MotionEvent mEvent2, float velocityX, float velocityY) {
            Log.d("HistoryActivity", "OnFling");
            float diffX = mEvent1.getX() - mEvent2.getX();
            float diffY = mEvent1.getY() - mEvent2.getY();
            if (diffX > diffY) {
                Log.d("HistoryActivity", "OnFling Success");
                showDeleteButton(mEvent1);
            }
            return true;
        }

        private int getIndexOfGesture(MotionEvent motionEvent) {
            int adapterIndex = listView.pointToPosition((int) motionEvent.getX(), (int) motionEvent.getY());
            int firstViewItemIndex = listView.getFirstVisiblePosition();
            int viewIndex = adapterIndex - firstViewItemIndex;
            return viewIndex;
        }

        private String getProductURL(String productName) {
            String productPresent, productURL = "";
            Boolean found = false;
            Cursor cursor = productDB.getAllProducts();
            while (cursor.moveToNext() && !found) {
                productPresent = cursor.getString(1);
                if (productPresent.equals(productName)) {
                    productURL = cursor.getString(2);
                    found = true;
                }
            } return productURL;
        }

        private String getProductIngredients(String productName) {
            String productPresent, productIngredients = "";
            Boolean found = false;
            Cursor cursor = productDB.getAllProducts();
            while (cursor.moveToNext() && !found) {
                productPresent = cursor.getString(1);
                if (productPresent.equals(productName)) {
                    productIngredients = cursor.getString(3);
                    found = true;
                }
            } return productIngredients;
        }

        private void showDeleteButton(MotionEvent motionEvent) {
            int swipeIndex = getIndexOfGesture(motionEvent);
            listItemSwiped = listView.getChildAt(swipeIndex);
            if (listItemSwiped != null) {
                delete = listItemSwiped.findViewById(R.id.deleteButton);
                delete.setVisibility(View.VISIBLE);
                delete.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View view) {
                        TextView product = listItemSwiped.findViewById(R.id.productName);
                        productDeleted = product.getText().toString();
                        prodDelURL = getProductURL(productDeleted);
                        prodDelWarn = getProductIngredients(productDeleted);
                        productDB.deleteName(productDeleted);
                        adapter.clear();
                        displayProductList();
                        Snackbar snackbar = Snackbar.make(historyLayout, "Product deleted", Snackbar.LENGTH_LONG)
                                .setAction("UNDO", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        productDB.insert(productDeleted, prodDelURL, prodDelWarn);
                                        adapter.clear();
                                        displayProductList();
                                        Snackbar undoSnackbar = Snackbar.make(historyLayout, "Product restored", Snackbar.LENGTH_SHORT);
                                        undoSnackbar.show();
                                    }
                                });
                        snackbar.show();
                    }
                });
            }
        }
    }
}

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

public class HistoryActivity extends AppCompatActivity {

    ListView listView;
    ProductDbHelper productDB;
    Cursor cursor;
    ArrayList<String> listItems;
    ArrayAdapter adapter;
    View listItemSwiped;
    GestureDetector gestureDetector;
    ImageButton delete;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        listView = findViewById(R.id.list_view);
        productDB = new ProductDbHelper(this);
        cursor = productDB.getAllProducts();
        listItems = new ArrayList<>();
        if (cursor.getCount() == 0)
            Toast.makeText(this, "No history to display", Toast.LENGTH_LONG).show();
        else {
            displayProductList();
            View productListView = findViewById(R.id.list_view);
            gestureDetector = new GestureDetector(this, new GestureListener());
            productListView.setOnTouchListener(touchListener);
        }
    }

    public void displayProductList() {
        cursor = productDB.getAllProducts();
        while (cursor.moveToNext())
            listItems.add(cursor.getString(1));
        adapter = new ArrayAdapter<>(this, R.layout.list_view_product, R.id.productName, listItems);
        listView.setAdapter(adapter);
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
            return true;
        }

        @Override
        public boolean onFling(MotionEvent mEvent1, MotionEvent mEvent2, float velocityX, float velocityY) {
            Log.d("HistoryActivity", "OnFling");
            float diffX = mEvent1.getX() - mEvent2.getX();
            float diffY = mEvent1.getY() - mEvent2.getY();
            if ((diffX > diffY) && (diffX > minSwipeDistance) && (velocityX > minSwipeVelocity)) {
                Log.d("HistoryActivity", "OnFling Success");
                showDeleteButton(mEvent1);
            }
            return true;
        }

        private void showDeleteButton(MotionEvent motionEvent) {
            int adapterIndex = listView.pointToPosition((int) motionEvent.getX(), (int) motionEvent.getY());
            int firstViewItemIndex = listView.getFirstVisiblePosition();
            int viewIndex = adapterIndex - firstViewItemIndex;
            listItemSwiped = listView.getChildAt(viewIndex);
            delete = listItemSwiped.findViewById(R.id.deleteButton);
            delete.setVisibility(View.VISIBLE);
            delete.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    TextView product = listItemSwiped.findViewById(R.id.productName);
                    String productName = product.getText().toString();
                    productDB.deleteName(productName);
                    adapter.clear();
                    displayProductList();
                }
            });
        }
    }
}

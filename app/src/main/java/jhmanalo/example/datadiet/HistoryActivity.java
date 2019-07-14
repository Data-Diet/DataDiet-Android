package jhmanalo.example.datadiet;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import java.util.List;
import java.util.ArrayList;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.graphics.Canvas;
import android.support.v7.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

public class HistoryActivity extends AppCompatActivity {

    private ProductDataAdapter pAdapter;
    private ProductDbHelper productDb;
    SwipeController swipeController = null;
    private RecyclerView recyclerView;
    private ItemClickListener itemClickListener;
    private boolean updateAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        productDb = new ProductDbHelper(this);
        setProductDataAdapter();
        setRecyclerView();
        updateAdapter = false;
        itemClickListener = new ItemClickListener() {
            @Override
            public void onItemClicked(int position) {
                openProductDetails(position);
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (updateAdapter) {
            Cursor cursor = productDb.getLatestProduct();
            Product lastProdViewed = new Product();
            lastProdViewed.setName(cursor.getString(cursor.getColumnIndex("PRODUCT_NAME")));
            lastProdViewed.setUrl(cursor.getString(cursor.getColumnIndex("PRODUCT_URL")));
            lastProdViewed.setWarnings(cursor.getString(cursor.getColumnIndex("WARNINGS")));
            lastProdViewed.setLabels(cursor.getString(cursor.getColumnIndex("LABELS")));
            pAdapter.products.add(0, lastProdViewed);
            pAdapter.notifyDataSetChanged();
            updateAdapter = false;
        }
    }

    private void setProductDataAdapter() {
        List<Product> products = new ArrayList<>();
        Cursor cursor = productDb.getLatestProduct();
        if ((productDb.getAllProducts()).getCount() > 0) {
            do {
                Product product = new Product();
                product.setName(cursor.getString(cursor.getColumnIndex("PRODUCT_NAME")));
                product.setUrl(cursor.getString(cursor.getColumnIndex("PRODUCT_URL")));
                product.setWarnings(cursor.getString(cursor.getColumnIndex("WARNINGS")));
                product.setLabels(cursor.getString(cursor.getColumnIndex("LABELS")));
                products.add(product);
            } while (cursor.moveToPrevious());
        }
        pAdapter = new ProductDataAdapter(products);
    }

    private void setRecyclerView() {
        recyclerView = findViewById(R.id.recyclerView);

        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(pAdapter);

        swipeController = new SwipeController(new SwipeControllerActions() {
            @Override
            public void onDeleteClicked(int position) {
                final Product delProd = pAdapter.products.get(position);
                final int delIndex = pAdapter.products.indexOf(delProd);
                pAdapter.products.remove(position);
                pAdapter.notifyItemRemoved(position);
                pAdapter.notifyItemRangeChanged(position, pAdapter.getItemCount());
                final ConstraintLayout historyLayout = findViewById(R.id.historyLayout);
                final int delPos = position;
                Snackbar snackbar = Snackbar.make(historyLayout, "Product deleted", Snackbar.LENGTH_LONG)
                        .setAction("UNDO", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                pAdapter.products.add(delIndex, delProd);
                                pAdapter.notifyItemInserted(delPos);
                                pAdapter.notifyItemRangeChanged(delPos, pAdapter.getItemCount());
                                Snackbar undoSnackbar = Snackbar.make(historyLayout, "Product restored", Snackbar.LENGTH_SHORT);
                                undoSnackbar.show();
                            }
                        });
                snackbar.show();
                if (!pAdapter.products.contains(delProd))
                    productDb.deleteName(delProd.getName());
            }
        });

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(swipeController);
        itemTouchHelper.attachToRecyclerView(recyclerView);

        recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
                swipeController.onDraw(c);
            }
        });
    }

    public void showPopUp(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.popUpTitle);
        builder.setMessage(R.string.popUpMessage);
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
        productDb.erase();
        pAdapter.products.clear();
        pAdapter.notifyDataSetChanged();
    }

    public void closeHistory(View view) { finish(); }

    public void onProductTapped(int position) {
        itemClickListener.onItemClicked(position);
    }

    public void openProductDetails(int position) {
        Product viewProd = pAdapter.products.get(position);
        productDb.deleteName(viewProd.getName());
        productDb.insert(viewProd.getName(), viewProd.getUrl(), "", "");
        pAdapter.products.remove(viewProd);
        updateAdapter = true;
        Intent intent = new Intent(HistoryActivity.this, ProductActivity.class);
        startActivity(intent);
    }

    public interface ItemClickListener {
        void onItemClicked(int position);
    }

    public class ProductDataAdapter extends RecyclerView.Adapter<ProductDataAdapter.ProductViewHolder> {
        public List<Product> products;


        public ProductDataAdapter(List<Product> players) {
            this.products = players;
        }

        public class ProductViewHolder extends RecyclerView.ViewHolder {
            private TextView name, warnings, labels;

            public ProductViewHolder(View view) {
                super(view);
                name = view.findViewById(R.id.name);
                warnings = view.findViewById(R.id.warnings);
                labels = view.findViewById(R.id.labels);

                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onProductTapped(getAdapterPosition());
                    }
                });
            }
        }

        @Override
        public ProductViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.product_list_item, parent, false);
            return new ProductViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(ProductViewHolder holder, int position) {
            Product player = products.get(position);
            holder.name.setText(player.getName());
            holder.warnings.setText(player.getWarnings());
            holder.labels.setText(player.getLabels());
        }

        @Override
        public int getItemCount() {
            return products.size();
        }
    }
}


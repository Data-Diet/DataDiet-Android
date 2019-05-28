package jhmanalo.example.datadiet;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.View;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

public class ProductActivity extends AppCompatActivity {

    ProductDbHelper ProductDb;

    Context context;

    Cursor cursor;
    String ProductURL;

    LinearLayout linearLayout;
    ScrollView scrollView;

    ExpandableListView expandableListView;
    ExpandableListAdapter expandableListAdapter;
    List<String> expandableListTitle;
    HashMap<String, List<String>> expandableListDetail;

    ProgressDialog ProgressData;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product);
        context = this;

        ProductDb = new ProductDbHelper(context);

        expandableListDetail = new HashMap<>();

        cursor = ProductDb.getLatestProduct();
        ProductURL = cursor.getString(cursor.getColumnIndex("PRODUCT_URL"));

        new JsonTask().execute(ProductURL);

    }

    public void closeProduct(View view) {
        Intent intent = new Intent(context, MainActivity.class);
        startActivity(intent);
    }

//    public String allergenCheck(String ingredients) {
//        try {
//            StringTokenizer ingrtTknzr = new StringTokenizer(ingredients, ",");
//
//            int i = 0;
//            while (ingrtTknzr.hasMoreTokens()) {
//            }
//
//        } catch (Exception e) {
//
//        }
//    }

    public void displayProduct (JSONObject obj){
        String imageURL = null;
        String productBrand = null;
        String productTitle = null;

        JSONArray JSONlabelArray = null;
        JSONArray JSONIngrArray = null;

        int labelsLen = 0;
        int ingredientsLen = 0;

        try {
            imageURL = obj.getJSONObject("product").get("image_front_url").toString();
            productBrand = obj.getJSONObject("product").get("brands").toString();
            productTitle = productBrand + " " +
                    obj.getJSONObject("product").get("product_name").toString();
        } catch (Exception e) {
            Log.e("Display Data", "error retrieving JSON title data");
        }

        try {
            JSONlabelArray = obj.getJSONObject("product").getJSONArray("labels_hierarchy");
            labelsLen = JSONlabelArray.length();
        } catch (Exception e) {
            Log.e("Display Data", "error retrieving labels JSON data");
        }

        try {
            JSONIngrArray = obj.getJSONObject("product").getJSONArray("ingredients_tags");
            ingredientsLen = JSONIngrArray.length();
        } catch (Exception e) {
            Log.e("Display Data", "error retrieving ingredients JSON data");
        }

        final StyleSpan bss = new StyleSpan(Typeface.BOLD); // Span to make text bold
        final StyleSpan iss = new StyleSpan(Typeface.ITALIC); //Span to make text italic

        SpannableString productTitleText = new SpannableString(productTitle);
        productTitleText.setSpan(new UnderlineSpan(), 0, productBrand.length(), 0);

        linearLayout = findViewById(R.id.itemLayout);
        scrollView = findViewById(R.id.itemScrollLayout);
        ImageView productPicture = findViewById(R.id.productPic);
        TextView productTitleView = findViewById(R.id.productTitle);
        expandableListView = findViewById(R.id.productItemList);


        Log.d("JSON URL", ProductURL);
        Log.d("product ingredients", String.valueOf(ingredientsLen));


        ProductDb.deleteURL(ProductURL);
        ProductDb.insert(productTitle, ProductURL, "TBD");

        productTitleView.setText(productTitleText);

        Picasso.get()
                .load(imageURL)
                .resize(1000,1000)
                .centerCrop()
                .into(productPicture);

        ArrayList<String> labelsList = new ArrayList<>();
        ArrayList<String> ingredientsList = new ArrayList<>();

        makeDropdownList(JSONlabelArray, labelsLen, labelsList, "labels");
        makeDropdownList(JSONIngrArray, ingredientsLen, ingredientsList, "ingredients");

        if (!ingredientsList.isEmpty())
            expandableListDetail.put("Ingredients", ingredientsList);
        if (!labelsList.isEmpty())
            expandableListDetail.put("Labels", labelsList);

        expandableListTitle = new ArrayList<>(expandableListDetail.keySet());

        if (expandableListDetail != null)
            expandableListAdapter = new CustomExpandableListAdapter(this, expandableListTitle, expandableListDetail);

        if (expandableListAdapter != null)
        expandableListView.setAdapter(expandableListAdapter);

        Log.d("My App", obj.toString());
    }

    public void makeDropdownList(JSONArray JArray, int JArrayLen, ArrayList<String> List, String listType) {
        if (JArray != null) {
            int i = 0;
            while (i < JArrayLen) {
                try {
                    List.add(JArray.get(i).toString().substring(3));

                } catch (Exception e) {
                    Log.d("product " + listType, "could not parse element " + i + " from ingredients");
                }
                i++;
            }
        }
    }

    private class JsonTask extends AsyncTask<String, String, String> {

        protected void onPreExecute() {
            super.onPreExecute();

            ProgressData = new ProgressDialog(context);
            ProgressData.setMessage("Checking for Product");
            ProgressData.setCancelable(true);
            ProgressData.show();
        }

        protected String doInBackground(String... params) {


            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();


                InputStream stream = connection.getInputStream();

                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuffer buffer = new StringBuffer();
                String line = "";

                while ((line = reader.readLine()) != null) {
                    buffer.append(line+"\n");
                    Log.d("Response: ", "> " + line);   //here u ll get whole response...... :-)

                }

                return buffer.toString();


            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (ProgressData.isShowing()) {
                ProgressData.dismiss();
            }

            JSONObject obj = null;

            try {
                obj = new JSONObject(result);

            } catch (Throwable t) {
                Log.e("My App", "Could not parse malformed JSON: \"" + result + "\"");
                Toast.makeText(context, "Sorry, product was not found", Toast.LENGTH_LONG).show();
                ProductDb.deleteURL(ProductURL);
                Intent intent = new Intent(context, MainActivity.class);
                startActivity(intent);
            }

            displayProduct(obj);
        }
    }
}

package jhmanalo.example.datadiet;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
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
import java.util.HashSet;
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

    public String allergenCheck(String ingredients) {
        String allergensFound = "";
        SharedPreferences preferences = this.getSharedPreferences(
                "jhmanalo.example.datadiet.activity_settings", Context.MODE_PRIVATE);
        Boolean allergiesChecked = preferences.getBoolean("allergiesChecked", false);
        Boolean veganChecked = preferences.getBoolean("allergiesChecked", false);
        Boolean vegetarianChecked = preferences.getBoolean("allergiesChecked", false);
        Boolean pescatarianChecked = preferences.getBoolean("allergiesChecked", false);
        Log.d("allergy check", allergiesChecked.toString());


        if (allergiesChecked) {
            HashSet<String> allergiesSet = new HashSet();
            StringTokenizer ingredientList = new StringTokenizer(ingredients, ",.:[]() ");
            StringTokenizer allergyList = new StringTokenizer(preferences.getString("allergylist", "not found"), ",.:[]() ");

            while (allergyList.hasMoreTokens()) {
                String allergen = allergyList.nextToken().toLowerCase();
                allergiesSet.add(allergen);
                Log.d("allergy check", allergen);
            }

            while (ingredientList.hasMoreTokens()) {
                String ingredient = ingredientList.nextToken().toLowerCase();
                if (allergiesSet.contains(ingredient)) {
                    Log.d("allergy check", "found: " + ingredient);
                    if (!allergensFound.contains(ingredient))
                        allergensFound += ingredient + ", \n";

                }
                Log.d("allergy check", ingredient);
            }
        }
        return "Allergens found from your preferences: \n" + allergensFound.substring(0, allergensFound.length() - 2);
    }

    public void displayProduct (JSONObject obj){

        JSONObject product = null;

        try {
            product = obj.getJSONObject("product");
        } catch (Exception e) {
            Log.e("Proudct", "error retrieving JSON Product");

        }

        linearLayout = findViewById(R.id.itemLayout);
        scrollView = findViewById(R.id.itemScrollLayout);
        ImageView productPicture = findViewById(R.id.productPic);
        TextView productTitleView = findViewById(R.id.productTitle);
        TextView warningTag = findViewById(R.id.warningTag);
        expandableListView = findViewById(R.id.productItemList);

        String imageURL = null;
        String productBrand = null;
        String productTitle = null;

        JSONArray JSONlabelArray = null;
        JSONArray JSONIngrArray = null;

        int labelsLen = 0;
        int ingredientsLen = 0;

        try {
            productBrand = product.get("brands").toString();
            productTitle = productBrand + " " +
                    product.get("product_name").toString();

            final StyleSpan bss = new StyleSpan(Typeface.BOLD); // Span to make text bold
            final StyleSpan iss = new StyleSpan(Typeface.ITALIC); //Span to make text italic

            SpannableString productTitleText = new SpannableString(productTitle);
            productTitleText.setSpan(new UnderlineSpan(), 0, productBrand.length(), 0);

            ProductDb.deleteURL(ProductURL);
            ProductDb.insert(productTitle, ProductURL, "TBD");

            productTitleView.setText(productTitleText);

        } catch (Exception e) {
            Log.e("Display Data", "error retrieving JSON title data");
            Toast.makeText(context, "Sorry, product was not found", Toast.LENGTH_LONG).show();
            ProductDb.deleteURL(ProductURL);
            Intent intent = new Intent(context, MainActivity.class);
            startActivity(intent);
        }

        try {
            imageURL = product.get("image_front_url").toString();

            Picasso.get()
                    .load(imageURL)
                    .resize(1000,1000)
                    .centerCrop()
                    .into(productPicture);

        } catch (Exception e) {
            Log.e("Display Data", "error retrieving JSON image data");
        }

        try {
            warningTag.setText(allergenCheck(product.getString("ingredients_text")));
        } catch (Exception e) {
            Log.d("allergen check", "error parsing ingredients Text JSON");
        }

        try {
            JSONlabelArray = product.getJSONArray("labels_hierarchy");
            labelsLen = JSONlabelArray.length();

            ArrayList<String> labelsList = new ArrayList<>();

            makeDropdownList(JSONlabelArray, labelsLen, labelsList, "labels");

            if (!labelsList.isEmpty())
                expandableListDetail.put("Labels", labelsList);


        } catch (Exception e) {
            Log.e("Display Data", "error retrieving labels JSON data");
        }

        try {
            JSONIngrArray = product.getJSONArray("ingredients_tags");
            ingredientsLen = JSONIngrArray.length();
        } catch (Exception e) {
            Log.e("Display Data", "error retrieving ingredients JSON data");
        }

        try{
            allergenCheck(product.getString("ingredients_text"));

            ArrayList<String> ingredientsList = new ArrayList<>();

            makeDropdownList(JSONIngrArray, ingredientsLen, ingredientsList, "ingredients");

            if (!ingredientsList.isEmpty())
                expandableListDetail.put("Ingredients", ingredientsList);
        } catch (Exception e) {
            Log.e("Display Data", "error retrieving allergen check");

        }


//        Log.d("JSON URL", ProductURL);
//        Log.d("product ingredients", String.valueOf(ingredientsLen));

        expandableListTitle = new ArrayList<>(expandableListDetail.keySet());

        if (expandableListDetail != null)
            expandableListAdapter = new CustomExpandableListAdapter(this, expandableListTitle, expandableListDetail);

        if (expandableListAdapter != null) {
            expandableListView.setAdapter(expandableListAdapter);
            //Utility.setListViewHeightBasedOnChildren(expandableListView);

        }


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
                if (Integer.parseInt(obj.getString("status")) == 0) {
                    Toast.makeText(context, "Sorry, product was not found", Toast.LENGTH_LONG).show();
                    ProductDb.deleteURL(ProductURL);
                    Intent intent = new Intent(context, MainActivity.class);
                    startActivity(intent);
                } else {
                    displayProduct(obj);
                }

            } catch (Throwable t) {
                Log.e("My App", "Could not parse malformed JSON: \"" + result + "\"");
                Toast.makeText(context, "Sorry, product was not found", Toast.LENGTH_LONG).show();
                ProductDb.deleteURL(ProductURL);
                Intent intent = new Intent(context, MainActivity.class);
                startActivity(intent);
            }

        }
    }
}

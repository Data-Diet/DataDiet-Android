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
import android.widget.SearchView;
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
import java.util.Comparator;
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

    SearchView searchBar;

    JSONObject product;

    JSONArray JSONlabelArray = null;
    JSONArray JSONIngrArray = null;

    int labelsLen = 0;
    int ingredientsLen = 0;

    ArrayList<String> ingredientsList;
    ArrayList<String> labelsList;

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

    public void resetScroll(View view) {
        scrollView.fullScroll(View.FOCUS_UP);
    }

    public String[] allergenCheck(String ingredients) {

        HashSet<String> allergensFound = new HashSet<>();
        SharedPreferences preferences = this.getSharedPreferences(
                "jhmanalo.example.datadiet.activity_settings", Context.MODE_PRIVATE);
        Boolean allergiesChecked = preferences.getBoolean("allergiesChecked", false);
        Boolean veganChecked = preferences.getBoolean("veganChecked", false);
        Boolean vegetarianChecked = preferences.getBoolean("vegetarianChecked", false);
        Boolean pescatarianChecked = preferences.getBoolean("pescatarianChecked", false);
        Log.d("allergy check", allergiesChecked.toString());


        if (allergiesChecked) {
            StringTokenizer ingredientList = new StringTokenizer(ingredients, ",():");

            while (ingredientList.hasMoreTokens()) {
                StringTokenizer allergyList = new StringTokenizer(preferences.getString("allergylist", "not found"), ",():");
                String ingredient = ingredientList.nextToken().toLowerCase().trim();
                while (allergyList.hasMoreTokens()) {
                    String allergen = allergyList.nextToken().toLowerCase().trim();

                    Log.d("allergy check", ingredient + " ->" + allergen);

                    if (ingredient.contains(allergen) && !allergensFound.contains(ingredient))
                        allergensFound.add(ingredient);
                }
            }
        }

        if (allergensFound.isEmpty())
            return new String[0];
        else
            return allergensFound.toArray(new String[allergensFound.size()]);
    }

    public void displayProduct (JSONObject obj){

        product = null;

        try {
            product = obj.getJSONObject("product");
        } catch (Exception e) {
            Log.e("Product", "error retrieving JSON Product");

        }

        searchBar = findViewById(R.id.ingredientSearch);
        linearLayout = findViewById(R.id.itemLayout);
        scrollView = findViewById(R.id.itemScrollLayout);
        ImageView productPicture = findViewById(R.id.productPic);
        TextView productTitleView = findViewById(R.id.productTitle);
        TextView warningTag = findViewById(R.id.warningTag);
        expandableListView = findViewById(R.id.productItemList);

        String imageURL = null;
        String productBrand = null;
        String productTitle = null;

        try {
            productBrand = product.get("brands").toString();
            productTitle = productBrand + "\n" +
                    product.get("product_name").toString();

            final StyleSpan bss = new StyleSpan(Typeface.BOLD); // Span to make text bold
            final StyleSpan iss = new StyleSpan(Typeface.ITALIC); //Span to make text italic

            SpannableString productTitleText = new SpannableString(productTitle);
            productTitleText.setSpan(new UnderlineSpan(), 0, productBrand.length(), 0);

            ProductDb.deleteURL(ProductURL);
            ProductDb.insert(product.get("product_name").toString(), ProductURL, "TBD");

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
            StringBuilder sbLine = new StringBuilder();
            StringBuilder sbUnline = new StringBuilder();

            String warningTextLine = "";
            String warningTextUnline = "";
            String[] allergensFound = allergenCheck(product.getString("ingredients_text"));

            if (allergensFound.length == 0)
                warningTag.setVisibility(View.GONE);
            else {
                for (String allergen: allergensFound) {
                    sbLine.append(allergen);
                    sbLine.append(", ");
                    sbUnline.append(allergen);
                    sbUnline.append(", \n");
                }

                warningTextLine = sbLine.toString().substring(2);
                warningTextUnline = "Allergens found from your preferences: \n" + sbUnline.toString().substring(0, sbUnline.length() - 3);

                warningTag.setText(warningTextUnline);
                ProductDb.deleteURL(ProductURL);
                ProductDb.insert(product.get("product_name").toString(), ProductURL, warningTextLine);
            }

        } catch (Exception e) {
            Log.e("allergen check", e.getLocalizedMessage());
            Log.d("allergen check", "error parsing ingredients Text JSON");
        }

        try {
            JSONlabelArray = product.getJSONArray("labels_hierarchy");
            labelsLen = JSONlabelArray.length();

            labelsList = new ArrayList<>();

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
            ingredientsList = new ArrayList<>();

            makeDropdownList(JSONIngrArray, ingredientsLen, ingredientsList, "ingredients");

            if (!ingredientsList.isEmpty())
                expandableListDetail.put("Ingredients", ingredientsList);
        } catch (Exception e) {
            Log.e("Display Data", "error retrieving allergen check");

        }

        if (JSONIngrArray == null && JSONlabelArray == null)
            searchBar.setVisibility(View.INVISIBLE);

        searchBar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                                               @Override
                                               public boolean onQueryTextChange(String newText) {
                                                   searchItems(newText, product);
                                                   return true;
                                               }

                                               @Override
                                               public boolean onQueryTextSubmit(String query) {
                                                   searchItems(query, product);
                                                   return true;
                                               }
                                           });


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

    public void searchItems(String query, JSONObject product) {
        String noResults = "no results found for: " + query;

        if (query.isEmpty()) {
            if (ingredientsList != null && !ingredientsList.isEmpty()) {
                expandableListDetail.remove("Ingredients");
                expandableListDetail.put("Ingredients", ingredientsList);
            }
            if (labelsList != null && !labelsList.isEmpty()) {
                expandableListDetail.remove("Labels");
                expandableListDetail.put("Labels", labelsList);
            }
            for (int i = 0; i < expandableListDetail.size(); i++) {
                expandableListView.collapseGroup(i);
                expandableListView.expandGroup(i, true);
            }

        } else {
            ArrayList<String> SearchedIngredientsList = new ArrayList();
            ArrayList<String> SearchedLabelsList = new ArrayList();

            if (ingredientsList != null && !ingredientsList.isEmpty()) {
                for (String ingredient : ingredientsList) {
                    if (ingredient.contains(query)) {
                        SearchedIngredientsList.add(ingredient);
                    }
                }

                if (SearchedIngredientsList.isEmpty())
                    SearchedIngredientsList.add(noResults);

                expandableListDetail.remove("Ingredients");
                expandableListDetail.put("Ingredients", SearchedIngredientsList);
            }

            if (labelsList != null && !labelsList.isEmpty()) {
                for (String label : labelsList) {
                    if (label.contains(query)) {
                        SearchedLabelsList.add(label);
                    }
                }
                if (SearchedLabelsList.isEmpty())
                    SearchedLabelsList.add(noResults);

                expandableListDetail.remove("Labels");
                expandableListDetail.put("Labels", SearchedLabelsList);
            }
        }

        for (int i = 0; i < expandableListDetail.size(); i++) {
            expandableListView.collapseGroup(i);
            expandableListView.expandGroup(i, true);
        }
    }

    public void makeDropdownList(JSONArray JArray, int JArrayLen, ArrayList<String> List, String listType) {
        if (JArray != null) {
            int i = 0;
            while (i < JArrayLen) {
                try {
                    List.add(JArray.get(i).toString().substring(3));

                } catch (Exception e) {
                    Log.d("product " + listType, "could not parse element " + i + " from " + listType);
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

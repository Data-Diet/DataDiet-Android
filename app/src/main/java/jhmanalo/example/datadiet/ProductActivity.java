package jhmanalo.example.datadiet;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
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

public class ProductActivity extends AppCompatActivity {

    ProductDbHelper ProductDb;

    Context context;

    LinearLayout linearLayout;
    ScrollView scrollView;

    ProgressDialog ProgressData;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;

        ProductDb = new ProductDbHelper(context);

        Cursor cursor = ProductDb.getLatestProduct();

        new JsonTask().execute(cursor.getString(cursor.getColumnIndex("PRODUCT_URL")));

    }

    private class JsonTask extends AsyncTask<String, String, String> {

        protected void onPreExecute() {
            super.onPreExecute();

            ProgressData = new ProgressDialog(context);
            ProgressData.setMessage("Retrieving product data");
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

            try {

                JSONObject obj = new JSONObject(result);

                linearLayout = findViewById(R.id.itemLayout);
                scrollView = findViewById(R.id.itemScrollLayout);
                ImageView productPicture = findViewById(R.id.productPic);
                TextView productTitleView = findViewById(R.id.productTitle);

                String imageURL = obj.getJSONObject("product").get("image_front_url").toString();
                Log.d("JSON URL", imageURL);
                String productTitle = obj.getJSONObject("product").get("product_name").toString();
                Log.d("JSON Array", String.valueOf(obj.getJSONObject("product").getJSONArray("labels_hierarchy").length()));

                productTitleView.setText(productTitle);

                Picasso.get()
                        .load(imageURL)
                        .resize(1000,1000)
                        .centerCrop()
                        .into(productPicture);

                TextView labelTitle = new TextView(context);
                labelTitle.setPadding(0,10,0,0);
                labelTitle.setText(R.string.labelTitle);
                labelTitle.setTextSize(20);
                labelTitle.setTextColor(getResources().getColor(R.color.black));
                linearLayout.addView(labelTitle);

                int i = 0;
                while (i < obj.getJSONObject("product").getJSONArray("labels_hierarchy").length()) {
                    TextView label = new TextView(context);
                    JSONArray labelArray = (JSONArray) obj.getJSONObject("product").get("labels_hierarchy");
                    label.setText(labelArray.get(i).toString().substring(3));
                    label.setTextColor(getResources().getColor(R.color.black));
                    label.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                    linearLayout.addView(label);
                    i++;
                }

                TextView ingredientsTitle = new TextView(context);
                ingredientsTitle.setPadding(0,10,0,0);
                ingredientsTitle.setText(R.string.ingredientsTitle);
                ingredientsTitle.setTextSize(20);
                ingredientsTitle.setTextColor(getResources().getColor(R.color.black));
                linearLayout.addView(ingredientsTitle);

                int j = 0;
                while (j < obj.getJSONObject("product").getJSONArray("ingredients_tags").length()) {
                    TextView label = new TextView(context);
                    JSONArray labelArray = (JSONArray) obj.getJSONObject("product").get("ingredients_tags");
                    label.setText(labelArray.get(j).toString().substring(3));
                    label.setTextColor(getResources().getColor(R.color.black));
                    label.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                    linearLayout.addView(label);
                    j++;
                }

                scrollView.bringToFront();
                linearLayout.bringToFront();
                scrollView.setVisibility(View.VISIBLE);

                Log.d("My App", obj.toString());

            } catch (Throwable t) {
                Intent intent = new Intent(context, MainActivity.class);
                startActivity(intent);
                Log.e("My App", "Could not parse malformed JSON: \"" + result + "\"");
                Toast.makeText(context, "Sorry, product was not found", Toast.LENGTH_LONG).show();
            }
        }
    }
}

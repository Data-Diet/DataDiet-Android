package jhmanalo.example.datadiet;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetector;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.squareup.picasso.Picasso;
import com.wonderkiln.camerakit.CameraKitError;
import com.wonderkiln.camerakit.CameraKitEvent;
import com.wonderkiln.camerakit.CameraKitEventListener;
import com.wonderkiln.camerakit.CameraKitImage;
import com.wonderkiln.camerakit.CameraKitVideo;
import com.wonderkiln.camerakit.CameraView;

import android.provider.MediaStore;
import android.widget.ImageView;


import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import dmax.dialog.SpotsDialog;


public class MainActivity extends AppCompatActivity {

    private FirebaseDatabase mDatabase;
    private DatabaseReference mGetReference;

    Context context;

    CameraView cameraView;
    Button btnDetect;
    Button btnSettings;
    Button btnImageGallery;
    Button btnHistory;
    AlertDialog waitingDialog;
    ProgressDialog pd;

    LinearLayout linearLayout;
    ScrollView scrollView;

    public final static int PICK_PHOTO_CODE = 1046;

    @Override
    protected void onResume() {
        super.onResume();
        cameraView.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        cameraView.stop();
    }

    public void closeProduct(View view) {
        scrollView.setVisibility(View.INVISIBLE);
        cameraView.start();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        FirebaseApp.initializeApp(this);
        mDatabase = FirebaseDatabase.getInstance();
        mGetReference = mDatabase.getReference();

        context = this;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        cameraView = findViewById(R.id.cameraview);
        btnDetect = findViewById(R.id.btndetect);
        btnImageGallery = findViewById(R.id.btnimagegallery);
        btnSettings = findViewById(R.id.btnsettings);
        btnHistory = findViewById(R.id.btnhistory);


        waitingDialog = new SpotsDialog.Builder()
                .setContext(this)
                .setMessage("Detecting Barcode")
                .setCancelable(false)
                .build();

        btnDetect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraView.start();
                cameraView.captureImage();
            }
        });

        cameraView.addCameraKitListener(new CameraKitEventListener() {
            @Override
            public void onEvent(CameraKitEvent cameraKitEvent) {

            }

            @Override
            public void onError(CameraKitError cameraKitError) {

            }

            @Override
            public void onImage(CameraKitImage cameraKitImage) {
                waitingDialog.show();
                Bitmap bitmap = cameraKitImage.getBitmap();
                bitmap = Bitmap.createScaledBitmap(bitmap, cameraView.getWidth(), cameraView.getHeight(), false);
                cameraView.stop();

                runDetector(bitmap);
            }

            @Override
            public void onVideo(CameraKitVideo cameraKitVideo) {

            }
        });

    }

    private void runDetector(Bitmap bitmap) {
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);
        FirebaseVisionBarcodeDetectorOptions options = new FirebaseVisionBarcodeDetectorOptions.Builder()
                .setBarcodeFormats(
                        FirebaseVisionBarcode.FORMAT_UPC_A//any
                )

                .build();
        FirebaseVisionBarcodeDetector detector = FirebaseVision.getInstance().getVisionBarcodeDetector(options);

        detector.detectInImage(image)
                .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionBarcode>>() {
                    @Override
                    public void onSuccess(List<FirebaseVisionBarcode> firebaseVisionBarcodes) {
                        Log.d("detectInImage", String.valueOf(firebaseVisionBarcodes.size()));
                        if (firebaseVisionBarcodes.size() > 0)
                            processResult(firebaseVisionBarcodes);
                        else {
                            Toast.makeText(context, "Unable to detect valid barcode", Toast.LENGTH_LONG).show();
                            cameraView.start();
                            waitingDialog.dismiss();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT);
                    }
                });
    }

    private void processResult(List<FirebaseVisionBarcode> firebaseVisionBarcodes) {
        for (FirebaseVisionBarcode item : firebaseVisionBarcodes)
        {
            int value_type = item.getValueType();
            Log.d("processResult", String.valueOf(value_type));
            switch (value_type)
            {
                case FirebaseVisionBarcode.TYPE_TEXT:
                {
                    android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this);
                    builder.setMessage(item.getRawValue());
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });
                    android.support.v7.app.AlertDialog dialog = builder.create();
                    dialog.show();
                }
                break;

                case FirebaseVisionBarcode.TYPE_URL:
                {

                }
                break;

                case FirebaseVisionBarcode.TYPE_PRODUCT:
                {
                    new JsonTask().execute("https://world.openfoodfacts.org/api/v1/product/" + item.getRawValue() + ".json");
                }
                break;

                default:
                    break;
            }
        }
        waitingDialog.dismiss();
    }

    public void openSettings(View view) {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    public void openHistory(View view) {
        Intent intent = new Intent(this, HistoryActivity.class);
        startActivity(intent);
    }

    public void openPhotos(View view) {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if (intent.resolveActivity(getPackageManager()) != null)
            startActivityForResult(intent, PICK_PHOTO_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data != null) {
            Uri photoUri = data.getData();
            try {
                Bitmap image = MediaStore.Images.Media.getBitmap(this.getContentResolver(), photoUri);
                runDetector(image);
            } catch (Exception e) {

            }
        }
    }

    private class JsonTask extends AsyncTask<String, String, String> {

        protected void onPreExecute() {
            super.onPreExecute();

            pd = new ProgressDialog(MainActivity.this);
            pd.setMessage("Retrieving product data");
            pd.setCancelable(true);
            pd.show();
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
            if (pd.isShowing()) {
                pd.dismiss();
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
                Log.e("My App", "Could not parse malformed JSON: \"" + result + "\"");
                Toast.makeText(context, "Sorry, product was not found", Toast.LENGTH_LONG).show();
                cameraView.start();
            }
        }
    }
}
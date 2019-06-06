package jhmanalo.example.datadiet;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import static jhmanalo.example.datadiet.OcrDetectorProcessor.results;

public class TextScannerResults extends AppCompatActivity {

    ArrayList<String> result;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_scanner_results);
        //TextView tv = findViewById(R.id.textView2);
        LinearLayout layout = findViewById(R.id.innerLinearLayout);
        result = getIntent().getStringArrayListExtra("ScannedResults");
        Log.d("textScannerResult", "size of array is " + result.size());
        int i = 1;
        if (result.size() == 0)
        {
            TextView tv = new TextView(this);
            tv.setText("May not have found allergens from you preferences.");
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, 100);
            tv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            tv.setLayoutParams(params);
            layout.addView(tv);
        }
        else {
            for (String s : results) {
                Log.d("textScannerResult", s);
                TextView tv = new TextView(this);
                tv.setText(s);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, 100);
                tv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                tv.setLayoutParams(params);
                layout.addView(tv);
                //tv.setText(s);
            }
        }

    }


    public void back(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        results.clear();
    }
}

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

public class TextScannerResults extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_scanner_results);
        //TextView tv = findViewById(R.id.textView2);
        LinearLayout layout = findViewById(R.id.innerLinearLayout);
        ArrayList<String> result = getIntent().getStringArrayListExtra("ScannedResults");
        Log.d("textScannerResult", "size of array is " + result.size());
        int i = 1;
        if (result.size() == 0)
        {
            TextView tv = new TextView(this);
            tv.setText("May not have found allergens from you preferences.");
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.leftMargin = 32;
            params.topMargin = i * 32;
            params.weight = 0;
            tv.setLayoutParams(params);
            layout.addView(tv);
        }
        else {
            for (String s : result) {
                Log.d("textScannerResult", s);
                TextView tv = new TextView(this);
                tv.setText(s);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                params.leftMargin = 32;
                params.topMargin = i * 32;
                params.weight = 0;
                tv.setLayoutParams(params);
                layout.addView(tv);
                i++;
                //tv.setText(s);
            }
        }
    }


    public void back(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}

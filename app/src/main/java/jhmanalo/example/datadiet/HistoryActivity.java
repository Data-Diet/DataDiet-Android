package jhmanalo.example.datadiet;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ScrollView;
import android.widget.LinearLayout;
import android.view.View;

public class HistoryActivity extends AppCompatActivity {

    ScrollView sv;
    LinearLayout ll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        sv = findViewById(R.id.scroll_view);
        ll = findViewById(R.id.linear_layout);
    }

    public void closeHistory(View view) {
        finish();
    }
}

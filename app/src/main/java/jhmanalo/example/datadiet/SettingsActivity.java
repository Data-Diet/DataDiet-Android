package jhmanalo.example.datadiet;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.view.View;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.content.SharedPreferences;

public class SettingsActivity extends AppCompatActivity {

    CheckBox vegan, vegetarian, pescatarian, allergies;
    EditText allergylist;
    public static SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        sp = getSharedPreferences("jhmanalo.example.datadiet.activity_settings", MODE_PRIVATE);
        vegan = findViewById(R.id.vegan_checkbox);
        vegetarian = findViewById(R.id.vegetarian_checkbox);
        pescatarian = findViewById(R.id.pescatarian_checkbox);
        allergies = findViewById(R.id.allergies_checkbox);
        allergylist = findViewById(R.id.allergy_list);
        vegan.setChecked(sp.getBoolean("veganChecked", false));
        vegetarian.setChecked(sp.getBoolean("vegetarianChecked", false));
        pescatarian.setChecked(sp.getBoolean("pescatarianChecked", false));
        allergies.setChecked(sp.getBoolean("allergiesChecked", false));
        if (allergies.isChecked())
            allergylist.setText(sp.getString("allergylist", ""));
        else
            allergylist.setVisibility(View.INVISIBLE);
        allergies.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    allergylist.setVisibility(View.VISIBLE);
                    allergylist.setText(sp.getString("allergylist", ""));
                } else
                    allergylist.setVisibility(View.INVISIBLE);
            }
        });
    }

    public static SharedPreferences getSp()
    {
        return sp;
    }

    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences.Editor edit = sp.edit();
        edit.putBoolean("veganChecked", vegan.isChecked());
        edit.putBoolean("vegetarianChecked", vegetarian.isChecked());
        edit.putBoolean("pescatarianChecked", pescatarian.isChecked());
        edit.putBoolean("allergiesChecked", allergies.isChecked());
        edit.putString("allergylist", allergylist.getText().toString());
        edit.apply();
    }

    public void closeSettings(View view) {
        finish();
    }
}

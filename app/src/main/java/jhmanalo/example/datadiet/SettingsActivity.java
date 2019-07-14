package jhmanalo.example.datadiet;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.view.View;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.content.SharedPreferences;
import android.widget.MultiAutoCompleteTextView;
import android.widget.ArrayAdapter;

public class SettingsActivity extends AppCompatActivity {

    CheckBox vegan, vegetarian, pescatarian, allergies, kosher, other;
    MultiAutoCompleteTextView allergylist;
    EditText otherDiet;
    public static SharedPreferences sp;
    String[] allergenSuggestions = {"acacia gum","alcohol","allura red ac","almond","anchovy","aspartame",
            "baking powder", "baking soda","bamboo","barley","blueberry","bran","brown rice", "buckwheat",
            "butternut squash", "caffeine","caramel","casein", "cashew","celery","cocoa","coconut","corn",
            "crustacean","durum","egg", "fish","flour","fluoride","garlic","gelatin", "gluten",
            "glycerol ester of wood rosin","grain","guarana", "hazelnut","iron","lactic acid","lemon","lime",
            "lupin", "macadamia nut","malt","milk","mollusk", "monosodium glutamate","mushroom","mustard",
            "mycoprotein","nut","nutmeg", "oat","onion","orange", "paprika","peanut","pink pepper","pistachio",
            "pork","protein","salt","sesame","sodium benzoate", "sodium citrate","soy","starch","sugar",
            "sulfite","sulfur dioxide","sunflower","tartrazine","triticale", "vanilla", "vitamin B12","wheat",
            "whey","xanthan","yeast"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        sp = getSharedPreferences("jhmanalo.example.datadiet.activity_settings", MODE_PRIVATE);

        vegan = findViewById(R.id.vegan_checkbox);
        vegetarian = findViewById(R.id.vegetarian_checkbox);
        pescatarian = findViewById(R.id.pescatarian_checkbox);
        kosher = findViewById(R.id.kosher_checkbox);
        other = findViewById(R.id.other_checkbox);
        otherDiet = findViewById(R.id.other_diet);
        allergies = findViewById(R.id.allergies_checkbox);
        allergylist = findViewById(R.id.allergy_list);

        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, allergenSuggestions);
        allergylist.setAdapter(adapter);
        allergylist.setThreshold(1);
        allergylist.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());

        vegan.setChecked(sp.getBoolean("veganChecked", false));
        vegetarian.setChecked(sp.getBoolean("vegetarianChecked", false));
        pescatarian.setChecked(sp.getBoolean("pescatarianChecked", false));
        kosher.setChecked(sp.getBoolean("kosherChecked", false));
        other.setChecked(sp.getBoolean("otherChecked", false));
        allergies.setChecked(sp.getBoolean("allergiesChecked", false));

        otherDiet.setText(sp.getString("otherDiet", ""));

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
        edit.putBoolean("kosherChecked", kosher.isChecked());
        edit.putBoolean("otherChecked", other.isChecked());
        edit.putString("otherDiet", otherDiet.getText().toString());
        edit.putBoolean("allergiesChecked", allergies.isChecked());
        edit.putString("allergylist", allergylist.getText().toString());
        edit.apply();
    }

    public void closeSettings(View view) {
        finish();
    }
}

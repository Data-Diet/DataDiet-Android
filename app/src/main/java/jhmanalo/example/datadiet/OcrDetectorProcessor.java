/*
 * Copyright (C) The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jhmanalo.example.datadiet;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseArray;

//import com.google.android.gms.samples.vision.ocrreader.ui.camera.GraphicOverlay;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.text.TextBlock;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;

import jhmanalo.example.datadiet.camera.GraphicOverlay;

import static android.content.Context.MODE_PRIVATE;

/**
 * A very simple Processor which gets detected TextBlocks and adds them to the overlay
 * as OcrGraphics.
 * TODO: Make this implement Detector.Processor<TextBlock> and add text to the GraphicOverlay
 */
public class OcrDetectorProcessor extends AppCompatActivity implements Detector.Processor<TextBlock> {

    private GraphicOverlay<OcrGraphic> graphicOverlay;

    SettingsActivity settings = new SettingsActivity();

    Boolean allergiesChecked = false;
    String allergies;
    String[] allergyList;
    public static boolean makeRed;
    public static String scanned;
    String[] tokens;
    public static ArrayList<String> results = new ArrayList<>();
    //public SharedPreferences sp = getSharedPreferences("jhmanalo.example.datadiet.activity_settings", MODE_PRIVATE);

    /*@Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        sp = getSharedPreferences("jhmanalo.example.datadiet.activity_settings", MODE_PRIVATE);
        allergiesChecked = sp.getBoolean("allergiesChecked", false);
        Log.d("processor on create", "allergiesChecked is " + allergiesChecked);
        allergies = sp.getString("allergylist", "");
        allergyList = allergies.split(",");
    }*/


    OcrDetectorProcessor(GraphicOverlay<OcrGraphic> ocrGraphicOverlay) {
        graphicOverlay = ocrGraphicOverlay;
    }

    // TODO:  Once this implements Detector.Processor<TextBlock>, implement the abstract methods.
    @Override
    public void receiveDetections(Detector.Detections<TextBlock> detections) {
        //SharedPreferences sp = getSharedPreferences("jhmanalo.example.datadiet.activity_settings", MODE_PRIVATE);
        allergiesChecked = OcrCaptureActivity.allergiesChecked;
        allergyList = OcrCaptureActivity.allergyList;


        graphicOverlay.clear();
        SparseArray<TextBlock> items = detections.getDetectedItems();

        for (int i = 0; i < items.size(); ++i) {
            TextBlock item = items.valueAt(i);
            if (item != null && item.getValue() != null) {
                Log.d("Processor", "Text detected! " + item.getValue());
                OcrGraphic graphic = new OcrGraphic(graphicOverlay, item, makeRed);
                graphicOverlay.add(graphic);

                if (allergiesChecked) {
                    for (String s : allergyList) {

                        if (item.getValue().contains(","))
                        {
                            tokens = item.getValue().split(",");
                        }
                        else
                        {
                            tokens = item.getValue().split(" ");
                        }

                        //String[] tokens = item.getValue().split(" ");
                        //if (item.getValue().contains("Ingredients:") || item.getValue().contains("INGREDIENTS:") || item.getValue().contains("ingredients:")) {
                            Log.d("processor", "lolll");
                            for (String t : tokens) {
                                String fromSharedPref = s.toLowerCase();
                                fromSharedPref = fromSharedPref.trim();

                                String scannedItem = t.toLowerCase();
                                scannedItem = scannedItem.replace("\n", " ");
                                scannedItem = scannedItem.trim();
                                if (scannedItem.equals("ingredients:")) {
                                    continue;
                                }
                                Log.d("processor", "value of fromSharedPref: " + fromSharedPref);
                                Log.d("processor", "value of scannedItem: " + scannedItem);
                                if (scannedItem.contains(fromSharedPref) && !fromSharedPref.equals("") && !scannedItem.contains("ingredient")) {
                                    Log.d("processor", "Warning! Allergen: " + scannedItem + " detected!");
                                    if (!results.contains(scannedItem))
                                    {
                                        Log.d("processor", "added " + scannedItem);
                                        results.add(scannedItem);
                                    }

                                    scanned = scannedItem;
                                    makeRed = true;
                                    OcrGraphic g = new OcrGraphic(graphicOverlay, item, makeRed);
                                    graphicOverlay.add(g);

                                } else {
                                    makeRed = false;
                                }
                            }
                        //}
                    }
                }
            }
        }
    }

    /*public String[] getDetectedWords(Detector.Detections<TextBlock> detections)
    {
        SparseArray<TextBlock> items = detections.getDetectedItems();

        for (int i = 0; i < items.size(); ++i)
        {
            TextBlock item = items.valueAt(i);
        }
    }*/

    @Override
    public void release() {
        graphicOverlay.clear();
    }
}

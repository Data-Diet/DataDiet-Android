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
                OcrGraphic graphic = new OcrGraphic(graphicOverlay, item);
                graphicOverlay.add(graphic);

                if (allergiesChecked) {
                    for (String s : allergyList) {

                        String[] tokens = item.getValue().split(" ");
                        for (String t : tokens) {
                            String fromSharedPref = s.toLowerCase();
                            fromSharedPref = fromSharedPref.trim();

                            String scannedItem = t.toLowerCase();
                            scannedItem = scannedItem.replace("\n", " ");
                            scannedItem = scannedItem.trim();
                            Log.d("processor", "value of s: " + fromSharedPref);
                            Log.d("processor", "value of t: " + scannedItem);
                            if (fromSharedPref.equals(scannedItem)) {
                                Log.d("processor", "shared preference detected: " + scannedItem);
                            }
                        }
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

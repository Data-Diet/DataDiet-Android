package com.example.assignment2;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

public class ImageDatabase extends SQLiteOpenHelper {

    SQLiteDatabase db;
    Context ctx;
    static String DB_NAME = "IMAGE_DATABASE";
    static String TABLE_NAME = "IMAGE_TABLE";
    static int VERSION = 1;

    public ImageDatabase(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        ctx = context;
        VERSION = version;
        DB_NAME = name;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //CREATE TABLE NAME_TABLE (_id INTEGER PRIMARY KEY, FIRST_NAME STRING, LAST_NAME STRING);
        db.execSQL("CREATE TABLE " + TABLE_NAME + "(_id INTEGER PRIMARY KEY, IMAGE_PATH STRING, IMAGE_TITLE STRING);");
        Toast.makeText(ctx, "TABLE IS CREATED", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //DROP TABLE IF EXISTS NAME_TABLE;
        if(VERSION == oldVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME + ";");
            VERSION = newVersion;
            onCreate(db);
            Toast.makeText(ctx, "TABLE IS UPGRAED", Toast.LENGTH_LONG).show();
        }

    }
    public void insert(String imagepath, String imagetitle){
        db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("IMAGE_PATH", imagepath);
        cv.put("IMAGE_TITLE", imagetitle);
        db.insert(TABLE_NAME, null, cv);
    }
    public Cursor view(){
        db = getReadableDatabase();
        //Cursor c = db.query(TABLE_NAME, new String[]{"FIRST_NAME", "LAST_NAME"}, null, null, null, null, "LAST_NAME ASC");
        Cursor c = db.rawQuery("SELECT _id, IMAGE_PATH, IMAGE_TITLE FROM " + TABLE_NAME+";", null);
        return c;
    }

    public void delete(int imageID, String imagetitle) {
        db = getWritableDatabase();
        //db.delete(TABLE_NAME, "FIRST_NAME = ? AND LAST_NAME = ?", new String[]{firstname, lastname});
        db.execSQL("DELETE FROM " + TABLE_NAME + " WHERE _id = \"" + imageID + "\" OR IMAGE_TITLE = \"" + imagetitle + "\";");


    }

    public int count() {
        db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + TABLE_NAME + ";", null);
        return c.getCount();
    }

    public void erase() {
        db = getWritableDatabase();
        onUpgrade(db, VERSION, VERSION + 1);
    }
}

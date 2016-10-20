package com.andreyaleev.metrosong.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by Andrey Aleev on 28.09.2015.
 */
public class MySQLiteHelper extends SQLiteOpenHelper {

    public static final String TABLE_SONGS = "songs";
    public static final String TABLE_SNIPPETS = "snippets";

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_TITLE = "title";

    public static final String COLUMN_BPM = "bpm";
    public static final String COLUMN_SONG_ID = "song_id";
    public static final String COLUMN_BARS_COUNT = "bars_count";
    public static final String COLUMN_BEATS_PER_BAR = "beats_per_bar";
    public static final String COLUMN_NOTE_VALUE = "note_value";

    private static final String DATABASE_NAME = "programmablemetronome.db";
    private static final int DATABASE_VERSION = 1;


    private static final String DATABASE_CREATE_SONGS = "create table "
            + TABLE_SONGS + "("
            + COLUMN_ID + " integer primary key autoincrement, "
            + COLUMN_TITLE + " text not null);";

    private static final String DATABASE_CREATE_SNIPPETS = "create table "
            + TABLE_SNIPPETS + "("
            + COLUMN_ID + " integer primary key autoincrement, "
            + COLUMN_SONG_ID + " integer, "
            + COLUMN_BPM + " integer, "
            + COLUMN_BARS_COUNT + " integer, "
            + COLUMN_BEATS_PER_BAR + " integer, "
            + COLUMN_NOTE_VALUE + " integer);";

    public MySQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE_SONGS);
        database.execSQL(DATABASE_CREATE_SNIPPETS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(MySQLiteHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SONGS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SNIPPETS);
        onCreate(db);
    }


}


package com.amagesoftware.vestibio.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by jkhinda on 26/06/18.
 */

public class MYSQLiteHelperVertibio  extends SQLiteOpenHelper {

        public static final String TABLE_SESSIONS = "sessions";

        public static final String COLUMN_ID = "_id";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_BPM = "bpm";
        public static final String COLUMN_SET_DURATION = "set_duration";
        public static final String COLUMN_SETS = "sets";
        public static final String COLUMN_REST = "rest";
        public static final String COLUMN_NOTES = "notes";
        public static final String COLUMN_TOTAL_DURATION = "total_duration";
        public static final String COLUMN_DIZZY_LEVEL = "dizzy_level";
        public static final String COLUMN_TIMESTAMP = "timestamp";


        public static final String DATABASE_NAME = "physiotherapyemetronome.db";
        private static final int DATABASE_VERSION = 2;


        private static final String DATABASE_CREATE_SESSIONS = "create table "
                + TABLE_SESSIONS + "("
                + COLUMN_ID + " integer primary key autoincrement, "
                + COLUMN_TITLE + " text not null, "
                + COLUMN_BPM + " integer,"
                + COLUMN_SET_DURATION + " integer,"
                + COLUMN_SETS + " integer,"
                + COLUMN_REST + " integer,"
                + COLUMN_TOTAL_DURATION + " integer,"
                + COLUMN_DIZZY_LEVEL + " integer,"
                + COLUMN_TIMESTAMP+ " long,"
                + COLUMN_NOTES +" text);";


        public MYSQLiteHelperVertibio(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase database) {
            database.execSQL(DATABASE_CREATE_SESSIONS);

        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(com.amagesoftware.vestibio.db.MYSQLiteHelperVertibio.class.getName(),
                    "Upgrading database from version " + oldVersion + " to "
                            + newVersion + ", which will destroy all old data");
           // db.execSQL("DROP TABLE IF EXISTS " + TABLE_SESSIONS);
           // onCreate(db);

//            if (newVersion > oldVersion) {
//                db.execSQL("ALTER TABLE TABLE_SESSIONS ADD COLUMN COLUMN_TOTAL_DURATION INTEGER DEFAULT 0");
//            }
            if (oldVersion < 2) {
               // db.execSQL("ALTER TABLE TABLE_SESSIONS ADD COLUMN COLUMN_TOTAL_DURATION INTEGER DEFAULT 0");
            }
            if (oldVersion < 3) {
                //db.execSQL("ALTER TABLE TABLE_SESSIONS ADD COLUMN COLUMN_TOTAL_DURATION INTEGER DEFAULT 0");
            }
        }

    }

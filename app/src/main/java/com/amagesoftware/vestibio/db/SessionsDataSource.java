package com.amagesoftware.vestibio.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.amagesoftware.vestibio.metronome.Session;


import java.util.ArrayList;

/**
 * Created by jkhinda on 26/06/18.
 */

public class SessionsDataSource {
    private SQLiteDatabase database;
    private MYSQLiteHelperVertibio dbHelper;
    private String[] sessionColumns = {
            MYSQLiteHelperVertibio.COLUMN_ID,
            MYSQLiteHelperVertibio.COLUMN_TITLE,
            MYSQLiteHelperVertibio.COLUMN_NOTES,
            MYSQLiteHelperVertibio.COLUMN_BPM,
            MYSQLiteHelperVertibio.COLUMN_DIZZY_LEVEL,
            MYSQLiteHelperVertibio.COLUMN_REST,
            MYSQLiteHelperVertibio.COLUMN_SET_DURATION,
            MYSQLiteHelperVertibio.COLUMN_SETS,
            MYSQLiteHelperVertibio.COLUMN_TIMESTAMP,
            MYSQLiteHelperVertibio.COLUMN_TOTAL_DURATION};

    private String[] dizzyColumn = {
            MYSQLiteHelperVertibio.COLUMN_ID,
            MYSQLiteHelperVertibio.COLUMN_DIZZY_LEVEL,
            };




    public SessionsDataSource(Context context) {
        dbHelper = new MYSQLiteHelperVertibio(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }


    public long updateSession(Session session){

        ContentValues values = new ContentValues();
        values.put(MYSQLiteHelperVertibio.COLUMN_TITLE, session.getTitle());
        values.put(MYSQLiteHelperVertibio.COLUMN_NOTES, session.getNotes());
        values.put(MYSQLiteHelperVertibio.COLUMN_BPM, session.getBpm());
        values.put(MYSQLiteHelperVertibio.COLUMN_DIZZY_LEVEL, session.getDizzynesslevel());
        values.put(MYSQLiteHelperVertibio.COLUMN_REST, session.getRest());
        values.put(MYSQLiteHelperVertibio.COLUMN_SET_DURATION, session.getDuration());
        values.put(MYSQLiteHelperVertibio.COLUMN_SETS, session.getSets());
        values.put(MYSQLiteHelperVertibio.COLUMN_TIMESTAMP, session.getTimeStamp());
        values.put(MYSQLiteHelperVertibio.COLUMN_TOTAL_DURATION, session.getTotalDuration());


        return database.update(MYSQLiteHelperVertibio.TABLE_SESSIONS, values, "_id "+"="+session.getId(), null);
    }

    public long insertSession(Session session) {
        ContentValues values = new ContentValues();
        values.put(MYSQLiteHelperVertibio.COLUMN_TITLE, session.getTitle());
        values.put(MYSQLiteHelperVertibio.COLUMN_NOTES, session.getNotes());
        values.put(MYSQLiteHelperVertibio.COLUMN_BPM, session.getBpm());
        values.put(MYSQLiteHelperVertibio.COLUMN_DIZZY_LEVEL, session.getDizzynesslevel());
        values.put(MYSQLiteHelperVertibio.COLUMN_REST, session.getRest());
        values.put(MYSQLiteHelperVertibio.COLUMN_SET_DURATION, session.getDuration());
        values.put(MYSQLiteHelperVertibio.COLUMN_SETS, session.getSets());
        values.put(MYSQLiteHelperVertibio.COLUMN_TIMESTAMP, session.getTimeStamp());
        values.put(MYSQLiteHelperVertibio.COLUMN_TOTAL_DURATION, session.getTotalDuration());

        long id = database.insert(MYSQLiteHelperVertibio.TABLE_SESSIONS, null,
                values);

        return id;
    }



    public int removeSession(Session session) {
        long id = session.getId();
        int deletedId = database.delete(MYSQLiteHelperVertibio.TABLE_SESSIONS, MYSQLiteHelperVertibio.COLUMN_ID
                + " = " + id, null);
        System.out.println("session deleted with id: " + id);
        return deletedId;
    }


    public ArrayList<Session> getAllSessions() {
        ArrayList<Session> sessions = new ArrayList<>();

        Cursor cursor = database.query(MYSQLiteHelperVertibio.TABLE_SESSIONS,
                sessionColumns, null, null, null, null,MYSQLiteHelperVertibio.COLUMN_TIMESTAMP+" desc" );

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Session session = getSession(cursor);
            sessions.add(session);
            cursor.moveToNext();
        }

        cursor.close();
        return sessions;
    }

    public ArrayList<Session> getsAllDizzySessions() {
        ArrayList<Session> sessions = new ArrayList<>();

        Cursor cursor = database.query(MYSQLiteHelperVertibio.TABLE_SESSIONS,
                dizzyColumn, null, null, null, null,MYSQLiteHelperVertibio.COLUMN_TIMESTAMP+" desc" );

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Session session = getDizzySessions(cursor);
            if(session.getDizzynesslevel()!=0){
                sessions.add(session);
                Log.d("Vestibio", "Database: "+ session.getDizzynesslevel());

            }
            cursor.moveToNext();
        }

        cursor.close();
        return sessions;
    }

    private Session getSession(Cursor cursor) {
        Session session = new Session();
        int columnIndexID = cursor.getColumnIndex(MYSQLiteHelperVertibio.COLUMN_ID);
        int columnIndexTitle = cursor.getColumnIndex(MYSQLiteHelperVertibio.COLUMN_TITLE);
        int columnIndexNotes = cursor.getColumnIndex(MYSQLiteHelperVertibio.COLUMN_NOTES);
        int columnIndexBPM = cursor.getColumnIndex(MYSQLiteHelperVertibio.COLUMN_BPM);
        int columnIndexDizzy = cursor.getColumnIndex(MYSQLiteHelperVertibio.COLUMN_DIZZY_LEVEL);
        int columnIndexRest = cursor.getColumnIndex(MYSQLiteHelperVertibio.COLUMN_REST);
        int columnIndexSetDuration = cursor.getColumnIndex(MYSQLiteHelperVertibio.COLUMN_SET_DURATION);
        int columnIndexSets = cursor.getColumnIndex(MYSQLiteHelperVertibio.COLUMN_SETS);
        int columnIndexTimeStamp = cursor.getColumnIndex(MYSQLiteHelperVertibio.COLUMN_TIMESTAMP);
        int columnIndexTotalDuration = cursor.getColumnIndex(MYSQLiteHelperVertibio.COLUMN_TOTAL_DURATION);


        int id = cursor.getInt(columnIndexID);
        session.setId(id);
        session.setTitle(cursor.getString(columnIndexTitle));
        session.setNotes(cursor.getString(columnIndexNotes));
        session.setBpm(cursor.getInt(columnIndexBPM));
        session.setDizzynesslevel(cursor.getInt(columnIndexDizzy));
        session.setRest(cursor.getInt(columnIndexRest));
        session.setDuration(cursor.getInt(columnIndexSetDuration));
        session.setTimeStamp(cursor.getLong(columnIndexTimeStamp));
        session.setSets(cursor.getInt(columnIndexSets));
        session.setTotalDuration(cursor.getInt(columnIndexTotalDuration));

        return session;
    }

    private Session getDizzySessions(Cursor cursor) {
        Session session = new Session();
        int columnIndexID = cursor.getColumnIndex(MYSQLiteHelperVertibio.COLUMN_ID);

        int columnIndexDizzy = cursor.getColumnIndex(MYSQLiteHelperVertibio.COLUMN_DIZZY_LEVEL);

        int id = cursor.getInt(columnIndexID);
        session.setId(id);
        session.setDizzynesslevel(cursor.getInt(columnIndexDizzy));
        return session;
    }


}

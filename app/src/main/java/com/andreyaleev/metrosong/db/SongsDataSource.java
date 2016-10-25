package com.andreyaleev.metrosong.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.andreyaleev.metrosong.metronome.Song;
import com.andreyaleev.metrosong.metronome.SongSnippet;

import java.util.ArrayList;

/**
 * Created by Andrey Aleev on 28.09.2015.
 */
public class SongsDataSource {

    private SQLiteDatabase database;
    private MySQLiteHelper dbHelper;
    private String[] songsColumns = {
            MySQLiteHelper.COLUMN_ID,
            MySQLiteHelper.COLUMN_TITLE};
    private String[] snippetsColumns = {
            MySQLiteHelper.COLUMN_ID,
            MySQLiteHelper.COLUMN_SONG_ID,
            MySQLiteHelper.COLUMN_BPM,
            MySQLiteHelper.COLUMN_BARS_COUNT,
            MySQLiteHelper.COLUMN_BEATS_PER_BAR,
            MySQLiteHelper.COLUMN_NOTE_VALUE};


    public SongsDataSource(Context context) {
        dbHelper = new MySQLiteHelper(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }


    public long updateSong(Song song){
        removeSnippets(song.getId());

        ContentValues values = new ContentValues();
        values.put(MySQLiteHelper.COLUMN_TITLE, song.getTitle());
        if(song.getSnippets()!=null) {
            for (SongSnippet snippet : song.getSnippets()) {
                long id = insertSnippet(song.getId(), snippet);
                Log.d(this.getClass().getSimpleName(), "snippet " + id + " created");
            }
        }
        return database.update(MySQLiteHelper.TABLE_SONGS, values, "_id "+"="+song.getId(), null);
    }

    public long insertSong(Song song) {
        ContentValues values = new ContentValues();
        values.put(MySQLiteHelper.COLUMN_TITLE, song.getTitle());
        long id = database.insert(MySQLiteHelper.TABLE_SONGS, null,
                values);
        if(song.getSnippets()!=null){
            for(SongSnippet snippet: song.getSnippets()){
                long idSnippet = insertSnippet((int) id, snippet);
                Log.d(this.getClass().getSimpleName(), "snippet " + idSnippet + " created");
            }
        }
        return id;
    }

    public long insertSnippet(int songId, SongSnippet snippet) {
        ContentValues values = new ContentValues();
        values.put(MySQLiteHelper.COLUMN_SONG_ID, songId);
        values.put(MySQLiteHelper.COLUMN_BPM, snippet.getBpm());
        values.put(MySQLiteHelper.COLUMN_BARS_COUNT, snippet.getBarsCount());
        values.put(MySQLiteHelper.COLUMN_BEATS_PER_BAR, snippet.getBeatsPerBar());
        values.put(MySQLiteHelper.COLUMN_NOTE_VALUE, snippet.getNoteValue());

        return database.insert(MySQLiteHelper.TABLE_SNIPPETS, null,
                values);
    }

    public int removeSong(Song song) {
        int id = song.getId();
        int deletedId = database.delete(MySQLiteHelper.TABLE_SONGS, MySQLiteHelper.COLUMN_ID
                + " = " + id, null);
        System.out.println("song deleted with id: " + id);
        removeSnippets(id);
        return deletedId;
    }

    public void removeSnippets(int songId){
        database.delete(MySQLiteHelper.TABLE_SNIPPETS, MySQLiteHelper.COLUMN_SONG_ID
                + " = " + songId, null);
    }

    public ArrayList<Song> getAllSongs() {
        ArrayList<Song> songs = new ArrayList<>();

        Cursor cursor = database.query(MySQLiteHelper.TABLE_SONGS,
                songsColumns, null, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Song song = getSong(cursor);
            songs.add(song);
            cursor.moveToNext();
        }

        cursor.close();
        return songs;
    }

    private Song getSong(Cursor cursor) {
        Song song = new Song();
        int columnIndexID = cursor.getColumnIndex(MySQLiteHelper.COLUMN_ID);
        int columnIndexTitle = cursor.getColumnIndex(MySQLiteHelper.COLUMN_TITLE);
        int id = cursor.getInt(columnIndexID);
        song.setId(id);
        song.setTitle(cursor.getString(columnIndexTitle));
        song.setSnippets(getSnippets4Song(id));
        return song;
    }

    public void showAllSnippets(){
        Cursor cursor = database.query(MySQLiteHelper.TABLE_SNIPPETS,
                snippetsColumns, null, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            SongSnippet snippet = getSnippet(cursor);
            int id = cursor.getInt(cursor.getColumnIndex(MySQLiteHelper.COLUMN_ID));
            String song_id = cursor.getString(cursor.getColumnIndex(MySQLiteHelper.COLUMN_SONG_ID));
            Log.d(this.getClass().getSimpleName(), "id: " + id  + " song_id: " + song_id + " " + snippet.toString());
            cursor.moveToNext();
        }

        cursor.close();
    }

    private ArrayList<SongSnippet> getSnippets4Song(int id){
        ArrayList<SongSnippet> snippets = new ArrayList<>();

        String whereClause = MySQLiteHelper.COLUMN_SONG_ID+" =? ";
        String[] whereArgs = {Integer.toString(id)};

        Cursor cursor = database.query(
                MySQLiteHelper.TABLE_SNIPPETS,
                snippetsColumns,
                whereClause,
                whereArgs,
                null,
                null,
                null
        );

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            SongSnippet snippet = getSnippet(cursor);
            snippets.add(snippet);
            cursor.moveToNext();
        }

        cursor.close();

        return  snippets;
    }

    private SongSnippet getSnippet(Cursor cursor) {
        int columnIndexBPM = cursor.getColumnIndex(MySQLiteHelper.COLUMN_BPM);
        int columnIndexBarsCount = cursor.getColumnIndex(MySQLiteHelper.COLUMN_BARS_COUNT);
        int columnIndexBeats = cursor.getColumnIndex(MySQLiteHelper.COLUMN_BEATS_PER_BAR);
        int columnIndexNoteValue = cursor.getColumnIndex(MySQLiteHelper.COLUMN_NOTE_VALUE);
        SongSnippet snippet = new SongSnippet();
        snippet.setBpm(cursor.getInt(columnIndexBPM));
        snippet.setBarCount(cursor.getInt(columnIndexBarsCount));
        snippet.setBeatsPerBar(cursor.getInt(columnIndexBeats));
        snippet.setNoteValue(cursor.getInt(columnIndexNoteValue));
        return snippet;
    }

}

package com.andreyaleev.metrosong.metronome;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Andrey Aleev on 14.09.2015.
 */
public class Song implements Serializable {

    public Song(int id, String title, ArrayList<SongSnippet> snippets) {
        this.id = id;
        this.title = title;
        this.snippets = snippets;
    }

    public Song() {
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public ArrayList<SongSnippet> getSnippets() {
        return snippets;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setSnippets(ArrayList<SongSnippet> snippets) {
        this.snippets = snippets;
    }

    private int id;
    private String title;
    private ArrayList<SongSnippet> snippets;

    @Override
    public String toString(){
        return "id " + getId() + " title " +  getTitle() + " snippets count " +  getSnippets().size();
    }

}

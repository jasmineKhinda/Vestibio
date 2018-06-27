package com.andreyaleev.metrosong.metronome;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Jasmine Khinda
 */
public class Session implements Serializable {

    public Session(int id, int bpm, String title, int duration, int sets, int rest, int totalDuration, String notes, int dizzynessLevel, long timeStamp) {
        this.id = id;
        this.title = title;
        this.duration = duration;
        this.bpm = bpm;
        this.timeStamp=timeStamp;
    }

    public Session() {
    }

    public long getTimeStamp() {


        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public int getBpm() {
        return bpm;
    }

    public void setBpm(int bpm) {
        this.bpm = bpm;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getSets() {
        return sets;
    }

    public void setSets(int sets) {
        this.sets = sets;
    }

    public int getRest() {
        return rest;
    }

    public void setRest(int rest) {
        this.rest = rest;
    }

    public int getTotalDuration() {
        return totalDuration;
    }

    public void setTotalDuration(int totalDuration) {
        this.totalDuration = totalDuration;
    }

    public int getDizzynesslevel() {
        return dizzynesslevel;
    }

    public void setDizzynesslevel(int dizzynesslevel) {
        this.dizzynesslevel = dizzynesslevel;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }



    private int id;
    private int bpm;
    private String title;
    private String notes;
    private int duration;
    private int sets;
    private int rest;
    private int totalDuration;
    private int dizzynesslevel;
    private long timeStamp;



    @Override
    public String toString(){
        return "id " + getId() + " title " +  getTitle() + " total session time " + totalDuration ;
    }

}

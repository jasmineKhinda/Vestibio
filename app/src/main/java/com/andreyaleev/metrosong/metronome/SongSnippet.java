package com.andreyaleev.metrosong.metronome;

import java.io.Serializable;

/**
 * Metronome snippet with determined duration
 * e.g. piece of song with determined bars count and bpm value
 * Created by Andrey Aleev on 07.09.2015.
 */
public class SongSnippet implements Serializable {

    public SongSnippet() {
    }

    public int getBpm() {
        return bpm;
    }

    public int getBarsCount() {
        return barsCount;
    }

    public int getBeatsPerBar() {
        return beatsPerBar;
    }

    public int getNoteValue() {
        return noteValue;
    }

    public void setBpm(int bpm) {
        this.bpm = bpm;
    }

    public void setBarCount(int barCount) {
        this.barsCount = barCount;
    }

    public void setBeatsPerBar(int beatsPerBar) {
        this.beatsPerBar = beatsPerBar;
    }

    public void setNoteValue(int noteValue) {
        this.noteValue = noteValue;
    }

    /**
     * Metronome snippet (part of song) with determined duration an bpm
     * @param bpm beats per minute
     * @param barsCount duration of how long metronome should play this snippet
     * @param beatsPerBar time signature: beats per bar
     * @param noteValue time signature: note value
     */
    public SongSnippet(int bpm, int barsCount, int beatsPerBar, int noteValue) {
        this.bpm = bpm;
        this.barsCount = barsCount;
        this.beatsPerBar = beatsPerBar;
        this.noteValue = noteValue;
    }

    @Override
    public String toString(){
        return "bpm: " + this.bpm + " barsCount: " + this.barsCount + " beatsPerBar: " + this.beatsPerBar + " noteValue: " + this.noteValue;
    }

    private int bpm;
    private int barsCount;
    private int beatsPerBar;
    private int noteValue;

}

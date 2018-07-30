package com.andreyaleev.vestibio.metronome;

/**
 * Created by Andrey on 24.10.2016.
 */

public class MetronomeSingleton {

    private static MetronomeSingleton mInstance = null;

    private MetronomeSingleton() {
    }

    public static MetronomeSingleton getInstance() {
        if (mInstance == null) {
            Class clazz = MetronomeSingleton.class;
            synchronized (clazz) {
                mInstance = new MetronomeSingleton();
            }
        }
        return mInstance;
    }

    private boolean isPlay;
    private int beat;
    private int noteValue;
    private int bpm;
    private int rest;

    public boolean isPlay() {
        return isPlay;
    }

    public int getBeat() {
        return beat;
    }

    public int getRest() {
        return rest;
    }

    public int getNoteValue() {
        return noteValue;
    }

    public int getBpm() {
        return bpm;
    }

    public void setPlay(boolean play) {
        isPlay = play;
    }

    public void setBeat(int beat) {
        this.beat = beat;
    }

    public void setRest(int rest) {
        this.rest = rest;
    }

    public void setNoteValue(int noteValue) {
        this.noteValue = noteValue;
    }

    public void setBpm(int bpm) {
        this.bpm = bpm;
    }
}

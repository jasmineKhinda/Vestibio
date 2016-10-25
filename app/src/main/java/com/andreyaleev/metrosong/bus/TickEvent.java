package com.andreyaleev.metrosong.bus;

/**
 * Created by Andrey on 24.10.2016.
 */

public class TickEvent {

    public final int currentBeat;
    public final int bpm;
    public final int noteValue;

    public TickEvent(int currentBeat, int bpm, int noteValue) {
        this.currentBeat = currentBeat;
        this.bpm = bpm;
        this.noteValue = noteValue;
    }


    @Override
    public String toString() {
        return "TickEvent{" +
                "currentBeat=" + currentBeat +
                ", bpm=" + bpm +
                ", noteValue=" + noteValue +
                '}';
    }
}

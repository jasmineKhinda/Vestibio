package com.andreyaleev.vestibio.bus;

/**
 * Created by Andrey on 24.10.2016.
 */

public class SongTickEvent extends TickEvent {

    public final int barsCount;
    public final int barsPassed;
    public final int beatsPerBar;
    public final int snippetNumber;

    public SongTickEvent(int currentBeat, int bpm, int noteValue,
                         int barsCount, int barsPassed, int beatsPerBar, int snippetNumber) {
        super(currentBeat, bpm, noteValue);
        this.barsCount = barsCount;
        this.barsPassed = barsPassed;
        this.beatsPerBar = beatsPerBar;
        this.snippetNumber = snippetNumber;
    }

    @Override
    public String toString() {
        return "SongTickEvent{" +
                "currentBeat=" + currentBeat +
                ", bpm=" + bpm +
                ", noteValue=" + noteValue +
                ", barsCount=" + barsCount +
                ", barsPassed=" + barsPassed +
                ", beatsPerBar=" + beatsPerBar +
                ", snippetNumber=" + snippetNumber +
                '}';
    }
}

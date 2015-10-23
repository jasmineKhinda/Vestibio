package pntanasis.android.metronome;

import static pntanasis.android.metronome.MidiNotes.A4;

/**
 * Created by felixdollack on 06.10.15.
 */
public class Note {
    // 0-127
    /*
     * Variables
     */

    private byte initialVolume = 100;
    private boolean accented;
    private boolean dotted;
    private byte noteVolume, noteNumber, noteDur, noteDecay;
    private double f;
    private static byte standardPitch;

    static {
        standardPitch = A4;
    }

    /*
     * Constructors
     */
    public Note( byte midiNumber ) {
        this.f = MidiNotes.frequency( midiNumber );
    }

    public Note( byte midiNumber, byte duration ) {
        this.f = MidiNotes.frequency( midiNumber );
        this.noteDur = duration;
    }

    public Note() {
        this.accented = false;
        this.dotted = false;
        this.noteVolume = initialVolume;
        this.noteNumber = Note.standardPitch;
        this.noteDur = 1;
        this.noteDecay = 0;
    }

    /*
     * set function if user changes default volume in settings
     */
    public void setInitialVolume( byte vol ) {
        this.initialVolume = vol;
    }
}

package com.amagesoftware.vestibio;

/**
 * Created by Andrey Aleev on 13.09.2015.
 */
public class Constants {

    public final static String PREFS = "programmable_metronome_preferences";
    public final static String PREFS_BACKUP = "backup_preferences";

    public static final int SERVICE_NOTIFICATION_ID = 5505;

    public final static short BPM_MIN = 1;
    public final static short BPM_MAX = 260;
    public final static short BPM_DEFAULT = 100;

    public final static String BPM = "BPM";
    public final static String BEAT_PER_BAR = "BEAT_PER_BAR";
    public final static String NOTE_VALUE = "NOTE_VALUE";

    public final static String SELECTED_BPM = "SELECTED_BPM";
    public final static String SELECTED_BEAT_PER_BAR = "SELECTED_BEAT_PER_BAR";
    public final static String SELECTED_NOTE_VALUE = "SELECTED_NOTE_VALUE";
    public final static String SELECTED_REST_VALUE = "SELECTED_REST_VALUE";

    public final static String ACTION_PARAMS_CHANGED = "com.andreyaleev.metrosong.ACTION_PARAMS_CHANGED";

    public static final String ACTION_STOP = "com.andreyaleev.metrosong.ACTION_STOP";

    public final static String LAST_BACKUP = "LAST_BACKUP";

}

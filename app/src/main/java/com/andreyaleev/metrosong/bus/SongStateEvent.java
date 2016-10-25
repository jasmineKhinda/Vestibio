package com.andreyaleev.metrosong.bus;

/**
 * Created by Andrey on 25.10.2016.
 */

public class SongStateEvent {

    public final boolean trackIsRunning;

    public SongStateEvent(boolean trackIsRunning) {
        this.trackIsRunning = trackIsRunning;
    }
}

package com.andreyaleev.metrosong.fragments;

import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.andreyaleev.metrosong.activities.MainActivity;

/**
 * Created by Andrey Aleev on 11.09.2015.
 */
public abstract class MetronomableFragment extends Fragment {

    protected short bpm = 100;
    protected short noteValue = 4;
    protected short beats = 4;
    protected short volume;
    protected short initialVolume;
    protected double beatSound = 2440;
    protected double sound = 6440;
    protected AudioManager audio;
    protected MainActivity activity;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        activity = (MainActivity) getActivity();
        audio = (AudioManager) activity.getSystemService(Context.AUDIO_SERVICE);
        initialVolume = (short) audio.getStreamVolume(AudioManager.STREAM_MUSIC);
        volume = initialVolume;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

}

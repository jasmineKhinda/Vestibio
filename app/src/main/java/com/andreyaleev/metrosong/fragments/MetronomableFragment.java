package com.andreyaleev.metrosong.fragments;

import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;

import com.andreyaleev.metrosong.tools.Utils;

/**
 * Created by Andrey on 25.10.2016.
 */

public abstract class MetronomableFragment extends BaseFragment {

    protected short initialVolume;
    protected AudioManager audio;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        audio = (AudioManager) activity.getSystemService(Context.AUDIO_SERVICE);
        initialVolume = (short) audio.getStreamVolume(AudioManager.STREAM_MUSIC);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Utils.checkAndStopService(getContext());
        audio.setStreamVolume(AudioManager.STREAM_MUSIC, initialVolume, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
    }
}

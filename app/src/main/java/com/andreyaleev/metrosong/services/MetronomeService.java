package com.andreyaleev.metrosong.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;

import com.andreyaleev.metrosong.App;
import com.andreyaleev.metrosong.Constants;
import com.andreyaleev.metrosong.bus.SongStateEvent;
import com.andreyaleev.metrosong.bus.SongTickEvent;
import com.andreyaleev.metrosong.bus.TickEvent;
import com.andreyaleev.metrosong.metronome.MetronomeSingleton;
import com.andreyaleev.metrosong.metronome.SongSnippet;

import java.util.ArrayList;

import pntanasis.android.metronome.AudioGenerator;
import pntanasis.android.metronome.MidiNotes;

/**
 * Created by Andrey on 23.10.2016.
 */

public class MetronomeService extends Service {

    public static final String SNIPPETS_KEY = "SNIPPETS_KEY";

    private int silence;
    protected double beatSound;
    protected double sound;
    private final int tick = 1000; // samples of tick
    private AudioGenerator audioGenerator = new AudioGenerator(8000);
    private double[] silenceSoundArray;
    private double[] soundTickArray;
    private double[] soundTockArray;
    private int currentBeat = 1;
    private Message msg;
    public MetronomeThread metronomeThread;
    public SongThread songThread;

    private ArrayList<SongSnippet> snippets;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.ACTION_PARAMS_CHANGED);
        registerReceiver(parametersReceiver, filter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                snippets = (ArrayList<SongSnippet>) bundle.getSerializable(SNIPPETS_KEY);
            }
        }
        this.audioGenerator.createPlayer();
        this.beatSound = MidiNotes.frequency(MidiNotes.A5);
        this.sound = MidiNotes.frequency(MidiNotes.FisGb5);

        if (snippets == null) {
            calcSilence();
            this.metronomeThread = new MetronomeThread();
            this.metronomeThread.start();
        } else {
            this.songThread = new SongThread();
            this.songThread.start();
        }

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(parametersReceiver);
        App.getBus().unregister(this);
        audioGenerator.destroyAudioTrack();
        if (metronomeThread != null) {
            metronomeThread.interrupt();
            metronomeThread = null;
        }
        if (songThread != null) {
            songThread.interrupt();
            songThread = null;
        }
    }

    private final BroadcastReceiver parametersReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equals(Constants.ACTION_PARAMS_CHANGED)){
                MetronomeSingleton.getInstance().setPlay(false);
                calcSilence();
                MetronomeSingleton.getInstance().setPlay(true);
            }
        }
    };

    synchronized public void calcSilence() {
        float tmp = ((60f / MetronomeSingleton.getInstance().getBpm()) * 8000);
        silence = (int) (tmp - tick);
        soundTickArray = new double[this.tick];
        soundTockArray = new double[this.tick];
        silenceSoundArray = new double[this.silence];
        msg = new Message();
        msg.obj = "" + currentBeat;
        double[] tick = audioGenerator.getSineWave(this.tick, 8000, beatSound);
        double[] tock = audioGenerator.getSineWave(this.tick, 8000, sound);
        for (int i = 0; i < this.tick; i++) {
            soundTickArray[i] = tick[i];
            soundTockArray[i] = tock[i];
        }
    }

    private class MetronomeThread extends Thread {
        @Override
        public void run() {
            super.run();
            while (MetronomeSingleton.getInstance().isPlay()) {
                if (currentBeat == 1) {
                    audioGenerator.writeSound(soundTickArray);
                } else {
                    audioGenerator.writeSound(soundTockArray);
                }
                App.getBus().post(new TickEvent(currentBeat, MetronomeSingleton.getInstance().getBpm(),
                        MetronomeSingleton.getInstance().getNoteValue()));
                audioGenerator.writeSilence(silence);
                currentBeat++;
                if (currentBeat > MetronomeSingleton.getInstance().getBeat()) {
                    currentBeat = 1;
                }
            }
        }
    }

    private class SongThread extends Thread {
        @Override
        public void run() {
            super.run();
            double[] tickArray = audioGenerator.getSineWave(tick, 8000, beatSound);
            double[] tockArray = audioGenerator.getSineWave(tick, 8000, sound);

            App.getBus().post(new SongStateEvent(true));

            for (int k = 0; k < snippets.size(); k++) {
                SongSnippet snippet = snippets.get(k);
                int bpm = snippet.getBpm();
                int barsCount = snippet.getBarsCount();
                int beatsPerBar = snippet.getBeatsPerBar();
                int noteValue = snippet.getNoteValue();

                // init arrays for snippet
                int silenceCoef = 480000 / bpm;
                int silence = silenceCoef - tick;
                double[] soundTickArray = new double[tick];
                double[] soundTockArray = new double[tick];
                double[] silenceSoundArray = new double[silence];
                for (int i = 0; i < tick; i++) {
                    soundTickArray[i] = tickArray[i];
                    soundTockArray[i] = tockArray[i];
                }
                for (int i = 0; i < silence; i++) {
                    silenceSoundArray[i] = 0;
                }

                int currentBeatProgram = 1;
                int i = 0;
                while (i < barsCount) {
                    App.getBus().post(new SongTickEvent(currentBeatProgram, bpm, noteValue,
                            barsCount, i, beatsPerBar, k));

                    if (currentBeatProgram == 1) {
                        audioGenerator.writeSound(soundTickArray);
                    } else {
                        audioGenerator.writeSound(soundTockArray);
                    }
                    audioGenerator.writeSilence(silence);

                    currentBeatProgram++;
                    if (currentBeatProgram > beatsPerBar) {
                        currentBeatProgram = 1;
                        i++;
                    }
                }
            }
            App.getBus().post(new SongStateEvent(false));
        }
    }

}

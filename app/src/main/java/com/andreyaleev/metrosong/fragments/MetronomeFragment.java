package com.andreyaleev.metrosong.fragments;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.andreyaleev.metrosong.Constants;
import com.andreyaleev.metrosong.R;
import com.andreyaleev.metrosong.activities.MainActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import pntanasis.android.metronome.Beats;
import pntanasis.android.metronome.Metronome;
import pntanasis.android.metronome.MidiNotes;
import pntanasis.android.metronome.NoteValues;

/**
 * Created by Andrey Aleev on 09.09.2015.
 */
public class MetronomeFragment extends MetronomableFragment {

    private final static String TAG = "MetronomeFragment";

    private View contentView;

    @BindView(R.id.tvBPM)
    TextView tvBPM;
    @BindView(R.id.btnPlus)
    Button btnPlus;
    @BindView(R.id.btnMinus)
    Button btnMinus;
    @BindView(R.id.spinnerBeat)
    Spinner spinnerBeat;
    @BindView(R.id.spinnerNote)
    Spinner spinnerNote;
    @BindView(R.id.seekbarBPM)
    SeekBar seekbarBPM;
    @BindView(R.id.btnStartStop)
    Button btnStartStop;

    private MetronomeAsyncTask metroTask;

    public static MetronomeFragment newInstance() {
        MetronomeFragment frag = new MetronomeFragment();
        return frag;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_metronome, null);
        contentView = v;
        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        activity = (MainActivity) getActivity();
        audio = (AudioManager) activity.getSystemService(Context.AUDIO_SERVICE);
        initialVolume = (short) audio.getStreamVolume(AudioManager.STREAM_MUSIC);
        volume = initialVolume;
        metroTask = new MetronomeAsyncTask(beats, noteValue, bpm);

        Integer savedBPM = activity.getSettings().getInt(Constants.BPM, bpm);
        if (savedBPM != null) {
            bpm = ((short) (int) savedBPM);
        }

        tvBPM.setText(String.valueOf(bpm));

        btnPlus.setOnClickListener(view -> onPlusClick());
        btnMinus.setOnClickListener(view -> onMinusClick());

        ArrayAdapter<Beats> arrayBeats = new ArrayAdapter<Beats>(activity, android.R.layout.simple_spinner_item, Beats.values());
        spinnerBeat.setAdapter(arrayBeats);
        spinnerBeat.setSelection(Beats.four.ordinal());
        arrayBeats.setDropDownViewResource(R.layout.spinner_dropdown);
        spinnerBeat.setOnItemSelectedListener(beatsSpinnerListener);

        ArrayAdapter<NoteValues> noteValues =
                new ArrayAdapter<NoteValues>(getActivity(),
                        android.R.layout.simple_spinner_item, NoteValues.values());
        spinnerNote.setAdapter(noteValues);
        spinnerNote.setSelection(NoteValues.four.ordinal());
        noteValues.setDropDownViewResource(R.layout.spinner_dropdown);
//        spinnerNote.setOnItemSelectedListener(noteValuesSpinnerListener);

        seekbarBPM.getProgressDrawable().setColorFilter(
                new PorterDuffColorFilter(getResources().getColor(R.color.primary), PorterDuff.Mode.SRC_ATOP));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            seekbarBPM.getThumb().setColorFilter(getResources().getColor(R.color.primary), PorterDuff.Mode.SRC_ATOP);
        }

        seekbarBPM.setProgress((bpm - 40));
        seekbarBPM.setOnSeekBarChangeListener(bpmSeekBarListener);

        btnStartStop.setOnClickListener(view -> onStartStopClick());

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            btnPlus.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_background));
            btnMinus.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_background));
        }
    }

    public synchronized void onStartStopClick() {
        if (metroTask != null && metroTask.getStatus() != AsyncTask.Status.RUNNING) {
            activity.getSlidingTabLayout().setVisibility(View.GONE);
            activity.getViewPager().setPagingEnabled(false);
            btnStartStop.setText(R.string.stop);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                metroTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[]) null);
            } else {
                metroTask.execute();
            }
            contentView.setKeepScreenOn(true);
        } else {
            activity.getSlidingTabLayout().setVisibility(View.VISIBLE);
            activity.getViewPager().setPagingEnabled(true);
            btnStartStop.setText(R.string.start);
            metroTask.stop();
            metroTask = new MetronomeAsyncTask(beats, noteValue, bpm);
            Runtime.getRuntime().gc();
            contentView.setKeepScreenOn(false);
        }
    }

    private void maxBpmGuard() {
        if (bpm >= Constants.BPM_MAX) {
            btnPlus.setEnabled(false);
            btnPlus.setPressed(false);
        } else if (!btnMinus.isEnabled() && bpm > Constants.BPM_MIN) {
            btnMinus.setEnabled(true);
        }
    }

    public void onPlusClick() {
        bpm++;
        tvBPM.setText(String.valueOf(bpm));
        metroTask.setBpm(bpm);
        maxBpmGuard();
    }

    private void minBpmGuard() {
        if (bpm <= Constants.BPM_MIN) {
            btnMinus.setEnabled(false);
            btnMinus.setPressed(false);
        } else if (!btnPlus.isEnabled() && bpm < Constants.BPM_MAX) {
            btnPlus.setEnabled(true);
        }
    }

    public void onMinusClick() {
        bpm--;
        tvBPM.setText(String.valueOf(bpm));
        metroTask.setBpm(bpm);
        minBpmGuard();
    }

    private SeekBar.OnSeekBarChangeListener bpmSeekBarListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            bpm = (short) (((short) progress) + 40);
            tvBPM.setText(String.valueOf(bpm));
            metroTask.setBpm(bpm);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        }
    };

    private AdapterView.OnItemSelectedListener beatsSpinnerListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            Beats beat = (Beats) arg0.getItemAtPosition(arg2);
            beats = beat.getNum();
            if (beats > noteValue) {
                beats = noteValue; // TODO fixme =(
            }
            metroTask.setBeat(beats);
        }

        @Override
        public void onNothingSelected(AdapterView<?> arg0) {
        }
    };


    private AdapterView.OnItemSelectedListener noteValuesSpinnerListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            NoteValues noteValue = (NoteValues) arg0.getItemAtPosition(arg2);
//            metroTask.setNoteValue(noteValue.getNum());
        }

        @Override
        public void onNothingSelected(AdapterView<?> arg0) {
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        // save bpm
        if (activity != null)
            activity.getSettings().edit().putInt(Constants.BPM, bpm).apply();
        metroTask.stop();
        metroTask.cancel(true);
        Runtime.getRuntime().gc();
        audio.setStreamVolume(AudioManager.STREAM_MUSIC, initialVolume, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
    }

    private class MetronomeAsyncTask extends AsyncTask<Void, Void, String> {

        private Metronome metronome;
        private short beatsPerBar;
        private short noteValue;
        private int bpm;

        MetronomeAsyncTask(short beatsPerBar, short noteValue, int bpm) {
            this.metronome = new Metronome();
            this.beatsPerBar = beatsPerBar;
            this.noteValue = noteValue;
            this.bpm = bpm;
        }

        protected String doInBackground(Void... params) {
            metronome.setBeat(beats);
            metronome.setNoteValue(noteValue);
            metronome.setBpm(bpm);
            metronome.setBeatSound(MidiNotes.frequency(MidiNotes.A5));
            metronome.setSound(MidiNotes.frequency(MidiNotes.FisGb5));
            metronome.play();
            return null;
        }

        public void stop() {
            metronome.stop();
            metronome = null;
        }

        public void setBpm(short bpm) {
            metronome.setBpm(bpm);
            metronome.calcSilence();
        }

        public void setBeat(short beat) {
            if (metronome != null)
                metronome.setBeat(beat);
        }

    }

}

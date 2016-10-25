package com.andreyaleev.metrosong.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.andreyaleev.metrosong.BuildConfig;
import com.andreyaleev.metrosong.Constants;
import com.andreyaleev.metrosong.R;
import com.andreyaleev.metrosong.activities.MainActivity;
import com.andreyaleev.metrosong.bus.TickEvent;
import com.andreyaleev.metrosong.metronome.MetronomeSingleton;
import com.andreyaleev.metrosong.services.MetronomeService;
import com.andreyaleev.metrosong.tools.Utils;
import com.squareup.otto.Subscribe;

import butterknife.BindView;
import butterknife.ButterKnife;
import pntanasis.android.metronome.Beats;
import pntanasis.android.metronome.NoteValues;

/**
 * Created by Andrey Aleev on 09.09.2015.
 */
public class MetronomeFragment extends MetronomableFragment {

    private View contentView;
    private SharedPreferences prefs;

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


    @Subscribe
    public void onTickEvent(TickEvent event) {
        if (BuildConfig.DEBUG) {
            Log.d(this.getClass().getSimpleName(), event.toString());
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        activity = (MainActivity) getActivity();
        prefs = activity.getSettings();

        MetronomeSingleton.getInstance().setBeat(prefs.getInt(Constants.SELECTED_BEAT_PER_BAR, 4));
        MetronomeSingleton.getInstance().setNoteValue(prefs.getInt(Constants.SELECTED_NOTE_VALUE, 4));
        int savedBPM = prefs.getInt(Constants.SELECTED_BPM, Constants.BPM_DEFAULT);
        MetronomeSingleton.getInstance().setBpm(savedBPM);

        tvBPM.setText(String.valueOf(savedBPM));

        btnPlus.setOnClickListener(view -> onPlusClick());
        btnMinus.setOnClickListener(view -> onMinusClick());

        ArrayAdapter<Beats> arrayBeats = new ArrayAdapter<>(activity, android.R.layout.simple_spinner_item, Beats.values());
        spinnerBeat.setAdapter(arrayBeats);
        spinnerBeat.setSelection(Beats.four.ordinal());
        arrayBeats.setDropDownViewResource(R.layout.spinner_dropdown);
        spinnerBeat.setOnItemSelectedListener(beatsSpinnerListener);

        ArrayAdapter<NoteValues> noteValues =
                new ArrayAdapter<>(getActivity(),
                        android.R.layout.simple_spinner_item, NoteValues.values());
        spinnerNote.setAdapter(noteValues);
        spinnerNote.setSelection(NoteValues.four.ordinal());
        noteValues.setDropDownViewResource(R.layout.spinner_dropdown);

        seekbarBPM.getProgressDrawable().setColorFilter(
                new PorterDuffColorFilter(getResources().getColor(R.color.primary), PorterDuff.Mode.SRC_ATOP));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            seekbarBPM.getThumb().setColorFilter(getResources().getColor(R.color.primary), PorterDuff.Mode.SRC_ATOP);
        }

        seekbarBPM.setProgress((savedBPM - 40));
        seekbarBPM.setOnSeekBarChangeListener(bpmSeekBarListener);

        btnStartStop.setOnClickListener(view -> onStartStopClick());

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            btnPlus.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_background));
            btnMinus.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_background));
        }
    }

    public synchronized void onStartStopClick() {
        if (Utils.isMetronomeServiceRunning(getContext())) {
            stopMetronome();
        } else {
            startMetronome();
        }
    }

    private void maxBpmGuard() {
        int bpm = MetronomeSingleton.getInstance().getBpm();
        if (bpm >= Constants.BPM_MAX) {
            btnPlus.setEnabled(false);
            btnPlus.setPressed(false);
        } else if (!btnMinus.isEnabled() && bpm > Constants.BPM_MIN) {
            btnMinus.setEnabled(true);
        }
    }

    public void onPlusClick() {
        int currentBPM = MetronomeSingleton.getInstance().getBpm();
        int newBPM = currentBPM + 1;
        prefs.edit().putInt(Constants.SELECTED_BPM, newBPM).apply();
        MetronomeSingleton.getInstance().setBpm(newBPM);
        tvBPM.setText(String.valueOf(newBPM));
        updateMetronome();
        maxBpmGuard();
    }

    public void onMinusClick() {
        int currentBPM = MetronomeSingleton.getInstance().getBpm();
        int newBPM = currentBPM - 1;
        prefs.edit().putInt(Constants.SELECTED_BPM, newBPM).apply();
        MetronomeSingleton.getInstance().setBpm(newBPM);
        tvBPM.setText(String.valueOf(newBPM));
        updateMetronome();
        minBpmGuard();
    }

    private void updateMetronome() {
        if (Utils.isMetronomeServiceRunning(getContext())) {
            Intent intent = new Intent(Constants.ACTION_PARAMS_CHANGED);
            getContext().sendBroadcast(intent);
        }
    }

    private void stopMetronome() {
        MetronomeSingleton.getInstance().setPlay(false);
        Utils.checkAndStopService(getContext());
        activity.getSlidingTabLayout().setVisibility(View.VISIBLE);
        activity.getViewPager().setPagingEnabled(true);
        btnStartStop.setText(R.string.start);
        contentView.setKeepScreenOn(false);
    }

    private void startMetronome() {
        btnStartStop.setText(R.string.stop);
        MetronomeSingleton.getInstance().setPlay(true);
        activity.getSlidingTabLayout().setVisibility(View.GONE);
        activity.getViewPager().setPagingEnabled(false);
        Intent myIntent = new Intent(getContext(), MetronomeService.class);
        getActivity().startService(myIntent);
        contentView.setKeepScreenOn(true);
    }

    private void minBpmGuard() {
        int bpm = MetronomeSingleton.getInstance().getBpm();
        if (bpm <= Constants.BPM_MIN) {
            btnMinus.setEnabled(false);
            btnMinus.setPressed(false);
        } else if (!btnPlus.isEnabled() && bpm < Constants.BPM_MAX) {
            btnPlus.setEnabled(true);
        }
    }

    private SeekBar.OnSeekBarChangeListener bpmSeekBarListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            int bpm = (short) (((short) seekBar.getProgress()) + 40);
            tvBPM.setText(String.valueOf(bpm));
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            int bpm = (short) (((short) seekBar.getProgress()) + 40);
            MetronomeSingleton.getInstance().setBpm(bpm);
            prefs.edit().putInt(Constants.SELECTED_BPM, bpm).apply();
            updateMetronome();
        }
    };

    private AdapterView.OnItemSelectedListener beatsSpinnerListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            Beats beat = (Beats) arg0.getItemAtPosition(arg2);
            int beats = beat.getNum();
            int noteValue = prefs.getInt(Constants.SELECTED_NOTE_VALUE, 4);
            if (beats > noteValue) {
                beats = noteValue; // TODO fixme =(
            }
            MetronomeSingleton.getInstance().setBeat(beats);
            prefs.edit().putInt(Constants.SELECTED_BEAT_PER_BAR, beats).apply();
            updateMetronome();
        }

        @Override
        public void onNothingSelected(AdapterView<?> arg0) {
        }
    };


    private AdapterView.OnItemSelectedListener noteValuesSpinnerListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            NoteValues noteValueObj = (NoteValues) arg0.getItemAtPosition(arg2);
            int noteValue = noteValueObj.getNum();
            MetronomeSingleton.getInstance().setNoteValue(noteValue);
            prefs.edit().putInt(Constants.SELECTED_NOTE_VALUE, noteValue).apply();
            updateMetronome();
        }

        @Override
        public void onNothingSelected(AdapterView<?> arg0) {
        }
    };

}

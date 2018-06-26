package com.andreyaleev.metrosong.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.text.Layout;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.andreyaleev.metrosong.BuildConfig;
import com.andreyaleev.metrosong.Constants;
import com.andreyaleev.metrosong.R;
import com.andreyaleev.metrosong.activities.MainActivity;
import com.andreyaleev.metrosong.adapters.CustomSpinnerAdapter;
import com.andreyaleev.metrosong.bus.TickEvent;
import com.andreyaleev.metrosong.metronome.MetronomeSingleton;
import com.andreyaleev.metrosong.services.MetronomeService;
import com.andreyaleev.metrosong.tools.Utils;
import com.squareup.otto.Subscribe;
import com.travijuu.numberpicker.library.Enums.ActionEnum;
import com.travijuu.numberpicker.library.Interface.LimitExceededListener;
import com.travijuu.numberpicker.library.Interface.ValueChangedListener;
import com.travijuu.numberpicker.library.NumberPicker;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

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
    private NumberPicker numberPicker;
    private NumberPicker numberPickerSets;
    private NumberPicker numberPickerRest;
    private int currentSet=0;
    private boolean isCanceled=false;

    TextView timer;
    TextView totalTime;
    @BindView(R.id.tvBPM)
    TextView tvBPM;
    @BindView(R.id.btnPlus)
    Button btnPlus;
    @BindView(R.id.btnMinus)
    Button btnMinus;
    @BindView(R.id.number_picker)
    NumberPicker spinnerBeat;
    @BindView(R.id.sets_picker)
    NumberPicker spinnerNote;
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
        timer= (TextView)v.findViewById(R.id.timer);
        totalTime= (TextView)v.findViewById(R.id.totalSession);
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

       // MetronomeSingleton.getInstance().setBeat(prefs.getInt(Constants.SELECTED_BEAT_PER_BAR, 0));
        //MetronomeSingleton.getInstance().setNoteValue(prefs.getInt(Constants.SELECTED_NOTE_VALUE, 4));
        int savedBPM = prefs.getInt(Constants.SELECTED_BPM, Constants.BPM_DEFAULT);
        int savedBeats = prefs.getInt(Constants.SELECTED_BEAT_PER_BAR, 30);
        int savedSets = prefs.getInt(Constants.SELECTED_NOTE_VALUE, 1);
        int savedRest = prefs.getInt(Constants.SELECTED_REST_VALUE, 30);
        MetronomeSingleton.getInstance().setBpm(savedBPM);
        MetronomeSingleton.getInstance().setBeat(savedBeats);
        MetronomeSingleton.getInstance().setBeat(savedRest);

        tvBPM.setText(String.valueOf(savedBPM));
        //spinnerBeat.setSelection(savedBeats);

        Log.d("vestibio", "instance of beats"+ savedBeats);

        btnPlus.setOnClickListener(view -> onPlusClick());
        btnMinus.setOnClickListener(view -> onMinusClick());


        numberPicker = (NumberPicker) getView().findViewById(R.id.number_picker);
        numberPicker.setMax(120);
        numberPicker.setMin(10);
        numberPicker.setUnit(1);
        numberPicker.setValue(savedBeats);
        numberPicker.setDisplayFocusable(true);
        numberPicker.clearFocus();
        numberPicker.setLimitExceededListener(new DefaultLimitExceededListenerBeats());
        // init listener for increment&decrement
        numberPicker.setValueChangedListener(new DefaultValueChangedListenerBeats());
        // init listener for focus change
        numberPicker.setOnFocusChangeListener(new DefaultOnFocusChangeListener(numberPicker));
        // init listener for done action in keyboard
        numberPicker.setOnEditorActionListener(new DefaultOnEditorActionListener(numberPicker));


        numberPickerSets = (NumberPicker) getView().findViewById(R.id.sets_picker);
        numberPickerSets.setMax(6);
        numberPickerSets.setMin(1);
        numberPickerSets.setUnit(1);
        numberPickerSets.setValue(savedSets);
        numberPickerSets.setOnFocusChangeListener(new DefaultOnFocusChangeListener(numberPickerSets));
        numberPickerSets.setLimitExceededListener(new DefaultLimitExceededListenerSets());
        numberPickerSets.setValueChangedListener(new DefaultValueChangedListenerSets());
        numberPickerSets.setOnEditorActionListener(new DefaultOnEditorActionListener(numberPickerSets));

        numberPickerRest = (NumberPicker) getView().findViewById(R.id.rest_picker);
        numberPickerRest.setMax(120);
        numberPickerRest.setMin(1);
        numberPickerRest.setUnit(1);
        numberPickerRest.setValue(savedRest);
        numberPickerRest.setOnFocusChangeListener(new DefaultOnFocusChangeListener(numberPickerRest));
        numberPickerRest.setLimitExceededListener(new DefaultLimitExceededListenerRest());
        numberPickerRest.setValueChangedListener(new DefaultValueChangedListenerRest());
        numberPickerRest.setOnEditorActionListener(new DefaultOnEditorActionListener(numberPickerRest));

        int time = (numberPicker.getValue() + numberPickerRest.getValue()) * numberPickerSets.getValue();
        totalTime.setText(getResources().getString(R.string.total_session).toString() + " "+timeConversion(time) );


//        ArrayAdapter<Beats> arrayBeats = new ArrayAdapter<>(activity, R.layout.spinner_item, Beats.values());
//        spinnerBeat.setAdapter(arrayBeats);
//        spinnerBeat.setSelection(savedBeats);
//        arrayBeats.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
//        spinnerBeat.setVerticalScrollBarEnabled(true);
//       spinnerBeat.setOnItemSelectedListener(beatsSpinnerListener);
//
//        final List<Beats> mGoalFilterArrayList = Arrays.asList(Beats.values());

//        CustomSpinnerAdapter customSpinnerAdapter = new CustomSpinnerAdapter (getContext(),android.R.layout.simple_dropdown_item_1line, mGoalFilterArrayList);
//        customSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_list_item_activated_1);
//        final Spinner mNavigationSpinner = (Spinner) getView().findViewById(R.id.spinnerBeat);
//        mNavigationSpinner.setAdapter(customSpinnerAdapter);
//        mNavigationSpinner.setOnItemSelectedListener(beatsSpinnerListener);
//        mNavigationSpinner.setVerticalScrollBarEnabled(true);
////


//        ArrayAdapter<NoteValues> noteValues =
//                new ArrayAdapter<>(getActivity(),
//                        R.layout.spinner_item, NoteValues.values());
//        spinnerNote.setAdapter(noteValues);
//        spinnerNote.setSelection(NoteValues.four.ordinal());
//        noteValues.setDropDownViewResource(R.layout.spinner_dropdown);

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
        registerNotificationReceiver();

        timer.setText("");



    }

    public synchronized void onStartStopClick() {
        if (Utils.isMetronomeServiceRunning(getContext())) {
            Log.d("Vestibio", "stop metronome:  on stop start click");
            stopMetronome();
            currentSet=0;
        } else if (currentSet<2){
            Log.d("Vestibio", "start metronome:  onstart stop click");
            startMetronome();
        }else{
            Log.d("Vestibio", "stop metronome first set:  on stop start click");
            stopMetronome();
            currentSet=0;
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

    @Override
    protected void onStopMetronome() {
        stopMetronome();
    }

    private void stopMetronome() {

        MetronomeSingleton.getInstance().setPlay(false);
        Utils.checkAndStopService(getContext());
        //uncomment here
//        activity.getSlidingTabLayout().setVisibility(View.VISIBLE);
//        activity.getViewPager().setPagingEnabled(true);
        btnStartStop.setText(R.string.start);
        contentView.setKeepScreenOn(false);
        issueServiceNotification();
        timer.setText("Session stopped");
        isCanceled=true;

    }


    private void pauseMetronome() {

        int totalSets = numberPickerSets.getValue();
        if(currentSet<totalSets ){
            //convert rest duration in seconds to milliseconds
            int restDuration = (numberPickerRest.getValue() * 1000) + 1000;

            currentSet = currentSet + 1;
            MetronomeSingleton.getInstance().setPlay(false);
            Utils.checkAndStopService(getContext());
            //activity.getSlidingTabLayout().setVisibility(View.VISIBLE);
            //activity.getViewPager().setPagingEnabled(true);
            //btnStartStop.setText(R.string.start);
            contentView.setKeepScreenOn(true);
            issueServiceNotification();

            CountDownTimer countDownTimer = new android.os.CountDownTimer(restDuration, 1000) {
                @Override
                public void onTick(long l) {
                    //               long millis = millisUntilFinished;
                    Log.d("Vestibio", "onTick pause metronome: ");
//                String hms = String.format("%02d:%02d:%02d",
//
//                        TimeUnit.MILLISECONDS.toHours(millis),
//                        TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)),
//                        TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis))
//                );
                    //               timer.setText(hms);
                    timer.setText("Rest done in : " + ((int) Math.round(l / 1000.0) - 1));
                    if (isCanceled) {
                        Log.d("Vestibio", "canceled!!!");
                        timer.setText("");
                        cancel();
                    }

                }

                @Override
                public void onFinish() {
                    Log.d("Vestibio", "finishing ");
                    timer.setText("");
                    continueMetronome();
                    cancel();
                }
            };
            countDownTimer.start();
        }else{
            stopMetronome();
            currentSet=0;
            timer.setText("Session Completed! ");
        }

    }

    private void startMetronome() {
        int sessionDuration =(numberPicker.getValue() *1000) +1000;
        //first set
        currentSet=1;
        isCanceled=false;

        MetronomeSingleton.getInstance().setPlay(true);
        //uncomment here
//        activity.getSlidingTabLayout().setVisibility(View.GONE);
//        activity.getViewPager().setPagingEnabled(false);
        if(!Utils.isMetronomeServiceRunning(getContext())){
            Intent myIntent = new Intent(getContext(), MetronomeService.class);
            getActivity().startService(myIntent);
        }
        btnStartStop.setText(R.string.stop);
        contentView.setKeepScreenOn(true);
        issueServiceNotification();
        CountDownTimer countDownTimer = new android.os.CountDownTimer(sessionDuration, 1000) {
            @Override
            public void onTick(long l) {
                //               long millis = millisUntilFinished;
                Log.d("Vestibio", "onTick start metronome: Start Metronome ");
                timer.setText("Set done in: "+((int)Math.round(l/1000.0)-1));
                if(isCanceled){
                    Log.d("Vestibio", "canceled!!!");
                    timer.setText("");
                    cancel();
                }
//                String hms = String.format("%02d:%02d:%02d",
//
//                        TimeUnit.MILLISECONDS.toHours(millis),
//                        TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)),
//                        TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis))
//                );
                //               timer.setText(hms);


            }

            @Override
            public void onFinish() {
                Log.d("Vestibio", "finishing ");
                timer.setText("");
                pauseMetronome();
                cancel();
            }
        };
        countDownTimer.start();
    }

    private void continueMetronome() {
        int sessionDuration =( numberPicker.getValue() *1000) +1000;
        int totalSets = numberPickerSets.getValue();
        //increments the set #
        //currentSet=currentSet+1;

        if(currentSet<=totalSets){
            MetronomeSingleton.getInstance().setPlay(true);
            //uncomment here
//            activity.getSlidingTabLayout().setVisibility(View.GONE);
//            activity.getViewPager().setPagingEnabled(false);
            if(!Utils.isMetronomeServiceRunning(getContext())){
                Intent myIntent = new Intent(getContext(), MetronomeService.class);
                getActivity().startService(myIntent);
            }
            btnStartStop.setText(R.string.stop);
            contentView.setKeepScreenOn(true);
            issueServiceNotification();
            CountDownTimer countDownTimer = new android.os.CountDownTimer(sessionDuration, 1000) {
                @Override
                public void onTick(long l) {

                    timer.setText("Set done in: "+((int)Math.round(l/1000.0)-1));

                    if(isCanceled){
                        Log.d("Vestibio", "canceled!!!");
                        timer.setText("");
                        cancel();
                    }

                    Log.d("Vestibio", "onTick continue: ");

                }

                @Override
                public void onFinish() {
                    Log.d("Vestibio", "finishing ");
                    timer.setText("");
                    pauseMetronome();
                    cancel();
                }
            };
            countDownTimer.start();
        }else{
            stopMetronome();
            currentSet=0;
            timer.setText("Session Completed! ");
        }


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

    private class DefaultValueChangedListenerBeats implements ValueChangedListener {

        public void valueChanged(int value, ActionEnum action) {

            String actionText = action == ActionEnum.MANUAL ? "manually set" : (action == ActionEnum.INCREMENT ? "incremented" : "decremented");
            String message = String.format("NumberPicker is %s to %d", actionText, value);
            Log.v(this.getClass().getSimpleName(), message);
            MetronomeSingleton.getInstance().setBeat(value);
            prefs.edit().putInt(Constants.SELECTED_BEAT_PER_BAR, value).apply();
            updateMetronome();
            Log.v(this.getClass().getSimpleName(),  "Vestibio " + message);

            int time = (numberPicker.getValue() + numberPickerRest.getValue()) * numberPickerSets.getValue();
            totalTime.setText(getResources().getString(R.string.total_session).toString() + " "+timeConversion(time) );

        }
    }

    private class DefaultValueChangedListenerSets implements ValueChangedListener {

        public void valueChanged(int value, ActionEnum action) {

            String actionText = action == ActionEnum.MANUAL ? "manually set" : (action == ActionEnum.INCREMENT ? "incremented" : "decremented");
            String message = String.format("NumberPicker is %s to %d", actionText, value);
            Log.v(this.getClass().getSimpleName(), message);
            MetronomeSingleton.getInstance().setNoteValue(value);
            prefs.edit().putInt(Constants.SELECTED_NOTE_VALUE, value).apply();
            updateMetronome();
            Log.v(this.getClass().getSimpleName(),  "Vestibio " + message);

            int time = (numberPicker.getValue() + numberPickerRest.getValue()) * numberPickerSets.getValue();
            totalTime.setText(getResources().getString(R.string.total_session).toString() + " "+timeConversion(time) );

        }
    }

    private class DefaultValueChangedListenerRest implements ValueChangedListener {

        public void valueChanged(int value, ActionEnum action) {

            String actionText = action == ActionEnum.MANUAL ? "manually set" : (action == ActionEnum.INCREMENT ? "incremented" : "decremented");
            String message = String.format("NumberPicker is %s to %d", actionText, value);
            Log.v(this.getClass().getSimpleName(), message);
            MetronomeSingleton.getInstance().setRest(value);
            prefs.edit().putInt(Constants.SELECTED_REST_VALUE, value).apply();
            updateMetronome();
            Log.v(this.getClass().getSimpleName(),  "Vestibio " + message);

            int time = (numberPicker.getValue() + numberPickerRest.getValue()) * numberPickerSets.getValue();
            totalTime.setText(getResources().getString(R.string.total_session).toString() + " "+timeConversion(time) );

        }
    }

    public class DefaultOnFocusChangeListener implements View.OnFocusChangeListener {

        NumberPicker layout;

        public DefaultOnFocusChangeListener(NumberPicker layout) {
            this.layout = layout;
        }

        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            EditText editText = (EditText) v;

            if (!hasFocus) {
                try {
                    int value = Integer.parseInt(editText.getText().toString());
                    layout.setValue(value);

                     switch(layout.getId()) {
                        case R.id.number_picker:
                            MetronomeSingleton.getInstance().setBeat(value);
                            prefs.edit().putInt(Constants.SELECTED_BEAT_PER_BAR, value).apply();
                            updateMetronome();
                            break;
                        case R.id.sets_picker:
                            MetronomeSingleton.getInstance().setNoteValue(value);
                            prefs.edit().putInt(Constants.SELECTED_NOTE_VALUE, value).apply();
                            updateMetronome();
                            break;
                        case R.id.rest_picker:
                            MetronomeSingleton.getInstance().setRest(value);
                            prefs.edit().putInt(Constants.SELECTED_REST_VALUE, value).apply();
                            updateMetronome();
                            break;
                    }

                    int time = (numberPicker.getValue() + numberPickerRest.getValue()) * numberPickerSets.getValue();
                    totalTime.setText(getResources().getString(R.string.total_session).toString() + " "+timeConversion(time) );

                    if (layout.getValue() == value) {
                        layout.getValueChangedListener().valueChanged(value, ActionEnum.MANUAL);
                    } else {
                        layout.refresh();
                    }
                } catch (NumberFormatException e) {
                    layout.refresh();
                }
            }
        }
    }


    public class DefaultOnEditorActionListener implements TextView.OnEditorActionListener {

        NumberPicker layout;

        public DefaultOnEditorActionListener(NumberPicker layout) {
            this.layout = layout;
        }

        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                try {
                    int value = Integer.parseInt(v.getText().toString());
                    layout.setValue(value);

                    switch(layout.getId()) {
                        case R.id.number_picker:
                            MetronomeSingleton.getInstance().setBeat(value);
                            prefs.edit().putInt(Constants.SELECTED_BEAT_PER_BAR, value).apply();
                            updateMetronome();
                            break;
                        case R.id.sets_picker:
                            MetronomeSingleton.getInstance().setNoteValue(value);
                            prefs.edit().putInt(Constants.SELECTED_NOTE_VALUE, value).apply();
                            updateMetronome();
                            break;
                        case R.id.rest_picker:
                            MetronomeSingleton.getInstance().setRest(value);
                            prefs.edit().putInt(Constants.SELECTED_REST_VALUE, value).apply();
                            updateMetronome();
                            break;
                    }

                    int time = (numberPicker.getValue() + numberPickerRest.getValue()) * numberPickerSets.getValue();
                    totalTime.setText(getResources().getString(R.string.total_session).toString() + " "+timeConversion(time) );

                    if (layout.getValue() == value) {
                        layout.getValueChangedListener().valueChanged(value, ActionEnum.MANUAL);
                        return false;
                    }
                } catch (NumberFormatException e) {
                    layout.refresh();
                }
            }
            return true;
        }
    }

    public class DefaultLimitExceededListenerBeats implements LimitExceededListener {

        public void limitExceeded(int limit, int exceededValue) {

            String message = String.format(getResources().getString(R.string.duration_toast_exceeded).toString(), exceededValue, limit);
            Log.v(this.getClass().getSimpleName(), message);
            Toast toast = Toast.makeText(getContext(), message,
                    Toast.LENGTH_LONG);

            toast.show();
        }
    }

    public class DefaultLimitExceededListenerSets implements LimitExceededListener {

        public void limitExceeded(int limit, int exceededValue) {

            String message = String.format(getResources().getString(R.string.sets_toast_exceeded).toString(), exceededValue, limit);
            Log.v(this.getClass().getSimpleName(), message);
            Toast toast = Toast.makeText(getContext(), message,
                    Toast.LENGTH_LONG);

            toast.show();
        }
    }

    public class DefaultLimitExceededListenerRest implements LimitExceededListener {

        public void limitExceeded(int limit, int exceededValue) {

            String message = String.format(getResources().getString(R.string.rest_toast_exceeded).toString(), exceededValue, limit);
            Log.v(this.getClass().getSimpleName(), message);
            Toast toast = Toast.makeText(getContext(), message,
                    Toast.LENGTH_LONG);

            toast.show();
        }
    }

    private AdapterView.OnItemSelectedListener beatsSpinnerListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            Beats beat = (Beats) arg0.getItemAtPosition(arg2);
            int beats = beat.getNum();
//            int noteValue = prefs.getInt(Constants.SELECTED_NOTE_VALUE, 1);
//            if (beats > noteValue) {
//                beats = noteValue; // TODO fixme =(
//            }

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

    private static String timeConversion(int totalSeconds) {
        int hours = totalSeconds / 60 / 60;
        int minutes = (totalSeconds - (hoursToSeconds(hours)))
                / 60;
        int seconds = totalSeconds
                - ((hoursToSeconds(hours)) + (minutesToSeconds(minutes)));

        return minutes + " min " + seconds + " sec";
    }
    private static int hoursToSeconds(int hours) {
        return hours * 60 * 60;
    }

    private static int minutesToSeconds(int minutes) {
        return minutes * 60;
    }


}

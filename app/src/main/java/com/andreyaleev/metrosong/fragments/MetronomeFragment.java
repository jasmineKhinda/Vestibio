package com.andreyaleev.metrosong.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.andreyaleev.metrosong.BuildConfig;
import com.andreyaleev.metrosong.Constants;
import com.andreyaleev.metrosong.R;
import com.andreyaleev.metrosong.activities.MainActivity;
import com.andreyaleev.metrosong.adapters.CustomSpinnerAdapter;
import com.andreyaleev.metrosong.bus.TickEvent;
import com.andreyaleev.metrosong.db.SessionsDataSource;
import com.andreyaleev.metrosong.db.SongsDataSource;
import com.andreyaleev.metrosong.metronome.MetronomeSingleton;
import com.andreyaleev.metrosong.metronome.Session;
import com.andreyaleev.metrosong.metronome.Song;
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
    private Session session;
   SpinnerAdapter adapter;
    private String spinner_item;
    private String[] title;
    private int time=0;
    SessionsDataSource dataSource;

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
    @BindView(R.id.btnLog)
    Button buttonLog;

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
        buttonLog= (Button)v.findViewById(R.id.btnLog);
        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        dataSource = new SessionsDataSource(getActivity());
        dataSource.open();

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
        timer.setVisibility(View.INVISIBLE);

        Log.d("vestibio", "instance of beats"+ savedBeats);

        btnPlus.setOnClickListener(view -> onPlusClick());
        btnMinus.setOnClickListener(view -> onMinusClick());


        numberPicker = (NumberPicker) getView().findViewById(R.id.number_picker);
        numberPicker.setMax(121);
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
        numberPickerSets.setMax(16);
        numberPickerSets.setMin(1);
        numberPickerSets.setUnit(1);
        numberPickerSets.setValue(savedSets);
        numberPickerSets.setOnFocusChangeListener(new DefaultOnFocusChangeListener(numberPickerSets));
        numberPickerSets.setLimitExceededListener(new DefaultLimitExceededListenerSets());
        numberPickerSets.setValueChangedListener(new DefaultValueChangedListenerSets());
        numberPickerSets.setOnEditorActionListener(new DefaultOnEditorActionListener(numberPickerSets));

        numberPickerRest = (NumberPicker) getView().findViewById(R.id.rest_picker);
        numberPickerRest.setMax(121);
        numberPickerRest.setMin(1);
        numberPickerRest.setUnit(1);
        numberPickerRest.setValue(savedRest);
        numberPickerRest.setOnFocusChangeListener(new DefaultOnFocusChangeListener(numberPickerRest));
        numberPickerRest.setLimitExceededListener(new DefaultLimitExceededListenerRest());
        numberPickerRest.setValueChangedListener(new DefaultValueChangedListenerRest());
        numberPickerRest.setOnEditorActionListener(new DefaultOnEditorActionListener(numberPickerRest));

        time = (numberPicker.getValue() + numberPickerRest.getValue()) * numberPickerSets.getValue();
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
        adapter= new SpinnerAdapter(getActivity().getApplicationContext());
        title = getResources().getStringArray(R.array.dizzyness);



        buttonLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
//                alert.setTitle("Log Session");
//
//
//                // Set an EditText view to get user input
//                final EditText inputTitle = new EditText(getActivity());
//                alert.setView(inputTitle);
//
//                alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int whichButton) {
//                        String value = inputTitle.getText().toString();
//                        Log.d("", "Pin Value : " + value);
//                        return;
//                    }
//                });
//
//                alert.setNegativeButton("Cancel",
//                        new DialogInterface.OnClickListener() {
//
//                            public void onClick(DialogInterface dialog, int which) {
//                                // TODO Auto-generated method stub
//                                return;
//                            }
//                        });
//                alert.show();
//            }
//        });
                final Dialog dialog = new Dialog(getActivity(), R.style.Dialog);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.log_session_dialog);
                dialog.setCancelable(true);
                dialog.setTitle(getResources().getString(R.string.Log));

                // set the custom dialog components - text, image and button
                final Spinner spinner = (Spinner) dialog.findViewById(R.id.spinner1);
                final EditText edittext = (EditText) dialog.findViewById(R.id.title);
                final EditText notes = (EditText) dialog.findViewById(R.id.notes);
                Button button = (Button) dialog.findViewById(R.id.button1);
                Button cancel = (Button) dialog.findViewById(R.id.buttonCancel);

                Spinner spinnerDd = (Spinner) dialog.findViewById(R.id.spinner1);
                ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                        R.array.dizzyness, R.layout.simple_spinner_item);
                adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
                spinnerDd.setAdapter(adapter);

                //spinner.setAdapter(adapter);
                spinnerDd.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        // TODO Auto-generated method stub
                        spinner_item = title[position];
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        // TODO Auto-generated method stub

                    }
                });

                button.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // TODO Auto-generated method stub
                        if (edittext.getText().toString().isEmpty()) {
                            edittext.setError(getString(R.string.empty_field));
                        }else{
                            saveSession(edittext.getText().toString(), spinner.getSelectedItem().toString(),notes.getText().toString() );
                            dialog.dismiss();
                            Toast.makeText(getActivity().getApplicationContext(),  "Session added:  " + edittext.getText().toString().trim(), Toast.LENGTH_LONG).show();
                        }

                    }
                });
                dialog.show();

                cancel.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // TODO Auto-generated method stub
                        dialog.dismiss();
                    }
                });
                dialog.show();



            }
        });




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

    //save the session into DB
    private void saveSession(String title, String dizzyness, String notes) {




        this.session = new Session();
        this.session.setTitle(title);
        if(dizzyness.equals("N/A")){
            this.session.setDizzynesslevel(0);
        }else{
            this.session.setDizzynesslevel(Integer.parseInt(dizzyness.trim()));
        }
        this.session.setNotes(notes);
        this.session.setTimeStamp(System.currentTimeMillis());
        this.session.setDuration(numberPicker.getValue());
        this.session.setSets(numberPickerSets.getValue());
        this.session.setRest(numberPickerRest.getValue());
        this.session.setTotalDuration(time);

        this.session.setBpm(Integer.parseInt(tvBPM.getText().toString()));


        long id = dataSource.insertSession(this.session);


        Log.d("Vestibio", "saveSession: "+ dataSource.getAllSessions());

    }
    @Override
    protected void onStopMetronome() {
        stopMetronome();
    }

    private void stopMetronome() {

        MetronomeSingleton.getInstance().setPlay(false);
        Utils.checkAndStopService(getContext());
        activity.getBottomViewNavigation().setVisibility(View.VISIBLE);
        activity.getViewToolbar().setVisibility(View.VISIBLE);
        //uncomment here
//        activity.getSlidingTabLayout().setVisibility(View.VISIBLE);
//        activity.getViewPager().setPagingEnabled(true);
        btnStartStop.setText(R.string.start);
        contentView.setKeepScreenOn(false);
        issueServiceNotification();
        timer.setText("Session stopped");
        buttonLog.setVisibility(View.VISIBLE);
        timer.setVisibility(View.INVISIBLE);
        isCanceled=true;


        String message = getResources().getString(R.string.session_completed).toString();
        Toast toast = Toast.makeText(getContext(), message,
                Toast.LENGTH_LONG);
        toast.show();

    }


    private void pauseMetronome() {

        int totalSets = numberPickerSets.getValue();
        if(currentSet<totalSets ){
            //convert rest duration in seconds to milliseconds
            int restDuration = (numberPickerRest.getValue() * 1000) + 1000;
            activity.getBottomViewNavigation().setVisibility(View.GONE);

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
                    timer.setText("Rest done in : " + ((int) Math.round(l / 1000.0) - 1) + " sec");
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
        activity.getBottomViewNavigation().setVisibility(View.GONE);
        activity.getViewToolbar().setVisibility(View.GONE);
        buttonLog.setVisibility(View.GONE);
        timer.setVisibility(View.VISIBLE);

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
                timer.setText("Set done in: "+((int)Math.round(l/1000.0)-1)+ " sec");
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
 //           activity.getSlidingTabLayout().setVisibility(View.GONE);
//            activity.getViewPager().setPagingEnabled(false);

            activity.getBottomViewNavigation().setVisibility(View.GONE);


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

                    timer.setText("Set done in: "+((int)Math.round(l/1000.0)-1)+ " sec");

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

            time = (numberPicker.getValue() + numberPickerRest.getValue()) * numberPickerSets.getValue();
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

            time = (numberPicker.getValue() + numberPickerRest.getValue()) * numberPickerSets.getValue();
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

            time = (numberPicker.getValue() + numberPickerRest.getValue()) * numberPickerSets.getValue();
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

                     time = (numberPicker.getValue() + numberPickerRest.getValue()) * numberPickerSets.getValue();
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

                    time = (numberPicker.getValue() + numberPickerRest.getValue()) * numberPickerSets.getValue();
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
            time = (numberPicker.getValue() + numberPickerRest.getValue()) * numberPickerSets.getValue();
            totalTime.setText(getResources().getString(R.string.total_session).toString() + " "+timeConversion(time) );
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

    public static String timeConversion(int totalSeconds) {
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

    public class SpinnerAdapter extends BaseAdapter{
        Context context;
        private LayoutInflater mInflater;

        public SpinnerAdapter(Context context) {
            this.context = context;
        }

        @Override
        public int getCount() {
            return title.length;
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final ListContent holder;
            View v = convertView;
            if (v == null) {
                mInflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
                v = mInflater.inflate(R.layout.custom_spinner_layout, null);
                holder = new ListContent();
                holder.text = (TextView) v.findViewById(R.id.spinner_item_text);

                v.setTag(holder);
            } else {
                holder = (ListContent) v.getTag();
            }

            holder.text.setText(title[position]);

            return v;
        }
    }

    static class ListContent {
        TextView text;
    }

    @Override
    public void onResume() {
        dataSource.open();
        super.onResume();
    }

    @Override
    public void onPause() {
        dataSource.close();
        super.onPause();
    }
}

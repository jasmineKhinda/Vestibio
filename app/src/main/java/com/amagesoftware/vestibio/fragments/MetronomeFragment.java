package com.amagesoftware.vestibio.fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.amagesoftware.vestibio.BuildConfig;
import com.amagesoftware.vestibio.Constants;
import com.amagesoftware.vestibio.R;

import com.amagesoftware.vestibio.activities.MainActivity;
import com.amagesoftware.vestibio.activities.SessionActivity;
import com.amagesoftware.vestibio.bus.TickEvent;
import com.amagesoftware.vestibio.db.SessionsDataSource;
import com.amagesoftware.vestibio.metronome.MetronomeSingleton;
import com.amagesoftware.vestibio.metronome.Session;
import com.amagesoftware.vestibio.services.MetronomeService;
import com.amagesoftware.vestibio.tools.AppRater;
import com.amagesoftware.vestibio.tools.Utils;
import com.squareup.otto.Subscribe;
import com.travijuu.numberpicker.library.Enums.ActionEnum;
import com.travijuu.numberpicker.library.Interface.LimitExceededListener;
import com.travijuu.numberpicker.library.Interface.ValueChangedListener;
import com.travijuu.numberpicker.library.NumberPicker;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Andrey Aleev on 09.09.2015.
 */
public class MetronomeFragment extends MetronomableFragment {

    private View contentView;
    private SharedPreferences prefs;
    private NumberPicker numberPicker;
    private NumberPicker numberPickerSets;
    private NumberPicker numberPickerRest;
     android.os.CountDownTimer countDownTimer;
    private  Dialog dialog;
    com.amagesoftware.vestibio.tools.CountDownTimer countDownTimerStart;


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
    @BindView(R.id.timerSetsRemaining)
    TextView timerSetsRemaining;
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
        timerSetsRemaining.setVisibility(View.INVISIBLE);

        Log.d("vestibio", "instance of beats"+ savedBeats);

        btnPlus.setOnClickListener(view -> onPlusClick());
        btnMinus.setOnClickListener(view -> onMinusClick());


        numberPicker = (NumberPicker) getView().findViewById(R.id.number_picker);
        numberPicker.setMax(240);
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
        numberPickerSets.setMax(20);
        numberPickerSets.setMin(1);
        numberPickerSets.setUnit(1);
        numberPickerSets.setValue(savedSets);
        numberPickerSets.setOnFocusChangeListener(new DefaultOnFocusChangeListener(numberPickerSets));
        numberPickerSets.setLimitExceededListener(new DefaultLimitExceededListenerSets());
        numberPickerSets.setValueChangedListener(new DefaultValueChangedListenerSets());
        numberPickerSets.setOnEditorActionListener(new DefaultOnEditorActionListener(numberPickerSets));

        numberPickerRest = (NumberPicker) getView().findViewById(R.id.rest_picker);
        numberPickerRest.setMax(240);
        numberPickerRest.setMin(1);
        numberPickerRest.setUnit(1);
        numberPickerRest.setValue(savedRest);
        numberPickerRest.setOnFocusChangeListener(new DefaultOnFocusChangeListener(numberPickerRest));
        numberPickerRest.setLimitExceededListener(new DefaultLimitExceededListenerRest());
        numberPickerRest.setValueChangedListener(new DefaultValueChangedListenerRest());
        numberPickerRest.setOnEditorActionListener(new DefaultOnEditorActionListener(numberPickerRest));

        time = (numberPicker.getValue() + numberPickerRest.getValue()) * numberPickerSets.getValue();
        totalTime.setText(getResources().getString(R.string.total_session).toString() + " "+timeConversion(time) );


        seekbarBPM.getProgressDrawable().setColorFilter(
                new PorterDuffColorFilter(getResources().getColor(R.color.primary), PorterDuff.Mode.SRC_ATOP));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            seekbarBPM.getThumb().setColorFilter(getResources().getColor(R.color.primary), PorterDuff.Mode.SRC_ATOP);
        }

        seekbarBPM.setProgress((savedBPM - 1));
        seekbarBPM.setOnSeekBarChangeListener(bpmSeekBarListener);

        btnStartStop.setOnClickListener(view -> onStartStopClick());

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            btnPlus.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_background));
            btnMinus.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_background));
        }
        registerNotificationReceiver();

        timer.setText("");
        timerSetsRemaining.setText("");
        adapter= new SpinnerAdapter(getActivity().getApplicationContext());
        title = getResources().getStringArray(R.array.dizzyness);

    }

    public synchronized void onStartStopClick() {
        if (Utils.isMetronomeServiceRunning(getContext())) {
            Log.d("Vestibio", "pause metronome:  on stop start click" + Utils.isMetronomeServiceRunning(getContext())+ " true or false");
            pauseMetronome();
        } else if (currentSet<2){
            Log.d("Vestibio", "start metronome:  onstart stop click");
            startMetronome();
        }else{
            Log.d("Vestibio", "pause  metronome during rest:  on stop start click" + currentSet);
            pauseMetronome();

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
    private long saveSession() {

        this.session = new Session();
        this.session.setTitle(getResources().getString(R.string.vestibular_session));
//        if(dizzyness.equals("N/A")){
//            this.session.setDizzynesslevel(0);
//        }else{
//            this.session.setDizzynesslevel(Integer.parseInt(dizzyness.trim()));
//        }
//        this.session.setNotes(notes);
        this.session.setTimeStamp(System.currentTimeMillis());
        this.session.setDuration(numberPicker.getValue());
        this.session.setSets(numberPickerSets.getValue());
        this.session.setRest(numberPickerRest.getValue());
        this.session.setTotalDuration(time);
        this.session.setBpm(Integer.parseInt(tvBPM.getText().toString()));


        long id = dataSource.insertSession(this.session);
        Log.d("Vestibio", "dizzy level session id: " + id);

        return id;

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
        timerSetsRemaining.setText("No Sets");
        timer.setVisibility(View.INVISIBLE);
        timerSetsRemaining.setVisibility(View.INVISIBLE);
        isCanceled=true;
        currentSet=0;

        String message = getResources().getString(R.string.session_completed).toString();
        Toast toast = Toast.makeText(getContext(), message,
                Toast.LENGTH_LONG);
        toast.show();

        long id =  saveSession();
        this.session.setId(id);
        this.session.setTitle(getResources().getString(R.string.vestibular_session)+ " " + session.getId());


        Intent intent = new Intent(getContext(), SessionActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable(SessionActivity.SESSION_TAG, session);
        bundle.putBoolean(SessionActivity.RATER_SHOW, true);
        intent.putExtras(bundle);
        getContext().startActivity(intent);
        Log.d("vestibio", "dizzy level  iddd"+ this.session.getId());
        Log.d("vestibio", "putting the rater boolean");


    }



    private void stopMetronomeExitFragment() {


        MetronomeSingleton.getInstance().setPlay(false);
        Utils.checkAndStopService(getContext());
       // activity.getBottomViewNavigation().setVisibility(View.VISIBLE);
        activity.getViewToolbar().setVisibility(View.VISIBLE);
        btnStartStop.setText(R.string.start);
        contentView.setKeepScreenOn(false);
        issueServiceNotification();
        timer.setText("Session stopped");
        timerSetsRemaining.setText("No Sets");
        timer.setVisibility(View.INVISIBLE);
        timerSetsRemaining.setVisibility(View.INVISIBLE);
        isCanceled=true;
        currentSet=0;




    }





    private void restMetronome() {

        int totalSets = numberPickerSets.getValue();

        if(currentSet<totalSets ){
            //convert rest duration in seconds to milliseconds
            int restDuration = (numberPickerRest.getValue() * 1000) + 1000;
            activity.getBottomViewNavigation().setVisibility(View.GONE);

            currentSet = currentSet + 1;
            MetronomeSingleton.getInstance().setPlay(false);
            Utils.checkAndStopService(getContext());
            contentView.setKeepScreenOn(true);
            issueServiceNotification();

            //there's a bug in countdownTimer.cancel() in Kitkat and below, so had to use another version of CountdownTimer in that case
            if (android.os.Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT){
                com.amagesoftware.vestibio.tools.CountDownTimer countDownTimer = new com.amagesoftware.vestibio.tools.CountDownTimer(restDuration, 1000) {
                    @Override
                    public void onTick(long l) {
                        //               long millis = millisUntilFinished;
                        Log.d("Vestibio", "onTick pause metronome: ");
                        if(null!=getContext()){
                            timer.setText(getString(R.string.rest_countdown) + " " + ((int) Math.round(l / 1000.0) - 1) + " sec");
                            timer.setTextColor(getResources().getColor(R.color.button_red));
                            timerSetsRemaining.setText("Current Set: " + currentSet + " out of " + totalSets);

                        }
                         if (isCanceled) {
                            Log.d("Vestibio", "canceled!!!");
                            this.cancel();

                        }

                    }

                    @Override
                    public void onFinish() {
                        Log.d("Vestibio", "finishing ");
                        continueMetronome();
                        this.cancel();
                    }
                };
                countDownTimer.start();
            }else {
                android.os.CountDownTimer countDownTimer = new android.os.CountDownTimer(restDuration, 1000) {
                    @Override
                    public void onTick(long l) {
                        //               long millis = millisUntilFinished;
                        Log.d("Vestibio", "onTick pause metronome: ");

                        if(null !=getContext()){
                            timer.setText(getString(R.string.rest_countdown) + " " + ((int) Math.round(l / 1000.0) - 1) + " sec");
                            timer.setTextColor(getResources().getColor(R.color.button_red));
                            timerSetsRemaining.setText("Current Set: " + currentSet + " out of " + totalSets);

                        }
                       if (isCanceled) {
                            Log.d("Vestibio", "canceled!!!");
                            this.cancel();

                        }

                    }

                    @Override
                    public void onFinish() {
                        Log.d("Vestibio", "finishing ");
                        continueMetronome();
                        this.cancel();
                    }
                };
                countDownTimer.start();
            }
        }else{
            stopMetronome();
            currentSet=0;
            Log.d("Vestibio", "Done, stopping Metronome. ");
        }

    }

    private void startMetronome() {
        int sessionDuration =(numberPicker.getValue() *1000) +1000;
        //first set
        currentSet=1;
        isCanceled=false;
        //cancel the old countDownTimer

        int totalSets = numberPickerSets.getValue();

        MetronomeSingleton.getInstance().setPlay(true);

        activity.getBottomViewNavigation().setVisibility(View.GONE);
        activity.getViewToolbar().setVisibility(View.GONE);

        timer.setVisibility(View.VISIBLE);
        timerSetsRemaining.setVisibility(View.VISIBLE);

        if(!Utils.isMetronomeServiceRunning(getContext())){
            Intent myIntent = new Intent(getContext(), MetronomeService.class);
            getActivity().startService(myIntent);
        }
        btnStartStop.setText(R.string.stop);
        contentView.setKeepScreenOn(true);
        issueServiceNotification();

        //there's a bug in countdownTimer.cancel() in Kitkat and below, so had to use another version of CountdownTimer in that case
        if (android.os.Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT){
           com.amagesoftware.vestibio.tools.CountDownTimer countDownTimerStart = new com.amagesoftware.vestibio.tools.CountDownTimer(sessionDuration, 1000) {
               @Override
               public void onTick(long l) {
                   //               long millis = millisUntilFinished;
                   Log.d("Vestibio", "onTick start metronome: Start Metronome ");
                   if(null!=getContext()){
                       timer.setText(getString(R.string.set_countdown) +" "+((int)Math.round(l/1000.0)-1)+ " sec");
                       timer.setTextColor(getResources().getColor(R.color.button_green));
                       timerSetsRemaining.setText("Current Set: " + currentSet + " out of "+ totalSets);
                   }
                   if(isCanceled){
                       Log.d("Vestibio", " Timer Canceled!");
                       this.cancel();
                   }
               }

               @Override
               public void onFinish() {
                   Log.d("Vestibio", "Resting Metronome. ");
                   restMetronome();
                   this.cancel();
               }
           };
           countDownTimerStart.start();
       }else{
           CountDownTimer countDownTimerStart = new CountDownTimer(sessionDuration, 1000) {
               @Override
               public void onTick(long l) {
                   //               long millis = millisUntilFinished;
                   Log.d("Vestibio", "onTick start metronome: Start Metronome ");
                   if(null!=getContext()){
                       timer.setText(getString(R.string.set_countdown) +" "+((int)Math.round(l/1000.0)-1)+ " sec");
                       timer.setTextColor(getResources().getColor(R.color.button_green));
                       timerSetsRemaining.setText("Current Set: " + currentSet + " out of "+ totalSets);

                   }
                  if(isCanceled){
                       Log.d("Vestibio", " Timer Canceled!");
                       this.cancel();
                   }
               }

               @Override
               public void onFinish() {
                   Log.d("Vestibio", "Resting Metronome. ");
                   restMetronome();
                   this.cancel();
               }
           };
           countDownTimerStart.start();
       }

    }

    private void pauseMetronome() {

        MetronomeSingleton.getInstance().setPlay(false);
        Utils.checkAndStopService(getContext());
        isCanceled=true;

        dialog= new Dialog(getActivity(), R.style.Dialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.temp_stop_metronome_dialog);
        dialog.setCancelable(false);
        dialog.setTitle(getResources().getString(R.string.Log));


        Button cont = (Button) dialog.findViewById(R.id.continue_button);
        Button finish = (Button) dialog.findViewById(R.id.finish_button);



        cont.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                dialog.dismiss();
                continueMetronome();
                isCanceled = false;
                Log.d("Vestibio", "Continuing Metronome ");

            }
        });
        dialog.show();

        finish.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                dialog.dismiss();
                stopMetronome();
                isCanceled = true;
                Log.d("Vestibio", "Stopping Metronome ");
            }
        });
        dialog.show();



    }

    private void continueMetronome() {
        int sessionDuration =( numberPicker.getValue() *1000) +1000;
        int totalSets = numberPickerSets.getValue();


        if(currentSet<=totalSets){
            MetronomeSingleton.getInstance().setPlay(true);
            activity.getBottomViewNavigation().setVisibility(View.GONE);
            activity.getViewToolbar().setVisibility(View.GONE);
            timer.setVisibility(View.VISIBLE);
            timerSetsRemaining.setVisibility(View.VISIBLE);


            if(!Utils.isMetronomeServiceRunning(getContext())){
                Intent myIntent = new Intent(getContext(), MetronomeService.class);
                getActivity().startService(myIntent);
            }
            btnStartStop.setText(R.string.stop);
            contentView.setKeepScreenOn(true);
            issueServiceNotification();

            //there's a bug in countdownTimer.cancel() in Kitkat and below, so had to use another version of CountdownTimer in that case
            if (android.os.Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT){
                com.amagesoftware.vestibio.tools.CountDownTimer countDownTimer = new com.amagesoftware.vestibio.tools.CountDownTimer(sessionDuration, 1000) {
                    @Override
                    public void onTick(long l) {

                        if(null!=getContext()){
                            timer.setVisibility(View.VISIBLE);
                            timer.setText(getString(R.string.set_countdown) + " " + ((int) Math.round(l / 1000.0) - 1) + " sec");
                            timer.setTextColor(getResources().getColor(R.color.button_green));
                            timerSetsRemaining.setText("Current Set: " + currentSet + " out of " + totalSets);

                        }

                        if (isCanceled) {
                            Log.d("Vestibio", "canceled!!!");
                            this.cancel();
                        }

                    }

                    @Override
                    public void onFinish() {
                        Log.d("Vestibio", "Metronome Resting");
                        restMetronome();
                        this.cancel();
                    }
                };
                countDownTimer.start();
            }else {
                android.os.CountDownTimer countDownTimer = new android.os.CountDownTimer(sessionDuration, 1000) {
                    @Override
                    public void onTick(long l) {

                        if(null!=getContext()){
                            timer.setVisibility(View.VISIBLE);
                            timer.setText(getString(R.string.set_countdown) + " " + ((int) Math.round(l / 1000.0) - 1) + " sec");
                            timer.setTextColor(getResources().getColor(R.color.button_green));
                            timerSetsRemaining.setText("Current Set: " + currentSet + " out of " + totalSets);

                        }

                        if (isCanceled) {
                            Log.d("Vestibio", "canceled!!!");
                            this.cancel();
                        }

                    }

                    @Override
                    public void onFinish() {
                        Log.d("Vestibio", "Metronome Resting");
                        restMetronome();
                        this.cancel();
                    }
                };
                countDownTimer.start();
            }
        }else{
            stopMetronome();
            currentSet=0;
            Log.d("Vestibio", "Metronome Stopping");
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
            int bpm = (short) (((short) seekBar.getProgress()) + 1);
            tvBPM.setText(String.valueOf(bpm));
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            int bpm = (short) (((short) seekBar.getProgress()) + 1);
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
        Log.d("vestibio", "Vestibio:onResume: ");
    }

    @Override
    public void onStop() {

        stopMetronomeExitFragment();
        Log.d("vestibio", "Vestibio:onStop: in metronome frag ");
        super.onStop();
    }
    @Override
    public void onDestroyView() {

        super.onDestroyView();
        Log.d("vestibio", "Vestibio:onDestroyView: in metronome frag ");
    }

    @Override
    public void onDetach() {

        super.onDetach();
        Log.d("vestibio", "Vestibio:onDetach: in metronome frag ");
    }
    @Override
    public void onPause() {
        stopMetronomeExitFragment();
       if (dialog!=null){
           dialog.dismiss();
       }
        super.onPause();
        Log.d("vestibio", "Vestibio:onPause: in metronome frag ");
    }

    @Override
    public void onDestroy() {


        super.onDestroy();
        Log.d("vestibio", "Vestibio:onDestroy: in metronome frag ");
    }


}
